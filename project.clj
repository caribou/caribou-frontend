(defproject antler/caribou-frontend "0.7.34"
  :description "The page routing ring handler for caribou"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [antler/caribou-core "0.7.23"]
                 [clj-time "0.4.4"]
                 [compojure "1.1.3" :exclusions [ring/ring-core ring]]
                 [antler/stencil "0.4.5"]
                 [fuziontech/ring-json-params "0.2.0" :exclusions [cheshire]]
                 [ring "1.1.6"
                  :exclusions [org.clojure/clojure
                               clj-stacktrace
                               hiccup]]
                 [ring/ring-core "1.1.6" :exclusions [commons-io]]
                 [hiccup "1.0.2"]
                 [org.clojars.doo/cheshire "2.2.3"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :autodoc {:name "Caribou Frontend"
            :page-title "Caribou Frontend - Documentation"
            :description
            "This is a routing and template rendering frontend for Caribou Core."}
  :ring {:handler caribou.app.core/handler
         :servlet-name "caribou-frontend"
         :init caribou.app.core/init
         :port 33333}
  :resources-path "resources")
