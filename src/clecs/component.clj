(ns clecs.component)


(defprotocol IComponent
  "Components must extend this protocol. Use [[defcomponent]]
  instead of extending this directly."
  (entity-id [this]))


(defn component?
  "Returns true is ``c`` is a component instance."
  [c]
  (and (not (class? c))
       (satisfies? IComponent c)))


(defn component-type?
  "Returns true is ``c`` is a component type."
  [c]
  (and (class? c)
       (extends? IComponent c)))


(defn component-label
  "Returns component label for component type ``c``.

  #### Example:

      (ns foo.bar)
      (defcomponent Baz [eid])

      (component-label Baz) => :foo.bar.Baz
  "
  [c]
  (if (component-type? c)
    (keyword (.getName c))
    (throw (IllegalArgumentException. (str c " is not a component.")))))


(defmacro defcomponent
  "Creates a component type.

  #### Parameters:

  component-name
  :   Name of the component record. Also used to resolve
      [[component-label]]'s.

  eid-param
  :   Required parameter for entity id's.

  params
  :   Any additional parameters for component. Optional.

  #### Example:

      ;; Components may take only an entity id parameter...
      (defcomponent WalkableComponent [eid])
      ;; ...or one or more optional parameters.
      (defcomponent PositionComponent [eid x y])

      ;; Components will have an auto-generated constructor.
      (def player-position (->PositionComponent player-eid 4 3))
      ;; Components extend IComponent
      (= (entity-id player-position) player-eid) => true
      ;; Components allow keyword access.
      (:x player-position) => 4
  "
  [component-name [eid-param & params]]
  `(defrecord ~component-name [~eid-param ~@params]
     IComponent
     (entity-id [_] ~eid-param)))
