(ns clecs.backend.atom-world.transactable-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world.transactable :refer :all]))


(deftype TestWorld [state])


(fact "-transaction! calls function with the world."
      (let [w (->TestWorld (atom ..state..))]
        (-transaction! w --f--) => nil
        (provided (--f-- w) => irrelevant)))
