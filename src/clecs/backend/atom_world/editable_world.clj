(ns clecs.backend.atom-world.editable-world
  (:require [clecs.world :as world]
            [clecs.backend.atom-world.editable :refer [-add-entity
                                                       -remove-component
                                                       -remove-entity
                                                       -set-component]]
            [clecs.backend.atom-world.queryable :refer [-component
                                                        -query]]
            [clecs.backend.atom-world.state :refer [*state*]]))


(deftype AtomEditableWorld []
  world/IEditableWorld
  (add-entity [_] (-add-entity))
  (remove-component [this eid ctype] (-remove-component eid ctype) this)
  (remove-entity [this eid] (-remove-entity eid) this)
  (set-component [this c] (-set-component c) this)
  world/IQueryableWorld
  (component [_ eid ctype] (-component *state* eid ctype))
  (query [_ q] (-query *state* q)))