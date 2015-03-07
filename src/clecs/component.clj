(ns clecs.component
  (:refer-clojure :exclude [name])
  (:require [clojure.string :refer [join]]))


(declare make-validator)


(defrecord Component [name params validate])


(defmacro component
  "Creates a component definition.

  #### Parameters:

  cname
  :   A keyword to be used to refer to this component
      later.

  cdef
  :   A map of component parameter names to their types.

  #### Examples:

      ;; A marker component.
      (component Renderable nil)

      ;; Components can have any number of parameters.
      (component HitPoints {hp Integer})
      (component Point {x Int y Integer})
      (component Player {name String
                         team Integer
                         alive Boolean})

  See also [[clecs.world/world]]."
  [name params]
  `(->Component ~name
                ~params
                ~(make-validator name params)))


(defn- make-validator [cname cdef]
  (let [parameter-count (count cdef)
        parameter-names (keys cdef)
        wrong-parameters-error (str cname
                                    " takes ["
                                    (join ", " parameter-names)
                                    "] parameters, you have passed [")]
    `(fn [~'cdata]
       (when-not (and (= (count ~'cdata) ~parameter-count)
                      ~@(map (fn [param-name] `(contains? ~'cdata ~param-name))
                             parameter-names))
         (throw (RuntimeException. (str ~wrong-parameters-error
                                        (join ", " (keys ~'cdata))
                                        "]"))))
       ~@(for [[parameter-name parameter-type] cdef
               :let [error-message (str parameter-name
                                        " is not a valid "
                                        parameter-type)]]
           `(when-not (validate-param ~parameter-type (~'cdata ~parameter-name))
              (throw (RuntimeException. ~error-message))))
       nil)))


(defn validate
  "Validate component data against a component definition.

  Throws `RuntimeException` if validation fails.

  #### Parameters:

  c
  :   Component definition to validate against.

  cdata
  :   A map containing component data.
  "
  [^Component c cdata]
  ((.validate c) cdata))


(defmulti validate-param
  "Validates a parameter.

  #### Parameters:

  parameter-type
  :   Parameter type to validate against.

  parameter-value
  :   Parameter value to validate.
  "
  (fn [parameter-type _] parameter-type))
(defmethod validate-param Boolean [_ v] (or (true? v) (false? v)))
(defmethod validate-param Integer [_ v] (integer? v))
(defmethod validate-param String [_ v] (string? v))
