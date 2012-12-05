(ns caribou.app.routing
  (:use [clj-time.core :only (now)]
        [clj-time.format :only (unparse formatters)]
        [compojure.core :only (routes GET POST PUT DELETE ANY)]
        [ring.middleware file file-info])
  (:require [clojure.string :as string]
            [compojure.handler :as compojure-handler]
            [caribou.app.controller :as controller]
            [caribou.app.template :as template]
            [caribou.app.util :as app-util]
            [caribou.config :as config]
            [caribou.logger :as log]
            [caribou.util :as util]))

(defonce caribou-routes (atom {}))
(defonce caribou-route-order (atom []))
(defonce route-paths (atom {}))
(defonce error-handlers (atom {}))
(defonce route-counter (atom 0))
(defonce pre-actions (atom {}))

(defn resolve-method
  [method path func]
  (condp = method
    "GET" (GET path {params :params} func)
    "POST" (POST path {params :params} func)
    "PUT" (PUT path {params :params} func)
    "DELETE" (DELETE path {params :params} func)
    (ANY path {params :params} func)))

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
  [slug method route func]
  (let [base (deslash slug)
        relevant-pre-actions (get @pre-actions base)
        full-action (wrap-pre-actions relevant-pre-actions func)]
    (log/debug (format "adding route %s : %s -- %s %s " slug base route method) :routing)
    (swap! caribou-routes assoc slug [@route-counter (resolve-method method route full-action)])
    (swap! route-counter inc)
    (swap! caribou-route-order conj slug)
    (swap! route-paths assoc (keyword slug) route)))

(defn routes-in-order
  [routes]
  (map second (vals (into (sorted-map-by (fn [a b] (compare (first (a routes)) (first (b routes))))) routes))))

(defn clear-routes
  "Clears the app's routes. Used by Halo to update the routes."
  []
  (reset! caribou-routes {})
  (reset! route-counter 0)
  (reset! caribou-route-order [])
  (reset! route-paths {}))

(defn clear-pre-actions
  []
  (reset! pre-actions {}))

(defn default-action
  "if a page doesn't have a defined action, we just send the params to the template"
  [params]
  (let [template (params :template)]
    (template params)))

(def built-in-formatter (formatters :basic-date-time))

(defn default-index
  [request]
  (format "Welcome to Caribou! Next step is to add some pages.<br /> %s" (unparse built-in-formatter (now))))

(defn add-default-route
  []
  (add-route :default "GET" "/" default-index))
