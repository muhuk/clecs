(ns clecs.backend.atom-world.queryable
  (:require [clecs.backend.atom-world.state :refer [*state*]]
            [clecs.component :refer [component-label]]))


(declare -with-state)


(defn -component [state-atom eid ctype]
  (-with-state state-atom get-in [:components (component-label ctype) eid]))


(defn -query [state-atom q]
  (-with-state state-atom
               #(reduce-kv (fn [coll k v]
                             (if (q (seq v))
                               (conj coll k)
                               coll))
                           []
                           (:entities %))))


(defn -with-state [state-atom f & args]
  (let [state (if (bound? #'*state*) *state* @state-atom)]
    (apply f (cons state args))))
