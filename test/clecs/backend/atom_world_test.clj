(ns clecs.backend.atom-world-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world :refer :all]
            [clecs.component :as component]
            [clecs.world :as world]))


(defrecord TestComponent [eid a b]
  component/IComponent)


;; World Initialization.

(fact "atom world implements IWorld."
      (type (make-world)) => (partial extends? world/IWorld))


(fact "a new world's entity-id counter starts by zero."
      (get-in @(.state (make-world)) [:entities :last-index]) => 0)


(fact "make-world accepts a state parameter."
      @(.state (make-world ..state..)) => ..state..)


;; Protocol delegation.


(facts "world/add-component! delegates to add-component"
       (world/add-component! (make-world ..state..) ..eid.. ..f..) => nil
       (provided (add-component ..state.. ..eid.. ..f..) => ..new-state..))


(fact "world/add-entity! delegates to add-entity!"
      (world/add-entity! (make-world ..state..)) => ..eid..
      (provided (add-entity ..state..) => ..new-state..
                (last-entity-id ..new-state..) => ..eid..))


(fact "world/process! delegates to process!"
      (let [world (make-world ..state..)]
        (world/process! world) => nil
        (provided (process! world) => ..result..)))


;; Entity operations.

(facts "adding an entity returns a new entity-id and the modified state."
       (get-in (add-entity EMPTY_WORLD) [:entities 1]) => #{}
       (last-entity-id (add-entity EMPTY_WORLD)) => 1
       (last-entity-id (add-entity {:entities {:last-index 41}})) => 42)


;; Component operations.

(fact "adding a component to an entity creates the component index."
      (let [state (add-entity EMPTY_WORLD)
            eid (last-entity-id state)
            expected-state {:components {..component-type.. {1 ..component..}}
                            :entities {1 #{..component-type..}
                                       :last-index 1}}]
        (add-component state eid ->TestComponent ..a.. ..b..) => expected-state
        (provided (->TestComponent eid ..a.. ..b..) => ..component..
                  (component/component-type ..component..) => ..component-type..)))
