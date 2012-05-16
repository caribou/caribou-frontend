(ns caribou.app.middleware
  (:use [caribou.debug :only [log]]))

(defonce middleware (atom []))

(defn wrap-custom-middleware [handler]
  (reduce (fn [cur [func args]] (apply func cur args))
          handler
          (seq @middleware)))

(defn add-custom-middleware
  "Add a middleware function to all noir handlers."
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
