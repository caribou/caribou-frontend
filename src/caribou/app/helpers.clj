(ns caribou.app.helpers
  (:require [caribou.asset :as asset]
            [caribou.config :as config]
            [caribou.app.pages :as pages]
            [lichen.path :as path]
            [lichen.core :as lichen]
            [caribou.field.timestamp :as stamp]
            [caribou.model :as model]
            [clojure.string :as string]
            [clj-time
             [core :as time]
             [coerce :as coerce-time]])
  (:import java.util.Date
           java.text.SimpleDateFormat))

(defn ago
  [date]
  (letfn [(notzero [convert data]
            (let [span (convert data)]
              (if (== span 0)
                false
                span)))
          (ago-str [string num direction]
            (str num " " string (if (== num 1) " " "s ") direction))]
    (let [date (if (string? date)
                 (stamp/read-date date)
                 date)
          last-time (coerce-time/from-date date)
          this-time (time/now)
          [last-time
           this-time
           direction] (if (time/before? last-time this-time)
                        [last-time this-time "ago"]
                        [this-time last-time "from now"])
          interval (time/interval last-time this-time)]
      (condp notzero interval
        time/in-years :>> #(ago-str "year" % direction)
        time/in-months :>> #(ago-str "month" % direction)
        time/in-days :>> #(ago-str "day" % direction)
        time/in-hours :>> #(ago-str "hour" % direction)
        time/in-minutes :>> #(ago-str "minute" % direction)
        time/in-secs :>> #(ago-str "second" % direction)
        (constantly 1) "right now"))))

;; pull date and time bits out of a date-field
(defn date-year
  [m key]
  (if-let [date (-> key keyword m)]
    (+ (. date getYear) 1900)
    nil))

(defn date-month
  [m key]
  (if-let [date (-> key keyword m)]
    (+ (. date getMonth) 1)
    nil))

(defn date-day
  [m key]
  (if-let [date (-> key keyword m)]
    (+ (. date getDate))
    nil))

(defn current-date
  []
  (model/current-timestamp))

(defn yyyy-mm-dd
  [m key]
  (let [frm (java.text.SimpleDateFormat. "yyyy-MM-dd")
        date (-> key keyword m)]
    (if-not (nil? date)
      (.format frm date)
      nil)))

; this is nonsense
(defn yyyy-mm-dd-or-current
  [m key]
  (if-let [date-string (yyyy-mm-dd m key)]
    date-string
    (yyyy-mm-dd {:d (current-date)} :d)))

(defn hh-mm
  [m key]
  (if-let [date (-> key keyword m)]
    (.format (java.text.SimpleDateFormat. "HH:mm") date)
    "00:00"))

(defn resize-image
  [image opts]
  (let [path (asset/asset-location image)
        asset-root (config/draw :assets :dir)
        lichen-path (str "/" path/lichen-root path)
        queries (path/query-string opts)
        target (path/lichen-uri lichen-path queries "")]
    (lichen/lichen-resize lichen-path opts asset-root)
    (str (config/draw :assets :root) "/" target)))

(defn safer-resize-image
  [image opts]
  (if image
    (resize-image image opts)
    ""))

(defn route-for
  [slug params & additional]
  (pages/route-for slug (apply merge (cons params additional))))

(defn safe-route-for
  [slug & args]
  (pages/route-for slug (pages/select-route slug (apply merge args))))

(defn truncate
  [string count]
  (subs string 0 count))

(defn linebreak
  [s]
  (->> s
       (#(string/split % #"\n"))
       (map #(str "<p>" % "</p>"))
       (apply str)))

(defn smartquote
  [s]
  (let [left-double "&#8220;" ; “
        right-double "&#8221;" ; ”
        double-replace (str left-double "$1" right-double)
        left-single "&#8216;" ; ‘
        right-single "&#8217;" ; ’
        single-replace (str left-single "$1" right-single)]
    (-> s
        (string/replace #"\"([^\"]*)\"" double-replace)
        (string/replace #"'([^']*)'" single-replace))))

(def helpers
  {:ago ago
   :now current-date
   :current-date current-date
   :date-year date-year
   :date-month date-month
   :date-day date-day
   :hh-mm hh-mm
   :yyyy-mm-dd yyyy-mm-dd
   :yyyy-mm-dd-or-current yyyy-mm-dd-or-current
   :equals =
   :linebreak linebreak
   :resize safer-resize-image
   :route-for safe-route-for
   ;; :safer-resize safer-resize-image
   ;; :safe-route-for safe-route-for
   :smartquote smartquote
   :truncate truncate})
