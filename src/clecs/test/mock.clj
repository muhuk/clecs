(ns clecs.test.mock
  (:require [clecs.world :refer [IEditableWorld
                                 IQueryableWorld
                                 IWorld
                                 IWorldFactory]]))


(defmacro ^:private def-mock-fn [n]
  `(defn ~n [~'& ~'_]
     (throw (RuntimeException. ~(str n " is called directly.")))))


(def-mock-fn -component)
(def-mock-fn -run)
(def-mock-fn -set-component)
(def-mock-fn -world)
(def-mock-fn add-entity)
(def-mock-fn component)
(def-mock-fn process!)
(def-mock-fn query)
(def-mock-fn remove-component)
(def-mock-fn remove-entity)


(defn mock-editable-world []
  (reify
    IEditableWorld
    (-set-component [this eid cname cdata] (-set-component this eid cname cdata))
    (add-entity [this] (add-entity this))
    (remove-component [this eid cname] (remove-component this eid cname))
    (remove-entity [this eid] (remove-entity this eid))
    IQueryableWorld
    (-component [this cname] (-component this cname))
    (component [this eid cname] (component this eid cname))
    (query [this q] (query this q))))


(defn mock-world []
  (reify
    IWorld
    (-run [this f dt] (-run this f dt))
    (process! [this dt] (process! this dt))))


(def mock-world-factory
  (reify
    IWorldFactory
    (-world [this params] (-world this params))))
