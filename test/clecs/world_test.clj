(ns clecs.world-test
  (:require [clecs.world :refer :all]
            [clecs.component :refer [component validate] :rename {component c}]
            [clecs.system :refer [system]]
            [clecs.test.checkers :refer :all]
            [midje.sweet :refer :all]
            [clecs.test.mock :as mock]))


(fact "-validate-world throws exception if components or systems are empty."
      (-validate-world [] [..system..]) => (throws RuntimeException
                                                   "You must provide at least one component.")
      (-validate-world [..component..] []) => (throws RuntimeException
                                                      "You must provide at least one system."))


(fact "-validate-world rejects components no system is using."
      (-validate-world [(c :Foo nil)
                        (c :Bar nil)]
                       [(system {:name :FooSystem
                                 :process-fn identity
                                 :reads #{:Foo}})]) => (throws RuntimeException
                                                               #"These components are not used by any system: "
                                                               #":Bar"))


(future-fact "-validate-world rejects systems associated with unknown components.")


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
                (-run [this f dt] (f ..editable-world.. dt) this))
            initializer (fn [w] (reset! initializer-called-with w))]
        (world mock/mock-world-factory {:components ..components..
                                        :initializer initializer
                                        :systems ..systems..}) => w
        (provided (-validate-world ..components.. ..systems..) => nil
                  (mock/-world mock/mock-world-factory {:components ..components..
                                                        :systems ..systems..}) => w)
        @initializer-called-with => ..editable-world..))
