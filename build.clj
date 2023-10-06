(ns build
  (:require [org.corfield.build :as bb]
            [clojure.tools.deps.alpha :as ctda]
            [clojure.java.io :as io]))

(def deps (ctda/slurp-deps (io/file "deps.edn")))
(def opts (-> deps :aliases :opts))

(defn add-basis-to-opts [opts]
  (assoc opts :basis (ctda/calc-basis nil))) ;; This works, the "things" are empty, but it still works. I don't understand it yet.

(defn remove-transitive-from-opts [opts]
  (dissoc opts :transitive))

(defn clean [_]
  (-> opts bb/clean))

(defn jar [_]
  (println "### Building jar")
  (-> opts bb/jar))

(defn install [_]
  (println "### Cleaning, Building jar and installing")
  (-> opts bb/clean bb/jar add-basis-to-opts bb/install))

(defn test1 [_]
  (println _))
