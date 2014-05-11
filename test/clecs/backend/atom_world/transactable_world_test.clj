(ns clecs.backend.atom-world.transactable-world-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world.queryable :refer [-component
                                                        -query]]
            [clecs.backend.atom-world.transactable :refer [-transaction!]]
            [clecs.backend.atom-world.transactable-world :refer :all]
            [clecs.world :as world]))


;; Protocol delegation - IQueryableWorld.

(fact "world/component delegates to -component."
      (let [w (->AtomTransactableWorld (atom ..state..) ..editable-world..)]
        (world/component w ..eid.. ..clabel..) => ..component..
        (provided (-component ..state.. ..eid.. ..clabel..) => ..component..)))


(fact "world/query delegates to -query"
      (let [w (->AtomTransactableWorld (atom ..state..) ..editable-world..)]
        (world/query w ..q..) => ..result..
        (provided (-query ..state.. ..q..) => ..result..)))


;; Protocol delegation - ITransactableWorld.

(fact "world/transaction! delegates to -transaction!"
      (let [w (->AtomTransactableWorld ..state.. ..editable-world..)]
        (world/transaction! w --f--) => nil
        (provided (-transaction! ..editable-world.. --f--) => nil)))
