(ns clecs.backend.atom-world.transactable-world
  {:no-doc true}
  (:require [clecs.backend.atom-world.queryable :as queryable]
            [clecs.backend.atom-world.transactable :as transactable]
            [clecs.world :as world]
            [clecs.world.queryable :refer [IQueryableWorld]]))


(deftype AtomTransactableWorld [state editable-world]
  IQueryableWorld
  (component [_ eid ctype] (queryable/component @state eid ctype))
  (query [_ q] (queryable/query @state q))
  world/ITransactableWorld
  (transaction! [this f] (transactable/transaction! this f)))
