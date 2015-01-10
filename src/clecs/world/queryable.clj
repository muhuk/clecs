(ns clecs.world.queryable)


(defprotocol IQueryableWorld
  (component
   [this eid ctype]
   "Return the component of type `ctype` associated with
   `eid`, or `nil` if none found.

   #### Parameters:

   eid
   :   Entity id.

   ctype
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
