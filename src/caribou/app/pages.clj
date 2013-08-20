(ns caribou.app.pages
  (:use [ns-tracker.core :only (ns-tracker)])
  (:require [clojure.java.jdbc :as sql]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [ring.util.codec :as codec]
            [ring.middleware.basic-authentication :only (wrap-basic-authentication)]
            [caribou.config :as config]
            [caribou.util :as util]
            [caribou.db :as db]
            [caribou.model :as model]
            [caribou.logger :as log]
            [caribou.app.auth :as auth]
            [caribou.app.controller :as controller]
            [caribou.app.routing :as routing]
            [caribou.app.template :as template]))

(defn create-missing-controller-action
  [controller-key action-key]
  (fn [request]
    {:status 500
     :body (format "Missing controller or action: %s/%s" controller-key action-key)}))

(defn retrieve-controller-action
  "Given the controller-key and action-key, return the function that is correspondingly defined by a controller."
  [controller-namespace controller-key action-key]
  (if-let [found-namespace (controller/get-controller-namespace controller-namespace controller-key)]
    (if-let [controller-action (controller/get-controller-action found-namespace action-key)]
      controller-action
      (do
        (log/info (format "No action named %s in %s!" action-key controller-key))
        routing/default-action))
    (do
      (log/info (format "No controller namespace named %s.%s!" controller-namespace controller-key))
      routing/default-action)))

(defn placeholder?
  [el]
  (and
   (keyword? el)
   (= \$ (-> el name first))))

(defn siphon-substitute
  [spec request]
  (walk/postwalk
   (fn [el]
     (if (placeholder? el)
       (let [key (-> el name (subs 1) keyword)]
         (-> request :params key))
       el))
   spec))

(def op-map
  {:gather model/gather
   :pick model/pick})

(defn draw-siphons
  [page request]
  (let [content
        (reduce
         (fn [content [key siphon]]
           (let [spec (:spec siphon)
                 model (:model spec)
                 op-slug (:op spec)
                 op (get op-map op-slug)
                 spec (dissoc spec :model :op)
                 filled (siphon-substitute spec request)
                 target (op model filled)]
             (assoc content key target)))
         {} (:siphons page))]
    (assoc page
      :content content)))

(defn generate-core-action
  [controller-namespace controller-key action-key template page]
  (let [action (retrieve-controller-action controller-namespace controller-key action-key)
        found-template (template/find-template (or template (page :template)))]
    (fn [request]
      (action (merge request {:template found-template :page (draw-siphons page request)})))))

