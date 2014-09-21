(ns clecs.backend.atom-world.queryable
  {:no-doc true}
  (:require [clecs.component :refer [component-label]]
            [clecs.query :refer [query?]]))


(declare make-pred)


(defn- all-loop [qelems pred]
  (loop [[clabel-or-sub-query & rest-of-query] qelems]
    (if (pred clabel-or-sub-query)
      (if (empty? rest-of-query)
        true
        (recur rest-of-query))
      false)))


(defn- any-loop [qelems pred]
  (loop [[clabel-or-sub-query & rest-of-query] qelems]
    (if (pred clabel-or-sub-query)
      true
      (if (empty? rest-of-query)
        false
        (recur rest-of-query)))))


(defn- compile-query* [q]
  (let [[query-type & query-elements] q
        loop-fn (case query-type
                  :all all-loop
                  :any any-loop)]
    (fn [clabels]
      (let [pred (make-pred clabels)]
        (loop-fn query-elements pred)))))


(defn- make-pred [clabels]
  (let [set-of-component-labels (set clabels)]
    (fn [query-elem]
      (if (query? query-elem)
        ((compile-query* query-elem) clabels)
        (set-of-component-labels query-elem)))))


(defn -compile-query [q]
  (if (query? q)
    (compile-query* q)
    (throw (IllegalArgumentException. "Invalid query."))))


(defn component [state eid ctype]
  (get-in state [:components (component-label ctype) eid]))


(defn query [state q]
  (let [f (-compile-query q)]
    (reduce-kv (fn [coll k v]
                 (if (f (seq v))
                   (conj coll k)
                   coll))
               (seq [])
               (:entities state))))
