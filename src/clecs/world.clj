(ns clecs.world)


(defprotocol IWorld
  (add-component [this eid f] [this eid f args])
  (add-entity [this])
  (component [this eid ct])
  (process! [this])
  (query [this q])
  (remove-component [this eid ct])
  (remove-entity [this eid])
  (transaction! [this f]))
