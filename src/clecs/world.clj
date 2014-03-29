(ns clecs.world)


(defprotocol IWorld
  (add-entity! [this])
  (process! [this]))
