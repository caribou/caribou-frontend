(ns caribou.app.test.pages
  (:use [clojure.test])
  (:require [caribou.app.pages :as pages]
            [caribou.app.routing :as routing]))

(deftest route-for-test
  (let [route-paths @routing/route-paths
        new-paths {:the_googles "/:site/:locale/content/:color/search"}]
    (dosync (swap! routing/route-paths (constantly new-paths)))
    (testing "null input"
      (is (= (get new-paths :the_googles)
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
    (dosync (swap! routing/route-paths (constantly route-paths)))))
