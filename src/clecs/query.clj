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
  (:require [clecs.component :refer [component-label component-type?]]))


(declare process-element)


(defmacro ^:private make-query-command [command-symbol]
  `(defn ~command-symbol
     [& ~'elements]
     (into [~(keyword command-symbol)]
           (mapcat (process-element ~(keyword command-symbol)) ~'elements))))


(defn ^:private process-element [same-keyword]
  (fn [x]
    (cond
     (component-type? x) [(component-label x)]
     (= (first x) same-keyword) (rest x)
     :else [x])))


(make-query-command all)


(make-query-command any)


(defn query? [q]
  (and (coll? q)
       (not (nil? (and (#{:all :any} (first q))
                       (fnext q))))))
