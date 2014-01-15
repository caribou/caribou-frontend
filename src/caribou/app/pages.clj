(ns caribou.app.pages
  (:use [ns-tracker.core :only (ns-tracker)])
  (:require [clojure.java.jdbc :as sql]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [ring.util.codec :as codec]
            [ring.middleware.basic-authentication :only (wrap-basic-authentication)]
            [polaris.core :as polaris]
            [caribou.util :as util]
            [caribou.config :as config]
            [caribou.model :as model]
            [caribou.logger :as log]
            [caribou.app.auth :as auth]
            [caribou.app.controller :as controller]
            [caribou.app.template :as template]))

(defn default-action
  [request]
  (controller/render request))

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
        default-action))
    (do
      (log/info (format "No controller namespace named %s.%s!" controller-namespace controller-key))
      default-action)))

(defn placeholder?
  [el]
  (or
   (and
    (keyword? el)
    (= \$ (-> el name first)))
   (and
    (string? el)
    (= \: (first el)))))

(defn extract-key
  [el]
  (-> el name (subs 1) keyword))

(defn siphon-substitute
  [spec request]
  (walk/postwalk
   (fn [el]
     (if (placeholder? el)
       (let [key (extract-key el)]
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
                 model (keyword (:model spec))
                 op-slug (keyword (:op spec))
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
  (let [action (retrieve-controller-action controller-namespace controller-key action-key)]
    (fn [request]
      (action (merge request {:template (or template (:template page)) :page (draw-siphons page request)})))))

(defn generate-reloading-action
  [controller-namespace controller-key action-key template page]
  (fn [request]
    (let [action (retrieve-controller-action controller-namespace controller-key action-key)]
      (action (merge request {:template (or template (:template page)) :page (draw-siphons page request)})))))

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

(defn divine-protection
  [page protection]
  (if (:protected page)
    {:username (:username page)
     :password (:crypted-password page)}
    protection))

(defn slashify-route
  [[path slug method]]
  [(str path "/") (keyword (str (name slug) "-with-slash")) method])

(defn up-key
  [k]
  (keyword (string/upper-case (name k))))

(defn present?
  [s]
  (and s (not (= "" s))))

(defn convert-page-to-route
  [{:keys [path slug id method controller action template siphons children] :as page} 
   controller-namespace above-path protection middleware]
  (let [subpath (str above-path (if-not (empty? path) (str "/" (name path))))
        page-slug (keyword (or slug id))
        protection (divine-protection page protection)
        method-key (if (present? method) (up-key method) :ALL)
        handler (generate-action page controller-namespace template controller action protection)]
    [subpath page-slug {method-key (middleware handler)}
     (map #(convert-page-to-route % controller-namespace nil protection middleware) children)]))

(defn convert-pages-to-routes
  ([pages] (convert-pages-to-routes pages (config/draw :controller :namespace)))
  ([pages controller-namespace] (convert-pages-to-routes pages controller-namespace ""))
  ([pages controller-namespace subpath] (convert-pages-to-routes pages controller-namespace subpath identity))
  ([pages controller-namespace subpath middleware]
     (let [localization (or (config/draw :app :localize-routes) "")
           path (str subpath localization)
           routes (map #(convert-page-to-route % controller-namespace path nil middleware) pages)]
       routes)))

(defn all-pages
  []
  (let [rows (model/gather
              :page
              {:include {:siphons {}}
               :order {:position :asc}})]
    (model/arrange-tree rows)))

(defn route-for
  [slug params]
  (polaris/reverse-route (deref (config/draw :routes)) slug params))

(defn safe-route-for
  [slug & params]
  (polaris/reverse-route (deref (config/draw :routes)) slug (apply merge {} params) {:no-query true}))

(declare bind-actions)

(defn bind-method
  [action namespace]
  (if (fn? action)
    action
    (generate-action action namespace (:template action) (:controller action) (:action action) nil)))

(defn bind-methods
  [[path key methods children] namespace]
  (let [bound (util/map-vals #(bind-method % namespace) methods)
        bound-children (bind-actions children namespace)]
    [path key bound bound-children]))

(defn bind-actions
  ([routes] (bind-actions routes (config/draw :controller :namespace)))
  ([routes namespace]
     (map #(bind-methods % namespace) routes)))

;; Old way of doing pages and routes

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

(defn merge-page-trees
  [base-tree over-tree]
  (let [groups (group-by #(-> % :slug keyword) (concat base-tree over-tree))
        tree-merge (map
                    (fn [[slug pages]]
                      (let [[base-page over-page] pages
                            [base-children over-children] (map :children pages)
                            children (merge-page-trees base-children over-children)
                            merged (merge base-page over-page)]
                        (assoc merged
                          :slug slug
                          :children children)))
                    groups)]
    tree-merge))
