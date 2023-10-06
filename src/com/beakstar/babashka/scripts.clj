(ns com.beakstar.babashka.scripts)

(require '[clojure.string :as str]
         '[babashka.fs :as fs]
         '[babashka.process :refer [shell]]
         '[selmer.parser :as parser]
         '[selmer.filters :as filters])

(defmacro << [& rest] `(parser/<< ~@rest))

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
