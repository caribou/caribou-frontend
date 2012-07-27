(ns caribou.app.controller
  (:require [clojure.java.io :as io]
            [caribou.logger :as log]
            [caribou.util :as util]
            [caribou.config :as config]))

(def broken-parameters?
  "return true from broken-parameters? in order to trigger an exception,
the arg passed will be the render parameters

we could use set-validator here, but there are many ways to be wrong,
and only one way to be right"
  (ref (if
           ;; true if debug is enabled, nil otherwise
           (log/whenlog :debug true)
         (fn [parameters]
           (if (and (:session parameters)
                    (:template parameters))
             false
             "error session or template missing"))
         (constantly false))))



;; example: throw an exception, print parameters, and force a stack trace
;; whenever render is called:
;;; (dosync (ref-set caribou.app.controller/broken-parameters? identity))


(def session-defaults (atom {}))

(defn load-namespace
  [paths])

(defn get-controller-action
  "Find the function corresponding to the given controller namespace and
   its name in that namespace"
  [controller-ns controller-key action-key]
  (if (and controller-key action-key)
    (let [full-ns-name (str controller-ns "." controller-key)
          full-ns (symbol full-ns-name)]
      (try
        (do
          (require :reload full-ns)
          (ns-resolve full-ns (symbol action-key)))
        (catch Exception e
          (log/error (str "Cannot load namespace " full-ns-name "\n" e)
                     :CONTROLLER))))))

(def content-map
  {:json "application/json"
   :text/plain "text/plain"})

(defn render
  "Render the template corresponding to this page and return a proper response."
  ([content-type params]
     (render (assoc params :content-type (content-type content-map))))
  ([params]
     (when-let [condition (@broken-parameters? params)]
       (throw (Exception.
               (str "@broken-session? reported an error: " condition))))
     (let [template (:template params)]
       {:status (or (:status params) 200)
        :session (:session params)
        :body (template params)
        :headers {"Content-Type" (or (:content-type params) "text/html")}})))

(defn redirect
  "Return a response corresponding to a redirect triggered in the user's browser."
  ([url]
     (redirect url {}))
  ([url params]
     (let [headers (merge (:headers params) {"Location" url})]
       (merge params {:status 302 :headers headers}))))

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
