(ns clecs.component)


;; TODO: Change IEditableWorld/set-component to accept
;;       an explicit component-type & eid.
;;
;;       Then remove this.
(defn entity-id [this]
  (-> (meta this)
      (:entity-id-key)
      (keyword)
      (this)))


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
