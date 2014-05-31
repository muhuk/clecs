(ns clecs.backend.atom-world
  (:require [clecs.backend.atom-world.editable-world :refer [->AtomEditableWorld]]
            [clecs.backend.atom-world.transactable-world :refer [->AtomTransactableWorld]]
            [clecs.world :as world]))


(def initial_state {:components {}
                    :entities {}
                    :last-entity-id 0})


(deftype AtomWorld [systems-atom transactable-world]
  world/ISystemManager
  (process! [this dt]
            (doseq [s (map second (seq @systems-atom))]
              (s transactable-world dt))
            this)
  (remove-system! [this slabel] (swap! systems-atom dissoc slabel) this)
  (set-system! [this slabel s] (swap! systems-atom assoc slabel s) this)
  (systems [_] (seq @systems-atom)))


(defn make-world
  ([initializer]
   (let [state (atom initial_state)
         systems (atom {})
         editable-world (->AtomEditableWorld)
         transactable-world (->AtomTransactableWorld state editable-world)]
     (world/transaction! transactable-world initializer)
     (->AtomWorld systems transactable-world))))
