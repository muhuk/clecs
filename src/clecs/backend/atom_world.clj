(ns clecs.backend.atom-world
  (:require [clecs.backend.atom-world.editable-world :refer [->AtomEditableWorld]]
            [clecs.backend.atom-world.transactable-world :refer [->AtomTransactableWorld]]
            [clecs.world :as world]))


(def initial_state {:components {}
                    :entities {}
                    :last-entity-id 0})


(deftype AtomWorld [state systems transactable-world]
  world/ISystemManager
  (set-system! [this slabel s] (swap! systems assoc slabel s) this))


(defn make-world
  ([f]
   (let [state (atom initial_state)
         systems (atom {})
         editable-world (->AtomEditableWorld)
         transactable-world (->AtomTransactableWorld state editable-world)]
     (world/transaction! transactable-world f)
     (->AtomWorld state systems transactable-world))))
