(ns clecs.backend.atom-world-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world :refer :all]
            [clecs.component :as component]
            [clecs.world :as world]))


(defrecord TestComponentA [eid]
  component/IComponent
  (entity-id [_] eid))


(defrecord TestComponentB [eid a b]
  component/IComponent
  (entity-id [_] eid))


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


(fact "world/add-entity delegates to -add-entity."
      (world/add-entity (make-world ..state..)) => ..eid..
      (provided (-add-entity) => ..eid..))


(fact "world/component delegates to -component."
      (let [world (make-world ..state..)
            state-atom (.state world)]
        (world/component world ..eid.. ..clabel..) => ..component..
        (provided (-component state-atom ..eid.. ..clabel..) => ..component..)))


(fact "world/process! delegates to -process!"
      (let [world (make-world ..state..)]
        (world/process! world) => nil
        (provided (-process! world) => ..result..)))


(fact "world/query delegates to -query"
      (let [world (make-world ..state..)
            state-atom (.state world)]
        (world/query world ..q..) => nil
        (provided (-query state-atom ..q..) => nil)))


(fact "world/remove-entity delegates to -remove-entity."
      (let [world (make-world ..state..)]
        (world/remove-entity world ..eid..) => nil
        (provided (-remove-entity ..eid..) => nil)))


(fact "world/set-component delegates to -set-component."
      (world/set-component (make-world ..state..)  ..c..) => nil
      (provided (-set-component ..c..) => nil))


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


(fact "-remove-entity can only be called within a transaction."
      (-remove-entity ..eid..) => (throws IllegalStateException))


(fact "-remove-entity removes entity-id from entity index."
      (binding [*state* {:components {}
                         :entities {:last-index 1 1 #{}}}]
        (-remove-entity 1) => nil
        *state* => {:components {}
                    :entities {:last-index 1}}))


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

(fact "-add-component can only be called within a transaction."
      (-add-component ..eid.. ..f.. ..args..) => (throws IllegalStateException))


(fact "-add-component works with a constructor without parameters."
      (let [eid 1
            clabel :clecs.backend.atom_world_test.TestComponentA
            initial-state {:components {}
                           :entities {eid #{}
                                      :last-index eid}}
            expected-state {:components {clabel {eid (->TestComponentA eid)}}
                           :entities {eid #{clabel}
                                      :last-index eid}}]
        (binding [*state* initial-state]
          (-add-component eid ->TestComponentA []) => nil
          *state* => expected-state)))


(fact "-add-component works with a constructor with parameters."
      (let [eid 1
            clabel :clecs.backend.atom_world_test.TestComponentB
            initial-state {:components {}
                           :entities {eid #{}
                                      :last-index eid}}
            expected-state {:components {clabel {eid (->TestComponentB eid ..a.. ..b..)}}
                           :entities {eid #{clabel}
                                      :last-index eid}}]
        (binding [*state* initial-state]
          (-add-component eid ->TestComponentB [..a.. ..b..]) => nil
          *state* => expected-state)))


(fact "-remove-component can only be called within a transaction."
      (-remove-component ..eid.. ..clabel..) => (throws IllegalStateException))


(fact "-remove-component works."
      (let [initial-state {:components {..clabel.. {..eid.. ..component..}}
                           :entities {..eid.. #{..clabel..}}}
            expected-state {:components {..clabel.. {}}
                            :entities {..eid.. #{}}}]
        (binding [*state* initial-state]
          (-remove-component ..eid.. ..clabel..) => nil
          *state* => expected-state)))


(fact "-set-component can only be called within a transaction."
      (-set-component ..c..) => (throws IllegalStateException))


(fact "-set-component validates its parameter is a component."
      (binding [*state* ..state..]
        (-set-component ..c..) => (throws IllegalArgumentException)))


(fact "-set-component adds the component if entity doesn't have one."
      (let [eid 1
            c (->TestComponentA eid)
            clabel (component/component-label TestComponentA)
            initial-state {:components {}
                           :entities {eid #{}
                                      :last-index eid}}
            expected-state {:components {clabel {eid c}}
                            :entities {eid #{clabel}
                                       :last-index eid}}]
        (binding [*state* initial-state]
          (-set-component c) => nil
          *state* => expected-state)))


(fact "-set-component replaces existing components."
      (let [eid 1
            c-old (->TestComponentB eid ..a.. ..b..)
            c-new (->TestComponentB eid ..c.. ..d..)
            clabel (component/component-label TestComponentB)
            initial-state {:components {clabel {eid c-old}}
                            :entities {eid #{clabel}
                                       :last-index eid}}
            expected-state {:components {clabel {eid c-new}}
                            :entities {eid #{clabel}
                                       :last-index eid}}]
        (binding [*state* initial-state]
          (-set-component c-new) => nil
          *state* => expected-state)))


;; Queries


(fact "-component dereferences the state outside of a transaction."
      (let [state {:components {..clabel.. {..eid.. ..component..}}}]
        (bound? #'*state*) => false
        (-component (atom state) ..eid.. ..clabel..) => ..component..))


(fact "-component uses bound state within a transaction."
      (binding [*state* {:components {..clabel.. {..eid.. ..component..}}}]
        (-component ..state-atom.. ..eid.. ..clabel..) => ..component..))


(facts "-query dereferences the state outside of a transaction."
       (bound? #'*state*) => false
       (-query (atom {:entities {..E1.. ..C1..
                                 ..E2.. ..C2..}}) --q--) => [..E2..]
       (provided (--q-- ..C1..) => false
                 (--q-- ..C2..) => true))


(fact "-query uses bound state within a transaction."
      (binding [*state* {:entities {..E1.. ..C1..
                                    ..E2.. ..C2..}}]
        (-query (atom ..state-atom..) --q--) => [..E2..]
        (provided (--q-- ..C1..) => false
                  (--q-- ..C2..) => true)))
