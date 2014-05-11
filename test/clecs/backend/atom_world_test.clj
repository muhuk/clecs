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
            [clecs.world :as world]))


;; World Initialization.

(fact "atom world implements IWorld."
      (type (make-world)) => (partial extends? world/ITransactableWorld))


(fact "a new world's entity-id counter starts by zero."
      (:last-entity-id @(.state (make-world))) => 0)


(fact "make-world accepts a state parameter."
      @(.state (make-world ..state..)) => ..state..)
