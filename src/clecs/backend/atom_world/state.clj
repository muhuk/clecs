(ns clecs.backend.atom-world.state)


(def ^:dynamic *state*)


(defn -ensure-no-transaction []
  (when (bound? #'*state*)
    (throw (IllegalStateException. "In a transaction."))))


(defn -ensure-transaction []
  (when-not (bound? #'*state*)
    (throw (IllegalStateException. "Not in a transaction."))))
