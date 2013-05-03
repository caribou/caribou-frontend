(ns caribou.app.helpers
  (:require [caribou.asset :as asset]
            [caribou.config :as config]
            [caribou.app.pages :as pages]
            [lichen.core :as lichen]))

(defn resize-image
  [image opts]
  (let [path (asset/asset-location image)
        asset-root (config/draw :assets :dir)
        lichen-path (str "/" lichen/lichen-root path)
        queries (lichen/query-string opts)
        target (lichen/lichen-uri lichen-path queries "")]
    (lichen/lichen-resize lichen-path opts asset-root)
    (str (config/draw :assets :root) "/" target)))

(defn route-for [slug params & additional]
  (pages/route-for slug (apply merge (cons params additional))))

(def helpers
  {:resize resize-image
   :route-for route-for})
