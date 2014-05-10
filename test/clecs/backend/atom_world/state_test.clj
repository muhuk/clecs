(ns clecs.backend.atom-world.state-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world.state :refer :all]))


(fact "-ensure-transaction throws exception when called outside of a transaction."
      (bound? #'*state*) => false
      (-ensure-transaction) => (throws IllegalStateException)
      (binding [*state* ..state..]
        (-ensure-transaction) => anything))


(fact "-ensure-no-transaction throws exception when called outside of a transaction."
      (bound? #'*state*) => false
      (-ensure-no-transaction) => anything
      (binding [*state* ..state..]
        (-ensure-no-transaction) => (throws IllegalStateException)))
