(ns clecs.backend.atom-world
  "Reference implementation of clecs API.

   `AtomWorld` stores it's data in-memory. It is backed by an
   `clojure.core/atom` internally.

   Currently systems run sequentially."
  (:require [clecs.backend.atom-world.query :as query]
            [clecs.component :refer [entity-id]]
            [clecs.util :refer [map-values]]
            [clecs.world.editable :refer [IEditableWorld]]
            [clecs.world.queryable :refer [IQueryableWorld]]
            [clecs.world.system :refer [ISystemManager]]))


(def ^:no-doc initial_state {:components {}
                             :entities {}
                             :last-entity-id 0})


(def ^{:dynamic true
       :no-doc true} *state*)



(declare -transaction!)


(deftype AtomEditableWorld []
  IEditableWorld
  (add-entity [_]
              (let [state *state*
                    eid (inc (:last-entity-id state))]
                (var-set #'*state*
                         (-> state
                             (assoc-in [:entities eid] #{})
                             (assoc :last-entity-id eid)))
                eid))
  (remove-component [this eid ctype]
                    (var-set #'*state*
                             (-> *state*
                                 (update-in [:entities eid] disj ctype)
                                 (update-in [:components ctype] dissoc eid)))
                    this)
  (remove-entity [this eid]
                 (let [state *state*]
                   (var-set #'*state*
                            (-> state
                                (update-in [:entities] dissoc eid)
                                (update-in [:components]
                                           (partial map-values #(dissoc % eid))))))
                 this)
  (set-component [this c]
                 (let [clabel ((comp :component-type meta) c)
                       eid (entity-id c)]
                   (var-set #'*state*
                            (-> *state*
                                (update-in [:entities eid] conj clabel)
                                (update-in [:components clabel] #(or % {}))
                                (update-in [:components clabel] conj [eid c]))))
                 this)
  IQueryableWorld
  (component [_ eid ctype] (get-in *state* [:components ctype eid]))
  (query [_ q]
         (let [f (query/-compile-query q)]
           (reduce-kv (fn [coll k v]
                        (if (f (seq v))
                          (conj coll k)
                          coll))
                      (seq [])
                      (:entities *state*)))))


(deftype AtomWorld [systems-atom state editable-world]
  ISystemManager
  (process! [this dt]
            (doseq [s (->> @systems-atom
                           (vals)
                           (map :process))]
              (-transaction! this s dt))
            this)
  (remove-system! [this slabel] (swap! systems-atom dissoc slabel) this)
  (set-system! [this slabel s]
               (let [s* (cond (fn? s) {:process s}
                              (map? s) s
                              :default (throw (RuntimeException. "Invalid system.")))]
                 (swap! systems-atom assoc slabel s*) this))
  (systems [_] (seq @systems-atom)))


(defn make-world
  "Makes a new `AtomWorld`. Use [[clecs.core/make-world]]
   instead of calling this directly."
  [initializer-fn]
  (let [state (atom initial_state)
        systems (atom {})
        editable-world (->AtomEditableWorld)]
    (doto (->AtomWorld systems state editable-world)
      (-transaction! (fn [w _] (initializer-fn w)) nil))))


(defn ^:no-doc -transaction! [world f dt]
  (swap! (.state world)
         (fn [state]
           (binding [*state* state]
             (f (.editable-world world) dt)
             *state*)))
  nil)


;; Hide internals from documentation generator.
(doseq [v [#'->AtomWorld
           #'->AtomEditableWorld]]
  (alter-meta! v assoc :no-doc true))
