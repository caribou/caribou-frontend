(ns caribou.app.pages
  (:use
        caribou.debug
        [clojure.walk :only (stringify-keys)])
  (:require
        [clojure.java.jdbc :as sql]
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

(defn retrieve-action
  "Given the controller-key and action-key, return the function that is correspondingly defined by a controller."
  [controller-key action-key]
  (let [controller-action (controller/get-controller-action (@config/app :controller-ns) controller-key action-key)
        action (if (and (not (nil? controller-key)) (nil? controller-action))
                  (create-missing-controller-action controller-key action-key)
                  (if (nil? controller-action)
                    routing/default-action
                    controller-action))]
    action))

(defn generate-action
  "Depending on the application environment, reload controller files (or not)."
  [page template controller-key action-key]
  (if (config/app-value-eq :debug true)
    (do
      ; reload templates
      (template/init)
      (fn [params]
        (let [action (retrieve-action controller-key action-key)
              found-template
              (or template
                  (do
                    (@template/templates (keyword (page :template)))))]
          (if found-template
            (action (merge params {:template found-template}))
            (str "No template by the name " (page :template))))))
    ;FIXME non-dev mode needs to load controller and template
    (let [action (retrieve-action controller-key action-key)]
      (if template
        (fn [params]
          (template params))
        (fn [params] (str "No template by the name " (page :template)))))))

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
        template (@template/templates (keyword (page :template)))
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

(defn invoke-pages
  "Call up the pages and arrange them into a tree."
  []
  (let [rows (db/query "select * from page")
        tree (model/arrange-tree rows)]
    (dosync
     (alter pages (fn [a b] b) tree))))

(defn create-page-routes
  "Invoke pages from the db and generate the routes based on them."
  []
  (if (@config/app :use-database)
    (sql/with-connection @config/db
      (let [_pages (invoke-pages)
            generated (doall (generate-page-routes @pages))]
        generated))))
