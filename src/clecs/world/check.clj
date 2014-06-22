(ns clecs.world.check
  (:require [clecs.world :as world]
            [clecs.backend.atom-world :as atom-world]
            [clecs.world.check.control :refer [call-command-on-state
                                               initial-state]]
            [clecs.world.check.generators :refer [gen-commands]]
            [clojure.pprint :refer [pprint]]
            [clojure.test :refer [run-tests]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))


(defn call-command-on-world [command w]
  (let [{:keys [method params]} command]
    (try
      (let [result (apply (find-var method) (cons w params))
            result (if (= result w) ::world result)]
        [:result result])
      (catch Throwable e [:exception e]))))


(defn compare-commands [commands w initial-state]
  (loop [cmds commands
         results []
         state initial-state]
    (if (empty? cmds)
      {:match? true
       :results results
       :state state}
      (let [[cmd & rest-of-commands] cmds
            actual-result (call-command-on-world cmd w)
            [expected-result-type expected-value new-state] (call-command-on-state cmd state)
            expected-result [expected-result-type expected-value]]
        (if (= actual-result expected-result)
          (recur rest-of-commands
                 (conj results [cmd actual-result])
                 new-state)
          {:match? false
           :results (conj results
                          [cmd {:actual actual-result :expected expected-result}])
           :state state})))))


(defn make-prop [world-initializer]
  (let [g (fn [commands] (let [initializer-fn (fn [w] (comment TODO))
                               world (world-initializer initializer-fn)]
                           (compare-commands commands world initial-state)))]
    (prop/for-all [result (gen/fmap g
                                    gen-commands)]
                  (:match? result))))

(defn -main
  "Entry point."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (let [no-of-iterations 10
        result (tc/quick-check no-of-iterations
                               (make-prop atom-world/make-world))]
    (pprint result)))
