(ns clecs.backend.atom-world.editable-world-test
  (:require [clecs.backend.atom-world.editable :refer [-add-entity
                                                       -remove-component
                                                       -remove-entity
                                                       -set-component]]
            [clecs.backend.atom-world.editable-world :refer :all]
            [clecs.backend.atom-world.queryable :refer [-component
                                                        -query]]
            [clecs.backend.atom-world.transactable :refer [*state*]]
            [clecs.world :as world]
            [midje.sweet :refer :all]))


;; Protocol delegation - IEditableWorld.

(fact "world/add-entity delegates to -add-entity."
      (world/add-entity (->AtomEditableWorld)) => ..eid..
      (provided (-add-entity) => ..eid..))


(fact "world/remove-component delegates to -remove-component."
      (let [world (->AtomEditableWorld)]
        (world/remove-component world ..eid.. ..component-type..) => world
        (provided (-remove-component ..eid.. ..component-type..) => nil)))


(fact "world/remove-entity delegates to -remove-entity."
      (let [world (->AtomEditableWorld)]
        (world/remove-entity world ..eid..) => world
        (provided (-remove-entity ..eid..) => nil)))


(fact "world/set-component delegates to -set-component."
      (let [world (->AtomEditableWorld)]
        (world/set-component world  ..c..) => world
        (provided (-set-component ..c..) => nil)))


;; Protocol delegation - IQueryableWorld.

(fact "world/component delegates to -component."
      (binding [*state* ..state..]
        (world/component (->AtomEditableWorld) ..eid.. ..clabel..) => ..component..
        (provided (-component ..state.. ..eid.. ..clabel..) => ..component..)))


(fact "world/query delegates to -query"
      (binding [*state* ..state..]
        (world/query (->AtomEditableWorld) ..q..) => ..result..
        (provided (-query *state* ..q..) => ..result..)))
