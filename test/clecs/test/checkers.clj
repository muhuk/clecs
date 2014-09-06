(ns clecs.test.checkers
  (:require [midje.sweet :refer :all]))


(defn implements-protocols [& protocols]
  (checker [obj]
    (let [t (type obj)]
      (every? #(extends? % t) protocols))))
