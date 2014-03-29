(ns clecs.component)


(defprotocol IComponent)


(defn component-type [c]
  (keyword (.getName (type c))))
