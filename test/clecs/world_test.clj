(ns clecs.world-test
  (:require [clecs.world :refer :all]
            [clecs.component :refer [validate]]
            [clecs.test.checkers :refer :all]
            [midje.sweet :refer :all]
            [clecs.test.mock :as mock]))


(fact "set-component validates component names."
      (let [w (mock/mock-editable-world)]
        (set-component w ..eid.. ..cname.. ..cdata..) => (throws RuntimeException
                                                                 #"Unknown")
        (provided (mock/-component w ..cname..) => nil)))


(fact "set-component validates component data."
      (let [w (mock/mock-editable-world)]
        (set-component w ..eid.. ..cname.. ..cdata..) => nil
        (provided (mock/-component w ..cname..) => ..c..
                  (validate ..c.. ..cdata..) => nil
                  (mock/-set-component w ..eid.. ..cname.. ..cdata..) => anything)))


(fact "world creates a new world and runs initializer."
      (let [initializer-called-with (atom nil)
            w (reify
                IWorld
                (-run [this f dt] (f ..editable-world.. dt) this))
            initializer (fn [w] (reset! initializer-called-with w))]
        (world mock/mock-world-factory {:initializer initializer}) => w
        (provided (mock/-world mock/mock-world-factory {}) => w)
        @initializer-called-with => ..editable-world..))
