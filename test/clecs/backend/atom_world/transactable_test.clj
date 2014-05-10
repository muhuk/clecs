(ns clecs.backend.atom-world.transactable-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world :refer [make-world]]
            [clecs.backend.atom-world.transactable :refer :all]
            [clecs.backend.atom-world.state :refer [*state*]]))


;; Transactions.

(fact "-transaction! calls function with the world."
      (let [w (make-world ..state..)]
        (-transaction! w --f--) => nil
        (provided (--f-- w) => irrelevant)))


(fact "-transaction! binds *state* to world's state."
      (let [w (make-world ..state..)
            a (atom nil)]
        (-transaction! w (fn [_] (reset! a *state*)))
        @a => ..state..))


(fact "-transaction! sets the state of the world to the result of the function."
      (let [w (make-world ..state..)]
        (-transaction! w (fn [_] (var-set #'*state* ..new-state..))) => nil
        @(.state w) => ..new-state..))


(fact "-transaction! throws exception if *state* is already bound."
      (let [w (make-world ..state..)]
        (binding [*state* ..other-state..]
          (-transaction! w --f--) => (throws IllegalStateException)
          (provided (--f-- w) => irrelevant :times 0))))
