(ns clecs.backend.atom-world
  "Reference implementation of clecs API.

  `AtomWorld` stores it's data in-memory. It is backed by an
  `clojure.core/atom` internally.

  Currently systems run sequentially."
  (:require [clecs.backend.atom-world.query :as query]
            [clecs.util :refer [map-values]]
            [clecs.world :refer [-run
                                 IEditableWorld
                                 IQueryableWorld
                                 IWorld
                                 IWorldFactory]]))


(def ^:no-doc initial_state {:components {}
                             :entities {}
                             :last-entity-id 0})


(def ^{:dynamic true
       :no-doc true} *state*)


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
  (remove-component [this eid cname]
                    (var-set #'*state*
                             (-> *state*
                                 (update-in [:entities eid] disj cname)
                                 (update-in [:components cname] dissoc eid)))
                    this)
  (remove-entity [this eid]
                 (let [state *state*]
                   (var-set #'*state*
                            (-> state
                                (update-in [:entities] dissoc eid)
                                (update-in [:components]
                                           (partial map-values #(dissoc % eid))))))
                 this)
  (-set-component [this eid cname cdata]
                  (var-set #'*state*
                           (-> *state*
                               (update-in [:entities eid] conj cname)
                               (update-in [:components cname] #(or % {}))
                               (update-in [:components cname] conj [eid cdata])))
                  this)
  IQueryableWorld
  (-component [_ cname] (components cname))
  (component [_ eid cname] (get-in *state* [:components cname eid]))
  (query [_ q]
         (let [f (query/-compile-query q)]
           (reduce-kv (fn [coll k v]
                        (if (f (seq v))
                          (conj coll k)
                          coll))
                      (seq [])
                      (:entities *state*)))))


(deftype AtomWorld [components systems state]
  IWorld
  (-run [this f dt]
        (swap! (.state this)
               (fn [state]
                 (binding [*state* state]
                   (f (->AtomEditableWorld components) dt)
                   *state*)))
        this)
  (process! [this dt]
            (doseq [s (vals systems)]
              (-run this s dt))
            this))


(def atom-world-factory
  "
  #### Examples:

      (clecs.world/world atom-world-factory
                         {:components [(component ...)
                                       (component ...)
                                       (component ...)
                                       ...]
                          :initializer (fn [w] ...)
                          :systems [(system ...)
                                    (system ...)
                                    ...]})
  "
  (reify
    IWorldFactory
    (-world [_ params]
            (let [{components :components
                   initializer :initializer
                   systems :systems} params
                  systems-map (->> systems
                                   (map (juxt :name identity))
                                   (into {}))
                  components-map (->> components
                                      (map (juxt :cname identity))
                                      (into {}))
                  world (->AtomWorld components-map
                                     systems-map
                                     (atom initial_state))]
              world))))


;; Hide internals from documentation generator.
(doseq [v [#'->AtomWorld
           #'->AtomEditableWorld]]
  (alter-meta! v assoc :no-doc true))
