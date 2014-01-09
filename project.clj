(defproject caribou/caribou-frontend "0.13.4"
  :description "The page routing ring handler for caribou"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [caribou/caribou-core "0.13.4"]
                 [cljsbuild "0.3.3"
                  :exclusions [fs]]
                 [clj-time "0.4.4"]
                 [polaris "0.0.2"]
                 [caribou/lichen "0.6.14"]
                 [ring/ring-json "0.2.0" :exclusions [cheshire]]
                 [ring "1.2.0"
                  :exclusions [org.clojure/clojure
                               clj-stacktrace
                               hiccup]]
                 [ring/ring-core "1.2.0" :exclusions [commons-io]]
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
