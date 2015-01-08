(ns clecs.component)


(defprotocol ^{:deprecated "1.1.0"} IComponent
  "~~Components must extend this protocol. Use [[defcomponent]]
  instead of extending this directly.~~

  Components are plain old clojure maps, it is no longer necessary
  to extends this protocol or use [[defcomponent]]. IComponent will
  be removed in version 2."
  (entity-id [this]))


(extend clojure.lang.APersistentMap
  IComponent
  {:entity-id (fn [this]
                (-> (meta this)
                    (:entity-id-key)
                    (keyword)
                    (this)))})


(defn ^{:deprecated "1.1.0"} component?
  "component-type? is deprecated and will be removed in version 2.

  ~~Returns true is `c` is a component instance.~~

  All maps with the suitable contents are components."
  [c]
  (-> (meta c)
      (:type)
      (= :component)))


(defn ^{:deprecated "1.1.0"} component-type?
  "component-type? is deprecated and will be removed in version 2.

  ~~Returns true is `c` is a component type.~~

  All maps with the suitable contents are components."
  [c]
  (-> (meta c)
      (:type)
      (= :component-type)))


(defn ^{:deprecated "1.1.0"} component-label
  "component-label is deprecated and will be removed in version 2.

  ~~Returns component label for component type `c`.~~

  Component labels are keywords.

  #### Example:

      (ns foo.bar)
      (defcomponent Baz [eid])

      (component-label Baz) => :foo.bar.Baz
  "
  [c]
  (if-let [clabel (-> (meta c) (:component-label))]
    clabel
    (throw (IllegalArgumentException.))))


(defmacro ^{:deprecated "1.1.0"} defcomponent
  "Creates a component type.

  Note that `defcomponent` is subject to change in version 2.

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
  [component-name [eid-param & params :as all-params]]
  (let [map-fn-name (symbol (str "map->" component-name))
        fn-name (symbol (str "->" component-name))
        component-label (-> (str *ns* "." component-name)
                            (clojure.string/replace #"-" "_")
                            (keyword))
        metadata {:component-label component-label
                  :entity-id-key (keyword eid-param)}]
    `(do
       (defn ~map-fn-name [x#]
         (with-meta x# ~(assoc metadata :type :component)))
       (defn ~fn-name [~@all-params]
         (~map-fn-name (hash-map ~@(mapcat #(vector (keyword %) %) all-params))))
       (def ~component-name (with-meta {:name ~component-label}
                                       ~(assoc metadata :type :component-type))))))
