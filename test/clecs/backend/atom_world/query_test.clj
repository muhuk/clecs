(ns clecs.backend.atom-world.query-test
  (:require [clecs.backend.atom-world.query :refer :all]
            [clecs.query :refer [all any query?]]
            [midje.sweet :refer :all]))


(facts "-compile-query handles and nodes."
       ((-compile-query (all ..C1.. ..C2..)) []) => falsey
       ((-compile-query (all ..C1.. ..C2..)) [..C3..]) => falsey
       ((-compile-query (all ..C1.. ..C2..)) [..C1..]) => falsey
       ((-compile-query (all ..C1.. ..C2..)) [..C2..]) => falsey
       ((-compile-query (all ..C1.. ..C2..)) [..C1.. ..C2..]) => truthy
       ((-compile-query (all ..C1.. ..C2..)) [..C2.. ..C1..]) => truthy)



(facts "-compile-query handles or nodes."
       ((-compile-query (any ..C1.. ..C2..)) []) => falsey
       ((-compile-query (any ..C1.. ..C2..)) [..C3..]) => falsey
       ((-compile-query (any ..C1.. ..C2..)) [..C1..]) => truthy
       ((-compile-query (any ..C1.. ..C2..)) [..C2..]) => truthy
       ((-compile-query (any ..C1.. ..C2..)) [..C1.. ..C2..]) => truthy
       ((-compile-query (any ..C1.. ..C2..)) [..C2.. ..C1..]) => truthy)


(facts "-compile-query handles sub-queries."
       (let [and-q (-compile-query (all ..C1.. (any ..C2.. ..C3..)))]
         (and-q [..C1..]) => falsey
         (and-q [..C2..]) => falsey
         (and-q [..C3..]) => falsey
         (and-q [..C1.. ..C2..]) => truthy
         (and-q [..C1.. ..C3..]) => truthy
         (and-q [..C2.. ..C3..]) => falsey
         (and-q [..C1.. ..C2.. ..C3..]) => truthy)
       (let [or-q (-compile-query (any ..C1.. (all ..C2.. ..C3..)))]
         (or-q [..C1..]) => truthy
         (or-q [..C2..]) => falsey
         (or-q [..C3..]) => falsey
         (or-q [..C1.. ..C2..]) => truthy
         (or-q [..C1.. ..C3..]) => truthy
         (or-q [..C2.. ..C3..]) => truthy
         (or-q [..C1.. ..C2.. ..C3..]) => truthy))


(facts "-compile-query validates its input."
       (-compile-query ..q..) => (throws IllegalArgumentException)
       (provided (query? ..q..) => false))
