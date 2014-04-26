(ns clecs.component-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.component :refer :all]))


(defrecord TestComponent [eid]
  IComponent
  (entity-id [_] eid))


(defrecord NonComponent [])


(facts "component? checks if its parameter is a component."
       (component? (->TestComponent ..eid..)) => true
       (component? (->NonComponent)) => false
       (component? TestComponent) => false
       (component? NonComponent) => false)


(facts "component? checks if its parameter is a component type."
       (component-type? TestComponent) => true
       (component-type? NonComponent) => false
       (component-type? (->TestComponent ..eid..)) => false
       (component-type? (->NonComponent)) => false)


(fact "component-label accepts component types."
      (component-label TestComponent) => :clecs.component_test.TestComponent)


(facts "component-label throws IllegalArgumentException on types that don't extend IComponent."
       (component-label NonComponent) => (throws IllegalArgumentException)
       (component-label (->NonComponent)) => (throws IllegalArgumentException))


(fact "component-label accepts component instances."
      (component-label (->TestComponent ..eid..)) => :clecs.component_test.TestComponent)
