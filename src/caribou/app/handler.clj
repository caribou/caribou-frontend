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

(comment
(defn- pack-routes
  []
  (if (empty? @routing/caribou-routes)
    (routing/add-default-route))
  (let [all-routes (routing/routes-in-order @routing/caribou-routes)]
    (apply
     routes
     (conj
      (into [] (cons (HEAD "/" [] "") all-routes))
      (route/files "/" {:root (@core-config/app :asset-dir)})
      (route/resources "/")
      (partial error/render-error :404))))))

;; STOLEN/adapted from compojure
(defn- add-wildcard
  "Add a wildcard to the end of a route path."
  [path]
  (str path (if (.endsWith path "/") "*" "/*")))

(defn resource-handler
  [options]
  (fn [request]
    (let [options (merge {:root "public"} options)
          file-path (-> request :route-params :*)]
      (resource-response file-path options))))

(defn resources
  "A route for serving static files from a directory. Accepts the following
  keys:
    :root - the root path where the files are stored. Defaults to 'public'."
  [path & [options]]
  (routing/add-route :--RESOURCES
                     :get
                     (add-wildcard path)
                     (resource-handler options)))

;(defn resources
  ;"A route for serving resources on the classpath. Accepts the following
  ;keys:
    ;:root - the root prefix to get the resources from. Defaults to 'public'."
  ;[path & [options]]
  ;(-> (GET (add-wildcard path) {{resource-path :*} :route-params}
        ;(let [root (:root options "public")]
          ;(resource-response (str root "/" resource-path))))
      ;(wrap-file-info (:mime-types options))
      ;(wrap-content-type options)
      ;(wrap-head)))
;; END stolen

(defn init-routes
  []
  (middleware/add-custom-middleware middleware/wrap-xhr-request)
  (let [routes (routing/routes-in-order @routing/caribou-routes)]
    (routing/add-head-routes routes)
    (resources "/")))

(defn handler
  []
  (-> (routing/router)
      (middleware/wrap-custom-middleware)
      (wrap-file-info)
      (wrap-head)))

(comment
(defn _dynamic-handler
  "calls the dynamic route generation functions and returns a composite handler"
  []
  (core-config/init)
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
  (app-util/memoize-reset dynamic-handler)))
