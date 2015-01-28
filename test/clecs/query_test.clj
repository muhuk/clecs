(ns clecs.query-test
  (:require [clecs.query :refer :all]
            [midje.sweet :refer :all]))


(facts "all returns a vector with :all as its first parameter."
       (all :c) => (has-prefix [:all])
       (all :a :b :c) => (has-prefix [:all])
       (all :c1 :c2) => [:all :c1 :c2])


(fact "all inlines other all calls."
      (all :a (all :b :c) :d) => [:all :a :b :c :d])


(fact "all doesn't modify any calls."
      (all :a (any :b :c) :d) => [:all :a ..any-result.. :d]
      (provided (any :b :c) => ..any-result..))


(facts "any returns a vector with :any as its first parameter."
       (any :c) => (has-prefix [:any])
       (any :a :b :c) => (has-prefix [:any])
       (any :a :b) => [:any :a :b])


(fact "any inlines other any calls."
      (any :a (any :b :c) :d) => [:any :a :b :c :d])


(fact "any doesn't modify all calls."
      (any :a (all :b :c) :d) => [:any :a ..all-result.. :d]
      (provided (all :b :c) => ..all-result..))


(facts "queries have :all or :any as first element."
       (query? []) => false
       (query? [anything]) => false
       (query? [anything anything]) => false
       (query? [:all]) => false
       (query? [:any]) => false
       (query? anything) => false
       (query? [:all anything]) => true
       (query? [:any anything]) => true)


(facts "queries must have at least one component type or sub-query."
       (query? []) => false
       (query? [:any]) => false
       (query? [:any anything]) => true)


(future-fact "query elements must be either components or valid queries.")
