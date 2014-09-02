(ns clecs.world.check
  (:require [clecs.world :as world]
            [clecs.backend.atom-world :as atom-world]
            [clecs.world.check.generators :refer [make-gen-app]]
            [clojure.pprint :refer [pprint]]
            [clojure.string :refer [split]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))


(def ^:dynamic *context* nil)


(def default-samples 10)


(defrecord ReturnedValue [value])


(defrecord ExceptionThrown [exception])


(declare run-commands
         wrap-param-fn
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


(defn compare-worlds [actual-world-initializer control-world-initializer]
  (fn [commands]
    (let [actual (run-commands commands actual-world-initializer)
          expected (run-commands commands control-world-initializer)]
      (compare-results actual expected))))


(defn dereference-world [result w]
  (if (identical? w result)
    ::world
    result))


(defn parse-args [args]
  (assert (not (empty? args)) "You must supply a world initializer.")
  (let [initializer-str (first args)
        initializer-module (first (split initializer-str #"/"))
        initializer (do
                      (require (symbol initializer-module))
                      (find-var (symbol initializer-str)))
        samples (try
                  (Integer/parseInt (second args))
                  (catch NumberFormatException _ default-samples))]
    [initializer samples]))


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


(defn run-test [world-initializer samples]
  (let [generator (gen/fmap (compare-worlds world-initializer
                                            atom-world/make-world)
                            (make-gen-app))
        prop (prop/for-all [results generator]
                           :match?)]
    (tc/quick-check samples prop)))

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
  (let [[world-initializer samples] (parse-args args)]
    (pprint (run-test world-initializer samples))))
