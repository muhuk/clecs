(ns clecs.world.check.control
  (:require [clecs.component :refer [component-label]]
            [clecs.world :as world]))



(declare extract-entities)


(def initial-state {:components {}
                    :in-transaction? false
                    :processing? false
                    :systems {}})


(defmulti call-command-on-state (fn [command state]
                                  [(:method command)
                                   (case (:processing? state)
                                     nil ::maybe-processing
                                     true ::processing
                                     false ::not-processing)
                                   (case (:in-transaction? state)
                                     nil ::maybe-in-transaction
                                     true ::in-transaction
                                     false ::not-in-transaction)]))


(derive ::processing ::maybe-processing)
(derive ::not-processing ::maybe-processing)
(derive ::in-transaction ::maybe-in-transaction)
(derive ::not-in-transaction ::maybe-in-transaction)


(defmethod call-command-on-state
  :default
  [_ state]
  [:exception (IllegalArgumentException.) state])


(defmethod call-command-on-state
  [`world/component ::processing ::maybe-in-transaction]
  [{[eid ctype] :params} state]
  [:result (get-in state [:components (component-label ctype) eid]) state])


(defmethod call-command-on-state
  [`world/query ::processing ::maybe-in-transaction]
  [{[q] :params} state]
  (let [entities (extract-entities state)
        result (reduce-kv (fn [coll eid ctypes]
                            (if (q ctypes)
                              (conj coll eid)
                              coll))
                          (seq [])
                          entities)]
    [:result result state]))


(defmethod call-command-on-state
  [`world/remove-system! ::not-processing ::not-in-transaction]
  [{[system-name] :params} state]
  [:result :clecs.world.check/world (update-in state [:systems] dissoc system-name)])


(defmethod call-command-on-state
  [`world/set-system! ::not-processing ::not-in-transaction]
  [{[system-name system] :params} state]
  [:result :clecs.world.check/world (update-in state [:systems] assoc system-name system)])


(defmethod call-command-on-state
  [`world/systems ::not-processing ::not-in-transaction]
  [_ state]
  [:result (seq (:systems state)) state])


(defn- extract-entities [state]
  (reduce-kv
   (fn [coll ctype components]
     (let [eids (keys components)]
       (reduce (fn [coll eid]
                 (update-in coll [eid] conj ctype))
               coll
               eids)))
   {}
   (:components state)))
