(ns clecs.backend.atom-world
  "Reference implementation of clecs API.

  `AtomWorld` stores it's data in-memory. It is backed by an
  `clojure.core/atom` internally.

  Currently systems run sequentially."
  (:require [clecs.backend.atom-world.query :as query]
            [clecs.util :refer [map-values]]
            [clecs.world :refer [IEditableWorld
                                 IQueryableWorld
                                 IWorld
                                 IWorldFactory]]))


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


(deftype AtomWorld [systems state editable-world]
  IWorld
  (-run [this f dt]
        (-transaction! this f dt)
        this)
  (process! [this dt]
            (doseq [s (map :process (vals systems))]
              (-transaction! this s dt))
            this))


(def atom-world-factory
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
                  world (->AtomWorld systems-map
                                     (atom initial_state)
                                     (->AtomEditableWorld components-map))]
              world))))


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
