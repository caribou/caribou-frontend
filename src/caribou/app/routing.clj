(ns caribou.app.routing
  (:use [clj-time.core :only (now)]
        [clj-time.format :only (unparse formatters)]
        [ring.middleware file file-info])
  (:require [clojure.string :as string]
            [clout.core :as clout]
            [flatland.ordered.map :as flatland]
            [caribou.app.controller :as controller]
            [caribou.app.error :as error]
            [caribou.app.template :as template]
            [caribou.app.util :as app-util]
            [caribou.logger :as log]
            [caribou.config :as config]
            [caribou.util :as util]))

(defrecord Route [slug method path route action])

;; these are backwards because we nest the func in reverse order
(defn prepend-pre-action
  [slug pre-action]
  (swap! (config/draw :pre-actions) update-in [(keyword slug)] #(concat % [pre-action])))

(defn append-pre-action
  [slug pre-action]
  (swap! (config/draw :pre-actions) update-in [(keyword slug)] (partial cons pre-action)))

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

(defn merge-route
  [routes slug method path action]
  (let [base (deslash slug)
        relevant-pre-actions (get (deref (config/draw :pre-actions)) base)
        full-action (wrap-pre-actions relevant-pre-actions action)
        method (if (empty? method) :all method)
        method (keyword (string/lower-case (name method)))
        compiled-route (clout/route-compile path)
        route (Route. slug method path compiled-route full-action)]
    (assoc routes slug route)))

(defn add-route
  [slug method path action]
  (log/debug (format "adding route %s : %s -- %s %s " slug (deslash slug) path method) :routing)
  (swap! (config/draw :routes) merge-route slug method path action))

(defn routes-in-order
  [routes]
  (vals routes))

(defn clear-routes!
  "Clears the app's routes. Used by Halo to update the routes."
  []
  (reset! (config/draw :routes) {}))

(defn clear-pre-actions!
  []
  (reset! (config/draw :pre-actions) {}))

(def built-in-formatter (formatters :basic-date-time))

(defn default-index
  [request]
  (format "Welcome to Caribou! Next step is to add some pages.<br /> %s" (unparse built-in-formatter (now))))

(defn default-action
  [request]
  (controller/render request))

(defn add-default-route
  []
  (add-route :default "GET" "/" default-index))

(defn merge-head-routes
  [routes]
  (reduce
   (fn [added route]
     (let [route-slug (keyword (str "--HEAD-" (name (:slug route))))
           route-re (-> route :route :re)]
       (merge-route added route-slug "HEAD" (str route-re) (fn [req] ""))))
   routes
   (routes-in-order routes)))
     
(defn add-head-routes
  []
  (swap! (config/draw :routes) merge-head-routes))

(defn route-matches?
  [request route]
  (let [request-method (:request-method request)
        compiled-route (:route route)
        method (:method route)
        method-matches (or (= :all method)
                           (= method request-method)
                           (and (nil? request-method) (= method :get)))]
    (when method-matches
      (when-let [match-result (clout/route-matches compiled-route request)]
        [route match-result]))))

(defn find-first
  [p s]
  (first (remove nil? (map p s))))

(defn router
  "takes a request and performs the action associated with the matching route"
  [routes]
  (fn [request]
    (let [ordered-routes (routes-in-order routes)
          [route match] (find-first (partial route-matches? request) ordered-routes)]
      (if match
        (let [request (assoc request :route-params match)
              request (update-in request [:params] #(merge % match))
              action (:action route)
              response (action request)]
          response)
        (error/render-error :404 request)))))
