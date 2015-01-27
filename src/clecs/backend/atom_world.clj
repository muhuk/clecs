(ns clecs.backend.atom-world
  "Reference implementation of clecs API.

   `AtomWorld` stores it's data in-memory. It is backed by an
   `clojure.core/atom` internally.

   Currently systems run sequentially."
  (:require [clecs.backend.atom-world.editable :as editable]
            [clecs.backend.atom-world.queryable :as queryable]
            [clecs.backend.atom-world.transactable :refer [*state* transaction!]]
            [clecs.world :as world]
            [clecs.world.editable :refer [IEditableWorld]]
            [clecs.world.queryable :refer [IQueryableWorld]]
            [clecs.world.system :refer [ISystemManager]]))


(def ^:no-doc initial_state {:components {}
                             :entities {}
                             :last-entity-id 0})


(deftype AtomEditableWorld []
  IEditableWorld
  (add-entity [_] (editable/add-entity))
  (remove-component [this eid ctype] (editable/remove-component eid ctype) this)
  (remove-entity [this eid] (editable/remove-entity eid) this)
  (set-component [this c] (editable/set-component c) this)
  IQueryableWorld
  (component [_ eid ctype] (queryable/component *state* eid ctype))
  (query [_ q] (queryable/query *state* q)))


(deftype AtomWorld [systems-atom state editable-world]
  ISystemManager
  (process! [this dt]
            (doseq [s (->> @systems-atom
                           (vals)
                           (map :process))]
              (transaction! this s dt))
            this)
  (remove-system! [this slabel] (swap! systems-atom dissoc slabel) this)
  (set-system! [this slabel s]
               (let [s* (cond (fn? s) {:process s}
                              (map? s) s
                              :default (throw (RuntimeException. "Invalid system.")))]
                 (swap! systems-atom assoc slabel s*) this))
  (systems [_] (seq @systems-atom)))


;; Hide auto-generated constructor for
;; AtomWorld from documentation generator.
(alter-meta! #'->AtomWorld assoc :no-doc true)


(defn make-world
  "Makes a new `AtomWorld`. Use [[clecs.core/make-world]]
   instead of calling this directly."
  [initializer-fn]
  (let [state (atom initial_state)
        systems (atom {})
        editable-world (->AtomEditableWorld)]
    (doto (->AtomWorld systems state editable-world)
      (transaction! initializer-fn))))
