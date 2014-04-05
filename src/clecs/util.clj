(ns clecs.util)


(defn map-values [f coll]
  (let [key-vals (map (fn [[k v]] [k (f v)]) coll)]
    (if (seq key-vals)
      (apply conj (empty coll) key-vals)
      coll)))
