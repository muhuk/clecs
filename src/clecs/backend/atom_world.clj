(ns clecs.backend.atom-world
  (:require [clecs.backend.atom-world.editable-world :refer [->AtomEditableWorld]]
            [clecs.backend.atom-world.transactable-world :refer [->AtomTransactableWorld]]
            [clecs.world :as world]))


(def initial_state {:components {}
                    :entities {}
                    :last-entity-id 0})


(deftype AtomWorld [state transactable-world]
  world/ISystemManager)


(defn make-world
  ([] (make-world initial_state))
  ([state]
   (let [state (atom state)
         editable-world (->AtomEditableWorld)
         transactable-world (->AtomTransactableWorld state editable-world)]
     (->AtomWorld state transactable-world))))
