(ns clecs.world.validate-test
  (:require [clecs.component :refer [component] :rename {component c}]
            [clecs.system :refer [system]]
            [clecs.world.validate :refer :all]
            [midje.sweet :refer :all]))


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


(fact "-validate-world rejects systems associated with unknown components."
      (-validate-world [(c :Foo nil)]
                       [(system {:name :FooSystem
                                 :process-fn identity
                                 :reads #{:Foo}})
                        (system {:name :BarSystem
                                 :process-fn identity
                                 :reads #{:Bar}})]) => (throws RuntimeException
                                                               #":BarSystem"
                                                               #"is using unknown components"
                                                               #":Bar"))
