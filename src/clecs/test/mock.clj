(ns clecs.test.mock
  (:require [clecs.world :refer [IEditableWorld IQueryableWorld]]))


(defmacro ^:private def-mock-fn [n]
  `(defn ~n [~'& ~'_]
     (throw (RuntimeException. ~(str n " is called directly.")))))


(def-mock-fn mock-add-entity)
(def-mock-fn mock-component)
(def-mock-fn mock-query)
(def-mock-fn mock-remove-component)
(def-mock-fn mock-remove-entity)
(def-mock-fn mock-set-component)


(defn mock-editable-world []
  (reify
    IEditableWorld
    (add-entity [_] (mock-add-entity))
    (remove-component [_ eid cname] (mock-remove-component eid cname))
    (remove-entity [_ eid] (mock-remove-entity eid))
    (set-component [_ eid cname cdata] (mock-set-component eid cname cdata))
    IQueryableWorld
    (component [_ eid cname] (mock-component eid cname))
    (query [_ q] (mock-query q))))
