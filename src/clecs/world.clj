(ns clecs.world)


(defprotocol IWorld
  (process! [this]))
