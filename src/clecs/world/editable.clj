(ns clecs.world.editable)


(defprotocol IEditableWorld
  (add-entity
   [this]
   "Create a new entity in the world and return its id.")
  (remove-component
   [this eid ctype]
   "Remove the component of type `ctype` that is associated
   with `eid` and return `nil`.

   This method is a no-op if there is no relevant component.

   #### Parameters:

   eid
   :   Entity id.

   ctype
   :   Component type. If you have a reference to a
       `component` instance, use `(type component)`.")
  (remove-entity
   [this eid]
   "Delete the entity with `eid`, all components
   associated with it and return `nil`.

   This method is a no-op if there is no relevant entity.

   #### Parameters:

   eid
   :   Entity id.")
  (set-component
   [this c]
   "Add component `c` to the world and return `nil`.

   Entity id is already associated with `c`. If the
   entity already has a component with the same type it
   will be replaced.

   #### Parameters:

   c
   :   Component instance."))
