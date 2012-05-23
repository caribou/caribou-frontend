(ns caribou.app.controller
  (:require [clojure.java.io :as io]
            [caribou.util :as util]))

(defn get-controller-action
  [controller-ns controller-key action-key]
  ;FIXME is string concatenation idomatic?
  (if (and controller-key action-key)
    (let [full-ns-name (str controller-ns "." controller-key)
          full-ns (symbol full-ns-name)]
      (try
        (do
          (require :reload full-ns)
          (ns-resolve full-ns (symbol action-key)))
        (catch Exception e (println "Cannot load namespace " full-ns-name))))))

(defn render
  [params]
  (let [template (params :template)]
    {:status (or (params :status) 200)
     :headers {"Content-Type" (or (params :content-type) "text/html")}
     :body (template params)
     :session (params :session)}))

