(ns clecs.backend.atom-world.transactable-world-test
  (:require [clecs.backend.atom-world.queryable :as queryable]
            [clecs.backend.atom-world.transactable :as transactable]
            [clecs.backend.atom-world.transactable-world :refer :all]
            [clecs.world :as world]
            [midje.sweet :refer :all]))


;; Protocol delegation - IQueryableWorld.

(fact "world/component delegates to component."
      (let [w (->AtomTransactableWorld (atom ..state..) ..editable-world..)]
        (world/component w ..eid.. ..clabel..) => ..component..
        (provided (queryable/component ..state.. ..eid.. ..clabel..) => ..component..)))


(fact "world/query delegates to query"
      (let [w (->AtomTransactableWorld (atom ..state..) ..editable-world..)]
        (world/query w ..q..) => ..result..
        (provided (queryable/query ..state.. ..q..) => ..result..)))


;; Protocol delegation - ITransactableWorld.

(fact "world/transaction! delegates to transaction!"
      (let [w (->AtomTransactableWorld ..state.. ..editable-world..)]
        (world/transaction! w --f--) => nil
        (provided (transactable/transaction! w --f--) => nil)))
