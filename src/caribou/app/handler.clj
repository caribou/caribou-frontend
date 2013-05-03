(ns caribou.app.handler
  (:use caribou.debug
        [ring.middleware.content-type :only (wrap-content-type)]
        [ring.middleware.file :only (wrap-file)]
        [ring.middleware.resource :only (wrap-resource)]
        [ring.middleware.file-info :only (wrap-file-info)]
        [ring.middleware.head :only (wrap-head)]
        [ring.middleware.json-params :only (wrap-json-params)]
        [ring.middleware.multipart-params :only (wrap-multipart-params)]
        [ring.middleware.session :only (wrap-session)]
        [ring.util.response :only (resource-response file-response)])
  (:require [caribou.util :as util]
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
      (handler request))))

(defn make-handler
  [& args]
  (init-routes)
  (template/init)
  (-> (routing/router (deref (config/draw :routes)))
      (middleware/wrap-custom-middleware)))

(defn handler
  [reset]
  (let [handle (ref (make-handler))]
    (fn [request]
      (condp = request
        :reset (do
                 (reset)
                 (dosync (alter handle make-handler)))
        (@handle request)))))

(defn reset-handler
  [handler]
  (handler :reset)
  handler)