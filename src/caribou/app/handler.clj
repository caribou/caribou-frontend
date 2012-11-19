(ns caribou.app.handler
  (:use [compojure.core :only (routes HEAD)]
        caribou.debug
        [ring.middleware.file :only (wrap-file)]
        [ring.middleware.resource :only (wrap-resource)]
        [ring.middleware.json-params :only (wrap-json-params)]
        [ring.middleware.multipart-params :only (wrap-multipart-params)]
        [ring.middleware.session :only (wrap-session)])
  (:require [caribou.util :as util]
            [compojure.route :as route]
            [compojure.handler :as compojure-handler]
            [caribou.config :as core-config]
            [caribou.model :as core-model]
            [caribou.db :as core-db]
            [caribou.app.halo :as halo]
            [caribou.app.i18n :as i18n]
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

(defn- pack-routes
  []
  (if (empty? @routing/caribou-routes)
    (routing/add-default-route))
  ;; FIXME this is ugly and tacked on, hit willhite on the back of the head if you see this message
  (apply
   routes
   (conj
    (into [] (cons (HEAD "/" [] "") (routing/routes-in-order @routing/caribou-routes)))
    (route/files "/" {:root (@core-config/app :asset-dir)})
    (route/resources "/")
    (partial error/render-error :404))))

(defn- init-routes
  []
  (-> (pack-routes)
      (middleware/wrap-custom-middleware)))

(defn base-handler
  []
  (middleware/add-custom-middleware middleware/wrap-xhr-request)
  (init-routes))

(defn _dynamic-handler
  "calls the dynamic route generation functions and returns a composite handler"
  []
  (core-model/init)
  (i18n/init)
  (template/init)
  ((:reload-pages @halo/halo-hooks))
  (halo/init)
  (base-handler))

(def dynamic-handler (app-util/memoize-visible-atom _dynamic-handler))

(defn gen-handler
  "Returns a function that calls our memoized handler on every request"
  []
  (fn [request]
    ((dynamic-handler) request)))

(defn reset-handler
  "clears the memoize atom in the metadata for dynamic-handler, which causes it to 'un-memoize'"
  []
  (log :handler "Resetting Handler")
  (routing/clear-routes)
  (app-util/memoize-reset dynamic-handler))
