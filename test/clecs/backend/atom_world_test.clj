(ns clecs.backend.atom-world-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world :refer :all]
            [clecs.backend.atom-world.editable :refer [-add-entity
                                                       -remove-component
                                                       -remove-entity
                                                       -set-component]]
            [clecs.backend.atom-world.queryable :refer [-component
                                                        -query]]
            [clecs.backend.atom-world.transactable :refer [-transaction!]]
            [clecs.test.checkers :refer :all]
            [clecs.world :as world]))


;; World Initialization.

(fact "Atom world implements ISystemManager."
      (make-world --init--) => (implements-protocols world/ISystemManager))


(fact "Initialization function is called withing a transaction."
      (make-world --init--) => irrelevant
      (provided (--init-- (as-checker (implements-protocols
                                       world/IEditableWorld
                                       world/IQueryableWorld))) => irrelevant))


;; System Operations

(fact "remove-system! unregisters the system with the system-label."
      (let [w (-> (make-world --init--)
                  (world/set-system! ..system-label.. ..system..))]
        (world/systems w) => (seq {..system-label.. ..system..})
        (world/remove-system! w ..system-label..) => w
        (world/systems w) => (seq {})))


(fact "set-system! registers a system with the system-label."
      (let [w (make-world --init--)]
        (world/systems w) => (seq {})
        (world/set-system! w ..system-label.. ..system..) => w
        (world/systems w) => (seq {..system-label.. ..system..})))


(fact "systems returns a seq of [system-label system] pairs."
      (-> (make-world --init--)
          (world/set-system! ..system-label.. ..system..)
          (world/systems)) => (seq {..system-label.. ..system..}))
