(ns clecs.backend.atom-world
  (:require [clecs.world :as world]
            [clecs.component :refer [component-label entity-id]]
            [clecs.util :refer [map-values]]))


(def ^:const EMPTY_WORLD {:components {}
                          :entities {}
                          :last-entity-id 0})


(def ^:dynamic *state*)


(declare -add-entity
         -component
         -process!
         -query
         -remove-component
         -remove-entity
         -set-component
         -transaction!
         -with-state)


(deftype AtomWorld [state]
  world/IQueryable
  (component [_ eid ctype] (-component state eid ctype))
  (query [_ q] (-query state q))
  world/ITransactor
  (add-entity [_] (-add-entity))
  (remove-component [this eid ctype] (-remove-component eid ctype) this)
  (remove-entity [this eid] (-remove-entity eid) this)
  (set-component [this c] (-set-component c) this)
  world/IWorld
  (transaction! [this f] (-transaction! this f)))


(defmacro ^{:private true} ensure-transaction [& body]
  `(do
     (when-not (bound? #'*state*)
       (throw (IllegalStateException. "Not in a transaction.")))
     ~@body))


(defn make-world
  ([] (make-world EMPTY_WORLD))
  ([state] (->AtomWorld (atom state))))


(defn -add-entity []
  (ensure-transaction
   (let [state *state*
         eid (inc (:last-entity-id state))]
     (var-set #'*state*
              (-> state
                  (assoc-in [:entities eid] #{})
                  (assoc :last-entity-id eid)))
     eid)))


(defn -component [state-atom eid ctype]
  (-with-state state-atom get-in [:components (component-label ctype) eid]))


(defn -query [state-atom q]
  (-with-state state-atom
               #(reduce-kv (fn [coll k v]
                             (if (q (seq v))
                               (conj coll k)
                               coll))
                           []
                           (:entities %))))


(defn -remove-component [eid ctype]
  (ensure-transaction
   (let [clabel (component-label ctype)]
     (var-set #'*state*
              (-> *state*
                  (update-in [:entities eid] disj clabel)
                  (update-in [:components clabel] dissoc eid)))))
  nil)


(defn -remove-entity [eid]
  (ensure-transaction
   (let [state *state*]
     (var-set #'*state*
              (-> state
                  (update-in [:entities] dissoc eid)
                  (update-in [:components]
                             (partial map-values #(dissoc % eid)))))))
  nil)


(defn -set-component [c]
  (ensure-transaction
   (let [clabel (component-label (type c))
         eid (entity-id c)]
     (var-set #'*state*
              (-> *state*
                  (update-in [:entities eid] conj clabel)
                  (update-in [:components clabel] #(or % {}))
                  (update-in [:components clabel] conj [eid c])))
     nil)))


(defn -transaction! [world f]
  (when (bound? #'*state*)
    (throw (IllegalStateException. "transaction! cannot be called within a transaction.")))
  (swap! (.state world)
         (fn [state]
           (binding [*state* state]
             (f world)
             *state*)))
  nil)


(defn -with-state [state-atom f & args]
  (let [state (if (bound? #'*state*) *state* @state-atom)]
    (apply f (cons state args))))
