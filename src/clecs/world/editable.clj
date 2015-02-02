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
   [this eid ctype cdata]
   "Set `eid`'s `ctype` component as `cdata` and return
   `nil`.

   #### Parameters:

   eid
   :   Entity id.

   ctype
   :   Component type.

   cdata
   :   Component data as a map."))
