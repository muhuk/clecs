(ns clecs.backend.atom-world
  (:require [clecs.world :as world]
            [clecs.component :as component]
            [clecs.util :refer [map-values]]))


(def ^:const EMPTY_WORLD {:components {}
                          :entities {:last-index 0}})


(def ^:dynamic *state*)


(declare -add-component
         -add-entity
         -component
         -process!
         -query
         -remove-component
         -remove-entity
         -transaction!
         -with-state)


(deftype AtomWorld [state]
  world/IWorld
  (add-component [this eid f] (world/add-component this eid f []))
  (add-component [_ eid f args] (-add-component eid f args))
  (add-entity [_] (-add-entity))
  (component [_ eid ct] (-component state eid ct))
  (process! [this] (-process! this) nil)
  (query [_ q] (-query state q))
  (remove-component [_ eid ct] (-remove-component eid ct))
  (remove-entity [_ eid] (-remove-entity eid))
  (transaction! [this f] (-transaction! this f)))


(defmacro ^{:private true} ensure-transaction [& body]
  `(do
     (when-not (bound? #'*state*)
       (throw (IllegalStateException. "Not in a transaction.")))
     ~@body))


(defn make-world
  ([] (make-world EMPTY_WORLD))
  ([state] (->AtomWorld (atom state))))


(defn -add-component [eid f args]
  (ensure-transaction
   (let [state *state*
         c (apply f (cons eid args))
         ct (component/component-type c)]
     (var-set #'*state*
              (-> state
                  (update-in [:entities eid] conj ct)
                  (update-in [:components ct] #(or % {}))
                  (update-in [:components ct] conj [eid c])))
     nil)))


(defn -add-entity []
  (ensure-transaction
   (let [state *state*
         eid (inc (get-in state [:entities :last-index]))]
     (var-set #'*state*
              (-> state
                  (assoc-in [:entities eid] #{})
                  (assoc-in [:entities :last-index] eid)))
     eid)))


(defn -component [state-atom eid ct]
  (-with-state state-atom get-in [:components ct eid]))


(defn -process! [world]
  (throw (UnsupportedOperationException.)))


(defn -query [state-atom q]
  (letfn [(normalize-query [q] (->> (if (sequential? q) q [q])
                                    (map #(if (set? %) % #{%}))))
          (entities-with [entities q1]
                         (filter #(some q1 (second %)) entities))
          (f [state q]
             (loop [entities (:entities state)
                    q q]
               (if (empty? q)
                 entities
                 (recur (entities-with entities (first q))
                        (rest q)))))]
    (lazy-seq (keys (->> (normalize-query q)
                         (-with-state state-atom f))))))


(defn -remove-component [eid ct]
  (ensure-transaction
   (var-set #'*state*
            (-> *state*
                (update-in [:entities eid] disj ct)
                (update-in [:components ct] dissoc eid))))
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
