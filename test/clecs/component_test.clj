(ns clecs.component-test
  (:require [clecs.component :refer :all]
            [midje.sweet :refer :all]))


(fact "A component with no parameters always validate."
      (valid? (component :Foo {}) {}) => true)


(fact "Number of parameters are validated."
      (valid? (component :Foo {:a nil :b nil}) {}) => false
      (valid? (component :Foo
                         {:a nil :b nil})
              {:a anything}) => false
      (valid? (component :Foo
                         {:a nil :b nil})
              {:a anything
               :b anything}) => true
      (valid? (component :Foo
                         {:a nil :b nil})
              {:a anything
               :b anything
               :c anything}) => false)


(future-fact "Parameter keys are validated.")


(future-fact "Parameter values are validated.")
