(ns clecs.query-test
  (:require [clecs.component :refer [component-label
                                     component-type?]]
            [clecs.query :refer :all]
            [midje.sweet :refer :all]))


(facts "all returns a vector with :all as its first parameter."
       (all ..A..) => (has-prefix [:all])
       (all ..A.. ..B.. ..C..) => (has-prefix [:all]))


(fact "all converts component types to component labels."
      (all ..ctype-1.. ..ctype-2..) => [:all ..clabel-1.. ..clabel-2..]
      (provided (component-label ..ctype-1..) => ..clabel-1..
                (component-label ..ctype-2..) => ..clabel-2..
                (component-type? (as-checker anything)) => true))


(fact "all inlines other all calls."
      (all ..A.. (all ..B.. ..C..) ..D..) => [:all ..a.. ..b.. ..c.. ..d..]
      (provided (component-label ..A..) => ..a..
                (component-label ..B..) => ..b..
                (component-label ..C..) => ..c..
                (component-label ..D..) => ..d..
                (component-type? ..A..) => true
                (component-type? ..C..) => true
                (component-type? ..B..) => true
                (component-type? ..D..) => true
                (component-type? [:all ..b.. ..c..]) => false))


(fact "all doesn't modify any calls."
      (all ..A.. (any ..B.. ..C..) ..D..) => [:all ..a.. ..any-result.. ..d..]
      (provided (component-label ..A..) => ..a..
                (component-label ..D..) => ..d..
                (component-type? ..A..) => true
                (component-type? ..D..) => true
                (component-type? ..any-result..) => false
                (any ..B.. ..C..) => ..any-result..))


(facts "any returns a vector with :any as its first parameter."
       (any ..A..) => (has-prefix [:any])
       (any ..A.. ..B.. ..C..) => (has-prefix [:any]))


(fact "any converts component types to component labels."
      (any ..ctype-1.. ..ctype-2..) => [:any ..clabel-1.. ..clabel-2..]
      (provided (component-label ..ctype-1..) => ..clabel-1..
                (component-label ..ctype-2..) => ..clabel-2..
                (component-type? (as-checker anything)) => true))


(fact "any inlines other any calls."
      (any ..A.. (any ..B.. ..C..) ..D..) => [:any ..a.. ..b.. ..c.. ..d..]
      (provided (component-label ..A..) => ..a..
                (component-label ..B..) => ..b..
                (component-label ..C..) => ..c..
                (component-label ..D..) => ..d..
                (component-type? ..A..) => true
                (component-type? ..C..) => true
                (component-type? ..B..) => true
                (component-type? ..D..) => true
                (component-type? [:any ..b.. ..c..]) => false))


(fact "any doesn't modify all calls."
      (any ..A.. (all ..B.. ..C..) ..D..) => [:any ..a.. ..all-result.. ..d..]
      (provided (component-label ..A..) => ..a..
                (component-label ..D..) => ..d..
                (component-type? ..A..) => true
                (component-type? ..D..) => true
                (component-type? ..all-result..) => false
                (all ..B.. ..C..) => ..all-result..))


(facts "queries have :and or :or as first element."
       (query? []) => false
       (query? [anything]) => false
       (query? [anything anything]) => false
       (query? [:all]) => false
       (query? [:any]) => false
       (query? anything) => false
       (query? [:all anything]) => true
       (query? [:any anything]) => true)


(facts "queries must have at least one component label or sub-query."
       (query? []) => false
       (query? [:any]) => false
       (query? [:any anything]) => true)


(future-fact "query elements must be either components or valid queries.")
