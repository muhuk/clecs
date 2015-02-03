(ns clecs.component)


(declare make-validator)


(defmacro component [ctype cdata]
  `{:ctype ~ctype
    :valid? ~(make-validator cdata)})


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
        component-type (-> (str *ns* "." component-name)
                            (clojure.string/replace #"-" "_")
                            (keyword))
        metadata {:component-type component-type
                  :entity-id-key (keyword eid-param)}]
    `(do
       (defn ~map-fn-name [x#]
         (with-meta x# ~(assoc metadata :type :component)))
       (defn ~fn-name [~@all-params]
         (~map-fn-name (hash-map ~@(mapcat #(vector (keyword %) %) all-params))))
       (def ~component-name (with-meta {:name ~component-type}
                                       ~(assoc metadata :type :component-type))))))


(defn- make-validator [cdata]
  `(fn [c#]
     (= (count c#) ~(count cdata))))


(defn valid? [c cdata]
  ((:valid? c) cdata))
