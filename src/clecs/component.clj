(ns clecs.component)


(defprotocol IComponent
  (entity-id [this]))


(defn component-type [c]
  (let [c (cond
           (satisfies? IComponent c) (type c)
           (extends? IComponent c) c
           :else (throw (IllegalArgumentException. "Not an IComponent.")))]
    (keyword (.getName c))))
