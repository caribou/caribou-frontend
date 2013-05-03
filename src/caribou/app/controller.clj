(ns caribou.app.controller
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [caribou.logger :as log]
            [caribou.util :as util]))

(defn load-namespace
  [paths])

(defn get-controller-namespace
  [controller-ns controller-key]
  (if controller-key
    (let [full-controller-ns-name (str controller-ns "." controller-key)
          full-controller-ns (symbol full-controller-ns-name)]
        (require :reload full-controller-ns)
        (find-ns full-controller-ns))))

(defn get-controller-action
  "Find the function corresponding to the given controller namespace and
   its name in that namespace"
  [controller-ns action-key]
  (if action-key
    (ns-resolve controller-ns (symbol action-key))))

(def content-map
  {:json "application/json"
   :text/plain "text/plain"})

(defn default-template
  [params]
  (str params))

(defn json-template
  [params]
  (json/generate-string (dissoc params :content-type :template :session :status)))

(defn render
  "Render the template corresponding to this page and return a proper response."
  ([content-type params]
     (render
      (assoc params
        :content-type (content-type content-map)
        :template json-template)))
  ([params]
     (let [template (or (:template params) default-template)
           content-type (or (:content-type params) (-> params :headers (get "Content-Type")) "text/html;charset=utf-8")
           headers (merge (or (:headers params) {}) {"Content-Type" content-type})
           status (:status params)
           session (:session params)
           response {:status (or status 200)
                     :body (template params)
                     :headers headers}]
       (if (contains? params :session)
         (assoc response :session session)
         response))))

(defn redirect
  "Return a response corresponding to a redirect triggered in the user's browser."
  ([url]
     (redirect url {}))
  ([url params]
     (let [headers (merge (:headers params) {"Location" url})]
       (merge params {:status 302 :headers headers :body ""}))))

(defn cookie
  "Get the value from the given cookie."
  [request key]
  (if-let [cookies (:cookies request)]
    (if-let [cookie (cookies (name key))]
      (:value cookie))))

(defn session-key
  "Define a key that should exist in the session and provide a default
   function to be called if that key does not exist at render time."
  [key default]
  (throw "NOT IMPLEMENTED!"))

(defn session
  "Return"
  ([request key]))