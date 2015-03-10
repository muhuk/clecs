(ns clecs.backend.atom-world.query
  {:no-doc true}
  (:require [clecs.query :refer [query?]]))


(declare make-pred)


(defn- all-loop [qelems pred]
  (loop [[cname-or-sub-query & rest-of-query] qelems]
    (if (pred cname-or-sub-query)
      (if (empty? rest-of-query)
        true
        (recur rest-of-query))
      false)))


(defn- any-loop [qelems pred]
  (loop [[cname-or-sub-query & rest-of-query] qelems]
    (if (pred cname-or-sub-query)
      true
      (if (empty? rest-of-query)
        false
        (recur rest-of-query)))))


(defn- compile-query* [q]
  (let [[query-type & query-elements] q
        loop-fn (case query-type
                  :clecs.query/all all-loop
                  :clecs.query/any any-loop)]
    (fn [component-names]
      (let [pred (make-pred component-names)]
        (loop-fn query-elements pred)))))


(defn- make-pred [component-names]
  (let [set-of-component-names (set component-names)]
    (fn [query-elem]
      (if (query? query-elem)
        ((compile-query* query-elem) component-names)
        (set-of-component-names query-elem)))))


(defn -compile-query [q]
  (if (query? q)
    (compile-query* q)
    (throw (IllegalArgumentException. "Invalid query."))))
