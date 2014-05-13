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
  ([f]
   (let [state (atom initial_state)
         editable-world (->AtomEditableWorld)
         transactable-world (->AtomTransactableWorld state editable-world)]
     (world/transaction! transactable-world f)
     (->AtomWorld state transactable-world))))
