(ns caribou.app.auth
  (:require [clojure.string :as string]
            [clojure.data.codec.base64 :as base64]))

(defn- byte-transform
  [direction-fn string]
  (try
    (reduce str (map char (direction-fn (.getBytes string))))
    (catch Exception _)))

(defn- encode-base64
  [^String string]
  (byte-transform base64/encode string))

(defn- decode-base64
  [^String string]
  (byte-transform base64/decode string))

(defn basic-authentication
  ([app request authenticate]
     (basic-authentication app request authenticate {} nil))
  ([app request authenticate denied-response]
     (basic-authentication app request authenticate denied-response nil))
  ([app request authenticate denied-response realm]
     (let [auth (get (:headers request) "authorization")
           cred (and auth (decode-base64 (last (re-find #"^Basic (.*)$" auth))))
           [user pass] (and cred (string/split (str cred) #":"))]
       (if-let [token (and cred (authenticate (str user) (str pass)))]
         (app (assoc request :basic-authentication token))
         (let [response (merge {:headers {"Content-Type" "text/plain"} :body "access denied"} denied-response)
               realm (or realm "restricted area")]
           (assoc response
             :status  401
             :headers (merge (:headers response)
                             {"WWW-Authenticate" (format "Basic realm=\"%s\"" realm)})))))))
