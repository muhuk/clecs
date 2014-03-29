(ns clecs.backend.atom-world
  (:require [clecs.world :as world]
            [clecs.component :as component]))


(def ^:const EMPTY_WORLD {:components {}
                          :entities {:last-index 0}})


(declare -add-component -add-entity -last-entity-id -process! -remove-component)


(deftype AtomWorld [state]
  world/IWorld
  (add-component [this eid f] (world/add-component this eid f []))
  (add-component [this eid f args]
                  (swap! state #(apply -add-component (concat [% eid f] args)))
                  nil)
  (add-entity! [this]
               (swap! state -add-entity)
               (-last-entity-id @state))
  (process! [this] (-process! this) nil)
  (remove-component! [this eid ct] (swap! state -remove-component eid ct) nil))



(defn make-world
  ([] (make-world EMPTY_WORLD))
  ([state] (->AtomWorld (atom state))))


(defn -add-component [state eid f & args]
  (let [c (apply f (cons eid args))
        ct (component/component-type c)]
    (-> state
        (update-in [:entities eid] conj ct)
        (update-in [:components ct] #(or % {}))
        (update-in [:components ct] conj [eid c]))))


(defn -add-entity [state]
  (let [eid (inc (get-in state [:entities :last-index]))]
    (-> state
        (assoc-in [:entities eid] #{})
        (assoc-in [:entities :last-index] eid))))


(defn -last-entity-id [state]
  (get-in state [:entities :last-index]))


(defn -process! [world]
  (throw (UnsupportedOperationException.)))


(defn -remove-component [state eid ct]
  (-> state
      (update-in [:entities eid] disj ct)
      (update-in [:components ct] dissoc eid)))
