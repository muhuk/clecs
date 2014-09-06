(ns clecs.backend.atom-world.transactable-test
  (:require [clecs.backend.atom-world.transactable :refer :all]
            [midje.sweet :refer :all]))


(deftype TestWorld [state editable-world])


(fact "-transaction! calls function with the world."
      (let [w (->TestWorld (atom ..state..) ..editable-world..)]
        (-transaction! w --f--) => nil
        (provided (--f-- ..editable-world..) => irrelevant)))
