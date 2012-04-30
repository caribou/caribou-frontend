(ns caribou.app.halo
  (:use compojure.core
        [caribou.debug])
  (:require [clojure.string :as string]
            [caribou.app.pages :as pages]
            [caribou.app.routing :as routing]
            [caribou.config :as config]
            [caribou.model :as model]))

(declare route-reloader)
(declare halo-routes)

;; =======================
;; Route creation
;; =======================

(defn check-key
  "Wraps Halo requests and inspects the X-Halo-Key request header."
  [request func]
  (let [headers (request :headers)
        req-key (headers "x-halo-key")
        app-key (@config/app :halo-key)]
    (if (= app-key req-key)
      (func request)
      {:status 401 :body "Forbidden"})))

(defn make-route
  [[method path func]]
  (let [full-path (str (@config/app :halo-prefix) "/" path)]
    (routing/add-route method full-path (fn [request] (check-key request func)))))

(defn generate-routes
  []
  (if (and (@config/app :halo-enabled) (@config/app :halo-key))
    (do
      (doall
        (map make-route halo-routes)))))

;; =======================
;; Halo routes
;; =======================

(defn reload-routes
  "reloads the Page routes in this Caribou app"
  [request]
  (pages/create-page-routes)
  (route-reloader)
  "Routes reloaded")

(defn reload-halo
  "reloads the Halo routes in this Caribou app"
  [request]
  (generate-routes)
  (route-reloader)
  "Halo reloaded")

(defn reload-models
  "reloads the models in this Caribou app"
  [request]
  (model/invoke-models)
  "Models reloaded")

(def halo-routes
  [["GET" "reload-routes" reload-routes]
   ["GET" "reload-halo" reload-halo]
   ["GET" "reload-models" reload-models]])

;; =======================
;; Initialization
;; =======================

(defn init
  [reset-handler]
  (def route-reloader reset-handler)
  (generate-routes))
