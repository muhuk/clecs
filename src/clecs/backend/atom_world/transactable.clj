(ns clecs.backend.atom-world.transactable)


(def ^:dynamic *state*)


(defn -transaction! [world f]
  (when (bound? #'*state*)
    (throw (IllegalStateException. "Already in a transaction.")))
  (swap! (.state world)
         (fn [state]
           (binding [*state* state]
             (f world)
             *state*)))
  nil)
