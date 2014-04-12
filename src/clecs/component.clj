(ns clecs.component)


(defprotocol IComponent
  (entity-id [this]))


(defn component-type [c]
  (keyword (.getName (type c))))
