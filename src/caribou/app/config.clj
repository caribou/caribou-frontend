(ns caribou.app.config
  (:require [flatland.ordered.map :as flatland]
            [caribou.util :as util]
            [caribou.config :as config]
            [caribou.model :as model]
            [caribou.app.pages :as pages]))

(defn default-config
  []
  (let [core-config (config/default-config)
        app-config {:app {:public-dir "public"
                          :default-locale "global"
                          :localize-routes ""}
                    :controller {:namespace "skel.controllers"
                                 :reload true
                                 :session-defaults (atom {})}
                    :actions (atom {})
                    :pages (atom ())
                    :routes (atom (flatland/ordered-map))
                    :handler (atom nil)
                    :reset (atom nil)
                    :pre-actions (atom {})
                    :template {:helpers (atom {})
                               :cache-strategy :never}
                    :error {:handlers (atom {})
                            :templates (atom {})}
                    :i18n {:enabled false
                           :locales (atom ())
                           :resource-map (atom {})}
                    :middleware (atom [])}]

                    ;; :halo {:enabled true
                    ;;        :hosts ["http://localhost:33333"]
                    ;;        :key "halo-caribou"
                    ;;        :prefix "/_halo"
                    ;;        :hooks (atom {:reload-pages pages/create-page-routes
                    ;;                      :reload-models model/invoke-models
                    ;;                      :reload-halo halo/generate-routes
                    ;;                      :halo-reset identity})
                    ;;        :routes (atom [["GET" "reload-routes" reload-pages]
                    ;;                       ["GET" "reload-halo" reload-halo]
                    ;;                       ["GET" "reload-models" reload-models]])}}]

    (util/deep-merge-with
     (fn [& args]
       (last args))
     core-config
     app-config)))