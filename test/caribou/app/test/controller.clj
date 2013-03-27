(ns caribou.app.test.controller
  (:use [clojure.test])
  (:require [caribou.app.controller :as controller]))


(deftest session-canary-test
  (let [old-canary @controller/session-canary
        new-canary (fn [params] (when-let [error (get params :error)]
                                  (str "error was " error)))]
    (dosync (ref-set controller/session-canary new-canary))
    (testing "Error thrown when expected."
      (is (thrown-with-msg? Exception #"canary reported an error:"
            (controller/render {:error true}))))
    (testing "No error if not expected."
      (is (seq (controller/render {:error false :template identity})))
      (dosync (ref-set controller/session-canary (constantly false)))
      (is (seq (controller/render {:error true :template identity}))))
    (dosync (ref-set controller/session-canary old-canary))))

(deftest render-test
  (let [defaulted (controller/render {:template identity})
        params {:status 500
                :template identity
                :session {:user "me"}
                :content-type "whatever/whocares"}
        provided (controller/render params)]
    (testing "Default values"
      (is (= 200 (get defaulted :status)))
      (is (= {:template identity} (get defaulted :body)))
      (is (= "text/html;charset=utf-8"
             (-> defaulted :headers (get "Content-Type")))))
    (testing "Explicit values"
      (is (= 500 (get provided :status)))
      (is (= {:user "me"} (get provided :session)))
      (is (= params (get provided :body)))
      (is (= "whatever/whocares"
             (-> provided :headers (get "Content-Type")))))))
