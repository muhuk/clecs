(ns clecs.backend.atom-world-test
  (:require [clecs.backend.atom-world :refer :all]
            [clecs.component :refer [component]]
            [clecs.query :refer [accessed]]
            [clecs.system :refer [system]]
            [clecs.test.checkers :refer :all]
            [clecs.world :as world]
            [midje.sweet :refer :all]))


(def editable-world-like (implements-protocols world/IEditableWorld
                                               world/IQueryableWorld))


;; World Initialization.


(fact "Supported types for atom-world."
      (world/-supported-types atom-world-factory) => (contains [Boolean
                                                                Integer
                                                                String]
                                                               :in-any-order))


(fact "Atom world implements IWorld."
      (world/-world atom-world-factory
                    nil
                    nil
                    nil) => (implements-protocols world/IWorld))


;; Processing

(fact "world/-run runs arbitrary code with an editable world, returns world."
      (let [w (world/-world atom-world-factory nil nil nil)]
        (world/-run w ..reads.. ..writes.. --f-- ..dt..) => w
        (provided (--f-- editable-world-like ..dt..) => anything)))


(fact "process! returns the world."
      (let [w (world/-world atom-world-factory nil nil nil)]
        (world/process! w ..dt..) => w))


(fact "process! calls each system with the world and delta time."
      (let [calls (atom [])
            s-one (system {:name :s-one
                           :process-fn (fn [& args] (swap! calls
                                                           conj
                                                           [:one-called args]))
                           :reads #{:Foo}})
            s-two (system {:name :s-two
                           :process-fn (fn [& args] (swap! calls
                                                           conj
                                                           [:two-called args]))
                           :reads #{:Foo}})
            w (world/-world atom-world-factory
                            nil
                            {:s-one s-one
                             :s-two s-two}
                            nil)]
        (world/process! w ..dt..) => irrelevant
        (set (map first @calls)) => (set [:one-called :two-called])
        (-> @calls first second first) => editable-world-like
        (-> @calls first second second) => ..dt..
        (-> @calls second second first) => editable-world-like
        (-> @calls second second second) => ..dt..))


(fact "process! calls -run with components filtered by system."
      (let [components {:Foo (component :Foo nil)
                        :Bar (component :Bar nil)
                        :Baz (component :Baz nil)}
            systems {:s1 (system {:name :s1
                                  :process-fn (fn [w dt] (--s1-- w dt))
                                  :reads #{:Bar}
                                  :writes #{:Foo}})
                     :2 (system {:name :s2
                                 :process-fn (fn [w dt] (--s2-- w dt))
                                 :reads #{:Baz}
                                 :writes #{:Bar}})}
            w (world/-world atom-world-factory
                            components
                            systems
                            nil)]
        (world/process! w ..dt..) => irrelevant
        (provided (->AtomEditableWorld #{:Foo :Bar}
                                       {:Foo (:Foo components)}) => ..editable-1..
                  (->AtomEditableWorld #{:Bar :Baz}
                                       {:Bar (:Bar components)}) => ..editable-2..
                  (--s1-- ..editable-1.. ..dt..) => irrelevant
                  (--s2-- ..editable-2.. ..dt..) => irrelevant)))


;; Editable world.

(fact "world/-component returns the component definition."
      (let [w (->AtomEditableWorld nil
                                   {::TestComponentA ..a..
                                    ::TestComponentB ..b..})]
        (world/-component w ::TestComponentA) => ..a..
        (world/-component w ::TestComponentB) => ..b..))


(fact "world/-component returns nil if component is not recognized."
      (let [w (->AtomEditableWorld nil {::TestComponentA ..c..})]
        (world/-component w ::TestComponentC) => nil))


(fact "world/-set-component adds the component if entity doesn't have one."
      (let [eid 1
            cdata {}
            w (->AtomEditableWorld #{::TestComponentA} nil)
            initial-state {:components {}
                           :entities {eid #{}}}
            expected-state {:components {::TestComponentA {eid cdata}}
                            :entities {eid #{::TestComponentA}}}]
        (binding [*state* initial-state]
          (world/-set-component w eid ::TestComponentA cdata) => w
          *state* => expected-state)))


(fact "world/-set-component replaces existing components."
      (let [eid 1
            c-old {:a ..a.. :b ..b..}
            c-new {:a ..c.. :b ..d..}
            w (->AtomEditableWorld #{::TestComponentB} nil)
            initial-state {:components {::TestComponentB {eid c-old}}
                            :entities {eid #{::TestComponentB}}}
            expected-state {:components {::TestComponentB {eid c-new}}
                            :entities {eid #{::TestComponentB}}}]
        (binding [*state* initial-state]
          (world/-set-component w eid ::TestComponentB c-new) => w
          *state* => expected-state)))


(fact "world/add-entity returns a new entity id."
      (binding [*state* {:last-entity-id 0}]
        (world/add-entity (->AtomEditableWorld nil nil)) => 1)
      (binding [*state* {:last-entity-id 41}]
        (world/add-entity (->AtomEditableWorld nil nil)) => 42))


(fact "world/add-entity adds the new entity-id to the entity index."
      (binding [*state* {:last-entity-id 0}]
        (let [eid (world/add-entity (->AtomEditableWorld nil nil))]
          (get-in *state* [:entities eid]) => #{})))


(fact "world/add-entity updates entity counter."
      (binding [*state* {:last-entity-id 0}]
        (world/add-entity (->AtomEditableWorld nil nil))
        (:last-entity-id *state*) => 1))


(fact "world/add-entity returns different values each time it's called."
      (let [w (->AtomEditableWorld nil nil)]
        (binding [*state* {:entities {}
                           :last-entity-id 0}]
          (repeatedly 42 #(world/add-entity w)) => (comp #(= % 42) count set))))


(fact "world/component resolves queried component."
      (binding [*state* {:components {::TestComponentA {..eid.. ..component..}}}]
        (let [w (->AtomEditableWorld #{::TestComponentA} nil)]
          (world/component w ..eid.. ::TestComponentA) => ..component..)))


(fact "world/component rejects unknown components."
      (let [w (->AtomEditableWorld {::TestComponentA ..c..} nil)]
        (world/component w ..eid.. ::TestComponentB) => (throws RuntimeException
                                                                #"Unknown component"
                                                                #"TestComponentB")))


(fact "world/query calls the query with a seq of component labels."
      (binding [*state* {:entities {..e1.. #{..c1.. ..c2..}
                                    ..e2.. #{..c2..}
                                    ..e3.. #{..c3..}}}]
        (world/query (->AtomEditableWorld nil nil) --q--) => (just [..e1.. ..e3..] :in-any-order)
        (provided (accessed anything) => #{}
                  (--q-- (just [..c1.. ..c2..] :in-any-order)) => true
                  (--q-- [..c2..]) => false
                  (--q-- [..c3..]) => true)))


(fact "world/query rejects queries trying to access unknown components."
      (world/query (->AtomEditableWorld #{::TestComponentA
                                          ::TestComponentC}
                                        nil) ..q..) => (throws RuntimeException
                                                               #"Unknown components"
                                                               #"TestComponentB")
      (provided (accessed ..q..) => #{::TestComponentB
                                      ::TestComponentC}))


(fact "world/remove-component works."
      (let [w (->AtomEditableWorld nil {..cname.. ..c..})
            initial-state {:components {..cname.. {..eid.. ..component..}}
                           :entities {..eid.. #{..cname..}}}
            expected-state {:components {..cname.. {}}
                            :entities {..eid.. #{}}}]
        (binding [*state* initial-state]
          (world/remove-component w ..eid.. ..cname..) => w
          *state* => expected-state)))


(fact "world/remove-component rejects unknown component."
      (let [w (->AtomEditableWorld nil {::TestComponentA ..c..})]
        (world/remove-component w ..eid.. ::TestComponentB) => (throws RuntimeException
                                                                       #"Unknown component"
                                                                       #"TestComponentB")))


(fact "world/remove-entity removes entity-id from entity index."
      (let [w (->AtomEditableWorld nil nil)]
        (binding [*state* {:components {}
                           :entities {1 #{}}
                           :last-entity-id 1}]
          (world/remove-entity w 1) => w
          *state* => {:components {}
                      :entities {}
                      :last-entity-id 1})))


(fact "world/remove-entity removes entity's components."
      (let [cname ::TestComponentA
            w (->AtomEditableWorld nil nil)
            initial-state {:components {cname {..eid.. ..i.. ..other-eid.. ..j..}}
                           :entities {}}
            expected-state {:components {cname {..other-eid.. ..j..}}
                            :entities {}}]
        (binding [*state* initial-state]
          (world/remove-entity w ..eid..) => w
          *state* => expected-state)))
