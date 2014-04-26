(ns clecs.component-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.component :refer :all]))


(defrecord TestComponent [eid]
  IComponent
  (entity-id [_] eid))


(defrecord NonComponent [])


(fact "component-type accepts component types."
      (component-type TestComponent) => :clecs.component_test.TestComponent)


(fact "component-type throws IllegalArgumentException on types that don't extend IComponent."
      (component-type NonComponent) => (throws IllegalArgumentException))


(fact "component-type accepts component instances."
      (component-type (->TestComponent ..eid..)) => :clecs.component_test.TestComponent)
