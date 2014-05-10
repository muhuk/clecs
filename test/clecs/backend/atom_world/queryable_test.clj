(ns clecs.backend.atom-world.queryable-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world :refer [make-world]]
            [clecs.backend.atom-world.queryable :refer :all]
            [clecs.backend.atom-world.state :refer [*state*]]
            [clecs.component :refer [component-label]]))


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


(fact "-query dereferences the state outside of a transaction."
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
