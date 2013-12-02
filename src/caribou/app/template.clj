(ns caribou.app.template
  (:use [caribou.debug])
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [caribou.util :as util]
            [caribou.config :as config]
            [antlers.core :as antlers]
            [antlers.parser :as parser]))


(defn path-for-template
  [path]
  ; FIXME: this should probably be configurable or able to look in multiple places
  (util/pathify ["templates" path]))

(defn template-exists?
  [template-path]
  (io/resource template-path))

(defn find-template
  [path]
  (when-not (empty? path)
    (let [template-path (path-for-template path)]
      (if (template-exists? template-path)
        template-path
        (throw (ex-info (str "No template by the name " path) {:type :template-not-found}))))))

(defn register-helper
  [helper-name helper]
  (swap! (config/draw :template :helpers) assoc helper-name helper))

(defn init
  []
  (let [cache-strategy (config/draw :template :cache-strategy)]
    (condp = cache-strategy
      :never (parser/set-cache-policy parser/cache-never)
      :always (parser/set-cache-policy parser/cache-forever)
      (parser/set-cache-policy parser/cache-never))))
