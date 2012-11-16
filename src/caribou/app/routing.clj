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

(defn resolve-method
  [method path func]
  (condp = method
    "GET" (GET path {params :params} func)
    "POST" (POST path {params :params} func)
    "PUT" (PUT path {params :params} func)
    "DELETE" (DELETE path {params :params} func)
    (ANY path {params :params} func)))

(defn add-route
  [slug method route func]
  (log/debug (format "adding route %s -- %s %s" slug route method) :routing)
  (swap! caribou-routes assoc slug (resolve-method method route func))
  (swap! caribou-route-order conj slug)
  (swap! route-paths assoc (keyword slug) route))

(defn ordered-routes
  [routes route-order]
  (map #(% routes) route-order))

(defn clear-routes
  "Clears the app's routes. Used by Halo to update the routes."
  []
  (reset! caribou-routes {})
  (reset! caribou-route-order [])
  (reset! route-paths {}))

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
