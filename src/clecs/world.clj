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
  its contents. The objects extend `IWorld` in this mode.

  Systems are called with two parameters; a reference
  to an editable world and the since last time the
  world is processed. The world that is passed into
  the system extends `IQueryableWorld` and
  `IEditableWorld`. Systems run inside a transaction.

  Modes of execution and relevant protocols are
  summarized in the table below:

  | Processing  | In Transaction | IWorld | IQueryableWorld | IEditableWorld |
  | :----------:|:--------------:|:------:|:---------------:|:--------------:|
  |  ✘  |  ✘  |  ✔  |  ✘  |  ✘  |
  |  ✔  |  ✔  |  ✘  |  ✔  |  ✔  |
  ")


(defprotocol IEditableWorld
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
   :   Component type.")
  (remove-entity
   [this eid]
   "Delete the entity with `eid`, all components
   associated with it and return `nil`.

   This method is a no-op if there is no relevant entity.

   #### Parameters:

   eid
   :   Entity id.")
  (set-component
   [this eid cname cdata]
   "Set `eid`'s `cname` component as `cdata` and return
   `nil`.

   #### Parameters:

   eid
   :   Entity id.

   cname
   :   Component type.

   cdata
   :   Component data as a map."))


(defprotocol IQueryableWorld
  (component
   [this eid cname]
   "Return the component of type `cname` associated with
   `eid`, or `nil` if none found.

   #### Parameters:

   eid
   :   Entity id.

   cname
   :   Component type. If you have a reference to a
       `component` instance, use `(type component)`.")
  (query
   [this q]
   "Return a sequence of entity id's using `q` as
   filter criteria.

   #### Parameters:

   q
   :   A query object. See [[clecs.query]] for more
       info."))


(defprotocol IWorld
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
       unit."))
