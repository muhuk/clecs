(ns clecs.world.check.generators
  {:no-doc true}
  (:require [clecs.component :refer [defcomponent]]
            [clojure.test.check.generators :as gen]))


(defn make-value [g]
  (gen/fmap #(hash-map :type :value, :value %) g))


(defcomponent Component01 [eid])
(defcomponent Component02 [eid])
(defcomponent Component03 [eid x])
(defcomponent Component04 [eid x y])
(defcomponent Component05 [eid x y z])


(def system-count 16)


(declare make-gen-protocol-editable-world
         make-gen-protocol-queryable-world
         make-gen-protocol-system-manager
         make-gen-protocol-transactable-world)


(def gen-eid gen/s-pos-int)


(def gen-component (gen/one-of [(gen/fmap (partial apply ->Component01)
                                          (gen/tuple gen-eid))
                                (gen/fmap (partial apply ->Component02)
                                          (gen/tuple gen-eid))
                                (gen/fmap (partial apply ->Component03)
                                          (gen/tuple gen-eid gen/any))
                                (gen/fmap (partial apply ->Component04)
                                          (gen/tuple gen-eid gen/any gen/any))
                                (gen/fmap (partial apply ->Component05)
                                          (gen/tuple gen-eid gen/any gen/any gen/any))]))


(def gen-clabel (gen/elements (map (comp :component-type meta) [Component01
                                                                Component02
                                                                Component03
                                                                Component04
                                                                Component05])))


(def gen-dt gen/s-pos-int)


(def gen-query (gen/return (fn [& _] false)))


(defn make-gen-system []
  (gen/hash-map :type (gen/return :system)
                :commands (gen/not-empty
                           (gen/vector
                            (gen/one-of [(make-gen-protocol-transactable-world)
                                         (make-gen-protocol-queryable-world)])))))


(def gen-system-name (gen/elements (doall (for [i (range system-count)]
                                            (keyword (str "system-" (inc i)))))))


(defn make-gen-transaction []
  (gen/hash-map :type (gen/return :transaction)
                :commands (gen/not-empty
                           (gen/vector
                            (gen/one-of [(make-gen-protocol-editable-world)
                                         (make-gen-protocol-queryable-world)])))))


;; Outside of processing
(def gen-cmd-process! (gen/hash-map :type (gen/return :command)
                                    :method (gen/return `world/process!)
                                    :params (gen/tuple (make-value gen-dt))))


(def gen-cmd-remove-system! (gen/hash-map :type (gen/return :command)
                                          :method (gen/return `world/remove-system!)
                                          :params (gen/tuple (make-value gen-system-name))))


(defn make-gen-cmd-set-system! []
  (gen/hash-map :type (gen/return :command)
                :method (gen/return `world/set-system!)
                :params (gen/tuple (make-value gen-system-name)
                                   (make-gen-system))))


(def gen-cmd-systems (gen/hash-map :type (gen/return :command)
                                   :method (gen/return `world/systems)
                                   :params (gen/return [])))


;; During processing
(defn make-gen-cmd-transaction! []
  (gen/hash-map :type (gen/return :command)
                :method (gen/return `world/transaction!)
                :params (gen/tuple (make-gen-transaction))))



;; In transaction
(def gen-cmd-add-entity (gen/hash-map :type (gen/return :command)
                                      :method (gen/return `world/add-entity)
                                      :params (gen/return [])))


(def gen-cmd-remove-component (gen/hash-map :type (gen/return :command)
                                            :method (gen/return `world/remove-component)
                                            :params (gen/tuple (make-value gen-eid)
                                                               (make-value gen-clabel))))


(def gen-cmd-remove-entity (gen/hash-map :type (gen/return :command)
                                         :method (gen/return `world/remove-entity)
                                         :params (gen/tuple (make-value gen-eid))))


(def gen-cmd-set-component (gen/hash-map :type (gen/return :command)
                                         :method (gen/return `world/set-component)
                                         :params (gen/tuple (make-value gen-component))))



;; Querying
(def gen-cmd-component (gen/hash-map :type (gen/return :command)
                                     :method (gen/return `world/component)
                                     :params (gen/tuple (make-value gen-eid)
                                                        (make-value gen-clabel))))


(def gen-cmd-query (gen/hash-map :type (gen/return :command)
                                 :method (gen/return `world/query)
                                 :params (gen/tuple gen-query)))



;; Protocols
(defn make-gen-protocol-editable-world []
  (gen/one-of [gen-cmd-add-entity
               gen-cmd-remove-component
               gen-cmd-remove-entity
               gen-cmd-set-component]))


(defn make-gen-protocol-system-manager []
  (gen/one-of [gen-cmd-process!
               gen-cmd-remove-system!
               (make-gen-cmd-set-system!)
               gen-cmd-systems]))


(defn make-gen-protocol-transactable-world []
  (make-gen-cmd-transaction!))


(defn make-gen-protocol-queryable-world []
  (gen/one-of [gen-cmd-component
               gen-cmd-query]))



;; Top Level

(defn make-gen-app []
  (gen/hash-map :type (gen/return :app)
                :initializer (make-gen-transaction)
                :commands (gen/not-empty (gen/vector (make-gen-protocol-system-manager)))))
