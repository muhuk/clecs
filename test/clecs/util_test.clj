(ns clecs.util-test
  (:require [clecs.util :refer :all]
            [midje.sweet :refer :all]))



(facts "About map-values"
       (let [coll {:a 2 :b 3 :c 5}]
         (fact "Map values with nil."
               (map-values ..f.. nil) => nil)

         (fact "Map values with an empty collection."
               (map-values ..f.. {}) => {})


         (fact "Map values using identity."
               (map-values identity coll) => coll)


         (fact "Map values using custom function."
               (map-values (fn [i] (* i i)) coll) => {:a 4 :b 9 :c 25})))
