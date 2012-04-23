(ns caribou.app.handler
  (:use
        [compojure.core :only (routes)]
        caribou.debug
        [ring.middleware.file :only (wrap-file)])
  (:require
        [caribou.util :as util]
        [compojure.handler :as compojure-handler]
        [caribou.config :as core-config]
        [caribou.model :as core-model]
        [caribou.db :as core-db]
        [caribou.app.util :as app-util]
        [caribou.app.pages :as pages]
        [caribou.app.request :as request]
        [caribou.app.routing :as routing]
        [caribou.app.template :as template]))

(declare reset-handler)

(defn use-public-wrapper
  [handler]
  (if-let [public-dir (@core-config/app :public-dir)]
    (fn [request] ((wrap-file handler public-dir) request))
    (fn [request] (handler request))))

(defn _dynamic-handler
  "calls the dynamic route generation functions and returns a composite handler"
  []
  (core-model/init)
  (template/init)
  (pages/create-page-routes)
  (-> (apply routes (vals @routing/caribou-routes))
      (use-public-wrapper)
      (core-db/wrap-db @core-config/db)
      (compojure-handler/api)))

(def dynamic-handler (app-util/memoize-visible-atom _dynamic-handler))

(defn reset-handler 
  "clears the memoize atom in the metadata for dynamic-handler, which causes it to 'un-memoize'"
  []
  (app-util/memoize-reset dynamic-handler))