(defn generate-reloading-action
  [controller-namespace controller-key action-key template page]
  ;; (let [watched-namespaces (ns-tracker ["src"])]
    (fn [request]
      ;; (doseq [ns-sym (watched-namespaces)]
      ;;   (require :reload ns-sym))
      (let [action (retrieve-controller-action controller-namespace controller-key action-key)
            found-template (template/find-template (or template (page :template)))]
        (action (merge request {:template found-template :page (draw-siphons page request)})))))

(defn protect-action
  [action protection]
  (let [auth-protection (auth/enact-protection protection)]
    (fn [request]
      (auth/basic-authentication action request auth-protection))))

(defn generate-action
  "Return a handler that can be configured to reload controller namespaces"
  [page controller-namespace template controller-key action-key protection]
  (let [generated
        (if (config/draw :controller :reload)
          (generate-reloading-action controller-namespace controller-key action-key template page)
          (generate-core-action controller-namespace controller-key action-key template page))]
    (if (empty? protection)
      generated
      (protect-action generated protection))))

(defn make-route
  [[path slug method]]
  (let [action (get (deref (config/draw :actions)) (routing/deslash slug))]
    (routing/add-route slug method path action)))

(defn divine-protection
  [page protection]
  (if (:protected page)
    {:username (:username page)
     :password (:crypted-password page)}
    protection))

(defn bind-action
  "Make a single route for a single page, given its overarching path (above-path)"
  [page controller-namespace above-path protection middleware]
  (let [page-path (:path page)
        path (str above-path (if-not (empty? page-path) (str "/" (name page-path))))
        page-slug (keyword (or (:slug page) (str (:id page))))
        controller-key (:controller page)
        action-key (:action page)
        method-key (:method page)
        template (:template page)
        protection (divine-protection page protection)
        full (generate-action page controller-namespace template controller-key action-key protection)]
    (swap! (config/draw :actions) merge {page-slug (middleware full)})
    (concat
     [[path page-slug method-key]]
     (mapcat #(bind-action % controller-namespace path protection middleware) (:children page)))))

(defn slashify-route
  [[path slug method]]
  [(str path "/") (keyword (str (name slug) "-with-slash")) method])

(defn generate-page-routes
  "Given a tree of pages construct and return a list of corresponding routes."
  [pages controller-namespace subpath middleware]
  (let [localization (or (config/draw :app :localize-routes) "")
        path (str subpath localization)
        routes (apply concat (map #(bind-action % controller-namespace path nil middleware) pages))
        direct (map make-route routes)
        unslashed (filter #(empty? (re-find #"/$" (first %))) routes)
        slashed (map slashify-route unslashed)
        indirect (map make-route slashed)]
    (concat indirect direct)))

(defn all-pages
  []
  (let [rows (model/gather
              :page
              {:include {:siphons {}}
               :order {:position :asc}})]
    (model/arrange-tree rows)))

(defn invoke-pages
  "Call up the pages and arrange them into a tree."
  [tree]
  (reset! (config/draw :pages) tree))

(defn empty-middleware
  [handler]
  (fn [request]
    (handler request)))

(defn create-page-routes
  "Invoke pages from the db and generate the routes based on them."
  ([] (create-page-routes (all-pages)))
  ([tree]
     (invoke-pages tree)
     (doall (generate-page-routes (deref (config/draw :pages)) (config/draw :controller :namespace) "" empty-middleware))))

(defn add-page-routes
  ([] (add-page-routes (all-pages)))
  ([pages] (add-page-routes pages (config/draw :controller :namespace)))
  ([pages controller-namespace] (add-page-routes pages controller-namespace ""))
  ([pages controller-namespace subpath] (add-page-routes pages controller-namespace subpath empty-middleware))
  ([pages controller-namespace subpath middleware]
     (doall (generate-page-routes pages controller-namespace subpath middleware))))

(defn get-path
  [routes slug]
  (or (get (get routes (keyword slug)) :path)
      (throw (new Exception
                  (str "route for " slug " not found")))))

(defn sort-route-opts
  [routes slug opts]
  (let [path (get-path routes slug)
        opt-keys (keys opts)
        route-keys (map
                    read-string
                    (filter
                     #(= (first %) \:)
                     (string/split path #"/")))
        query-keys (remove (into #{} route-keys) opt-keys)]
    {:path path
     :route (select-keys opts route-keys)
     :query (select-keys opts query-keys)}))

(defn select-route
  [slug opts]
  (:route (sort-route-opts (deref (config/draw :routes)) slug opts)))

(defn select-query
  [slug opts]
  (:query (sort-route-opts (deref (config/draw :routes)) slug opts)))

(defn reverse-route
  [routes slug opts]
  (let [{path :path
         route-matches :route
         query-matches :query} (sort-route-opts routes slug opts)
        route-keys (keys route-matches)
        query-keys (keys query-matches)
        opt-keys (keys opts)
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
  (reverse-route (deref (config/draw :routes)) slug opts))

(declare build-page-tree)

(defn build-page
  [route pages]
  (let [[path key subroutes] route
        methods (get pages key)
        children (build-page-tree subroutes pages)
        spec {:path (string/replace path #"^/" "") 
              :slug key}
        method-pages (mapv
                      (fn [method]
                        (merge (assoc spec :method method) (get methods method)))
                      (keys methods))]
    (assoc-in method-pages [0 :children] children)))

(defn build-page-tree
  [routes pages]
  (mapcat #(build-page % pages) routes))
