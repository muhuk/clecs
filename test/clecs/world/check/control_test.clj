(ns clecs.world.check.control-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.component :refer [component-label]]
            [clecs.world :as world]
            [clecs.world.check.control :refer :all]))


(fact "Calling an undefined method throws IllegalArgumentException."
      (call-command-on-state {:method nil :params []}
                             ..state..) => (just [:exception
                                                  (partial instance? IllegalArgumentException)
                                                  ..state..]))


(fact "component can only be called during processing."
      (call-command-on-state {:method `world/component}
                             {:processing? false}) => (just [:exception
                                                             (partial instance? IllegalArgumentException)
                                                             {:processing? false}]))


(fact "component returns the component for given eid and ctype."
      (let [state {:components {..clabel.. {..eid.. ..component..}}
                   :processing? true}]
        (call-command-on-state {:method `world/component
                                :params [..eid.. ..ctype..]}
                               state) => [:result ..component.. state]
        (provided (component-label ..ctype..) => ..clabel..)))


(facts "component returns nil if there's no component for given eid and ctype."
       (let [state {:components {..clabel.. {..eid.. ..component..}}
                    :processing? true}]
         (call-command-on-state {:method `world/component
                                 :params [..eid-other.. ..ctype..]}
                                state) => [:result nil state]
         (call-command-on-state {:method `world/component
                                 :params [..eid.. ..ctype-other..]}
                                state) => [:result nil state]
         (call-command-on-state {:method `world/component
                                 :params [..eid-other.. ..ctype-other..]}
                                state) => [:result nil state])
       (against-background (component-label ..ctype..) => ..clabel..
                           (component-label ..ctype-other..) => ..clabel-other..))


(fact "query can only be called during processing."
      (call-command-on-state {:method `world/query}
                             {:processing? false}) => (just [:exception
                                                             (partial instance? IllegalArgumentException)
                                                             {:processing? false}]))


(fact "query returns a sequence of entities matching the criteria."
      (let [state {:components {..clabel-a.. {..eid-1.. ..a-1..
                                              ..eid-2.. ..a-2..}
                                ..clabel-b.. {..eid-2.. ..b-2..
                                              ..eid-3.. ..b-3..}
                                ..clabel-c.. {..eid-3.. ..c-3..}}
                   :processing? true}]
        (call-command-on-state {:method `world/query
                                :params [--q--]}
                               state) => [:result '(..eid-2..) state]
        (provided (--q-- '(..clabel-a..)) => false
                  (--q-- (as-checker (just [..clabel-a..
                                            ..clabel-b..]
                                           :in-any-order))) => true
                  (--q-- (as-checker (just [..clabel-b..
                                            ..clabel-c..]
                                           :in-any-order))) => false)))


(facts "remove-system! can only be called when not processing."
       (call-command-on-state {:method `world/remove-system!}
                              {:processing? true}) => (just [:exception
                                                             (partial instance? IllegalArgumentException)
                                                             {:processing? true}])
       (call-command-on-state {:method `world/remove-system!}
                              {:in-transaction? true}) => (just [:exception
                                                                 (partial instance? IllegalArgumentException)
                                                                 {:in-transaction? true}]))


(facts "remove-system! dissociates the system to be deleted and returns the world."
       (call-command-on-state {:method `world/remove-system!
                               :params [..system-name..]}
                              {:in-transaction? false
                               :processing? false
                               :systems {..system-name.. ..system..}}) => [:result
                                                                           :clecs.world.check/world
                                                                           {:in-transaction? false
                                                                            :processing? false
                                                                            :systems {}}]
       (call-command-on-state {:method `world/remove-system!
                               :params [..system-name..]}
                              {:in-transaction? false
                               :processing? false
                               :systems {..system-name.. ..system..
                                         ..other-system-name.. ..other-system..}}) => [:result
                                                                                       :clecs.world.check/world
                                                                                       {:in-transaction? false
                                                                                        :processing? false
                                                                                        :systems {..other-system-name.. ..other-system..}}])


(fact "remove-system! does nothing and returns the world if the system doesn't exist."
      (call-command-on-state {:method `world/remove-system!
                              :params [..system-name..]}
                             {:in-transaction? false
                              :processing? false
                              :systems {}}) => [:result
                                                :clecs.world.check/world
                                                {:in-transaction? false
                                                 :processing? false
                                                 :systems {}}])


(facts "set-system! can only be called when not processing."
       (call-command-on-state {:method `world/set-system!}
                              {:processing? true}) => (just [:exception
                                                             (partial instance? IllegalArgumentException)
                                                             {:processing? true}])
       (call-command-on-state {:method `world/set-system!}
                              {:in-transaction? true}) => (just [:exception
                                                                 (partial instance? IllegalArgumentException)
                                                                 {:in-transaction? true}]))


(facts "set-system! adds the new system."
       (call-command-on-state {:method `world/set-system!
                               :params [..system-name.. ..system..]}
                              {:in-transaction? false
                               :processing? false
                               :systems {}}) => [:result
                                                 :clecs.world.check/world
                                                 {:in-transaction? false
                                                  :processing? false
                                                  :systems {..system-name.. ..system..}}]
       (call-command-on-state {:method `world/set-system!
                               :params [..system-name.. ..system..]}
                              {:in-transaction? false
                               :processing? false
                               :systems {..other-system-name.. ..other-system..}}) => [:result
                                                                                       :clecs.world.check/world
                                                                                       {:in-transaction? false
                                                                                        :processing? false
                                                                                        :systems {..system-name.. ..system..
                                                                                                  ..other-system-name.. ..other-system..}}])


(fact "set-system! replaces an existing system."
      (call-command-on-state {:method `world/set-system!
                              :params [..system-name.. ..new-system..]}
                             {:in-transaction? false
                              :processing? false
                              :systems {..system-name.. ..system..}}) => [:result
                                                                          :clecs.world.check/world
                                                                          {:in-transaction? false
                                                                           :processing? false
                                                                           :systems {..system-name.. ..new-system..}}]
      (call-command-on-state {:method `world/set-system!
                              :params [..system-name.. ..new-system..]}
                             {:in-transaction? false
                              :processing? false
                              :systems {..system-name.. ..system..
                                        ..other-system-name.. ..other-system..}}) => [:result
                                                                                      :clecs.world.check/world
                                                                                      {:in-transaction? false
                                                                                       :processing? false
                                                                                       :systems {..system-name.. ..new-system..
                                                                                                 ..other-system-name.. ..other-system..}}])


(facts "systems can only be called when not processing."
       (call-command-on-state {:method `world/systems}
                              {:processing? true}) => (just [:exception
                                                             (partial instance? IllegalArgumentException)
                                                             {:processing? true}])
       (call-command-on-state {:method `world/systems}
                              {:in-transaction? true}) => (just [:exception
                                                                 (partial instance? IllegalArgumentException)
                                                                 {:in-transaction? true}]))


(fact "systems returns the list of system name and value pairs."
      (let [state {:in-transaction? false
                   :processing? false
                   :systems {..system-a-key.. ..system-a..
                             ..system-b-key.. ..system-b..}}]
        (call-command-on-state {:method `world/systems
                                :params []}
                               state) => [:result [[..system-a-key.. ..system-a..]
                                                   [..system-b-key.. ..system-b..]] state]))
