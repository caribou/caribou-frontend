(ns caribou.app.test.pages
  (:use [clojure.test])
  (:require [caribou.app.pages :as pages]
            [caribou.app.routing :as routing]))

(deftest route-for-test
  (let [old-routes @routing/routes]
    (routing/clear-routes!)
    (routing/add-route :the_googles :get "/:site/:locale/content/:color/search" identity)
        ;; new-routes {:the_googles "/:site/:locale/content/:color/search"}]
    
    (testing "null input"
      (is (= (-> @routing/routes :the_googles :path)
             (pages/route-for :the_googles {})))
      (is (= {}
             (pages/select-query :the_googles {})))
      (is (= {}
             (pages/select-route :the_googles {}))))
    (testing "valid input"
      (let [base-map {:site "g"
                      :locale "o"
                      :color "red"}
            query-map {:no "yes"
                       :yes "no"}
            full-route (pages/route-for :the_googles
                                        (merge (assoc base-map :color "blue")
                                               query-map))
            base-opts (pages/select-route :the_googles
                                          (merge base-map query-map))
            query-opts (pages/select-query :the_googles
                                           (merge base-map query-map))]
        (is (= "/g/o/content/red/search"
               (pages/route-for :the_googles base-map)))
        (is (or (= "/g/o/content/blue/search?yes=no&no=yes"
                   full-route)
                (= "/g/o/content/blue/search?no=yes&yes=no"
                   full-route)))
        (is (= base-map base-opts))
        (is (= query-map query-opts))))
    (testing "invalid input"
      (is (thrown-with-msg? Exception #"route for .* not found"
            (pages/route-for :the_bing {}))))
    (dosync (swap! routing/routes (constantly old-routes)))))
