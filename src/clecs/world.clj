(ns clecs.world)


(defprotocol IWorld
  (add-entity [this])
  (component [this eid ct])
  (process! [this])
  (query [this q])
  (remove-component [this eid ct])
  (remove-entity [this eid])
  (set-component [this c])
  (transaction! [this f]))
