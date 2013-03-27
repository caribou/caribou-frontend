(ns caribou.app.test.routing
  (:use [clojure.test])
  (:require [caribou.app.routing :as routing]))

(deftest ^:routing routing-test
  (routing/add-route :home :get "/" (fn [a] (str :yoyoyoyo)))
  (routing/add-route :later :get "/later/:what" #(-> % :params :what))
  (routing/add-route :under :get "/:under/:okay" #(str (-> % :params :under) "-" (-> % :params :okay)))
  (routing/add-head-routes)
  (let [router (routing/router @routing/routes)]
    (is (= ":yoyoyoyo" (router {:uri "/" :request-method :get})))
    (is (= "foundational" (router {:uri "/later/foundational" :request-method :get})))
    (is (= "probiotic-growth" (router {:uri "/probiotic/growth" :request-method :get})))))