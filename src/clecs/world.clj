(ns clecs.world)


(defprotocol IWorld
  (add-component [this eid f] [this eid f args])
  (add-entity [this])
  (process! [this])
  (remove-component [this eid ct])
  (transaction! [this f]))
