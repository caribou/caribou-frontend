(ns ^{:skip-wiki true}
  caribou.app.core
  (:require [caribou.core :as caribou]
            [caribou.config :as config]
            [caribou.app.config :as app-config]
            [caribou.app.handler :as handler]))

(declare handler)

(defn environment-config
  []
  (let [default (app-config/default-config)
        config (config/config-from-environment default)]
    (caribou/init config)))

(defn init
  []
  (let [config (environment-config)]
    (def handler
      (caribou/with-caribou config
        (-> (handler/handler identity)
            (handler/wrap-caribou config))))))

