(ns clecs.backend.atom-world.editable-world
  {:no-doc true}
  (:require [clecs.backend.atom-world.editable :refer [-add-entity
                                                       -remove-component
                                                       -remove-entity
                                                       -set-component]]
            [clecs.backend.atom-world.queryable :refer [-component
                                                        -query]]
            [clecs.backend.atom-world.transactable :refer [*state*]]
            [clecs.world :as world]))


(deftype AtomEditableWorld []
  world/IEditableWorld
  (add-entity [_] (-add-entity))
  (remove-component [this eid ctype] (-remove-component eid ctype) this)
  (remove-entity [this eid] (-remove-entity eid) this)
  (set-component [this c] (-set-component c) this)
  world/IQueryableWorld
  (component [_ eid ctype] (-component *state* eid ctype))
  (query [_ q] (-query *state* q)))
