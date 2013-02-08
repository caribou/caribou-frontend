(ns ^{:skip-wiki true}
  caribou.app.core
  (:require [caribou.config :as config]
            [caribou.app.handler :as handler]))

(declare app)

(defn init
  []
  (config/init)
  (def app (handler/gen-handler)))

