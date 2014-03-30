(ns clecs.backend.atom-world-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world :refer :all]
            [clecs.component :as component]
            [clecs.world :as world]))


(defrecord TestComponentA [eid]
  component/IComponent)


(defrecord TestComponentB [eid a b]
  component/IComponent)


;; World Initialization.

(fact "atom world implements IWorld."
      (type (make-world)) => (partial extends? world/IWorld))


(fact "a new world's entity-id counter starts by zero."
      (get-in @(.state (make-world)) [:entities :last-index]) => 0)


(fact "make-world accepts a state parameter."
      @(.state (make-world ..state..)) => ..state..)


;; Protocol delegation.


(facts "world/add-component delegates to -add-component."
       (let [w (make-world ..state..)]
         (world/add-component w ..eid.. ..f..) => nil
         (provided (-add-component ..eid.. ..f.. []) => nil))
       (let [w (make-world ..state..)]
         (world/add-component w ..eid.. ..f.. ..args..) => nil
         (provided (-add-component ..eid.. ..f.. ..args..) => nil)))


(fact "world/add-entity delegates to -add-entity"
      (world/add-entity (make-world ..state..)) => ..eid..
      (provided (-add-entity) => ..eid..))


(fact "world/process! delegates to -process!"
      (let [world (make-world ..state..)]
        (world/process! world) => nil
        (provided (-process! world) => ..result..)))


(fact "world/transaction! delegates to -transaction!"
      (let [w (make-world ..state..)]
        (world/transaction! w --f--) => ..result..
        (provided (-transaction! w --f--) => ..result..)))


(fact "world/remove-component delegates to -remove-component."
      (world/remove-component (make-world ..state..) ..eid.. ..component-type..) => nil
      (provided (-remove-component ..eid.. ..component-type..) => nil))


;; Transactions.

(fact "-transaction! calls function with the world."
      (let [w (make-world ..state..)]
        (-transaction! w --f--) => nil
        (provided (--f-- w) => irrelevant)))


(fact "-transaction! binds *state* to world's state."
      (let [w (make-world ..state..)
            a (atom nil)]
        (-transaction! w (fn [_] (reset! a *state*)))
        @a => ..state..))


(fact "-transaction! sets the state of the world to the result of the function."
      (let [w (make-world ..state..)]
        (-transaction! w (fn [_] (var-set #'*state* ..new-state..))) => nil
        @(.state w) => ..new-state..))


(fact "-transaction! throws exception if *state* is already bound."
      (let [w (make-world ..state..)]
        (binding [*state* ..other-state..]
          (-transaction! w --f--) => (throws IllegalStateException)
          (provided (--f-- w) => irrelevant :times 0))))


;; Entity operations.

(fact "-add-entity can only be called within a transaction."
      (-add-entity) => (throws IllegalStateException))


(facts "-add-entity returns a new entity id."
       (binding [*state* {:entities {:last-index 0}}]
         (-add-entity) => 1)
       (binding [*state* {:entities {:last-index 41}}]
         (-add-entity) => 42))

(fact "-add-entity adds the new entity-id to the entity index."
      (binding [*state* {:entities {:last-index 0}}]
        (let [eid (-add-entity)]
          (get-in *state* [:entities eid]) => #{})))


(fact "-add-entity updates entity counter."
      (binding [*state* {:entities {:last-index 0}}]
        (-add-entity)
        (get-in *state* [:entities :last-index]) => 1))


;; Component operations.

(fact "-add-component can only be called within a transaction."
      (-add-component ..eid.. ..f.. ..args..) => (throws IllegalStateException))


(fact "-add-component works with a constructor without parameters."
      (let [eid 1
            ct :clecs.backend.atom_world_test.TestComponentA
            initial-state {:components {}
                           :entities {eid #{}
                                      :last-index eid}}
            expected-state {:components {ct {eid (->TestComponentA eid)}}
                           :entities {eid #{ct}
                                      :last-index eid}}]
        (binding [*state* initial-state]
          (-add-component eid ->TestComponentA []) => nil
          *state* => expected-state)))


(fact "-add-component works with a constructor with parameters."
      (let [eid 1
            ct :clecs.backend.atom_world_test.TestComponentB
            initial-state {:components {}
                           :entities {eid #{}
                                      :last-index eid}}
            expected-state {:components {ct {eid (->TestComponentB eid ..a.. ..b..)}}
                           :entities {eid #{ct}
                                      :last-index eid}}]
        (binding [*state* initial-state]
          (-add-component eid ->TestComponentB [..a.. ..b..]) => nil
          *state* => expected-state)))


(fact "-remove-component can only be called within a transaction."
      (-remove-component ..eid.. ..ct..) => (throws IllegalStateException))


(fact "-remove-component works."
      (let [initial-state {:components {..component-type.. {..eid.. ..component..}}
                           :entities {..eid.. #{..component-type..}}}
            expected-state {:components {..component-type.. {}}
                            :entities {..eid.. #{}}}]
        (binding [*state* initial-state]
          (-remove-component ..eid.. ..component-type..) => nil
          *state* => expected-state)))
