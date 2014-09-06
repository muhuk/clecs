(ns clecs.backend.atom-world.editable
  {:no-doc true}
  (:require [clecs.backend.atom-world.transactable :refer [*state*]]
            [clecs.component :refer [component-label entity-id]]
            [clecs.util :refer [map-values]]))


(defn -add-entity []
  (let [state *state*
        eid (inc (:last-entity-id state))]
    (var-set #'*state*
             (-> state
                 (assoc-in [:entities eid] #{})
                 (assoc :last-entity-id eid)))
    eid))


(defn -remove-component [eid ctype]
  (let [clabel (component-label ctype)]
    (var-set #'*state*
             (-> *state*
                 (update-in [:entities eid] disj clabel)
                 (update-in [:components clabel] dissoc eid))))
  nil)


(defn -remove-entity [eid]
  (let [state *state*]
    (var-set #'*state*
             (-> state
                 (update-in [:entities] dissoc eid)
                 (update-in [:components]
                            (partial map-values #(dissoc % eid))))))
  nil)


(defn -set-component [c]
  (let [clabel (component-label (type c))
        eid (entity-id c)]
    (var-set #'*state*
             (-> *state*
                 (update-in [:entities eid] conj clabel)
                 (update-in [:components clabel] #(or % {}))
                 (update-in [:components clabel] conj [eid c])))
    nil))
