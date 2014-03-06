(defproject caribou/caribou-frontend "0.14.0"
  :description "The page routing ring handler for caribou"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [caribou/caribou-core "0.14.0"]
                 [cljsbuild "1.0.2"
                  :exclusions [fs]]
                 [clj-time "0.4.4"]
                 [polaris "0.0.4"]
                 [caribou/lichen "0.6.16"]
                 [ring "1.2.1"
                  :exclusions [org.clojure/clojure
                               clj-stacktrace
                               hiccup]]
                 [ring/ring-json "0.2.0"
                  :exclusions [ring/ring-core]]
                 [ring/ring-core "1.2.1"]
                 [ring-basic-authentication "1.0.1"]
                 [clj-stacktrace "0.2.5"]
                 [hiccup "1.0.2"]
                 [cheshire "5.0.2"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [org.clojure/data.xml "0.0.7"]
                 [org.flatland/ordered "1.4.0"]
                 [ns-tracker "0.2.1"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :ring {:handler caribou.app.core/handler
         :init caribou.app.core/init
         :servlet-name "caribou-frontend"
         :port 33333}
  :resources-path "resources")
