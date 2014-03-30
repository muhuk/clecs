(ns clecs.backend.atom-world
  (:require [clecs.world :as world]
            [clecs.component :as component]))


(def ^:const EMPTY_WORLD {:components {}
                          :entities {:last-index 0}})


(def ^:dynamic *state*)


(declare -add-component
         -add-entity
         -process!
         -remove-component
         -transaction!)


(deftype AtomWorld [state]
  world/IWorld
  (add-component [this eid f] (world/add-component this eid f []))
  (add-component [_ eid f args] (apply -add-component (concat [eid f] args)))
  (add-entity [_] (-add-entity))
  (process! [this] (-process! this) nil)
  (remove-component [_ eid ct] (-remove-component eid ct))
  (transaction! [this f] (-transaction! this f)))



(defmacro ^{:private true} ensure-transaction [& body]
  `(do
     (when-not (bound? #'*state*)
       (throw (IllegalStateException. "Not in a transaction.")))
     ~@body))


(defn make-world
  ([] (make-world EMPTY_WORLD))
  ([state] (->AtomWorld (atom state))))


(defn -add-component [eid f & args]
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


(defn -process! [world]
  (throw (UnsupportedOperationException.)))


(defn -remove-component [eid ct]
  (ensure-transaction
   (var-set #'*state*
            (-> *state*
                (update-in [:entities eid] disj ct)
                (update-in [:components ct] dissoc eid))))
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
