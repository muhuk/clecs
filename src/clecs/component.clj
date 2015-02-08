(ns clecs.component)


(declare make-validator)


(defmacro component [cname cdef]
  `{:cname ~cname
    :valid? ~(make-validator cdef)})


(defn- make-validator [cdef]
  `(fn [c#]
     (= (count c#) ~(count cdef))))


(defn valid? [c cdata]
  ((:valid? c) cdata))
