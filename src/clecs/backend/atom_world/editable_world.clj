(ns clecs.backend.atom-world.editable-world
  {:no-doc true}
  (:require [clecs.backend.atom-world.editable :as editable]
            [clecs.backend.atom-world.queryable :as queryable]
            [clecs.backend.atom-world.transactable :refer [*state*]]
            [clecs.world :as world]))


(deftype AtomEditableWorld []
  world/IEditableWorld
  (add-entity [_] (editable/add-entity))
  (remove-component [this eid ctype] (editable/remove-component eid ctype) this)
  (remove-entity [this eid] (editable/remove-entity eid) this)
  (set-component [this c] (editable/set-component c) this)
  world/IQueryableWorld
  (component [_ eid ctype] (queryable/component *state* eid ctype))
  (query [_ q] (queryable/query *state* q)))
