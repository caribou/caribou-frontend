(ns caribou.app.pages
  (:use [clojure.walk :only (stringify-keys)])
  (:require [clojure.java.jdbc :as sql]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [ring.util.codec :as codec]
            [caribou.config :as config]
            [caribou.util :as util]
            [caribou.db :as db]
            [caribou.model :as model]
            [caribou.app.controller :as controller]
            [caribou.app.routing :as routing]
            [caribou.app.template :as template]))

(defonce actions (ref {}))
(defonce pages (ref ()))
(defonce page-map (ref {}))

(defn create-missing-controller-action
  [controller-key action-key]
  (fn [params]
    (format "Missing controller or action: %s/%s" controller-key action-key)))

(defn retrieve-controller-action
  "Given the controller-key and action-key, return the function that is correspondingly defined by a controller."
  [controller-key action-key]
  (let [controller-action (controller/get-controller-action (@config/app :controller-ns) controller-key action-key)]
    (if (and (not (nil? controller-key)) (nil? controller-action))
      (create-missing-controller-action controller-key action-key)
      (if (nil? controller-action)
        routing/default-action
        controller-action))))

(defn generate-action
  "Depending on the application environment, reload controller files (or not)."
  [page template controller-key action-key]
  (let [action (retrieve-controller-action controller-key action-key)
        found-template (template/find-template (or template (page :template)))]
    (fn [params]
      (action (merge params {:template found-template :page page})))))

(defn make-route
  [[path slug method]]
  (let [action (get @actions (routing/deslash slug))]
    (routing/add-route slug method path action)))

(defn match-action-to-template
  "Make a single route for a single page, given its overarching path (above-path)"
  [page above-path]
  (let [page-path (page :path)
        path (str above-path (if-not (empty? page-path) (str "/" (name page-path))))
        page-slug (keyword (or (:slug page) (str (:id page))))
        controller-key (page :controller)
        action-key (page :action)
        method-key (page :method)
        template (page :template)
        full (generate-action page template controller-key action-key)]
    (dosync
     (alter actions merge {page-slug full}))
    (concat
     [[path page-slug method-key]]
     (mapcat #(match-action-to-template % path) (page :children)))))

(defn slashify-route
  [[path slug method]]
  [(str path "/") (keyword (str (name slug) "-with-slash")) method])

(defn generate-page-routes
  "Given a tree of pages construct and return a list of corresponding routes."
  [pages]
  (let [localization (or (:localize-routes @config/app) "")
        routes (apply concat (map #(match-action-to-template % localization) pages))
        direct (map make-route routes)
        unslashed (filter #(empty? (re-find #"/$" (first %))) routes)
        slashed (map slashify-route unslashed)
        indirect (map make-route slashed)]
    (concat indirect direct)))

(defn all-pages
  []
  (if (@config/app :use-database)
    (sql/with-connection @config/db
      (let [rows (util/query "select * from page order by position asc")]
        (model/arrange-tree rows)))
    []))

(defn invoke-pages
  "Call up the pages and arrange them into a tree."
  [tree]
  (dosync
   (alter pages (fn [a b] b) tree)))

(defn create-page-routes
  "Invoke pages from the db and generate the routes based on them."
  ([] (create-page-routes (all-pages)))
  ([tree]
     (invoke-pages tree)
     (doall (generate-page-routes @pages))))

(defn reverse-route
  [routes slug opts]
  (let [path (get routes (keyword slug)
                  ;; throw the exception if the route for the slug is not found
                  (lazy-seq
                    (throw (new Exception
                                (str "route for " slug " not found")))))
        opt-keys (keys opts)
        route-keys (map read-string (filter #(= (first %) \:)
                                            (string/split path #"/")))
        query-keys (remove (into #{} route-keys) opt-keys)
        base (reduce
              #(string/replace-first %1 (str (keyword %2)) (get opts %2))
              path opt-keys)
        query-item (fn [[k v]] (str (codec/url-encode (name k))
                                    "="
                                    (codec/url-encode v)))
        query (string/join "&" (map query-item (select-keys opts query-keys)))
        query (and (seq query) (str "?" query))]
    (str base query)))

(defn route-for
  [slug opts]
  (reverse-route @routing/route-paths slug opts))
