(ns caribou.app.config
  (:require [flatland.ordered.map :as flatland]
            [caribou.util :as util]
            [caribou.config :as config]
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
                            :templates (atom {})
                            :show-stacktrace false}
                    :cljs {:root "resources/cljs"
                           :reload false
                           :options 
                           {:output-dir "resources/public/js/out"
                            :pretty-print true}}
                    :i18n {:enabled false
                           :locales (atom ())
                           :resource-map (atom {})}
                    :middleware (atom [])}]

    (util/deep-merge-with
     (fn [& args]
       (last args))
     core-config
     app-config)))
