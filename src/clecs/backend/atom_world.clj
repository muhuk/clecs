(ns clecs.backend.atom-world
  (:require [clecs.world :as world]
            [clecs.backend.atom-world.editable-world :refer [->AtomEditableWorld]]
            [clecs.backend.atom-world.transactable-world :refer [->AtomTransactableWorld]]))


(def initial_state {:components {}
                    :entities {}
                    :last-entity-id 0})


(deftype AtomWorld [state transactable-world]
  world/ISystemManager
  world/ITransactableWorld
  (transaction! [this f] (world/transaction! transactable-world f)))


(defn make-world
  ([] (make-world initial_state))
  ([state]
   (let [state (atom state)
         editable-world (->AtomEditableWorld)
         transactable-world (->AtomTransactableWorld state editable-world)]
     (->AtomWorld state transactable-world))))
