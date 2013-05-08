(ns ^{:skip-wiki true}
  caribou.app.core
  (:require [caribou.core :as caribou]
            [caribou.app.config :as config]
            [caribou.app.handler :as handler]))

(declare handler)

(defn init
  []
  (let [config (config/default-config)
        config (caribou/init config)]
    (def handler
      (caribou/with-caribou config
        (-> (handler/handler identity)
            (handler/wrap-caribou config))))))

(defn environment-config
  []
  (let [default (config/default-config)
        config (config/config-from-environment default)]
    (caribou/init config)))
