(ns clecs.world.check.control
  (:require [clecs.world :as world]))


(def initial-state {:components {}
                    :systems {}})


(defmulti call-command-on-state (fn [command _] (:method command)))


(defmethod call-command-on-state
  `world/remove-system!
  [{[system-name] :params} state]
  [:result :clecs.world.check/world (update-in state [:systems] dissoc system-name)])


(defmethod call-command-on-state
  `world/set-system!
  [{[system-name system] :params} state]
  [:result :clecs.world.check/world (update-in state [:systems] assoc system-name system)])


(defmethod call-command-on-state
  `world/systems
  [_ state]
  [:result (seq (:systems state)) state])
