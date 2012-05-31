(ns caribou.app.middleware
  (:use [caribou.debug :only [log]]))

(defonce middleware (atom []))

(defn wrap-custom-middleware [handler]
  (reduce (fn [cur [func args]] (apply func cur args))
          handler
          (seq @middleware)))

(defn add-custom-middleware
  [func & args]
  (swap! middleware conj [func args]))

(defn is-xhr?
  [request]
  (if-let [headers (:headers request)]
    (if-let [xhr-hdr (headers "x-requested-with")]
      (= (.toLowerCase xhr-hdr) "xmlhttprequest")
      false)
    false))

(defn wrap-xhr-request
  [handler]
  (fn [request]
    (handler (assoc request :is_xhr (is-xhr? request)))))

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

