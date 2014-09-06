(ns clecs.component-test
  (:require [clecs.component :refer :all]
            [midje.sweet :refer :all]))


(defcomponent TestComponent [eid])


(defrecord NonComponent [])


(facts "component? checks if its parameter is a component."
       (component? (->TestComponent ..eid..)) => true
       (component? (->NonComponent)) => false
       (component? TestComponent) => false
       (component? NonComponent) => false)


(facts "component-type? checks if its parameter is a component type."
       (component-type? TestComponent) => true
       (component-type? NonComponent) => false
       (component-type? (->TestComponent ..eid..)) => false
       (component-type? (->NonComponent)) => false)


(facts "component-label accepts component types."
       (component-label TestComponent) => :clecs.component_test.TestComponent
       (component-label (->TestComponent ..eid..)) => (throws IllegalArgumentException)
       (component-label NonComponent) => (throws IllegalArgumentException)
       (component-label (->NonComponent)) => (throws IllegalArgumentException))
