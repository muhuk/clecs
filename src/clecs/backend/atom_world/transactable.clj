(ns clecs.backend.atom-world.transactable
  {:no-doc true})


(def ^:dynamic *state*)


(defn -transaction! [world f]
  (swap! (.state world)
         (fn [state]
           (binding [*state* state]
             (f (.editable-world world))
             *state*)))
  nil)
