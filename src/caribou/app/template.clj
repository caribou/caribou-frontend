(ns caribou.app.template
  (:use [caribou.debug])
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [caribou.util :as util]
            [caribou.config :as config]
            [stencil.core :as stencil]
            [stencil.loader :as loader]))

(def templates (ref {}))
(def helpers (atom {}))

(defn default-template
  [path]
  (fn [params]
    (str "No template by the name " path)))

(defn template-closure
  [path]
  (fn [params]
    (stencil/render-file path params)))

(defn find-template
  [path]
  (let [template-path (util/pathify ["templates" path])]
    (if (io/resource template-path)
      (template-closure template-path)
      (default-template path))))

(defn register-helper
  [helper-name helper]
  (swap! helpers assoc helper-name helper))

(defn init
  []
  (let [env (config/environment)]
    (condp = env
      :development (loader/set-cache-policy loader/cache-never)
      :production (loader/set-cache-policy loader/cache-forever))))


