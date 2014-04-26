(ns clecs.world)


(defprotocol IWorld
  (add-entity [this])
  (component [this eid clabel])
  (process! [this])
  (query [this q])
  (remove-component [this eid clabel])
  (remove-entity [this eid])
  (set-component [this c])
  (transaction! [this f]))
