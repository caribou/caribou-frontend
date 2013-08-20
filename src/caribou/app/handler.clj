(ns caribou.app.handler
  (:use [ring.middleware.content-type :only (wrap-content-type)]
        [ring.middleware.file :only (wrap-file)]
        [ring.middleware.resource :only (wrap-resource)]
        [ring.middleware.file-info :only (wrap-file-info)]
        [ring.middleware.head :only (wrap-head)]
        [ring.middleware.json-params :only (wrap-json-params)]
        [ring.middleware.multipart-params :only (wrap-multipart-params)]
        [ring.middleware.session :only (wrap-session)]
        [ring.util.response :only (resource-response file-response)])
  (:require [flatland.ordered.map :as flatland]
            [ns-tracker.core :as ns-tracker]
            [clojure.string :as string]
            [caribou.logger :as log]
            [caribou.util :as util]
            [caribou.config :as config]
            [caribou.core :as caribou]
            [caribou.app.middleware :as middleware]
            [caribou.app.pages :as pages]
            [caribou.app.error :as error]
            [caribou.app.request :as request]
            [caribou.app.routing :as routing]
            [caribou.app.template :as template]
            [caribou.app.util :as app-util]))

(declare reset-handler)

(defn use-public-wrapper
  [handler public-dir]
  (if public-dir
    (fn [request] ((wrap-resource handler public-dir) request))
    (fn [request] (handler request))))

(defn init-routes
  []
  (middleware/add-custom-middleware middleware/wrap-xhr-request)
  (let [routes (routing/routes-in-order (deref (config/draw :routes)))]
    (routing/add-head-routes)))

(defn wrap-caribou
  [handler config]
  (fn [request]
    (caribou/with-caribou config
      (try 
        (handler request)
        (catch Exception e 
          (let [trace (.getStackTrace e)
                stack (map #(str "ERROR    |--> " %) trace)]
            (log/error (string/join "\n" (cons (str e) stack)))
            (if (config/draw :error :show-stacktrace)
              (throw e)
              (error/render-error :500 request))))))))

(defn make-handler
  [& args]
  (init-routes)
  (template/init)
  (-> (routing/router (deref (config/draw :routes)))
      (middleware/wrap-custom-middleware)))

(declare reset-handler)

(def modified-namespaces
  (ns-tracker/ns-tracker ["src"]))

(defn handler
  [reset]
  (let [handler (make-handler)]
    (reset! (config/draw :handler) handler)
    (reset! (config/draw :reset) reset)
    (fn [request]
      (if (config/draw :controller :reload)
        (let [stale (modified-namespaces)]
          (if (seq stale)
            (do
              (doseq [ns-sym stale]
                (log/info (str "Reloading: " ns-sym))
                (require :reload ns-sym))
              (reset-handler)))))
      (let [handler (deref (config/draw :handler))]
        (handler request)))))

(defn trigger-reset
  []
  ((deref (config/draw :reset))))

(defn reset-handler
  []
  (reset! (config/draw :routes) (flatland/ordered-map))
  (trigger-reset)
  (reset! (config/draw :handler) (make-handler)))
