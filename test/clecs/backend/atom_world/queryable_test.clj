(ns clecs.backend.atom-world.queryable-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world.queryable :refer :all]
            [clecs.component :refer [component-label]]))


(fact "-component works."
      (let [state {:components {..clabel.. {..eid.. ..component..}}}]
        (-component state ..eid.. ..ctype..) => ..component..
        (provided (component-label ..ctype..) => ..clabel..)))


(fact "-query works."
      (let [components-1 #{..C1.. ..C2..}
            seq-1 (seq components-1)
            components-2 #{..C2..}
            seq-2 (seq components-2)
            state {:entities {..E1.. components-1
                              ..E2.. components-2}}]
        (-query state --q--) => [..E2..]
        (provided (--q-- seq-1) => false
                  (--q-- seq-2) => true)))
