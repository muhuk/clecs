(ns ^:no-doc writer
  (require [codox.writer.html :as html-writer]
           [clojure.java.io :refer [file]]))


(defn html-file? [f]
  (.endsWith (.getName f) ".html"))


(defn add-google-analytics [f]
  (let [matcher (re-matcher #"^(?s)(.*)(</body>.*)$" (slurp f))
        [_ head tail] (re-find matcher)
        ga-code (slurp "resources/ga.inc")]
    (spit f (str head ga-code tail))))


(defn write-docs
  [project]
  (html-writer/write-docs project)
  (doseq [f (->> (:output-dir project)
                 (file)
                 (file-seq)
                 (filter html-file?))]
    (add-google-analytics f)))
