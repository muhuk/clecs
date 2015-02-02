(ns clecs.world.system)


;; TODO Rename this as IWorld
(defprotocol ISystemManager
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
