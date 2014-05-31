(ns clecs.core
  (:require [clecs.backend.atom-world :as atom-world]))

(defn make-world [initializer]
  (atom-world/make-world initializer))
