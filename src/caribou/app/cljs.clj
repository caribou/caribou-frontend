(ns caribou.app.cljs
  (:require [cljs.closure :as cljsc]
            [cljs.repl :as repl]
            [cljs.repl.browser :as browser]
            [caribou.logger :as log]
            [caribou.config :as config]))

(defn brepl-init
  ([]
     (if (config/draw :cljs :brepl :listen)
       (brepl-init (or (config/draw :cljs :brepl :port) 44994))))
  ([port]
     (brepl-init port (or (config/draw :cljs :brepl :path) "repl")))
  ([port path]
     (try 
       ;; (let [brepl-env (browser/repl-env :port (Integer. port) :working-dir path)
       ;;       brepl (future (do (println *in*) (println *out*) (repl/repl brepl-env)))]
       ;;   (swap! (config/draw :cljs :brepl :env) (constantly brepl-env))
       ;;   (swap! (config/draw :cljs :brepl :server) (constantly brepl)))
       nil
       (catch Exception e 
         (log/info (format "brepl already running on port %s" port))
         (log/print-exception e)))))

(defn build
  []
  (cljsc/build 
   (config/draw :cljs :root)
   (config/draw :cljs :options)))

(defn wrap-cljs
  [handler]
  (fn [request]
    (if (config/draw :cljs :reload)
      (build))
    (handler request)))
