(ns clecs.system
  (:require [clojure.set :refer [difference]]
            [clojure.string :as s]))


(declare validate-components
         validate-name
         validate-process)


(defn system [s]
  (-> s
      (validate-name)
      (validate-process)
      (validate-components)))


(defn- assoc-process-fn [s]
  (let [p-var (:process s)
        ns-sym (symbol (s/replace (str p-var) #"/.*$" ""))]
    (require ns-sym)
    (assoc s :process-fn (var-get (find-var p-var)))))


(defn- validate-components [s]
  (when-not (or (contains? s :reads) (contains? s :writes))
    (throw (IllegalArgumentException.
            "Either :reads or :writes must be specified")))
  (when (and (empty? (:reads s)) (empty? (:writes s)))
    (throw (IllegalArgumentException.
            "At least one component must be specified")))
  (let [writes (set (:writes s))
        reads (difference (set (:reads s)) writes)]
    (-> s
        (assoc :reads reads)
        (assoc :writes writes))))


(defn- validate-name [s]
  (if-not (contains? s :name)
    (throw (IllegalArgumentException.
            ":name is required for systems"))
    s))


(defn- validate-process [s]
  (when-not (or (contains? s :process)
                (contains? s :process-fn))
    (throw (IllegalArgumentException. "Either :process or :process-fn must be specified")))
  (when (and (contains? s :process)
             (not (symbol? (:process s))))
    (throw (IllegalArgumentException. ":process must be a symbol")))
  (when (and (contains? s :process-fn)
             (not (fn? (:process-fn s))))
    (throw (IllegalArgumentException. ":process-fn must be a function.")))
  (cond-> s
          (not (contains? s :process-fn)) (assoc-process-fn)))
