(ns clecs.backend.atom-world.transactable-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clecs.backend.atom-world :refer [make-world]]
            [clecs.backend.atom-world.transactable :refer :all]
            [clecs.world :as world]))


(defchecker editable-world? [w]
  (let [t (type w)]
    (and
     (extends? world/IEditableWorld t)
     (extends? world/IQueryableWorld t))))


(fact "-transaction! calls function with an editable version of world."
      (let [w (make-world ..state..)]
        (-transaction! w --f--) => nil
        (provided (--f-- editable-world?) => ..new-state..)))
