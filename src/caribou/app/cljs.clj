(ns caribou.app.cljs
  (:require [cljs.closure :as cljsc]
            [caribou.logger :as log]
            [caribou.config :as config]))

(defn build
  []
  (cljsc/build 
   (config/draw :cljs :root)
   (config/draw :cljs :options)))

(defn wrap-cljs
  [handler]
  (fn [request]
    (if (config/draw :cljs :reload)
      (build))
    (handler request)))
