(ns clecs.world.validate
  (:require [clojure.set :refer [difference superset? union]]))


(declare -validate-components
         -validate-systems
         fail-when-extra)


(defn validate-world
  "Performs following checks:

  -   Components validation:
      - At least one component must be defined.
  -   Systems validation:
      - At least one system must be defined.
      - Each component must be used by at least one system.
      - Systems can't access to unknown components."
  [components systems supported-types]
  (-validate-components components supported-types)
  (-validate-systems components systems))


(defn ^:no-doc -validate-components [components supported-types]
  (when (empty? components)
    (throw (RuntimeException.
            "You must provide at least one component.")))
  (let [types-used (set (mapcat #(-> % (:params) (vals)) components))
        supported-types (set supported-types)]
    (when-not (superset? supported-types types-used)
      (throw (RuntimeException. (format "Parameter types %s are not supported."
                                        (difference types-used supported-types)))))))


(defn ^:no-doc -validate-systems [components systems]
  (when (empty? systems)
    (throw (RuntimeException.
            "You must provide at least one system.")))
  (let [components-defined (set (map :name components))
        components-by-system (into {}
                                   (map (fn [s]
                                          [(:name s)
                                           (union (:reads s) (:writes s))])
                                        systems))
        components-used (apply union (vals components-by-system))]
    (fail-when-extra components-defined
                     components-used
                     "These components are not used by any system: %s")
    (doseq [[s cs] components-by-system]
      (fail-when-extra cs
                       components-defined
                       (str s " is using unknown components: %s")))))


(defn- fail-when-extra [a b error-message]
  (when-let [diff (seq (difference a b))]
    (throw (RuntimeException. (format error-message diff)))))
