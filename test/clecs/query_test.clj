(ns clecs.query-test
  (:require [clecs.query :refer :all]
            [midje.sweet :refer :all]))


(facts "Query primitives return an IQuery."
       (all :a) => (partial satisfies? IQueryNode)
       (any :a) => (partial satisfies? IQueryNode)
       (all :a :b :c) => (partial satisfies? IQueryNode)
       (any :a :b :c) => (partial satisfies? IQueryNode))


(fact "all inlines other `all` children."
      (all :a (all :b :c) :d) => (->Query (->All #{:a :b :c :d})))


(fact "all doesn't inline `any` children."
             (all :a (any :b :c) :d) => (->Query (->All #{:a
                                                          (->Any #{:b :c})
                                                          :d})))


(fact "any inlines other `any` children."
      (any :a (any :b :c) :d) => (->Query (->Any #{:a :b :c :d})))


(fact "any doesn't inline `all` children."
      (any :a (all :b :c) :d) => (->Query (->Any #{:a
                                                   (->All #{:b :c})
                                                   :d})))


(fact "Multiple levels of nesting is allowed."
      (any :a
           (all :b
                (any :c
                     (all :d
                          (any :e))))) =>
      (->Query (->Any #{:a
                        (->All #{:b
                                 (->Any #{:c
                                          (->All #{:d
                                                   (->Any #{:e})})})})})))


(facts "Queries can be build from sub-queries."
       (let [sub-query-one (all :x :y)
             sub-query-two (all :u :v :w)]
         (any sub-query-one sub-query-two) => (->Query (->Any #{(->All #{:x :y})
                                                                (->All #{:u :v :w})})))
       (let [sub-query-one (any :x :y)
             sub-query-two (any :u :v :w)]
         (all sub-query-one sub-query-two) => (->Query (->All #{(->Any #{:x :y})
                                                                (->Any #{:u :v :w})}))))


(fact "A query must have at least one criteria."
      (all) => (throws IllegalArgumentException)
      (any) => (throws IllegalArgumentException))


(facts "Queries are callable, checking if given components satisfy them."
       (let [q (all :a :b)]
         (q nil) => falsey
         (q [:a]) => falsey
         (q [:b]) => falsey
         (q [:c]) => falsey
         (q [:a :c]) => falsey
         (q [:b :c]) => falsey
         (q [:a :b]) => truthy
         (q [:a :b :c]) => truthy)
       (let [q (any :a :b)]
         (q nil) => falsey
         (q [:a]) => truthy
         (q [:b]) => truthy
         (q [:c]) => falsey
         (q [:a :c]) => truthy
         (q [:b :c]) => truthy
         (q [:a :b]) => truthy
         (q [:a :b :c]) => truthy)
       (let [q (all :a (any :b :c))]
         (q nil) => falsey
         (q [:a]) => falsey
         (q [:b]) => falsey
         (q [:c]) => falsey
         (q [:a :c]) => truthy
         (q [:b :c]) => falsey
         (q [:a :b]) => truthy
         (q [:a :b :c]) => truthy
         (q [:a :b :c :d]) => truthy)
       (let [q (any :a (all :b :c))]
         (q nil) => falsey
         (q [:a]) => truthy
         (q [:b]) => falsey
         (q [:c]) => falsey
         (q [:a :c]) => truthy
         (q [:b :c]) => truthy
         (q [:a :b]) => truthy
         (q [:a :b :c]) => truthy
         (q [:a :b :c :d]) => truthy))
