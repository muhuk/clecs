(ns clecs.backend.atom-world.editable-world-test
  (:require [clecs.backend.atom-world.editable :as editable]
            [clecs.backend.atom-world.editable-world :refer :all]
            [clecs.backend.atom-world.queryable :as queryable]
            [clecs.backend.atom-world.transactable :refer [*state*]]
            [clecs.world :as world]
            [midje.sweet :refer :all]))


;; Protocol delegation - IEditableWorld.

(fact "world/add-entity delegates to add-entity."
      (world/add-entity (->AtomEditableWorld)) => ..eid..
      (provided (editable/add-entity) => ..eid..))


(fact "world/remove-component delegates to remove-component."
      (let [world (->AtomEditableWorld)]
        (world/remove-component world ..eid.. ..component-type..) => world
        (provided (editable/remove-component ..eid.. ..component-type..) => nil)))


(fact "world/remove-entity delegates to remove-entity."
      (let [world (->AtomEditableWorld)]
        (world/remove-entity world ..eid..) => world
        (provided (editable/remove-entity ..eid..) => nil)))


(fact "world/set-component delegates to set-component."
      (let [world (->AtomEditableWorld)]
        (world/set-component world  ..c..) => world
        (provided (editable/set-component ..c..) => nil)))


;; Protocol delegation - IQueryableWorld.

(fact "world/component delegates to component."
      (binding [*state* ..state..]
        (world/component (->AtomEditableWorld) ..eid.. ..clabel..) => ..component..
        (provided (queryable/component ..state.. ..eid.. ..clabel..) => ..component..)))


(fact "world/query delegates to query"
      (binding [*state* ..state..]
        (world/query (->AtomEditableWorld) ..q..) => ..result..
        (provided (queryable/query *state* ..q..) => ..result..)))
