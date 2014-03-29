(ns clecs.backend.atom-world-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world :refer :all]
            [clecs.world :as world]))


(fact "INCOMPLETE: atom world creation"
      (make-world) => anything)


(fact "world/process! delegates to process!"
      (let [world (make-world)]
        (world/process! world) => ..result..
        (provided (process! world) => ..result..)))
