(ns caribou.app.halo
  (:use caribou.debug)
  (:require [clojure.string :as string]
            [caribou.app.pages :as pages]
            [caribou.app.routing :as routing]
            [caribou.config :as config]
            [caribou.model :as model]))

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
    (routing/add-route (keyword (str "halo-" path)) method full-path (fn [request] (check-key request func)))))

(defn append-route
  [& args]
  (swap! halo-routes conj (vec args)))

(defn generate-routes
  []
  (if (and (@config/app :halo-enabled) (@config/app :halo-key))
    (doall
     (map make-route @halo-routes))))

(def halo-hooks
  (ref
   {:reload-pages pages/create-page-routes
    :reload-models model/invoke-models
    :reload-halo generate-routes
    :halo-reset identity}))

;; =======================
;; Halo routes
;; =======================

(defn reload-pages
  "reloads the Page routes in this Caribou app"
  [request]
  ((:reload-pages @halo-hooks))
  ((:halo-reset @halo-hooks))
  "Routes reloaded")

(defn reload-models
  "reloads the models in this Caribou app"
  [request]
  ((:reload-models @halo-hooks))
  ((:halo-reset @halo-hooks))
  "Models reloaded")

(defn reload-halo
  "reloads the Halo routes in this Caribou app"
  [request]
  ((:reload-halo @halo-hooks))
  ((:halo-reset @halo-hooks))
  "Halo reloaded")

(def halo-routes
  (atom
   [["GET" "reload-routes" reload-pages]
    ["GET" "reload-halo" reload-halo]
    ["GET" "reload-models" reload-models]]))

;; =======================
;; Initialization
;; =======================

(defn init
  ([hooks]
     (dosync
      (alter halo-hooks merge hooks))
     (init))
  ([] (generate-routes)))
