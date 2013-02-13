(ns caribou.app.test.auth
  (:use [clojure.test])
  (:require [caribou.app.auth :as auth]
            [caribou.auth :as c-auth]))

(deftest authentication
  (let [hash (c-auth/hash-password "hello world")
        validator (auth/enact-protection {:username "turing"
                                          :password hash})
        bad-validator (auth/enact-protection {:username "turing"
                                              :password "hello world"})]
    (testing "Protection"
      (is (thrown-with-msg? java.lang.IllegalArgumentException #"Invalid salt"
            (bad-validator "turing" "hello world")))
      (is (validator "turing" "hello world"))
      (is (not (validator "Turing" "Hello World"))))))