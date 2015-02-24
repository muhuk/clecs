(ns clecs.component-test
  (:require [clecs.component :refer :all]
            [midje.sweet :refer :all]))


(fact "A component with no parameters always validate."
      (validate (component :Foo {}) {}) => nil)


(future-fact "Component names must be keywords.")


(fact "Number of parameters are validated."
      (let [c (component :Foo {:a Int :b Str})]
        (validate c {}) => (throws RuntimeException #"parameters")
        (validate c {:a anything}) => (throws RuntimeException #"parameters")
        (validate c {:a anything
                     :b anything}) =not=> (throws RuntimeException #"parameters")
        (validate c {:a anything
                     :b anything
                     :c anything}) => (throws RuntimeException #"parameters")))


(fact "Parameter keys are validated."
      (let [c (component :Bar {:a Str :b Bool})]
        (validate c {:a anything :b anything}) =not=> (throws RuntimeException #"parameters")
        (validate c {:c anything :b anything}) => (throws RuntimeException #"parameters")
        (validate c {:a anything :d anything}) => (throws RuntimeException #"parameters")
        (validate c {:c anything :d anything}) => (throws RuntimeException #"parameters")))


(facts "Parameter values are validated."
       (fact "Booleans are validated."
             (let [c (component :Baz {:x Bool})]
               (validate c {:x true}) => nil
               (validate c {:x false}) => nil
               (validate c {:x 3}) => (throws RuntimeException #"not a valid")
               (validate c {:x "Fubar"}) => (throws RuntimeException #"not a valid")))

       (fact "Integers are validated."
             (let [c (component :Baz {:x Int})]
               (validate c {:x 3}) => nil
               (validate c {:x true}) => (throws RuntimeException #"not a valid")
               (validate c {:x false}) => (throws RuntimeException #"not a valid")
               (validate c {:x "Fubar"}) => (throws RuntimeException #"not a valid")))
       (fact "Strings are validated."
             (let [c (component :Baz {:x Str})]
               (validate c {:x "Fubar"}) => nil
               (validate c {:x true}) => (throws RuntimeException #"not a valid")
               (validate c {:x false}) => (throws RuntimeException #"not a valid")
               (validate c {:x 3}) => (throws RuntimeException #"not a valid"))))
