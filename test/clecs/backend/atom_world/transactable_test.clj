(ns clecs.backend.atom-world.transactable-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world.transactable :refer :all]
            [clecs.world :as world]))


(deftype TestWorld [state editable-world])


(fact "-transaction! calls function with the world."
      (let [w (->TestWorld (atom ..state..) ..editable-world..)]
        (-transaction! w --f--) => nil
        (provided (--f-- ..editable-world..) => irrelevant)))
