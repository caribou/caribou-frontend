(ns caribou.app.core
  (:use compojure.core
        [clojure.string :only (join)]
        [caribou.debug])
  (:require [clojure.string :as string]
            [clojure.java.jdbc :as sql]
            [clojure.java.io :as io]
            [caribou.model :as model]
            [caribou.util :as util]
            [caribou.db :as db]
            [caribou.config :as config]
            [caribou.app.controller :as controller]
            [caribou.app.routing :as routing]
            [caribou.app.template :as template]
            [caribou.app.view :as view]))

(import (java.io File))

(defn default-template [env]
  (env :result))

(defn render-template [env]
  (let [template (or (@template/templates (keyword (env :template))) default-template)]
    (template env)))

(defn page-init []
  (model/init)
  (controller/load-controllers "app/controllers")
  (def all-routes (invoke-routes)))

(declare app)

(defn configure
  [app-config]
  (template/load-templates (util/pathify [config/root "app" "templates"])))
  (controller/load-controllers "app/controllers"))

(defn init
  []
  (page-init))

