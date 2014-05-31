(ns clecs.core
  (:require [clecs.backend.atom-world :as atom-world]))

(defn make-world [initializer-fn]
  (atom-world/make-world initializer-fn))
