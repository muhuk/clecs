(ns clecs.system
  (:require [clojure.set :refer [difference]]
            [clojure.string :as s]))


(declare validate-components
         validate-name
         validate-process)


(defn system
  "Create a system.

  Takes a map with the following elements:

  :name
  :   A keyword to refer to this system later.

  :process
  :   Namespaced symbol of the system's
      function.
      If `:process-fn` is provided, :process
      can be omitted. However the system
      won't be serializable in this case.

  :process-fn
  :   System's function itself.
      `:process-fn` will be resolved automatically
      if `:process` is provided.

  :reads
  :   Components this system will read.

  :writes
  :   Components this system will read and write.

  Components not specified in either `:reads` or
  `:writes` won't be accessible to the system.
  "
  [s]
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
