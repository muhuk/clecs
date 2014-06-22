(ns clecs.world.check.generators
  (:require [clecs.world :as world]
            [clojure.test.check.generators :as gen]))


(def system-count 16)


(def gen-system-name (gen/elements (doall (for [i (range system-count)]
         (keyword (str "system-" (inc i)))))))


(def gen-system (gen/return (fn [world dt] (throw (RuntimeException. "Not Implemented.")))))


;; Outside of processing
(def gen-cmd-systems (gen/hash-map :method (gen/return `world/systems)
                                   :params (gen/return [])))


(def gen-cmd-set-system! (gen/hash-map :method (gen/return `world/set-system!)
                                       :params (gen/tuple gen-system-name gen-system)))


(def gen-cmd-remove-system! (gen/hash-map :method (gen/return `world/remove-system!)
                                          :params (gen/tuple gen-system-name)))


;; Protocols
(def gen-cmd-system-manager (gen/one-of [gen-cmd-systems
                                         gen-cmd-set-system!
                                         gen-cmd-remove-system!]))


(def gen-commands (gen/not-empty (gen/vector gen-cmd-system-manager)))
