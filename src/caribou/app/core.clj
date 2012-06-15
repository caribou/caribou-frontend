(ns ^{:skip-wiki true}
  caribou.app.core
  (:require [caribou.config :as config]
            [caribou.app.handler :as handler]))

(declare app)

(defn init
  []
  (config/init)
  ; Define the app handler, we have to delay this until after the routes are created.
  (def app (handler/gen-handler)))

;; (defn default-template [env]
;;   (env :result))

;; (defn render-template [env]
;;   (let [template (or (@template/templates (keyword (env :template))) default-template)]
;;     (template env)))

;; (defn page-init []
;;   (model/init)
;;   (controller/load-controllers "app/controllers")
;;   (def all-routes (invoke-routes)))

;; (defn configure
;;   [app-config]
;;   (template/load-templates (util/pathify [config/root "app" "templates"])))
;;   (controller/load-controllers "app/controllers"))

;; (defn init
;;   []
;;   (page-init))

