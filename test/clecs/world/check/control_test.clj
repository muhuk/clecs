(ns clecs.world.check.control-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.world :as world]
            [clecs.world.check.control :refer :all]))


(facts "remove-system! dissociates the system to be deleted and returns the world."
      (call-command-on-state {:method `world/remove-system!
                              :params [..system-name..]}
                             {:systems {..system-name.. ..system..}}) => [:result
                                                                          :clecs.world.check/world
                                                                          {:systems {}}]
      (call-command-on-state {:method `world/remove-system!
                              :params [..system-name..]}
                             {:systems {..system-name.. ..system..
                                        ..other-system-name.. ..other-system..}}) => [:result
                                                                                      :clecs.world.check/world
                                                                                      {:systems {..other-system-name.. ..other-system..}}])


(fact "remove-system! does nothing and returns the world if the system doesn't exist."
      (call-command-on-state {:method `world/remove-system!
                              :params [..system-name..]}
                             {:systems {}}) => [:result
                                                :clecs.world.check/world
                                                {:systems {}}])


(facts "set-system! adds the new system."
       (call-command-on-state {:method `world/set-system!
                               :params [..system-name.. ..system..]}
                              {:systems {}}) => [:result
                                                 :clecs.world.check/world
                                                 {:systems {..system-name.. ..system..}}]
       (call-command-on-state {:method `world/set-system!
                               :params [..system-name.. ..system..]}
                              {:systems {..other-system-name.. ..other-system..}}) => [:result
                                                                                       :clecs.world.check/world
                                                                                       {:systems {..system-name.. ..system..
                                                                                                  ..other-system-name.. ..other-system..}}])


(fact "set-system! replaces an existing system."
      (call-command-on-state {:method `world/set-system!
                              :params [..system-name.. ..new-system..]}
                             {:systems {..system-name.. ..system..}}) => [:result
                                                                      :clecs.world.check/world
                                                                      {:systems {..system-name.. ..new-system..}}]
      (call-command-on-state {:method `world/set-system!
                              :params [..system-name.. ..new-system..]}
                             {:systems {..system-name.. ..system..
                                        ..other-system-name.. ..other-system..}}) => [:result
                                                                                      :clecs.world.check/world
                                                                                      {:systems {..system-name.. ..new-system..
                                                                                                 ..other-system-name.. ..other-system..}}])


(fact "systems returns the list of system name and value pairs."
      (let [state {:systems {..system-a-key.. ..system-a..
                             ..system-b-key.. ..system-b..}}]
        (call-command-on-state {:method `world/systems
                                :params []}
                               state) => [:result [[..system-a-key.. ..system-a..]
                                                   [..system-b-key.. ..system-b..]] state]))
