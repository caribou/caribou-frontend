(ns caribou.app.controller
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [caribou.logger :as log]
            [caribou.util :as util]))

(def session-canary
  "return true from session-canary in order to trigger an exception,
the arg passed will be the session

we could use set-validator here, but there are many ways to be wrong,
and only one way to be right"
  (ref (constantly false)))

;; example: throw an exception, print session, and force a stack trace whenever
;; render is called:
;;; (dosync (ref-set caribou.app.controller/session-canary identity))


(def session-defaults (atom {}))

(defn load-namespace
  [paths])

(defn get-controller-namespace
  [controller-ns controller-key]
  (if controller-key
    (let [full-controller-ns-name (str controller-ns "." controller-key)
          full-controller-ns (symbol full-controller-ns-name)]
      (try
        (do
          (require :reload full-controller-ns)
          (find-ns full-controller-ns))
        (catch Exception e
          (log/error (str "Cannot load namespace " full-controller-ns-name "\n" e) :CONTROLLER)
          ; log/error returns a weird list
          nil)))))

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
     (render (assoc params
               :content-type (content-type content-map)
               :template json-template)))
  ([params]
     (when-let [condition (@session-canary params)]
       (throw (Exception.
               (str "session canary reported an error: " condition))))
     (let [template (or (:template params) default-template)
           content-type (:content-type params)
           status (:status params)
           session (:session params)
           response {:status (or status 200)
                     :body (template params)
                     :headers {"Content-Type" (or content-type "text/html")}}]
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
