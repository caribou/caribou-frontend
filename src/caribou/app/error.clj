(ns caribou.app.error
  (:require [clojure.java.io :as io]
            [caribou.config :as config]
            [caribou.logger :as logger]
            [caribou.util :as util]
            [caribou.app.controller :as controller]
            [caribou.app.template :as template]))

(defonce error-handlers (atom {}))
(defonce error-templates (atom {}))
  
(defn register-error-handler
  [err f]
  (swap! error-handlers merge {err f}))

(defn register-error-template
  [err temp]
  (swap! error-templates merge {err temp}))

(defn default-handler
  [request]
  (controller/render (assoc request :content-type "text/html")))

(defn template-for-error
  [err]
  (util/pathify ["errors" (str (name err) ".html")]))

(def default-error-template (util/pathify ["errors" "default.html"]))

(defn find-error-template
  [err]
  (let [error-template (template-for-error err)]
    (if (template/template-exists? (template/path-for-template error-template))
      error-template
      default-error-template)))

(defn render-error 
  [err request]
  (let [err-handler (or (err @error-handlers) default-handler)
        err-template (or (err @error-templates) (find-error-template err))
        err-request (assoc request :status (read-string (name err)) :template (template/find-template err-template))]
    (err-handler err-request)))
