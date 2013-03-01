(ns caribou.app.i18n
  (:require [caribou.db :as db]
            [caribou.config :as config]
            [caribou.util :as util]
            [caribou.app.halo :as halo]
            [caribou.app.middleware :as middleware]
            [caribou.app.template :as template]))

(def ^{:dynamic true} *current-locale*)
(def ^{:dynamic true} forced-locale nil)
(declare load-resources)
(def locales (atom ()))
(def resource-map (atom {}))

(def resources
  (delay (do (load-resources) resource-map)))

(defn current-locale
  "Returns the locale in the current thread"
  []
  *current-locale*)

(defn load-resources
  "Loads translations and locales from the DB."
  []
  (let [translations (util/query "select i.*, l.code from i18n i inner join locale l on (l.id=i.locale_id) order by resource_key")
        resource-keys (distinct (map #(% :resource_key) translations))]
    (swap! locales concat (map #(% :code) (util/query "select code from locale")))
        
    (doseq [resource-key resource-keys]
      (let [key-matches (filter #(= (% :resource_key) resource-key) translations)
            translations (apply hash-map (mapcat (fn [_] (list (_ :code) (_ :value))) key-matches))]
        (swap! resource-map assoc resource-key translations))))
    @resource-map)

(defn get-default-locale
  "Gets the default locale from the app config.  Falls back to en_US"
  []
  (or (config/app :default-locale)
                  "en_US"))

(defn ^{:dynamic true} user-locale-func 
  "The function that will be called whenever a translation is requested.  
  The app should override this."
  []
  (get-default-locale))

(defn- get-locale
  "Runs through a series of values to determine the current locale"
  []
  (or forced-locale
      (user-locale-func)
      (get-default-locale)))

(defn- get-locale-string
  "Gets a locale translation from the bundle.
  Will return a string in the default locale if a translation does
  not exist for the given locale."
  [bundle locale]
  (if (nil? bundle)
    ""
    (or (bundle locale)
        (bundle (get-default-locale)))))

(defn get-resource
  "Usage: (get-resource \"key\")
          (get-resource \"key\" {:locale \"es_ES\"})
          (get-resource \"key\" {:values [\"replacement value 1\" \"replacement value 2\"]})
  Gets a translation from the DB for the given key and optional options map."
  ([resource-key options]
    (let [bundle (@@resources resource-key)
          locale (or (options :locale) (get-locale))
          values (options :values)
          locale-string (get-locale-string bundle locale)]
                                
      (apply format locale-string values)))

  ([resource-key]
    (get-resource resource-key {})))

(defn set-locale-func
  "Use this to set the function that will be used by the i18n
  system to determine the locale"
  [f]
  (def user-locale-func f))

(defn locale-override
  "Forces the locale to the specified value in the wrapped function"
  [locale func & args]
  (binding [forced-locale locale]
    (apply func args)))

(defn wrap-i18n
  "Ring handler wrapper that ensures that the current locale is set
  in the *current-locale* var"
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
