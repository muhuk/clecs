(ns clecs.backend.atom-world
  (:require [clecs.world :as world]
            [clecs.component :as component]
            [clecs.util :refer [map-values]]))


(def ^:const EMPTY_WORLD {:components {}
                          :entities {:last-index 0}})


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
  world/IWorld
  (add-entity [_] (-add-entity))
  (component [_ eid clabel] (-component state eid clabel))
  (process! [this] (-process! this) nil)
  (query [_ q] (-query state q))
  (remove-component [_ eid clabel] (-remove-component eid clabel))
  (remove-entity [_ eid] (-remove-entity eid))
  (set-component [_ c] (-set-component c))
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
         eid (inc (get-in state [:entities :last-index]))]
     (var-set #'*state*
              (-> state
                  (assoc-in [:entities eid] #{})
                  (assoc-in [:entities :last-index] eid)))
     eid)))


(defn -component [state-atom eid clabel]
  (-with-state state-atom get-in [:components clabel eid]))


(defn -process! [world]
  (throw (UnsupportedOperationException.)))


(defn -query [state-atom q]
  (-with-state state-atom
               #(reduce-kv (fn [coll k v]
                             (if (q (seq v))
                               (conj coll k)
                               coll))
                           []
                           (:entities %))))


(defn -remove-component [eid clabel]
  (ensure-transaction
   (var-set #'*state*
            (-> *state*
                (update-in [:entities eid] disj clabel)
                (update-in [:components clabel] dissoc eid))))
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
   (let [clabel (component/component-label c)
         eid (component/entity-id c)]
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
