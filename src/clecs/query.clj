(ns clecs.query
  "Primitives for query criteria.

  `query` method of [[clecs.world.queryable/IQueryableWorld]]
  is used to find entities based on their components.

  #### Examples:

      ;; Return a seq containing entity id's that
      ;; has FooComponent:
      (query queryable-world (all FooComponent))

      ;; Same as the previous one:
      (query queryable-world (any FooComponent))

      ;; Entities with both FooComponent and BarComponent
      (query queryable-world
             (all FooComponent BarComponent))

      ;; Entities with either FooComponent or BarComponent
      (query queryable-world
             (any FooComponent BarComponent))

      ;; Entities with FooComponent and either
      ;; BazComponent or BatComponent
      (query queryable-world
             (all FooComponent (any BazComponent
                                    BatComponent)))

      ;; Entities with either FooComponent and
      ;; BarComponent or BazComponent and BatComponent
      (query queryable-world
             (any (all FooComponent BarComponent)
                  (all BazComponent BatComponent)))

  You can nest primitive calls infinitely. However
  it is considered bad design to query too many
  components at once.

  See also [[clecs.world.queryable/IQueryableWorld]]."
  (:import [clojure.lang Keyword]))


(defprotocol ^:no-doc IQuery)


(defprotocol ^:no-doc IQueryNode
  (satisfies [this components])
  (simplify [this]))


(defrecord Query [root cs]
  clojure.lang.IFn
  (invoke [this components] (satisfies this (set components)))
  IQuery
  IQueryNode
  (satisfies [_ components] (satisfies root components))
  (simplify [_]
            (->Query (simplify root) nil)))


(defrecord All [children]
  IQueryNode
  (satisfies [_ components] (loop [[head & tail] (seq children)]
                              (if (nil? head)
                                true
                                (if (satisfies head components)
                                  (recur tail)
                                  false))))
  (simplify [_]
            (->> children
                 (map simplify)
                 (reduce (fn [acc elem]
                           (if (instance? Query elem)
                             (if (instance? All (:root elem))
                               (into acc (get-in elem [:root :children]))
                               (conj acc (:root elem)))
                             (conj acc elem)))
                         #{})
                 (->All))))


(defrecord Any [children]
  IQueryNode
  (satisfies [_ components] (loop [[head & tail] (seq children)]
                              (if (nil? head)
                                false
                                (if (satisfies head components)
                                  true
                                  (recur tail)))))
  (simplify [_]
            (->> children
                 (map simplify)
                 (reduce (fn [acc elem]
                           (if (instance? Query elem)
                             (if (instance? Any (:root elem))
                               (into acc (get-in elem [:root :children]))
                               (conj acc (:root elem)))
                             (conj acc elem)))
                         #{})
                 (->Any))))



(extend-protocol IQueryNode
  Keyword
  (satisfies [this components] (contains? components this))
  (simplify [this] this))


(defn- query [constructor elems]
  (if (seq elems)
    (-> (set elems)
        (constructor)
        (->Query nil)
        (simplify))
    (throw (IllegalArgumentException. "You cannot create an empty query"))))


(defn all [& elems]
  (query ->All elems))


(defn any [& elems]
  (query ->Any elems))


;; Hide internals from documentation generator.
(doseq [v [#'->All
           #'map->All
           #'->Any
           #'map->Any
           #'->Query
           #'map->Query]]
  (alter-meta! v assoc :no-doc true))
