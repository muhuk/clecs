(ns clecs.world.validate-test
  (:require [clecs.component :refer [component] :rename {component c}]
            [clecs.system :refer [system]]
            [clecs.world.validate :refer :all]
            [midje.sweet :refer :all]))



(fact "validate-world validates components then systems."
      (validate-world ..components.. ..systems..) => nil
      (provided (-validate-components ..components..) => nil
                (-validate-systems ..components.. ..systems..) => nil))


(fact "-validate-components fails if there are no components."
      (-validate-components []) => (throws RuntimeException
                                          "You must provide at least one component."))


(fact "-validate-systems fails if there are no systems."
      (-validate-systems [..component..] []) => (throws RuntimeException
                                                       "You must provide at least one system."))


(fact "-validate-systems rejects components no system is using."
      (-validate-systems [(c :Foo nil)
                         (c :Bar nil)]
                        [(system {:name :FooSystem
                                  :process-fn identity
                                  :reads #{:Foo}})]) => (throws RuntimeException
                                                                #"These components are not used by any system: "
                                                                #":Bar"))


(fact "-validate-systems rejects systems associated with unknown components."
      (-validate-systems [(c :Foo nil)]
                        [(system {:name :FooSystem
                                  :process-fn identity
                                  :reads #{:Foo}})
                         (system {:name :BarSystem
                                  :process-fn identity
                                  :reads #{:Bar}})]) => (throws RuntimeException
                                                                #":BarSystem"
                                                                #"is using unknown components"
                                                                #":Bar"))
