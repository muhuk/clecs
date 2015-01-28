(ns clecs.backend.atom-world.query
  {:no-doc true}
  (:require [clecs.query :refer [query?]]))


(declare make-pred)


(defn- all-loop [qelems pred]
  (loop [[ctype-or-sub-query & rest-of-query] qelems]
    (if (pred ctype-or-sub-query)
      (if (empty? rest-of-query)
        true
        (recur rest-of-query))
      false)))


(defn- any-loop [qelems pred]
  (loop [[ctype-or-sub-query & rest-of-query] qelems]
    (if (pred ctype-or-sub-query)
      true
      (if (empty? rest-of-query)
        false
        (recur rest-of-query)))))


(defn- compile-query* [q]
  (let [[query-type & query-elements] q
        loop-fn (case query-type
                  :all all-loop
                  :any any-loop)]
    (fn [ctypes]
      (let [pred (make-pred ctypes)]
        (loop-fn query-elements pred)))))


(defn- make-pred [ctypes]
  (let [set-of-component-types (set ctypes)]
    (fn [query-elem]
      (if (query? query-elem)
        ((compile-query* query-elem) ctypes)
        (set-of-component-types query-elem)))))


(defn -compile-query [q]
  (if (query? q)
    (compile-query* q)
    (throw (IllegalArgumentException. "Invalid query."))))
