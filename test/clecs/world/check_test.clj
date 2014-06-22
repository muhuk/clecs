(ns clecs.world.check-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clecs.world :as world]
            [clecs.world.check :refer :all]
            [clecs.world.check.control :refer [call-command-on-state]]))


;; Midje chokes on apply as a prerequisite.
;; See: https://github.com/marick/Midje/issues/162

;; (fact "call-command-on-world returns the result on a successful call."
;;       (call-command-on-world {:method ..method..
;;                               :params [..a.. ..b..]} ..world..) => [:result ..result..]
;;       (provided (find-var ..method..) => --method--
;;                 (apply --method-- '(..world.. ..a.. ..b..)) => ..result..))


;; (fact "call-command-on-world returns a special keyword when result is the world."
;;       (call-command-on-world {:method ..method..
;;                               :params [..a.. ..b..]} ..world..) => [:result :clecs.world.check/world]
;;       (provided (find-var ..method..) => --method--
;;                 (apply --method-- '(..world.. ..a.. ..b..)) => ..world..))


;; (fact "call-command-on-world returns the exception on a failed call."
;;       (let [e (RuntimeException.)]
;;         (call-command-on-world {:method ..method..
;;                                 :params [..a.. ..b..]} ..world..) => [:exception e]
;;         (provided  (find-var ..method..) => --method--
;;                    (apply --method-- '(..world.. ..a.. ..b..)) =throws=> e)))


(fact "comparing an empty list of commands."
      (compare-commands []
                        ..world..
                        ..initial-state..) => {:match? true
                                               :results []
                                               :state ..initial-state..})


(fact "comparing a single command that matches."
      (compare-commands [..cmd..]
                        ..world..
                        ..initial-state..) => {:match? true
                                               :results [[..cmd.. [..result-type.. ..result..]]]
                                               :state ..new-state..}
      (provided (call-command-on-world ..cmd.. ..world..) => [..result-type.. ..result..]
                (call-command-on-state ..cmd.. ..initial-state..) => [..result-type..
                                                                      ..result..
                                                                      ..new-state..]))


(fact "comparing a single command that doesn't match."
      (compare-commands [..cmd..]
                        ..world..
                        ..initial-state..) => {:match? false
                                               :results [[..cmd..
                                                          {:actual [..actual-result-type.. ..actual-result..]
                                                           :expected [..expected-result-type.. ..expected-result..]}]]
                                               :state ..initial-state..}
      (provided (call-command-on-world ..cmd.. ..world..) => [..actual-result-type.. ..actual-result..]
                (call-command-on-state ..cmd.. ..initial-state..) => [..expected-result-type..
                                                                      ..expected-result..
                                                                      ..new-state..]))


(fact "comparing multiple commands that all match."
      (compare-commands [..cmd-a.. ..cmd-b.. ..cmd-c..]
                        ..world..
                        ..initial-state..) => {:match? true
                                               :results [[..cmd-a.. [..result-type-a.. ..result-a..]]
                                                         [..cmd-b.. [..result-type-b.. ..result-b..]]
                                                         [..cmd-c.. [..result-type-c.. ..result-c..]]]
                                               :state ..state-c..}
      (provided (call-command-on-world ..cmd-a.. ..world..) => [..result-type-a.. ..result-a..]
                (call-command-on-state ..cmd-a.. ..initial-state..) => [..result-type-a..
                                                                        ..result-a..
                                                                        ..state-a..]
                (call-command-on-world ..cmd-b.. ..world..) => [..result-type-b.. ..result-b..]
                (call-command-on-state ..cmd-b.. ..state-a..) => [..result-type-b..
                                                                  ..result-b..
                                                                  ..state-b..]
                (call-command-on-world ..cmd-c.. ..world..) => [..result-type-c.. ..result-c..]
                (call-command-on-state ..cmd-c.. ..state-b..) => [..result-type-c..
                                                                  ..result-c..
                                                                  ..state-c..]))


(fact "comparing multiple commands that don't match."
      (compare-commands [..cmd-a.. ..cmd-b.. ..cmd-c..]
                        ..world..
                        ..initial-state..) => {:match? false
                                               :results [[..cmd-a.. [..result-type-a.. ..result-a..]]
                                                         [..cmd-b.. {:actual [..result-type-b1.. ..result-b1..]
                                                                     :expected [..result-type-b2.. ..result-b2..]}]]
                                               :state ..state-a..}
      (provided (call-command-on-world ..cmd-a.. ..world..) => [..result-type-a.. ..result-a..]
                (call-command-on-state ..cmd-a.. ..initial-state..) => [..result-type-a..
                                                                        ..result-a..
                                                                        ..state-a..]
                (call-command-on-world ..cmd-b.. ..world..) => [..result-type-b1.. ..result-b1..]
                (call-command-on-state ..cmd-b.. ..state-a..) => [..result-type-b2..
                                                                  ..result-b2..
                                                                  ..state-b..]))
