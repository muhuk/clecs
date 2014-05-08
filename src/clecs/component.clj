(ns clecs.component)


(defprotocol IComponent
  (entity-id [this]))


(defn component? [c]
  (and (not (class? c))
       (satisfies? IComponent c)))


(defn component-type? [c]
  (and (class? c)
       (extends? IComponent c)))


(defn component-label [c]
  (if (or (component? c)
          (component-type? c))
    (keyword (.getName (if (class? c) c (type c))))
    (throw (IllegalArgumentException. (str c " is not a component.")))))


(defmacro defcomponent [component-name [eid-param & params]]
  `(defrecord ~component-name [~eid-param ~@params]
     IComponent
     (entity-id [_] ~eid-param)))
