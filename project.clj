(defproject antler/caribou-frontend "0.11.24"
  :description "The page routing ring handler for caribou"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [antler/caribou-core "0.11.22"]
                 [clj-time "0.4.4"]
                 [clout "1.1.0"]
                 [antler/antlers "0.5.2"]
                 [antler/lichen "0.3.3"]
                 [fuziontech/ring-json-params "0.2.0" :exclusions [cheshire]]
                 [ring "1.1.6"
                  :exclusions [org.clojure/clojure
                               clj-stacktrace
                               hiccup]]
                 [ring/ring-core "1.1.6" :exclusions [commons-io]]
                 [ring-basic-authentication "1.0.1"]
                 [clj-stacktrace "0.2.5"]
                 [hiccup "1.0.2"]
                 [cheshire "5.0.2"]
                 [org.flatland/ordered "1.4.0"]
                 [ns-tracker "0.2.1"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :autodoc {:name "Caribou Frontend"
            :page-title "Caribou Frontend - Documentation"
            :description
            "This is a routing and template rendering frontend for Caribou Core."}
  :ring {:handler caribou.app.core/handler
         :init caribou.app.core/init
         :servlet-name "caribou-frontend"
         :port 33333}
  :resources-path "resources")
