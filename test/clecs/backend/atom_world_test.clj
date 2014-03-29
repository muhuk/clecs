(ns clecs.backend.atom-world-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world :refer :all]
            [clecs.world :as world]))


;; World Initialization.

(fact "atom world implements IWorld."
      (type (make-world)) => (partial extends? world/IWorld))


(fact "a new world's entity-id counter starts by zero."
      (get-in @(.state (make-world)) [:entities :last-index]) => 0)


(fact "make-world accepts a state parameter."
      @(.state (make-world ..state..)) => ..state..)


;; Protocol delegation.

(fact "world/process! delegates to process!"
      (let [world (make-world ..state..)]
        (world/process! world) => nil
        (provided (process! world) => ..result..)))


(fact "world/add-entity! delegates to add-entity!"
      (let [world (make-world ..state..)]
        (world/add-entity! world) => nil
        (provided (add-entity ..state..) => ..result..)))


;; Entity operations.

(facts "adding an entity returns a new entity-id."
       (get-in (add-entity EMPTY_WORLD) [:entities 1]) => #{}
       (get-in (add-entity EMPTY_WORLD) [:entities :last-index]) => 1)
