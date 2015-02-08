(ns clecs.component)


(declare make-validator)


(defmacro component [ctype cdef]
  `{:ctype ~ctype
    :valid? ~(make-validator cdef)})


(defn- make-validator [cdef]
  `(fn [c#]
     (= (count c#) ~(count cdef))))


(defn valid? [c cdata]
  ((:valid? c) cdata))
