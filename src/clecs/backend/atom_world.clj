(ns clecs.backend.atom-world
  (:require [clecs.world :refer [IWorld]]))


(deftype AtomWorld []
  IWorld
  (process! [this] (process! this)))


(defn make-world []
  (->AtomWorld))


(defn process! [world]
  (throw (UnsupportedOperationException.)))
