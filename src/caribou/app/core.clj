(ns ^{:skip-wiki true}
  caribou.app.core
  (:require [caribou.config :as config]
            [caribou.model :as model]
            [caribou.app.handler :as handler]))

(declare handler)

(defn init
  []
  (config/init)
  (model/init)
  (def handler (handler/handler)))

