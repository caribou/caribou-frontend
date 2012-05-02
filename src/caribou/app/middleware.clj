(ns caribou.app.middleware)

(defonce middleware (atom []))

(defn wrap-custom-middleware [handler]
  (reduce (fn [cur [func args]] (apply func cur args))
          handler
          (seq @middleware)))

(defn add-custom-middleware
  "Add a middleware function to all noir handlers."
  [func & args]
  (swap! middleware conj [func args]))
