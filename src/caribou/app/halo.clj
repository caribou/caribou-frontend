(ns caribou.app.halo
  (:require [clojure.string :as string]
            [caribou.app.pages :as pages]
            [caribou.app.routing :as routing]
            [caribou.config :as config]
            [caribou.model :as model]))

;; (declare halo-routes)

;; =======================
;; Route creation
;; =======================

(defn check-key
  "Wraps Halo requests and inspects the X-Halo-Key request header."
  [request func]
  (let [headers (request :headers)
        req-key (headers "x-halo-key")
        app-key (config/draw :halo :key)]
    (if (and app-key (= app-key req-key))
      (func request)
      {:status 401 :body "Forbidden"})))

(defn make-route
  [[method path func]]
  (let [full-path (str (config/draw :halo :prefix) "/" path)]
    (routing/add-route (keyword (str "halo-" path)) method full-path (fn [request] (check-key request func)))))

(defn append-route
  [& args]
  (swap! (config/draw :halo :routes) conj (vec args)))

(defn generate-routes
  []
  (if (and (config/draw :halo :enabled) (config/draw :halo :key))
    (doall
     (map make-route (deref (config/draw :halo :routes))))))

;; (def halo-hooks
;;   (ref
;;    {:reload-pages pages/create-page-routes
;;     :reload-models model/invoke-models
;;     :reload-halo halo/generate-routes
;;     :halo-reset identity}))

;; =======================
;; Halo routes
;; =======================

(defn run-halo-hook
  [request key]
  (let [hooks (deref (config/draw :halo :hooks))]
    ((get hooks key))
    ((get hooks :halo-reset))))

(defn reload-pages
  "reloads the Page routes in this Caribou app"
  [request]
  (run-halo-hook request :reload-pages)
  ;; ((:reload-pages @halo-hooks))
  ;; ((:halo-reset @halo-hooks))
  "Routes reloaded")

(defn reload-models
  "reloads the models in this Caribou app"
  [request]
  (run-halo-hook request :reload-models)
  ;; ((:reload-models @halo-hooks))
  ;; ((:halo-reset @halo-hooks))
  "Models reloaded")

(defn reload-halo
  "reloads the Halo routes in this Caribou app"
  [request]
  (run-halo-hook request :reload-halo)
  ;; ((:reload-halo @halo-hooks))
  ;; ((:halo-reset @halo-hooks))
  "Halo reloaded")

;; (def halo-routes
;;   (atom
;;    [["GET" "reload-routes" reload-pages]
;;     ["GET" "reload-halo" reload-halo]
;;     ["GET" "reload-models" reload-models]]))

;; =======================
;; Initialization
;; =======================

;; (defn init
;;   ([hooks]
;;      (dosync
;;       (alter halo-hooks merge hooks))
;;      (init))
;;   ([] (generate-routes)))
