(ns ^{:skip-wiki true}
  caribou.app.core
  (:require [caribou.model :as model]
            [caribou.core :as caribou]
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