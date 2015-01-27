(ns clecs.backend.atom-world.transactable
  {:no-doc true})


;; TODO: Remove this module.


(def ^:dynamic *state*)


(defn transaction!
  ([world f]
   (swap! (.state world)
          (fn [state]
            (binding [*state* state]
              (f (.editable-world world))
              *state*)))
   nil)
  ([world f dt]
   (swap! (.state world)
          (fn [state]
            (binding [*state* state]
              (f (.editable-world world) dt)
              *state*)))
   nil))
