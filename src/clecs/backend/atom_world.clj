(ns clecs.backend.atom-world
  (:require [clecs.world :as world]
            [clecs.component :refer [component-label entity-id]]
            [clecs.backend.atom-world.editable :refer [-add-entity
                                                       -remove-component
                                                       -remove-entity
                                                       -set-component]]
            [clecs.backend.atom-world.queryable :refer [-component
                                                        -query]]
            [clecs.backend.atom-world.transactable :refer [-transaction!]]))


(def ^:const EMPTY_WORLD {:components {}
                          :entities {}
                          :last-entity-id 0})


(deftype AtomWorld [state]
  world/IEditableWorld
  (add-entity [_] (-add-entity))
  (remove-component [this eid ctype] (-remove-component eid ctype) this)
  (remove-entity [this eid] (-remove-entity eid) this)
  (set-component [this c] (-set-component c) this)
  world/IQueryableWorld
  (component [_ eid ctype] (-component state eid ctype))
  (query [_ q] (-query state q))
  world/ITransactableWorld
  (transaction! [this f] (-transaction! this f)))


(defn make-world
  ([] (make-world EMPTY_WORLD))
  ([state] (->AtomWorld (atom state))))
