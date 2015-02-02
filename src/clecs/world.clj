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


  Transaction
  :   A transaction is a special context that allows
      modifications to the world. When a transaction
      is committed all operations are applied at once.
      If a transaction fails none of the operations
      take effect and the world doesn't change.
      Changes to the world are only visible within the
      transaction context during a transaction.
      Therefore worlds can be considered to be immutable
      outside of transactions even when underlying data
      storage is mutable.


  Query
  :   There are two kinds of queries in clecs:

      1. Queries for entities associated with certain
         components.
      1. Queries of a certain entity for its individual
         components.

      Queries can be only be run by systems.


  #### Modes of Execution

  There are three modes of execution for worlds.
  Functionality available depends on these modes.

  When you first create a world, you are *outside of
  processing*. You can add, remove and query
  systems. You can also process the world. Processing
  means running all the systems and possibly modify
  its contents. The objects extend `ISystemManager`
  in this mode.

  Systems are called with two parameters; a reference
  to an editable world and the since last time the
  world is processed. The world that is passed into
  the system extends `IQueryableWorld` and
  `IEditableWorld`. Systems run inside a transaction.

  Modes of execution and relevant protocols are
  summarized in the table below:

  | Processing  | In Transaction | ISystemManager | IQueryableWorld | IEditableWorld |
  | :----------:|:--------------:|:--------------:|:---------------:|:--------------:|
  |  ✘  |  ✘  |  ✔  |  ✘  |  ✘  |
  |  ✔  |  ✔  |  ✘  |  ✔  |  ✔  |
  "
  (:require [clecs.world.editable :as editable]
            [clecs.world.queryable :as queryable]
            [clecs.world.system :as system]))



;; Functions delegating to IEditableWorld
(defn add-entity [w] (editable/add-entity w))
(defn remove-component [w eid ctype] (editable/remove-component w eid ctype))
(defn remove-entity [w eid] (editable/remove-entity w eid))
(defn set-component [w eid ctype cdata] (editable/set-component w eid ctype cdata))


;; Functions delegating to IQueryableWorld
(defn component [w eid ctype] (queryable/component w eid ctype))
(defn query [w q] (queryable/query w q))


;; Functions delegating to ISystemManager
(defn process! [w dt] (system/process! w dt))
