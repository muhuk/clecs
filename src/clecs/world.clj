(ns clecs.world)


(defprotocol IQueryable
  (component [this eid clabel])
  (query [this q]))


(defprotocol ITransactor
  (add-entity [this])
  (remove-component [this eid clabel])
  (remove-entity [this eid])
  (set-component [this c]))


(defprotocol IWorld
  (transaction! [this f]))
