(ns ^:no-doc clecs.backend.atom-world.transactable-world
  (:require [clecs.backend.atom-world.queryable :refer [-component
                                                        -query]]
            [clecs.backend.atom-world.transactable :refer [-transaction!]]
            [clecs.world :as world]))



(deftype AtomTransactableWorld [state editable-world]
  world/IQueryableWorld
  (component [_ eid ctype] (-component @state eid ctype))
  (query [_ q] (-query @state q))
  world/ITransactableWorld
  (transaction! [this f] (-transaction! this f)))
