(ns clecs.backend.atom-world-test
  (:require [clecs.backend.atom-world :refer :all]
            [clecs.backend.atom-world.query :as query]
            [clecs.component :refer [defcomponent]]
            [clecs.test.checkers :refer :all]
            [clecs.world :as world]
            [clecs.world.editable :refer [IEditableWorld]]
            [clecs.world.queryable :refer [IQueryableWorld]]
            [clecs.world.system :refer [ISystemManager]]
            [midje.sweet :refer :all]))


(def editable-world-like (implements-protocols IEditableWorld
                                               IQueryableWorld))


(defcomponent TestComponentA [eid])


(defcomponent TestComponentB [eid a b])


;; World Initialization.

(fact "Atom world implements ISystemManager."
      (make-world --init--) => (implements-protocols ISystemManager))


(fact "Initialization function is called within a transaction."
      (make-world --init--) => irrelevant
      (provided (--init-- (as-checker editable-world-like)) => irrelevant))


(fact "transaction! calls function with an editable world and time delta."
      (let [w (->AtomWorld nil (atom ..state..) ..editable-world..)]
        (-transaction! w --f-- ..dt..) => nil
        (provided (--f-- ..editable-world.. ..dt..) => irrelevant)))


;; System Operations

(fact "remove-system! unregisters the system with the system-label."
      (let [w (-> (make-world --init--)
                  (world/set-system! ..system-label.. {:process ..system..}))]
        (world/systems w) => (seq {..system-label.. {:process ..system..}})
        (world/remove-system! w ..system-label..) => w
        (world/systems w) => (seq {})))


(fact "set-system! registers a system with the system-label."
      (let [w (make-world --init--)]
        (world/systems w) => (seq {})
        (world/set-system! w ..system-label.. {:process ..system..}) => w
        (world/systems w) => (seq {..system-label.. {:process ..system..}})))


(fact "systems returns a seq of [system-label system] pairs."
      (-> (make-world --init--)
          (world/set-system! ..system-label.. {:process ..system..})
          (world/systems)) => (seq {..system-label.. {:process ..system..}}))


;; Processing

(fact "process! returns the world."
      (let [w (make-world --init--)]
        (world/process! w ..dt..) => w))


(fact "process! calls each system with the world and delta time."
      (let [calls (atom [])
            s-one {:process (fn [& args] (swap! calls conj [:one-called args]))}
            s-two {:process (fn [& args] (swap! calls conj [:two-called args]))}
            w (-> (make-world --init--)
                  (world/set-system! ..l-one.. s-one)
                  (world/set-system! ..l-two.. s-two))]
        (world/process! w ..dt..) => irrelevant
        (set (map first @calls)) => (set [:one-called :two-called])
        (-> @calls first second first) => editable-world-like
        (-> @calls first second second) => ..dt..
        (-> @calls second second first) => editable-world-like
        (-> @calls second second second) => ..dt..))


;; Editable world.

(fact "world/add-entity returns a new entity id."
      (binding [*state* {:last-entity-id 0}]
        (world/add-entity (->AtomEditableWorld)) => 1)
      (binding [*state* {:last-entity-id 41}]
        (world/add-entity (->AtomEditableWorld)) => 42))


(fact "world/add-entity adds the new entity-id to the entity index."
      (binding [*state* {:last-entity-id 0}]
        (let [eid (world/add-entity (->AtomEditableWorld))]
          (get-in *state* [:entities eid]) => #{})))


(fact "world/add-entity updates entity counter."
      (binding [*state* {:last-entity-id 0}]
        (world/add-entity (->AtomEditableWorld))
        (:last-entity-id *state*) => 1))


(fact "world/add-entity returns different values each time it's called."
      (let [w (->AtomEditableWorld)]
        (binding [*state* {:entities {}
                           :last-entity-id 0}]
          (repeatedly 42 #(world/add-entity w)) => (comp #(= % 42) count set))))


(fact "world/component resolves queried component."
      (binding [*state* {:components {..ctype.. {..eid.. ..component..}}}]
        (world/component (->AtomEditableWorld) ..eid.. ..ctype..) => ..component..))


(fact "world/query returns a seq."
      (binding [*state* {:entities {..e1.. #{..c1.. ..c2..}
                                    ..e2.. #{..c2..}}}]
        (world/query (->AtomEditableWorld) ..q..) => seq?
        (provided (query/-compile-query ..q..) => (partial some #{..c1..}))))


(fact "world/query compiles query and then calls the result with a seq of component labels."
      (binding [*state* {:entities {..e1.. #{..c1.. ..c2..}
                                    ..e2.. #{..c2..}
                                    ..e3.. #{..c3..}}}]
        (sort-by str (world/query (->AtomEditableWorld) ..q..)) => [..e1.. ..e3..]
        (provided (query/-compile-query ..q..) => (partial some #{..c1.. ..c3..}))))


(fact "world/remove-component works."
      (let [w (->AtomEditableWorld)
            initial-state {:components {..ctype.. {..eid.. ..component..}}
                           :entities {..eid.. #{..ctype..}}}
            expected-state {:components {..ctype.. {}}
                            :entities {..eid.. #{}}}]
        (binding [*state* initial-state]
          (world/remove-component w ..eid.. ..ctype..) => w
          *state* => expected-state)))


(fact "world/remove-entity removes entity-id from entity index."
      (let [w (->AtomEditableWorld)]
        (binding [*state* {:components {}
                           :entities {1 #{}}
                           :last-entity-id 1}]
          (world/remove-entity w 1) => w
          *state* => {:components {}
                      :entities {}
                      :last-entity-id 1})))


(fact "world/remove-entity removes entity's components."
      (let [w (->AtomEditableWorld)
            ctype :clecs.backend.atom_world_test.TestComponentA
            initial-state {:components {ctype {..eid.. ..i.. ..other-eid.. ..j..}}
                           :entities {}}
            expected-state {:components {ctype {..other-eid.. ..j..}}
                            :entities {}}]
        (binding [*state* initial-state]
          (world/remove-entity w ..eid..) => w
          *state* => expected-state)))


(fact "world/set-component adds the component if entity doesn't have one."
      (let [w (->AtomEditableWorld)
            eid 1
            cdata {}
            ctype :clecs.backend.atom_world_test.TestComponentA
            initial-state {:components {}
                           :entities {eid #{}}}
            expected-state {:components {ctype {eid cdata}}
                            :entities {eid #{ctype}}}]
        (binding [*state* initial-state]
          (world/set-component w eid ctype cdata) => w
          *state* => expected-state)))


(fact "world/set-component replaces existing components."
      (let [w (->AtomEditableWorld)
            eid 1
            c-old {:a ..a.. :b ..b..}
            c-new {:a ..c.. :b ..d..}
            ctype :clecs.backend.atom_world_test.TestComponentB
            initial-state {:components {ctype {eid c-old}}
                            :entities {eid #{ctype}}}
            expected-state {:components {ctype {eid c-new}}
                            :entities {eid #{ctype}}}]
        (binding [*state* initial-state]
          (world/set-component w eid ctype c-new) => w
          *state* => expected-state)))
