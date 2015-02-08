(ns clecs.backend.atom-world
  "Reference implementation of clecs API.

   `AtomWorld` stores it's data in-memory. It is backed by an
   `clojure.core/atom` internally.

   Currently systems run sequentially."
  (:require [clecs.backend.atom-world.query :as query]
            [clecs.component :refer [valid?]]
            [clecs.util :refer [map-values]]
            [clecs.world :refer [IEditableWorld IQueryableWorld IWorld]]))


(def ^:no-doc initial_state {:components {}
                             :entities {}
                             :last-entity-id 0})


(def ^{:dynamic true
       :no-doc true} *state*)


(declare -transaction!)


(deftype AtomEditableWorld [components]
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
  (set-component [this eid ctype cdata]
                 (if (valid? (components ctype) cdata)
                   (do
                     (var-set #'*state*
                              (-> *state*
                                  (update-in [:entities eid] conj ctype)
                                  (update-in [:components ctype] #(or % {}))
                                  (update-in [:components ctype] conj [eid cdata])))
                     this)
                   (throw (RuntimeException. "Invalid component data."))))
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


(deftype AtomWorld [systems state editable-world]
  IWorld
  (process! [this dt]
            (doseq [s (map :process (vals systems))]
              (-transaction! this s dt))
            this))


(defn atom-world [components
                  initial-transaction
                  systems]
  (let [systems-map (->> systems
                         (map (juxt :name identity))
                         (into {}))
        components-map (->> components
                         (map (juxt :ctype identity))
                         (into {}))]
    (doto (->AtomWorld systems-map
                       (atom initial_state)
                       (->AtomEditableWorld components-map))
      (-transaction! (fn [w _] (initial-transaction w)) nil))))


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
