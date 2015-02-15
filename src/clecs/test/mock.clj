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
    (add-entity [this] (mock-add-entity))
    (remove-component [this eid cname] (mock-remove-component this eid cname))
    (remove-entity [this eid] (mock-remove-entity this eid))
    (set-component [this eid cname cdata] (mock-set-component this eid cname cdata))
    IQueryableWorld
    (component [this eid cname] (mock-component this eid cname))
    (query [this q] (mock-query this q))))
