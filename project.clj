(defproject antler/caribou-frontend "0.5.22"
  :description "The page routing ring handler for caribou"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [antler/caribou-core "0.6.18"]
                 [compojure "1.0.4"]
                 [clj-time "0.3.6"]
                 [antler/stencil "0.3.5"]
                 [fuziontech/ring-json-params "0.2.0"]
                 [ring "1.1.0"]
                 [org.clojars.doo/cheshire "2.2.3"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :autodoc {:name "Caribou Frontend"
            :page-title "Caribou Frontend - Documentation"
            :description
            "This is a routing and template rendering frontend for Caribou Core."}
  :ring {:handler caribou.app.core/handler
         :servlet-name "caribou-frontend"
         :init caribou.app.core/init
         :port 33333})
