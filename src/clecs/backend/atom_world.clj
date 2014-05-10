(ns clecs.backend.atom-world
  (:require [clecs.world :as world]
            [clecs.backend.atom-world.queryable :refer [-component
                                                        -query]]
            [clecs.backend.atom-world.transactable :refer [-transaction!]]))


(def ^:const EMPTY_WORLD {:components {}
                          :entities {}
                          :last-entity-id 0})


(deftype AtomWorld [state]
  world/IQueryableWorld
  (component [_ eid ctype] (-component @state eid ctype))
  (query [_ q] (-query @state q))
  world/ITransactableWorld
  (transaction! [this f] (-transaction! this f)))


(defn make-world
  ([] (make-world EMPTY_WORLD))
  ([state] (->AtomWorld (atom state))))
