(ns clecs.backend.atom-world.queryable
  (:require [clecs.component :refer [component-label]]))


(declare -with-state)


(defn -component [state eid ctype]
  (get-in state [:components (component-label ctype) eid]))


(defn -query [state q]
  (reduce-kv (fn [coll k v]
               (if (q (seq v))
                 (conj coll k)
                 coll))
             (seq [])
             (:entities state)))
