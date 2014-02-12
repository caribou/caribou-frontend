(ns caribou.app.middleware
  (:require [caribou.logger :as log]
            [caribou.config :as config]))

(defn wrap-custom-middleware [handler]
  (reduce
   (fn [cur [func args]]
     (apply func cur args))
   handler
   (seq (deref (config/draw :middleware)))))

(defn add-custom-middleware
  [func & args]
  (swap! (config/draw :middleware) conj [func args]))

(defn is-xhr?
  [request]
  (if-let [headers (:headers request)]
    (if-let [xhr-hdr (headers "x-requested-with")]
      (= (.toLowerCase xhr-hdr) "xmlhttprequest")
      false)
    false))

(defn wrap-request-diagnostics
  [handler]
  (fn [request]
    (log/debug request :REQUEST)
    (let [response (handler request)]
      (log/debug response :RESPONSE)
      response)))

(defn wrap-xhr-request
  [handler]
  (fn [request]
    (handler 
     (assoc request 
       :is-xhr (is-xhr? request)))))

(defn wrap-servlet-path-info
  "Removes the deployed servlet context from the request URI when running as a war"
  [handler]
  (fn [request]
    (if-let [servlet-req (:servlet-request request)]
      (let [context (.getContextPath servlet-req)
            uri (:uri request)]
        (if (and (.startsWith uri context) (not= uri context))
          (handler (assoc request :uri (.substring uri (.length context))))
          (handler request)))
      (handler request))))

(defn wrap-default-content-type
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (get-in response [:headers "Content-Type"])
        response
        (assoc-in response [:headers "Content-Type"] "text/html;charset=utf8")))))
