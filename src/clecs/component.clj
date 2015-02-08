(ns clecs.component
  (:require [clojure.string :refer [join]]))


(declare make-validator)


(defmacro component [cname cdef]
  `{:cname ~cname
    :validate ~(make-validator cname cdef)})


(defn- make-validator [cname cdef]
  (let [parameter-count (count cdef)
        parameter-names (keys cdef)
        wrong-parameters-error (str cname
                                    " takes "
                                    (join ", " parameter-names)
                                    " parameters, you have passed ")]
    `(fn [~'cdata]
       (when-not (and (= (count ~'cdata) ~parameter-count)
                      ~@(map (fn [param-name] `(contains? ~'cdata ~param-name))
                             parameter-names))
         (throw (RuntimeException. (str ~wrong-parameters-error
                                        (join ", " (keys ~'cdata))
                                        "."))))

       nil)))


(defn validate [c cdata]
  ((:validate c) cdata))
