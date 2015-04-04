(ns clecs.world-test
  (:require [clecs.component :refer [validate]]
            [clecs.test.mock :as mock]
            [clecs.world :refer :all]
            [clecs.world.validate :refer [validate-world]]
            [midje.sweet :refer :all]))



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


(fact "world validates its parameters, creates a new world and runs initializer."
      (let [initializer-called-with (atom nil)
            w (reify
                IWorld
                (-run [this _ _ f dt] (f ..editable-world.. dt) this))
            initializer (fn [w] (reset! initializer-called-with w))
            components [{:name :c1}
                        {:name :c2}
                        {:name :c3}]
            systems [{:name :s1}
                     {:name :s2}
                     {:name :s3}]
            supported-types [:c1 :c2 :c3]]
        (world mock/mock-world-factory {:components components
                                        :initializer initializer
                                        :systems systems}) => w
        (provided (mock/-supported-types mock/mock-world-factory) => supported-types
                  (validate-world components systems supported-types) => nil
                  (mock/-world mock/mock-world-factory
                               {:c1 {:name :c1}
                                :c2 {:name :c2}
                                :c3 {:name :c3}}
                               {:s1 {:name :s1}
                                :s2 {:name :s2}
                                :s3 {:name :s3}}
                               {}) => w)
        @initializer-called-with => ..editable-world..))
