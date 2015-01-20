(ns clecs.backend.atom-world
  "Reference implementation of clecs API.

   `AtomWorld` stores it's data in-memory. It is backed by an
   `clojure.core/atom` internally.

   Currently systems run sequentially."
  (:require [clecs.backend.atom-world.editable-world :refer [->AtomEditableWorld]]
            [clecs.backend.atom-world.transactable-world :refer [->AtomTransactableWorld]]
            [clecs.world :as world]
            [clecs.world.system :refer [ISystemManager]]))


(def ^:no-doc initial_state {:components {}
                             :entities {}
                             :last-entity-id 0})


(deftype AtomWorld [systems-atom transactable-world]
  ISystemManager
  (process! [this dt]
            (doseq [s (->> @systems-atom
                           (vals)
                           (map :process))]
              (s transactable-world dt))
            this)
  (remove-system! [this slabel] (swap! systems-atom dissoc slabel) this)
  (set-system! [this slabel s]
               (let [s* (cond (fn? s) {:process s}
                              (map? s) s
                              :default (throw (RuntimeException. "Invalid system.")))]
                 (swap! systems-atom assoc slabel s*) this))
  (systems [_] (seq @systems-atom)))


;; Hide auto-generated constructor for
;; AtomWorld from documentation generator.
(alter-meta! #'->AtomWorld assoc :no-doc true)


(defn make-world
  "Makes a new `AtomWorld`. Use [[clecs.core/make-world]]
   instead of calling this directly."
  [initializer-fn]
  (let [state (atom initial_state)
        systems (atom {})
        editable-world (->AtomEditableWorld)
        transactable-world (->AtomTransactableWorld state editable-world)]
    (world/transaction! transactable-world initializer-fn)
    (->AtomWorld systems transactable-world)))
