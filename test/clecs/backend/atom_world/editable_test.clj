(ns clecs.backend.atom-world.editable-test
  (:require [clecs.backend.atom-world.editable :refer :all]
            [clecs.backend.atom-world.transactable :refer [*state*]]
            [clecs.component :refer [component-label defcomponent]]
            [midje.sweet :refer :all]))



(defcomponent TestComponentA [eid])


(defcomponent TestComponentB [eid a b])


;; Entity operations.

(fact "-add-entity returns a new entity id."
      (binding [*state* {:last-entity-id 0}]
        (add-entity) => 1)
      (binding [*state* {:last-entity-id 41}]
        (add-entity) => 42))


(fact "-add-entity adds the new entity-id to the entity index."
      (binding [*state* {:last-entity-id 0}]
        (let [eid (add-entity)]
          (get-in *state* [:entities eid]) => #{})))


(fact "-add-entity updates entity counter."
      (binding [*state* {:last-entity-id 0}]
        (add-entity)
        (:last-entity-id *state*) => 1))


(fact "-remove-entity removes entity-id from entity index."
      (binding [*state* {:components {}
                         :entities {1 #{}}
                         :last-entity-id 1}]
        (remove-entity 1) => nil
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
          (remove-entity ..eid..) => nil
          *state* => expected-state)))


;; Component operations.

(fact "-remove-component works."
      (let [initial-state {:components {..clabel.. {..eid.. ..component..}}
                           :entities {..eid.. #{..clabel..}}}
            expected-state {:components {..clabel.. {}}
                            :entities {..eid.. #{}}}]
        (binding [*state* initial-state]
          (remove-component ..eid.. ..ctype..) => nil
          (provided (component-label ..ctype..) => ..clabel..)
          *state* => expected-state)))


(fact "-set-component validates its parameter is a component."
      (binding [*state* ..state..]
        (set-component ..c..) => (throws IllegalArgumentException)))


(fact "-set-component adds the component if entity doesn't have one."
      (let [eid 1
            c (->TestComponentA eid)
            clabel (component-label TestComponentA)
            initial-state {:components {}
                           :entities {eid #{}}}
            expected-state {:components {clabel {eid c}}
                            :entities {eid #{clabel}}}]
        (binding [*state* initial-state]
          (set-component c) => nil
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
          (set-component c-new) => nil
          *state* => expected-state)))
