(ns clecs.world)


(defprotocol IEditableWorld
  (add-entity [this])
  (remove-component [this eid ctype])
  (remove-entity [this eid])
  (set-component [this c]))


(defprotocol IQueryableWorld
  (component [this eid ctype])
  (query [this q]))


(defprotocol ISystemManager
;;   (process! [this dt])
;;   (remove-system! [this slabel])
;;   (set-system! [this slabel s])
;;   (systems [this])
)


(defprotocol ITransactableWorld
  (transaction! [this f]))
