(ns caribou.app.handler
  (:use
        [compojure.core :only (routes)]
        caribou.debug)
  (:require
        [compojure.handler :as compojure-handler]
        [caribou.model :as model]
        [caribou.app.util :as app-util]
        [caribou.app.pages :as pages]
        [caribou.app.request :as request]
        [caribou.app.routing :as routing]
        [caribou.app.template :as template]))

(declare reset-handler)

(defn _dynamic-handler
  "calls the dynamic route generation functions and returns a composite handler"
  []
  (model/init)
  (template/init)
  (pages/create-page-routes)
  (-> (apply routes (vals @routing/caribou-routes))
      (compojure-handler/api)))

(def dynamic-handler (app-util/memoize-visible-atom _dynamic-handler))

(defn reset-handler 
  "clears the memoize atom in the metadata for dynamic-handler, which causes it to 'un-memoize'"
  []
  (app-util/memoize-reset dynamic-handler))
