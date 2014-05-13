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


(fact "A new world's entity-id counter starts by zero."
      (:last-entity-id @(.state (make-world --init--))) => 0)


(fact "Initialization function is called withing a transaction."
      (make-world --init--) => irrelevant
      (provided (--init-- (as-checker (implements-protocols
                                       world/IEditableWorld
                                       world/IQueryableWorld))) => irrelevant))


;; System Operations

(fact "set-system!"
      (let [w (make-world --init--)]
        @(.systems w) => {}
        (world/set-system! w ..system-label.. ..system..) => w
        @(.systems w) => {..system-label.. ..system..}))
