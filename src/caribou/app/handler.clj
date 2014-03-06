(ns caribou.app.handler
  (:require [flatland.ordered.map :as flatland]
            [ns-tracker.core :as ns-tracker]
            [clojure.string :as string]
            [polaris.core :as polaris]
            [caribou.logger :as log]
            [caribou.util :as util]
            [caribou.config :as config]
            [caribou.core :as caribou]
            [caribou.app.middleware :as middleware]
            [caribou.app.error :as error]
            [caribou.app.request :as request]
            [caribou.app.template :as template]
            [caribou.app.util :as app-util]))

(defn wrap-caribou
  [handler config]
  (fn [request]
    (caribou/with-caribou config
      (if (config/draw :error :catch-exceptions)
        (try 
          (handler request)
          (catch Exception e 
            (let [trace (.getStackTrace e)
                  stack (map #(str "ERROR    |--> " %) trace)]
              (log/error (string/join "\n" (cons (str e) stack)))
              (if (config/draw :error :show-stacktrace)
                (throw e)
                (error/render-error :500 request)))))
        (handler request)))))

(defn make-router
  [reset]
  (let [routes (polaris/build-routes (reset))]
    (reset! (config/draw :routes) routes)
    (polaris/router routes)))

(def modified-namespaces
  (ns-tracker/ns-tracker ["src"]))

(defn handler
  [reset]
  (let [handler (atom (make-router reset))]
    (fn [request]
      (if (config/draw :controller :reload)
        (let [stale (modified-namespaces)]
          (if (seq stale)
            (do
              (doseq [ns-sym stale]
                (log/info (str "Reloading: " ns-sym))
                (require :reload ns-sym))
              (reset! handler (make-router reset))))))
      (let [response (@handler request)]
        (cond 
         (:reset-handler response)
         (do
           (reset! handler (make-router reset))
           (dissoc response :reset-handler))

         (= 404 (:status response))
         (error/render-error :404 request)

         :else response)))))
