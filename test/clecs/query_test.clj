(ns clecs.query-test
  (:require [clecs.query :refer :all]
            [midje.sweet :refer :all]))


(facts "all returns a vector with :all as its first parameter."
       (all :c) => (has-prefix [:clecs.query/all])
       (all :a :b :c) => (has-prefix [:clecs.query/all])
       (all :c1 :c2) => [:clecs.query/all :c1 :c2])


(fact "all inlines other all calls."
      (all :a (all :b :c) :d) => [:clecs.query/all :a :b :c :d])


(fact "all doesn't modify any calls."
      (all :a (any :b :c) :d) => [:clecs.query/all :a ..any-result.. :d]
      (provided (any :b :c) => ..any-result..))


(facts "any returns a vector with :any as its first parameter."
       (any :c) => (has-prefix [:clecs.query/any])
       (any :a :b :c) => (has-prefix [:clecs.query/any])
       (any :a :b) => [:clecs.query/any :a :b])


(fact "any inlines other any calls."
      (any :a (any :b :c) :d) => [:clecs.query/any :a :b :c :d])


(fact "any doesn't modify all calls."
      (any :a (all :b :c) :d) => [:clecs.query/any :a ..all-result.. :d]
      (provided (all :b :c) => ..all-result..))


(facts "queries have :all or :any as first element."
       (query? []) => false
       (query? [anything]) => false
       (query? [anything anything]) => false
       (query? [:clecs.query/all]) => false
       (query? [:clecs.query/any]) => false
       (query? anything) => false
       (query? [:clecs.query/all anything]) => true
       (query? [:clecs.query/any anything]) => true)


(facts "queries must have at least one component type or sub-query."
       (query? []) => false
       (query? [:clecs.query/any]) => false
       (query? [:clecs.query/any anything]) => true)


(future-fact "query elements must be either components or valid queries.")
