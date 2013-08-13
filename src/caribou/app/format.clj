(ns caribou.app.format
  (:require [clojure.string :as string]
            [cheshire.core :as cheshire]
            [clojure-csv.core :as csv]
            [clojure.data.xml :as xml]
            [caribou.config :as config]))

(defn wrap-jsonp
  "Turn a callback name and a result into a jsonp response."
  [callback result]
  (str callback "(" result ")"))

(defn to-csv-column
  "Translate a list into something that can inhabit a single csv row."
  [bulk key]
  (let [morph (bulk (keyword key))]
    (cond
     (or (seq? morph) 
         (vector? morph) 
         (list? morph)) 
     (string/join "|" (map #(str (last (first %))) morph))
     :else (str morph))))

(defn to-csv
  "Convert a bunch of data to CSV."
  [headings bulk]
  (csv/write-csv [(filter identity (map #(to-csv-column bulk %) headings))]))

(def prep-xml)

(defn prep-xml-item
  "Convert something into XML."
  [bulk]
  (map (fn [key] [key (prep-xml (bulk key))]) (keys bulk)))

(defn prep-xml
  "Convert everything into XML."
  [bulk]
  (cond
   (map? bulk) (prep-xml-item bulk)
   (or (seq? bulk) (vector? bulk) (list? bulk)) (map (fn [item]
                                                       [:item (prep-xml-item item)])
                                                       bulk)
   :else (str bulk)))

(def content-map
  {:json "application/json"
   :xml "application/xml"
   :csv "text/csv"
   :text "text/plain"})

(def format-handlers
  {:json (fn [result params]
           (let [jsonify (cheshire/generate-string result)
                 jsonp (params :jsonp)]
             (if jsonp
               (wrap-jsonp jsonp jsonify)
               jsonify)))
   :xml  (fn [result params]
           (let [xmlify (prep-xml result)]
             (xml/emit-str (xml/sexp-as-element [:api xmlify]))))
   :csv  (fn [result params]
           (let [bulk (:response result)
                 what (-> result :meta :type keyword)
                 headings (if what 
                            (map name (keys (get-in @(config/draw :models) [(keyword what) :fields]))))
                 header (if what 
                          (csv/write-csv [headings]) "")]
             (cond
              (map? bulk) (str header (to-csv headings bulk))
              
              (or (seq? bulk) 
                  (vector? bulk) 
                  (list? bulk)) 
              (apply str (cons header (map #(to-csv headings %) bulk))))))})
