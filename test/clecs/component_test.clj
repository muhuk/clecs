(ns clecs.component-test
  (:require [clecs.component :refer :all]
            [midje.sweet :refer :all]))


(fact "A component with no parameters always validate."
      (validate (component :Foo {}) {}) => nil)


(fact "Number of parameters are validated."
      (let [c (component :Foo {:a nil :b nil})]
        (validate c {}) => (throws RuntimeException)
        (validate c {:a anything}) => (throws RuntimeException)
        (validate c {:a anything :b anything}) => nil
        (validate c {:a anything
                     :b anything
                     :c anything}) => (throws RuntimeException)))


(fact "Parameter keys are validated."
      (let [c (component :Foo {:a nil :b nil})]
        (validate c {:a anything :b anything}) => nil
        (validate c {:c anything :b anything}) => (throws RuntimeException)
        (validate c {:a anything :d anything}) => (throws RuntimeException)
        (validate c {:c anything :d anything}) => (throws RuntimeException)))


(future-fact "Parameter values are validated.")
