(ns clecs.query-test
  (:require [clecs.query :refer :all]
            [midje.sweet :refer :all])
  (:import [clecs.query All Any Query]))


(facts "Query primitives return an IQuery."
       (all :a) => (partial satisfies? IQueryNode)
       (any :a) => (partial satisfies? IQueryNode)
       (all :a :b :c) => (partial satisfies? IQueryNode)
       (any :a :b :c) => (partial satisfies? IQueryNode))


(fact "all inlines other `all` children."
      (all :a (all :b :c) :d) => (just (Query. (All. #{:a :b :c :d}) irrelevant)))


(fact "all doesn't inline `any` children."
      (all :a (any :b :c) :d) => (just (Query. (All. #{:a
                                                        (Any. #{:b :c})
                                                        :d})
                                                irrelevant)))


(fact "any inlines other `any` children."
      (any :a (any :b :c) :d) => (just (Query. (Any. #{:a :b :c :d}) irrelevant)))


(fact "any doesn't inline `all` children."
      (any :a (all :b :c) :d) => (just (Query. (Any. #{:a
                                                       (All. #{:b :c})
                                                       :d})
                                               irrelevant)))


(fact "Multiple levels of nesting is allowed."
      (any :a
           (all :b
                (any :c
                     (all :d
                          (any :e :f))))) =>
      (just (Query. (Any. #{:a
                            (All. #{:b
                                    (Any. #{:c
                                            (All. #{:d
                                                    (Any. #{:e :f})})})})})
                    irrelevant)))


(facts "Queries can be build from sub-queries."
       (let [sub-query-one (all :x :y)
             sub-query-two (all :u :v :w)]
         (any sub-query-one sub-query-two) => (just (Query. (Any. #{(All. #{:x :y})
                                                                    (All. #{:u :v :w})})
                                                            irrelevant)))
       (let [sub-query-one (any :x :y)
             sub-query-two (any :u :v :w)]
         (all sub-query-one sub-query-two) => (just (Query. (All. #{(Any. #{:x :y})
                                                                    (Any. #{:u :v :w})})
                                                            irrelevant))))


(facts "Single element sub-queries are eliminated."
       (all :a) => (just (Query. (All. #{:a}) irrelevant))
       (any :a) => (just (Query. (Any. #{:a}) irrelevant))
       (all :a (any :b) :c) => (just (Query. (All. #{:a :b :c}) irrelevant))
       (any :a (all :b) :c) => (just (Query. (Any. #{:a :b :c}) irrelevant)))


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


(facts "IQueryNode/accesses returns all components in the query tree."
       (accesses (all :a :b)) => #{:a :b}
       (accesses (any :a :b)) => #{:a :b}
       (accesses (all :a (any :b :c))) => #{:a :b :c}
       (accesses (any :a (all :b :c))) => #{:a :b :c})
