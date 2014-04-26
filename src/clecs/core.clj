(ns clecs.core
  (:require [clecs.backend.atom-world :as atom-world]))

(defn make-world []
  (atom-world/make-world))
