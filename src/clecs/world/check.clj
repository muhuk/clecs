(ns clecs.world.check
  (:require [clecs.world :as world]
            [clecs.backend.atom-world :as atom-world]
            [clecs.world.check.generators :refer [make-gen-app]]
            [clojure.pprint :refer [pprint]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))


(def ^:dynamic *context* nil)


(defrecord ReturnedValue [value])


(defrecord ExceptionThrown [exception])


(declare wrap-param-fn
         wrap-params)


(defn apply-command [method w params]
  (apply (find-var method) (cons w params)))


(defn compare-results [actual expected]
  (if (= actual expected)
    {:match? true
     :results actual}
    (let [pairs (map vector actual expected)]
      (loop [remaining-pairs pairs
             merged []]
        (let [[pair & rest-of-pairs] remaining-pairs
              [actual expected] pair]
          (if (= actual expected)
            (if (empty? remaining-pairs)
              (throw (RuntimeException.))
              (recur rest-of-pairs (conj merged actual)))
            (if (= (first actual) (first expected))
              {:match? false
               :results (conj merged {:command (first actual)
                                      :actual (second actual)
                                      :expected (second expected)})}
              (throw (IllegalArgumentException. "Commands don't match.")))))))))


(defn dereference-world [result w]
  (if (identical? w result)
    ::world
    result))


(defn report-results [context results]
  (reset! context results)
  results)


(defn run-command
  ([w command]
   (run-command w command (atom [])))
  ([w {:keys [method params] :as command} context]
   (assert (= (:type command) :command))
   (binding [*context* context]
     (let [result (try
                    (-> method
                        (apply-command w (wrap-params params))
                        (dereference-world w)
                        (->ReturnedValue))
                    (catch Throwable e (->ExceptionThrown e)))]
       {:command command
        :result result
        :sub-results @*context*}))))


(defn run-commands [{:keys [commands initializer]} world-initializer]
  (let [w (world-initializer initializer)]
    (doall (map (partial run-command w) commands))))


(defn wrap-param [param]
  (case (:type param)
    :system (wrap-param-fn param)
    :transaction (wrap-param-fn param)
    :value (:value param)))


(defn- wrap-param-fn [{:keys [commands]}]
  (fn [w & _]
    (let [results (doall (map (partial run-command w) commands))]
      (report-results *context* results))))


(defn wrap-params [params]
  (map wrap-param params))


(defn -main
  "Entry point."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (pprint (tc/quick-check 10
                          (prop/for-all [results (gen/fmap (fn [commands]
                                                             (let [actual (run-commands commands atom-world/make-world)
                                                                   expected (run-commands commands atom-world/make-world)]
                                                               (compare-results actual expected)))
                                                           (make-gen-app))]
                                        :match?))))
