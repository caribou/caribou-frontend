(ns caribou.app.error
  (:require [clojure.java.io :as io]
            [caribou.logger :as logger]
            [caribou.util :as util]
            [caribou.config :as config]
            [caribou.app.controller :as controller]
            [caribou.app.template :as template]))

(defn register-error-handler
  [err f]
  (swap! (config/draw :error :handlers) merge {err f}))

(defn register-error-template
  [err temp]
  (swap! (config/draw :error :templates) merge {err temp}))

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
  (let [err-handler (or (get (deref (config/draw :error :handlers)) err) default-handler)
        err-template (or (get (deref (config/draw :error :templates)) err) (find-error-template err))
        err-request (assoc request :status (read-string (name err)) :template (template/find-template err-template))]
    (err-handler err-request)))
