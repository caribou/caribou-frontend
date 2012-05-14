(ns caribou.app.template.freemarker
  (:use caribou.debug
        [caribou.util :only (stringify-keys)])
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [freemarker.template Configuration TemplateMethodModel]
           [freemarker.ext.beans BeansWrapper]
           [freemarker.cache NullCacheStorage]
           [java.io StringWriter File]))

(def freemarker-config (Configuration.))

(defn init
  "Set up our Freemarker config"
  [template-path]
  (doto freemarker-config
    (.setDirectoryForTemplateLoading (File. template-path))
    (.setCacheStorage (NullCacheStorage.))
    (.setObjectWrapper (BeansWrapper.))))

(defn get-template
  "Gets a Freemarker template from the Configuration"
  [template-name]
  (.getTemplate freemarker-config template-name))

(defn render
  "Process a Freemarker template, returns a String"
  ([template root template-length]
  (let [out (StringWriter. template-length)]
    (.process template (stringify-keys root) out)
    (.toString out)))

  ([template root]
    (let [template-length (.length (.toString template))]
      (render template root template-length))))

(defn render-wrapper
  "Wraps a template filename in a render"
  [template-name helpers]
  (fn [root]
    ; we put get-template inside the func call because we want freemarker to handle
    ; caching/reloading for us
    (let [template (get-template template-name)
          template-length (.length (.toString template))
          merged-root (merge root helpers)]
        (render template merged-root template-length))))

(defn create-helper
  [helper]
  (proxy [TemplateMethodModel] []
    (exec [args] (apply helper args))))

;; (defn load-templates
;;   "recurse through the view directory and add all the templates that can be found"
;;   [path]
;;   (freemarker-init path)
;;   (loop [fseq (file-seq (io/file path))]
;;     (if fseq
;;       (let [filename (.toString (first fseq))
;;             template-name (string/replace filename (str path "/") "")]
;;         (if (.isFile (first fseq))
;;           (let [template (render-wrapper template-name @helpers)
;;                 template-key (keyword template-name)]
;;             (log :template (format "Found template %s" template-name))
;;             (dosync
;;              (alter templates merge {template-key template}))))
;;         (recur (next fseq))))))

;; (defn init
;;   []
;;   (if-let [freemarker-dir (@config/app :freemarker-dir)]
;;     (load-templates freemarker-dir)))


