(ns clecs.world.validate
  (:require [clojure.set :refer [difference union]]))


(defn ^:no-doc validate-world [components systems]
  (cond
   (empty? components) (throw (RuntimeException.
                               "You must provide at least one component."))
   (empty? systems) (throw (RuntimeException.
                            "You must provide at least one system.")))
  (let [component-names (set (map :name components))
        system-components (into {}
                                (map #(vector (:name %)
                                              (union (:reads %) (:writes %)))
                                     systems))
        components-used (apply union (vals system-components))]
    (let [unused-components (difference component-names components-used)]
      (when (seq unused-components)
        (throw (RuntimeException. (str "These components are not used by any system: "
                                       unused-components)))))
    (doseq [[s cs] system-components]
      (let [unknown-components (difference cs component-names)]
        (when (seq unknown-components)
          (throw (RuntimeException. (str s
                                         " is using unknown components: "
                                         unknown-components))))))))
