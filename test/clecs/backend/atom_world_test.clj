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


;; Protocol delegation - IEditableWorld.

(fact "world/add-entity delegates to -add-entity."
      (world/add-entity (make-world ..state..)) => ..eid..
      (provided (-add-entity) => ..eid..))


(fact "world/remove-component delegates to -remove-component."
      (let [world (make-world ..state..)]
        (world/remove-component world ..eid.. ..component-type..) => world
        (provided (-remove-component ..eid.. ..component-type..) => nil)))


(fact "world/remove-entity delegates to -remove-entity."
      (let [world (make-world ..state..)]
        (world/remove-entity world ..eid..) => world
        (provided (-remove-entity ..eid..) => nil)))


(fact "world/set-component delegates to -set-component."
      (let [world (make-world ..state..)]
        (world/set-component world  ..c..) => world
        (provided (-set-component ..c..) => nil)))


;; Protocol delegation - IQueryableWorld.

(fact "world/component delegates to -component."
      (let [world (make-world ..state..)
            state-atom (.state world)]
        (world/component world ..eid.. ..clabel..) => ..component..
        (provided (-component state-atom ..eid.. ..clabel..) => ..component..)))


(fact "world/query delegates to -query"
      (let [world (make-world ..state..)
            state-atom (.state world)]
        (world/query world ..q..) => nil
        (provided (-query state-atom ..q..) => nil)))


;; Protocol delegation - ITransactableWorld.

(fact "world/transaction! delegates to -transaction!"
      (let [w (make-world ..state..)]
        (world/transaction! w --f--) => ..result..
        (provided (-transaction! w --f--) => ..result..)))
