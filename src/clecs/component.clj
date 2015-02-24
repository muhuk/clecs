(ns clecs.component
  (:require [clojure.string :refer [join]]))


(declare make-validator)


(def Bool {:name 'Bool
           :validate #(or (true? %) (false? %))})
(def Int {:name 'Int
          :validate integer?})
(def Str {:name 'Str
          :validate string?})
(def ^:private parameter-types {'Bool Bool
                                'Int Int
                                'Str Str})


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
      (component HitPoints {hp Int})
      (component Point {x Int y Int})
      (component Player {name Str
                         team Int
                         alive Bool})

  See also [[clecs.world/world]]."
  [cname cdef]
  (doseq [parameter-type (set (vals cdef))]
    (when-not (contains? parameter-types parameter-type)
      (throw (RuntimeException. (str "Unknown parameter type '" parameter-type "'")))))
  `{:cname ~cname
    :validate ~(make-validator cname cdef)})


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
                                        parameter-type)
                     validator-fn (:validate (parameter-types parameter-type))]]
           `(when-not (~validator-fn (~'cdata ~parameter-name))
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
  [c cdata]
  ((:validate c) cdata))
