(ns caribou.app.pages
  (:use [clojure.walk :only (stringify-keys)])
  (:require [clojure.java.jdbc :as sql]
            [clojure.java.io :as io]
            [caribou.config :as config]
            [caribou.db :as db]
            [caribou.model :as model]
            [caribou.app.controller :as controller]
            [caribou.app.routing :as routing]
            [caribou.app.template :as template]))

(defonce actions (ref {}))
(defonce pages (ref ()))

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
  [[path action method]]
  (let [this-action (actions action)]
    (routing/add-route method path this-action)))

(defn match-action-to-template
  "Make a single route for a single page, given its overarching path (above-path)"
  [page above-path]
  (let [page-path (page :path)
        path (str above-path "/" (if page-path (name page-path) ""))
        page-id (keyword (str (page :id)))
        controller-key (page :controller)
        action-key (page :action)
        method-key (page :method)
        template (page :template)
        full (generate-action page template controller-key action-key)]
    (dosync
     (alter actions merge {(keyword (str (page :id))) full}))
    (concat
     [[path page-id method-key]]
     (mapcat #(match-action-to-template % path) (page :children)))))

(defn generate-page-routes
  "Given a tree of pages construct and return a list of corresponding routes."
  [pages]
  (let [routes (apply concat (map #(match-action-to-template % "") pages))
        direct (map make-route routes)
        unslashed (filter #(empty? (re-find #"/$" (first %))) routes)
        slashed (map #(cons (str (first %) "/") (rest %)) unslashed)
        indirect (map make-route slashed)]
    (concat direct indirect)))

(defn all-pages
  []
  (if (@config/app :use-database)
    (sql/with-connection @config/db
      (let [rows (db/query "select * from page")]
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
