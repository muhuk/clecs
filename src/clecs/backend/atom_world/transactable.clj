(ns clecs.backend.atom-world.transactable)


(def ^:dynamic *state*)


(defn -transaction! [world f]
  (swap! (.state world)
         (fn [state]
           (binding [*state* state]
             (f (.editable-world world))
             *state*)))
  nil)
