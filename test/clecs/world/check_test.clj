(ns clecs.world.check-test
  (:require [clecs.world.check :refer :all]
            [clojure.test :refer :all]
            [midje.sweet :refer :all]))


;; compare-results

(fact "Comparing two sets of matching results returns a matching output."
      (compare-results ..results..
                       ..results..) => {:match? true
                                        :results ..results..})


(fact "Comparing two different results returns a non-matching output."
      (compare-results [[..command..
                         ..actual-result..]]
                       [[..command..
                         ..expected-result..]]) => {:match? false
                                                    :results [{:command ..command..
                                                               :actual ..actual-result..
                                                               :expected ..expected-result..}]})


(fact "Comparing two different results returns a non-matching output after the difference."
      (compare-results [..same-result..
                        [..command.. ..actual-result..]
                        ..different-result..]
                       [..same-result..
                        [..command.. ..expected-result..]
                        ..different-result..]) => {:match? false
                                                   :results [..same-result..
                                                             {:command ..command..
                                                              :actual ..actual-result..
                                                              :expected ..expected-result..}]})


(fact "compare-results throws exception if the commands in the result doesn't match."
      (compare-results [[..command..
                         ..same-result..]]
                       [[..other-command..
                         ..same-result..]]) => (throws IllegalArgumentException))



;; compare-worlds

(fact "compare-worlds returns a function."
      (compare-worlds ..world-initialier..
                      ..world-initializer-control..) => fn?)


(fact "compare-worlds' result is called with a list of commands."
      ((compare-worlds ..init-actual.. ..init-expected..) ..commands..) => ..results..
      (provided (run-commands ..commands.. ..init-actual..) => ..results-actual..
                (run-commands ..commands.. ..init-expected..) => ..results-expected..
                (compare-results ..results-actual.. ..results-expected..) => ..results..))



;; dereference-world

(fact "dereference-world returns ::world if the result is the world."
      (dereference-world ..world.. ..world..) => :clecs.world.check/world)


(fact "dereference-world returns the result if the it's not the world."
      (dereference-world ..result.. ..world..) => ..result..)



;; report-results

(fact "report-results"
      (let [context (atom ..initial-value..)]
        (report-results context ..results..) => ..results..
        @context => ..results..))



;; run-command

(fact "running a command with sub-commands collects sub-results."
      (let [command {:method ..method..
                     :params ..params..
                     :type :command}
            context (atom ..sub-results..)]
        (run-command ..world..
                     command
                     context) => {:command command
                                  :result (->ReturnedValue ..dereferenced-result..)
                                  :sub-results ..sub-results..}
        (provided (wrap-params ..params..) => ..wrapped-params..
                  (dereference-world ..result.. ..world..) => ..dereferenced-result..
                  (apply-command ..method..
                                 ..world..
                                 ..wrapped-params..) => ..result..)))


(fact "Exceptions thrown from commands are caught."
      (let [command {:method ..method..
                     :params ..params..
                     :type :command}
            context (atom ..sub-results..)
            exception (Throwable.)]
        (run-command ..world..
                     command
                     context) => {:command command
                                  :result (->ExceptionThrown exception)
                                  :sub-results ..sub-results..}
        (provided (wrap-params ..params..) => ..wrapped-params..
                  (apply-command ..method..
                                 ..world..
                                 ..wrapped-params..) =throws=> exception)))



;; run-commands

(fact "run-commands calls world initializer and then calls each command on it."
      (run-commands {:initializer ..initializer..
                     :commands [..a.. ..b.. ..c..]}
                    --world-initializer--) => [..result-a..
                                               ..result-b..
                                               ..result-c..]
      (provided (--world-initializer-- ..initializer..) => ..world..
                (run-command ..world.. ..a..) => ..result-a..
                (run-command ..world.. ..b..) => ..result-b..
                (run-command ..world.. ..c..) => ..result-c..))



;; wrap-param

(fact "values are returned as is."
      (wrap-param {:type :value, :value ..value..}) => ..value..)


(fact "wrapping a transaction returns a function."
      (wrap-param {:type :transaction}) => fn?)


(fact "wrapped system is called with a world and it runs its commands."
      (binding [*context* ..context..]
        ((wrap-param {:type :transaction
                      :commands [..command-a.. ..command-b..]}) ..world..) => anything
        (provided (run-command ..world.. ..command-a..) => ..result-a..
                  (run-command ..world.. ..command-b..) => ..result-b..
                  (report-results ..context.. [..result-a.. ..result-b..]) => anything)))


(fact "wraping a system returns a function."
      (wrap-param {:type :system}) => fn?)


(fact "wrapped system is called with a world and dt, then it runs its commands."
      (binding [*context* ..context..]
        ((wrap-param {:type :system
                      :commands [..command-a.. ..command-b..]}) ..world.. ..dt..) => anything
        (provided (run-command ..world.. ..command-a..) => ..result-a..
                  (run-command ..world.. ..command-b..) => ..result-b..
                  (report-results ..context.. [..result-a.. ..result-b..]) => anything)))
