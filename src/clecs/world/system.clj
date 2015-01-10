(ns clecs.world.system)


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
       unit.")
  (remove-system!
   [this slabel]
   "Remove system registered with `slabel`.

   #### Parameters:

   slabel
   :   Label for the system to remove.")
  (set-system!
   [this slabel s]
   "Register system `s` with label `slabel`.

   The system will not be run until `process!`
   is called.

   #### Parameters:

   slabel
   :   A keyword to refer the system later.

   s
   :   System to register.")
  (systems
   [this]
   "Return a sequence of all registered systems."))
