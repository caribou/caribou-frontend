(ns caribou.app.routing
  (:use [clj-time.core :only (now)]
        [clj-time.format :only (unparse formatters)]
        [clout.core :only (route-compile route-matches)]
        [ring.middleware file file-info])
  (:require [clojure.string :as string]
            [caribou.app.controller :as controller]
            [caribou.app.error :as error]
            [caribou.app.template :as template]
            [caribou.app.util :as app-util]
            [caribou.config :as config]
            [caribou.logger :as log]
            [caribou.util :as util]))

(defonce routes (atom {}))
(defonce routes-order (atom []))
(defonce pre-actions (atom {}))

(defrecord Route [slug method route action])

;; these are backwards because we nest the func in reverse order
(defn prepend-pre-action
  [slug pre-action]
  (swap! pre-actions update-in [(keyword slug)] #(concat % [pre-action])))

(defn append-pre-action
  [slug pre-action]
  (swap! pre-actions update-in [(keyword slug)] (partial cons pre-action)))

(defn register-pre-action
  ([slug pre-action]
     (append-pre-action slug pre-action))
  ([place slug pre-action]
     (condp = (keyword place)
       :prepend (prepend-pre-action slug pre-action)
       (append-pre-action slug pre-action))))

(defn wrap-pre-action
  [pre-action action]
  (fn [request]
    (pre-action action request)))

(defn wrap-pre-actions
  [pre-actions func]
  (loop [pre-actions pre-actions
         func func]
    (if (empty? pre-actions)
      func
      (recur (rest pre-actions) (wrap-pre-action (first pre-actions) func)))))

(defn deslash
  [key]
  (keyword (or (last (re-find #"(.+)-with-slash" (name key))) key)))

(defn add-route
  [routes slug method path action]
  (let [base (deslash slug)
        relevant-pre-actions (get @pre-actions base)
        full-action (wrap-pre-actions relevant-pre-actions action)
        method (or method :get)
        method (keyword (string/lower-case (name method)))
        compiled-route (route-compile path)
        route (Route. slug method compiled-route full-action)]
    (log/debug (format "adding route %s : %s -- %s %s " slug base path method) :routing)
    (swap! routes-order conj slug)
    (swap! routes assoc slug route)))

(defn routes-in-order
  [routes routes-order]
  (map (partial get routes) routes-order))

(defn clear-routes!
  "Clears the app's routes. Used by Halo to update the routes."
  []
  (reset! routes-order [])
  (reset! routes {}))

(defn clear-pre-actions!
  []
  (reset! pre-actions {}))

(def built-in-formatter (formatters :basic-date-time))

(defn default-index
  [request]
  (format "Welcome to Caribou! Next step is to add some pages.<br /> %s" (unparse built-in-formatter (now))))

(defn add-default-route
  [routes]
  (add-route routes :default "GET" "/" default-index))

(defn add-head-routes
  [routes]
  (doseq [route @routes]
    (let [route-slug (keyword (str "--HEAD-" (name (:slug route))))
          route-re (-> route :route :re)]
      (add-route routes route-slug :head (str route-re) (fn [req] "")))))

(defn route-matches?
  [request route-number route]
  (let [method (:request-method request)
        compiled-route (:route route)
        method-matches (= method (:method route))]
    (when method-matches
      (when-let [match-result (route-matches compiled-route request)]
       [route-number match-result])))) ;FIXME: is this ugly?

(defn router
  "takes a request and performs the action associated with the matching route"
  [routes routes-order]
  (fn [request]
    (let [routes (routes-in-order routes routes-order)
          route-search (first (keep-indexed (partial route-matches? request) routes))]
      (if route-search
        (let [route-number (first route-search)
              route-params (second route-search)
              request (assoc request :route-params route-params)
              matched-route (nth routes route-number)
              action (:action matched-route)]
          (action request))
        (error/render-error :404 request)))))
