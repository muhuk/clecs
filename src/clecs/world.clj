(ns clecs.world
  "Protocols that define clecs API.

  #### Definitions

  World
  :   Worlds are top level containers. Entities,
      components are stored in worlds and systems
      are run within the context of worlds.

  Entity
  :   Objects in the world. Entities are merely
      identifiers, they do not store any information.
      This is an important difference from object
      oriented data structures, where all information
      related to an object is stored with the object.

      Clecs refers to entities as `entity-id` or
      `eid`. An entity-id can be an integer or a
      UUID or any other type depending on the
      world implementation.

  Component
  :   Components encode a single aspect about an
      entity. Entities can have any number of
      components and components can be added or
      removed from an entity during its lifetime.
      Therefore there is not necessarily a static
      set components for a set of entities. This
      is another difference from the object oriented
      approach which enforces a static template of
      aspects for each type of entity.

  System
  :   Systems are either maps (new style) or (old style)
      callables that define operations
      over the world. Each system should ideally
      deal with a unique aspect of the application.

      Systems may primarily deal with a single
      component but it is not a requirement.

  Query
  :   There are two kinds of queries in clecs:

      1. Queries for entities associated with certain
         components.
      1. Queries of a certain entity for its individual
         components.

      Queries can be only be run by systems.


  #### Relationships Between Concepts

      +++++++++++++++++++++++++++++++++++++
      |                                   |
      |  Main  +++++++++++++++++++++++++  |
      |  Loop  |                       |  |
      |        |  World                |  |
      |        |                       |  |
      |        |  +++++++++++++++++++  |  |
      |        |  |                 |  |  |
      |        |  |  Components     |  |  |
      |        |  |                 |  |  |
      |        |  +++++++++++++++++++  |  |
      |        |  |                 |  |  |
      |        |  |  Systems        |  |  |
      |        |  |                 |  |  |
      |        |  |  +++++++++++++  |  |  |
      |        |  |  |           |  |  |  |
      |        |  |  |  Queries  |  |  |  |
      |        |  |  |           |  |  |  |
      |        |  |  +++++++++++++  |  |  |
      |        |  |                 |  |  |
      |        |  +++++++++++++++++++  |  |
      |        |                       |  |
      |        +++++++++++++++++++++++++  |
      |                                   |
      +++++++++++++++++++++++++++++++++++++
  "
  (:require [clecs.component :refer [validate]]
            [clecs.world.validate :refer [validate-world]]))


(defprotocol IEditableWorld
  (-set-component
   [this eid cname cdata]
   "Sets a component without validating.

   Use [[set-component]] instead of calling this directly.

   #### Parameters:

   eid
   :   Entity id.

   cname
   :   Component name.

   cdata
   :   Component data as a map.
   ")
  (add-entity
   [this]
   "Create a new entity in the world and return its id.")
  (remove-component
   [this eid cname]
   "Remove the component of type `cname` that is associated
   with `eid` and return `nil`.

   This method is a no-op if there is no relevant component.

   #### Parameters:

   eid
   :   Entity id.

   cname
   :   Component name.
   ")
  (remove-entity
   [this eid]
   "Delete the entity with `eid`, all components
   associated with it and return `nil`.

   This method is a no-op if there is no relevant entity.

   #### Parameters:

   eid
   :   Entity id.
   "))


(defprotocol IQueryableWorld
  (-component
   [this cname]
   "Return component definition for `cname` or `nil`
   if none found.

   Use [[component]] to query a component for an entity.

   #### Parameters:

   cname
   :   Component name.
   ")
  (component
   [this eid cname]
   "Return the component of type `cname` associated with
   `eid`, or `nil` if none found.

   #### Parameters:

   eid
   :   Entity id.

   cname
   :   Component name.
   ")
  (query
   [this q]
   "Return a sequence of entity id's using `q` as
   filter criteria.

   #### Parameters:

   q
   :   A query object. See [[clecs.query]] for more
       info.
   "))


(defprotocol IWorld
  (-run
   [this reads writes f dt]
   "Run `f` passing it an editable world and `dt`. Return world.

   Use [[process!]] instead of calling `-run` directly.

   #### Parameters:

   reads
   :   Components that are readable for `f`.

   writes
   :   Components that are readable & writable for `f`.

   f
   :   A function that takes an editable world and
       a time increment as parameters.

   dt
   :   Time passed since this function is last run.
   ")
  (process!
   [this dt]
   "Run systems using `dt` as time increment.

   This is the function that will be called in
   the main loop.

   #### Parameters:

   dt
   :   Time passed since process! was called last
       time. This value is passed to the systems.
       It is recommended to use miliseconds as
       unit.
   "))


(defprotocol IWorldFactory
  (-supported-types
   [this]
   "Component parameter types supported by this backend.")
  (-world
   [this components systems extra-config]
   "Creates a world.

   #### Parameters:

   components
   :   A map of component names to components.

   systems
   :   A map of system names to systems.

   extra-config
   :   A map containing other elements passed
       to [[world]]. Some backends may require
       certains parameters other than components
       and systems.

   Use [[world]] instead of calling this directly."))


(defn set-component
  "Set `eid`'s `cname` component as `cdata` and return
  `nil`.

  #### Parameters:

  world
  :   World.

  eid
  :   Entity id.

  cname
  :   Component type.

  cdata
  :   Component data as a map."
  [world eid cname cdata]
  (if-some [c (-component world cname)]
           (do
             (validate c cdata)
             (-set-component world eid cname cdata)
             nil)
           (throw (RuntimeException. (str "Unknown component " cname)))))


(defn world
  "Creates a world.

  #### Parameters:

  world-factory
  :   A instance of [[IWorldFactory]].

  params
  :   A map of world parameters. Keys recognized by
      all backends are described below:

      :components
      :   A sequence of [[clecs.component/component]]
          instances. At least one component must be
          specified.

      :initializer
      :   A function that takes an editable world and
          run as soon as the world is created.

      :systems
      :   A sequence of [[clecs.system/system]]
          instances. At least one system must be
          specified.

      Any other keys will be passed to the factory.
      See the backend's documentation for recognized
      parameters.


  #### Examples:

      (clecs.world/world atom-world-factory
                         {:components [(component ...)
                                       (component ...)
                                       (component ...)
                                       ...]
                          :initializer (fn [w] ...)
                          :systems [(system ...)
                                    (system ...)
                                    ...]})
  "
  [world-factory
   {components :components
    initializer :initializer
    systems :systems :as params}]
  (validate-world components
                  systems
                  (-supported-types world-factory))
  (let [systems-map (->> systems
                         (map (juxt :name identity))
                         (into {}))
        components-map (->> components
                            (map (juxt :name identity))
                            (into {}))
        extra-config (dissoc params
                             :components
                             :initializer
                             :systems)
        w (-world world-factory
                  components-map
                  systems-map
                  extra-config)]
    (if initializer
      (-run w
            components-map
            components-map
            (fn [w _] (initializer w))
            nil)
      w)))
