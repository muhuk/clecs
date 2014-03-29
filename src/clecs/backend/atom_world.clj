(ns clecs.backend.atom-world
  (:require [clecs.world :refer [IWorld]]))


(def ^:const EMPTY_WORLD {:entities {:last-index 0}})


(declare add-entity last-entity-id process!)


(deftype AtomWorld [state]
  IWorld
  (add-entity! [this]
               (swap! state add-entity)
               (last-entity-id @state))
  (process! [this] (process! this) nil))


(defn add-entity [state]
  (let [eid (inc (get-in state [:entities :last-index]))]
    (-> state
        (assoc-in [:entities eid] #{})
        (assoc-in [:entities :last-index] eid))))


(defn last-entity-id [state]
  (get-in state [:entities :last-index]))


(defn make-world
  ([] (make-world EMPTY_WORLD))
  ([state] (->AtomWorld (atom state))))


(defn process! [world]
  (throw (UnsupportedOperationException.)))
