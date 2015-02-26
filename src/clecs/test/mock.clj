(ns clecs.test.mock
  "Mock worlds that delegate their operations to functions.

  Because protocol method can't be mocked using `with-redefs`
  etc. systems and world backends are difficult to unit test.
  Paying one level of indirection, protocols can be mocked
  using functions defined in this module.

  #### Examples:

      (def foo-system [w dt]
        (doseq [eid (world/query w (all :FooComponent
                                        (any :BarComponent
                                             :BazComponent)))]
          (world/remove-component w eid :FooComponent)))

      (fact \"foo-system does stuff.\"
            (let [w (mock/mock-editable-world)
                  q (all :FooComponent
                         (any :BarComponent
                              :BazComponent))]
              (foo-system w anything) => anything
              (provided (mock/query w q) => [..e1.. ..e2..]
                        (mock/remove-component w ..e1.. :FooComponent) => nil
                        (mock/remove-component w ..e2.. :FooComponent) => nil)))
  "
  (:require [clecs.world :refer [IEditableWorld
                                 IQueryableWorld
                                 IWorld
                                 IWorldFactory]]))


(defmacro ^:private def-mock-fn [n]
  `(defn ~n [~'& ~'_]
     (throw (RuntimeException. ~(str n " is called directly.")))))


(def-mock-fn -component)
(def-mock-fn -run)
(def-mock-fn -set-component)
(def-mock-fn -world)
(def-mock-fn add-entity)
(def-mock-fn component)
(def-mock-fn process!)
(def-mock-fn query)
(def-mock-fn remove-component)
(def-mock-fn remove-entity)


(defn mock-editable-world []
  (reify
    IEditableWorld
    (-set-component [this eid cname cdata] (-set-component this eid cname cdata))
    (add-entity [this] (add-entity this))
    (remove-component [this eid cname] (remove-component this eid cname))
    (remove-entity [this eid] (remove-entity this eid))
    IQueryableWorld
    (-component [this cname] (-component this cname))
    (component [this eid cname] (component this eid cname))
    (query [this q] (query this q))))


(defn mock-world []
  (reify
    IWorld
    (-run [this reads writes f dt] (-run this reads writes f dt))
    (process! [this dt] (process! this dt))))


(def mock-world-factory
  (reify
    IWorldFactory
    (-world [this params] (-world this params))))
