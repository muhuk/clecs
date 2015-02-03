(ns clecs.component)


(declare make-validator)


(defmacro component [ctype cdata]
  `{:ctype ~ctype
    :valid? ~(make-validator cdata)})


(defn- make-validator [cdata]
  `(fn [c#]
     (= (count c#) ~(count cdata))))


(defn valid? [c cdata]
  ((:valid? c) cdata))
