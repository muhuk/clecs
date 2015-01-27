(ns clecs.backend.atom-world-test
  (:require [clecs.backend.atom-world :refer :all]
            [clecs.backend.atom-world.editable :as editable]
            [clecs.backend.atom-world.queryable :as queryable]
            [clecs.backend.atom-world.transactable :refer [*state*]]
            [clecs.test.checkers :refer :all]
            [clecs.world :as world]
            [clecs.world.editable :refer [IEditableWorld]]
            [clecs.world.queryable :refer [IQueryableWorld]]
            [clecs.world.system :refer [ISystemManager]]
            [midje.sweet :refer :all]))


(def editable-world-like (implements-protocols IEditableWorld
                                               IQueryableWorld))


;; World Initialization.

(fact "Atom world implements ISystemManager."
      (make-world --init--) => (implements-protocols ISystemManager))


(fact "Initialization function is called within a transaction."
      (make-world --init--) => irrelevant
      (provided (--init-- (as-checker editable-world-like)) => irrelevant))


;; System Operations

(fact "remove-system! unregisters the system with the system-label."
      (let [w (-> (make-world --init--)
                  (world/set-system! ..system-label.. {:process ..system..}))]
        (world/systems w) => (seq {..system-label.. {:process ..system..}})
        (world/remove-system! w ..system-label..) => w
        (world/systems w) => (seq {})))


(fact "set-system! registers a system with the system-label."
      (let [w (make-world --init--)]
        (world/systems w) => (seq {})
        (world/set-system! w ..system-label.. {:process ..system..}) => w
        (world/systems w) => (seq {..system-label.. {:process ..system..}})))


(fact "set-system! registers a function system by wrapping it into a map."
      (let [w (make-world --init--)
            sys-fn (fn [_ _] nil)]
        (world/systems w) => nil
        (world/set-system! w ..system-label.. sys-fn) => w
        (world/systems w) => (seq {..system-label.. {:process sys-fn}})))


(fact "systems returns a seq of [system-label system] pairs."
      (-> (make-world --init--)
          (world/set-system! ..system-label.. {:process ..system..})
          (world/systems)) => (seq {..system-label.. {:process ..system..}}))


;; Processing

(fact "process! returns the world."
      (let [w (make-world --init--)]
        (world/process! w ..dt..) => w))


(fact "process! calls each system with the world and delta time."
      (let [calls (atom [])
            f-one (fn [& args] (swap! calls conj [:one-called args]))
            f-two (fn [& args] (swap! calls conj [:two-called args]))
            w (-> (make-world --init--)
                  (world/set-system! ..l-one.. f-one)
                  (world/set-system! ..l-two.. f-two))]
        (world/process! w ..dt..) => irrelevant
        (set (map first @calls)) => (set [:one-called :two-called])
        (-> @calls first second first) => editable-world-like
        (-> @calls first second second) => ..dt..
        (-> @calls second second first) => editable-world-like
        (-> @calls second second second) => ..dt..))


;; Protocol delegation - IEditableWorld.

(fact "world/add-entity delegates to add-entity."
      (world/add-entity (->AtomEditableWorld)) => ..eid..
      (provided (editable/add-entity) => ..eid..))


(fact "world/remove-component delegates to remove-component."
      (let [world (->AtomEditableWorld)]
        (world/remove-component world ..eid.. ..component-type..) => world
        (provided (editable/remove-component ..eid.. ..component-type..) => nil)))


(fact "world/remove-entity delegates to remove-entity."
      (let [world (->AtomEditableWorld)]
        (world/remove-entity world ..eid..) => world
        (provided (editable/remove-entity ..eid..) => nil)))


(fact "world/set-component delegates to set-component."
      (let [world (->AtomEditableWorld)]
        (world/set-component world  ..c..) => world
        (provided (editable/set-component ..c..) => nil)))


;; Protocol delegation - IQueryableWorld.

(fact "world/component delegates to component."
      (binding [*state* ..state..]
        (world/component (->AtomEditableWorld) ..eid.. ..clabel..) => ..component..
        (provided (queryable/component ..state.. ..eid.. ..clabel..) => ..component..)))


(fact "world/query delegates to query"
      (binding [*state* ..state..]
        (world/query (->AtomEditableWorld) ..q..) => ..result..
        (provided (queryable/query *state* ..q..) => ..result..)))
