(defproject antler/caribou-frontend "0.3.3"
  :description "The page routing ring handler for caribou"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [antler/caribou-core "0.5.1"]
                 [compojure "1.0.1"]
                 [clj-time "0.3.6"]
                 [ring/ring-core "1.1.0"
                  :exclusions [org.clojure/clojure
                               clj-stacktrace]]
                 [hiccup "0.3.6"]
                 [org.clojars.doo/cheshire "2.2.3"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :ring {:handler caribou.app.core/handler
         :servlet-name "caribou-frontend"
         :init caribou.app.core/init
         :port 33333}
  :repositories {"snapshots" {:url "http://battlecat:8080/nexus/content/repositories/snapshots" 
                              :username "deployment" :password "deployment"}
                 "releases"  {:url "http://battlecat:8080/nexus/content/repositories/releases" 
                              :username "deployment" :password "deployment"}})
