(ns com.beakstar.babashka.scripts)

(require '[clojure.string :as str]
         '[babashka.fs :as fs]
         '[babashka.process :refer [shell]]
         '[selmer.parser :as parser]
         '[selmer.filters :as filters])

(import [java.security SecureRandom])

(defmacro <<
  "Re-wraps selmer.parser/<< for reduced deps. Also added \"qq\" filter.


  Resolves the variables from your template string from the local-env, or the
  namespace and puts them into your template for you.

  e.g. (let [a 1] (<< \"{{a}} + {{a}} = 2\")) ;;=> \"1 + 1 = 2\" "
  [& rest] `(parser/<< ~@rest))

(filters/add-filter! :qq (fn [%] [:safe (str "\"" % "\"")]))

(defn get-script-relative-dir
  "Returns an absoluted path which combines [relative-path]
  with the path of the calling script.

  This is intended to be assigned with def at script startup."
  [relative-path]
  (-> *file*
      fs/parent
      (fs/file (str/replace relative-path #"^/" ""))
      fs/canonicalize
      str))

(defn println-now
  "Prints with println, then calls flush."
  [s]
  (println s)
  (flush))

(defn exec
  "Each argument is executed by babashka.process/shell. STDOUT
  and STDERR are printed. It will continue rather than throw
  when non-zero exit codes are encoutered."
  [& commands]
  (doseq [command commands]
    (let [result (shell {:continue true :out :string :err :out} command)]
      (print (:out result)))))

(defmacro with-dir-exec
  "Each argument is executed by babashka.process/shell in the
  supplied directory (dir). STDOUT and STDERR are printed. It will
  continue rather than throw when non-zero exit codes are encoutered."
  [dir & commands]
  `(doseq [command# '~commands]
     (let [result# (shell {:dir ~dir :continue true :out :string :err :out} command#)]
       (print (:out result#)))))

(defn qq
  "Double quote a string. For use in calling external shell commands."
  [string]
  (str "\"" string "\""))

(defn random-number
  "Return a random integer from 0 to max (inclusive). If min is not
  supplied, zero is assumed."
  ([min max]
   (+ min (.nextInt (SecureRandom.) (- (inc max) min))))
  ([max]
   (random-number 0 max)))

(defn remove-nth
  "Returns a lazy sequence with the element at the index location
  removed."
  [coll index]
  (concat (take index coll) (drop (inc index) coll)))

(defn take-random
  "Returns a sequece consisting of n random elements from the collection."
  [n coll]
  (loop [coll (seq coll)
         results []]
    (let [index (.nextInt (SecureRandom.) (count coll))
          results (conj results (nth coll index))]
      (if (= n (count results))
        results
        (recur (remove-nth coll index) results)))))
