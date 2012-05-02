(ns caribou.app.i18n
  (:use caribou.debug)
  (:require [caribou.db :as db]
            [caribou.config :as config]
            [caribou.app.halo :as halo]
            [caribou.app.middleware :as middleware]
            [caribou.app.template :as template]))

(declare ^{:dynamic true} *current-locale*)
(declare load-resources)
(def locales (atom ()))
(def resource-map (atom nil))

(def resources
  (delay (do (load-resources) resource-map)))

(defn current-locale
  []
  *current-locale*)

(defn load-resources
  []
  (let [translations (db/query "select i.*, l.code from i18n i inner join locale l on (l.id=i.locale_id) order by resource_key")
        resource-keys (distinct (map #(% :resource_key) translations))]
    (swap! locales concat (map #(% :code) (db/query "select code from locale")))
        
    (doseq [resource-key resource-keys]
      (let [key-matches (filter #(= (% :resource_key) resource-key) translations)
            translations (apply hash-map (mapcat (fn [_] (list (_ :code) (_ :value))) key-matches))]
        (swap! resource-map assoc resource-key translations))))
    @resource-map)

(defn get-default-locale
  []
  (or (config/app :default-locale)
                  "en_US"))

(defn get-locale
  []
  (get-default-locale))

(defn get-resource
  [resource-key & args]
  (let [bundle (@@resources resource-key)
        first-arg (first args)
        string-args args
        locale (if (contains? locales first-arg) first-arg (get-locale))
        final-args (if (contains? locales first-arg) (rest string-args) string-args)]
    (apply format (bundle locale) final-args)))

(defn locale-setter
  [f]
  (def get-locale f))

(defn wrap-i18n
  [handler locale-func]
  (fn [request]
    (binding [*current-locale* (locale-func)]
      (handler request)))) 

(defn init
  []
  (if (@config/app :i18n-enabled)
    (do
      (if (@config/app :halo-enabled)
        (do 
          (halo/append-route "GET" "reload-i18n" (fn [request] (load-resources) "i18n Reloaded"))))

      (middleware/add-custom-middleware wrap-i18n get-locale)
      (template/register-helper "_" get-resource))))
