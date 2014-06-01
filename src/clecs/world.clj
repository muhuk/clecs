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

      Clects refers to entities as ``entity-id`` or
      ``eid``.

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

      See also [[clecs.component/IComponent]].

  System
  :   Systems are callables that define operations
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
      1. Queries for individual components.

      Queries can be only be run by systems. Queries
      can be run within transactions and also outside
      of transactions. Note that changes within
      transaction will be reflected to the queries run
      within transactions.

  #### Modes of Execution

  There are three modes of execution for worlds.
  Functionality available depends on these modes.

  When you first create a world, you are *outside of
  processing*. You can add, remove and query
  systems. You can also process the world. Processing
  means running all the systems and possibly modift
  its contents. The objects extend ``ISystemManager``
  in this mode.

  Systems are called with two parameters; a reference
  to the world that is *inside processing but outside
  of transactions* and the time since last time the
  world is processed. You can run queries and start
  transactions. The world that is passed into the system
  extends ``IQueryableWorld`` and ``ITransactableWorld``.

  When a transaction is started a world reference that
  is *inside a transaction* is passed to the transaction
  function. World modifying functions as well as queries
  are available in this mode. The world reference that
  is provided extends ``IQueryableWorld`` and
  ``IEditableWorld``.

  Modes of execution and relevant protocols are
  summarized in the table below:

  | Processing  | In Transaction | ISystemManager | ITransactableWorld | IQueryableWorld | IEditableWorld |
  | :----------:|:--------------:|:--------------:|:------------------:|:---------------:|:--------------:|
  |  ✘  |  ✘  |  ✔  |  ✘  |  ✘  |  ✘  |
  |  ✔  |  ✘  |  ✘  |  ✔  |  ✔  |  ✘  |
  |  ✔  |  ✔  |  ✘  |  ✘  |  ✔  |  ✔  |
  ")


(defprotocol IEditableWorld
  (add-entity [this])
  (remove-component [this eid ctype])
  (remove-entity [this eid])
  (set-component [this c]))


(defprotocol IQueryableWorld
  (component [this eid ctype])
  (query [this q]))


(defprotocol ISystemManager
  (process! [this dt])
  (remove-system! [this slabel])
  (set-system! [this slabel s])
  (systems [this]))


(defprotocol ITransactableWorld
  (transaction! [this f]))
