(ns clecs.backend.atom-world-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world :refer :all]
            [clecs.component :refer [component-label defcomponent]]
            [clecs.world :as world]))


(defcomponent TestComponentA [eid])


(defcomponent TestComponentB [eid a b])


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
       (binding [*state* {:last-entity-id 0}]
         (-add-entity) => 1)
       (binding [*state* {:last-entity-id 41}]
         (-add-entity) => 42))

(fact "-add-entity adds the new entity-id to the entity index."
      (binding [*state* {:last-entity-id 0}]
        (let [eid (-add-entity)]
          (get-in *state* [:entities eid]) => #{})))


(fact "-add-entity updates entity counter."
      (binding [*state* {:last-entity-id 0}]
        (-add-entity)
        (:last-entity-id *state*) => 1))


(fact "-remove-entity can only be called within a transaction."
      (-remove-entity ..eid..) => (throws IllegalStateException))


(fact "-remove-entity removes entity-id from entity index."
      (binding [*state* {:components {}
                         :entities {1 #{}}
                         :last-entity-id 1}]
        (-remove-entity 1) => nil
        *state* => {:components {}
                    :entities {}
                    :last-entity-id 1}))


(fact "-remove-entity removes entity's components."
      (let [clabel :clecs.backend.atom_world_test.TestComponentA
            initial-state {:components {clabel {..eid.. ..i.. ..other-eid.. ..j..}}
                           :entities {}}
            expected-state {:components {clabel {..other-eid.. ..j..}}
                            :entities {}}]
        (binding [*state* initial-state]
          (-remove-entity ..eid..) => nil
          *state* => expected-state)))


;; Component operations.

(fact "-remove-component can only be called within a transaction."
      (-remove-component ..eid.. ..ctype..) => (throws IllegalStateException))


(fact "-remove-component works."
      (let [initial-state {:components {..clabel.. {..eid.. ..component..}}
                           :entities {..eid.. #{..clabel..}}}
            expected-state {:components {..clabel.. {}}
                            :entities {..eid.. #{}}}]
        (binding [*state* initial-state]
          (-remove-component ..eid.. ..ctype..) => nil
          (provided (component-label ..ctype..) => ..clabel..)
          *state* => expected-state)))


(fact "-set-component can only be called within a transaction."
      (-set-component ..c..) => (throws IllegalStateException))


(fact "-set-component validates its parameter is a component."
      (binding [*state* ..state..]
        (-set-component ..c..) => (throws IllegalArgumentException)))


(fact "-set-component adds the component if entity doesn't have one."
      (let [eid 1
            c (->TestComponentA eid)
            clabel (component-label TestComponentA)
            initial-state {:components {}
                           :entities {eid #{}}}
            expected-state {:components {clabel {eid c}}
                            :entities {eid #{clabel}}}]
        (binding [*state* initial-state]
          (-set-component c) => nil
          *state* => expected-state)))


(fact "-set-component replaces existing components."
      (let [eid 1
            c-old (->TestComponentB eid ..a.. ..b..)
            c-new (->TestComponentB eid ..c.. ..d..)
            clabel (component-label TestComponentB)
            initial-state {:components {clabel {eid c-old}}
                            :entities {eid #{clabel}}}
            expected-state {:components {clabel {eid c-new}}
                            :entities {eid #{clabel}}}]
        (binding [*state* initial-state]
          (-set-component c-new) => nil
          *state* => expected-state)))


;; Queries


(fact "-component dereferences the state outside of a transaction."
      (let [state {:components {..clabel.. {..eid.. ..component..}}}]
        (bound? #'*state*) => false
        (-component (atom state) ..eid.. ..ctype..) => ..component..
        (provided (component-label ..ctype..) => ..clabel..)))


(fact "-component uses bound state within a transaction."
      (binding [*state* {:components {..clabel.. {..eid.. ..component..}}}]
        (-component ..state-atom.. ..eid.. ..ctype..) => ..component..
        (provided (component-label ..ctype..) => ..clabel..)))


(facts "-query dereferences the state outside of a transaction."
       (let [components-1 #{..C1.. ..C2..}
             seq-1 (seq components-1)
             components-2 #{..C2..}
             seq-2 (seq components-2)]
         (bound? #'*state*) => false
         (-query (atom {:entities {..E1.. components-1
                                   ..E2.. components-2}}) --q--) => [..E2..]
         (provided (--q-- seq-1) => false
                   (--q-- seq-2) => true)))


(fact "-query uses bound state within a transaction."
      (let [components-1 #{..C1.. ..C2..}
            seq-1 (seq components-1)
            components-2 #{..C2..}
            seq-2 (seq components-2)]
        (binding [*state* {:entities {..E1.. components-1
                                      ..E2.. components-2}}]
          (-query (atom ..state-atom..) --q--) => [..E2..]
          (provided (--q-- seq-1) => false
                    (--q-- seq-2) => true))))


;; Utilities

(facts "-ensure-transaction throws exception when called outside of a transaction."
       (bound? #'*state*) => false
       (-ensure-transaction) => (throws IllegalStateException)
       (binding [*state* ..state..]
        (-ensure-transaction) => anything))


(facts "-ensure-no-transaction throws exception when called outside of a transaction."
       (bound? #'*state*) => false
       (-ensure-no-transaction) => anything
       (binding [*state* ..state..]
        (-ensure-no-transaction) => (throws IllegalStateException)))
