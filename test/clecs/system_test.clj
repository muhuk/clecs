(ns clecs.system-test
  (:require [clecs.system :refer :all]
            [midje.sweet :refer :all]))


(defn mock-system [_ _] nil)


(fact "system requires :name, :process or :process-fn and :reads or :writes."
      (let [s {:name :FooSystem
               :process 'clecs.system-test/mock-system
               :process-fn mock-system
               :reads #{:BarComponent}
               :writes #{:BazComponent}}]
        (system nil) => (throws IllegalArgumentException)
        (system (select-keys s [:name :process])) => (throws IllegalArgumentException
                                                             "Either :reads or :writes must be specified")
        (system (select-keys s [:name :process-fn])) => (throws IllegalArgumentException
                                                                "Either :reads or :writes must be specified")
        (system (select-keys s [:name :reads])) => (throws IllegalArgumentException
                                                           "Either :process or :process-fn must be specified")
        (system (select-keys s [:name :writes])) => (throws IllegalArgumentException
                                                            "Either :process or :process-fn must be specified")
        (system (select-keys s [:process :reads])) => (throws IllegalArgumentException
                                                              ":name is required for systems")
        (system (select-keys s [:process :writes])) => (throws IllegalArgumentException
                                                               ":name is required for systems")
        (system (select-keys s [:process-fn :reads])) => (throws IllegalArgumentException
                                                                 ":name is required for systems")
        (system (select-keys s [:process-fn :writes])) => (throws IllegalArgumentException
                                                                  ":name is required for systems")))


(fact ":process must be a symbol."
      (let [s {:name :FooSystem
               :reads #{:FooComponent}}]
        (system (assoc s :process nil)) => (throws IllegalArgumentException
                                                   ":process must be a symbol")
        (system (assoc s :process 1)) => (throws IllegalArgumentException
                                                 ":process must be a symbol")
        (system (assoc s :process true)) => (throws IllegalArgumentException
                                                    ":process must be a symbol")
        (system (assoc s :process (fn [& _] nil))) => (throws IllegalArgumentException
                                                              ":process must be a symbol")))


(fact ":process-fn must be a function."
             (let [s {:name :FooSystem
                      :reads #{:FooComponent}}]
               (system (assoc s :process-fn nil)) => (throws IllegalArgumentException
                                                             ":process-fn must be a function.")
               (system (assoc s :process-fn 1)) => (throws IllegalArgumentException
                                                           ":process-fn must be a function.")
               (system (assoc s :process-fn true)) => (throws IllegalArgumentException
                                                              ":process-fn must be a function.")
               (system (assoc s :process-fn 'foo)) => (throws IllegalArgumentException
                                                              ":process-fn must be a function.")))


(fact "If :process-fn is missing resolve it using :process."
      (system {:name :FooSystem
               :process 'clecs.system-test/mock-system
               :reads #{:FooComponent}}) => (contains [[:process-fn (exactly mock-system)]]))


(fact "At least one component must be specified in :reads or :writes."
      (let [s {:name :FooSystem
               :process-fn (fn [& _] nil)
               :reads nil
               :writes nil}
            s-reads (assoc s :reads #{:BarComponent})
            s-writes (assoc s :writes #{:BazComponent})
            s-reads-writes (-> s
                               (assoc :reads #{:BarComponent})
                               (assoc :writes #{:BazComponent}))]
        (system s) => (throws IllegalArgumentException
                              "At least one component must be specified")
        (system s-reads) =not=> (throws IllegalArgumentException
                                        "At least one component must be specified")
        (system s-writes) =not=> (throws IllegalArgumentException
                                         "At least one component must be specified")
        (system s-reads-writes) =not=> (throws IllegalArgumentException
                                               "At least one component must be specified")))


(fact "Any component in :writes implies :reads, remove them from :reads."
      (system {:name :FooSystem
               :process-fn (fn [& _] nil)
               :reads #{:BarComponent :BazComponent}
               :writes #{:BazComponent}}) => (contains [[:reads #{:BarComponent}]
                                                        [:writes #{:BazComponent}]]))


(fact ":reads & :writes are converted to sets."
      (let [s {:name :FooSystem
               :process-fn (fn [& _] nil)}]
        (system (assoc s :reads [:FooComponent])) => (contains [[:reads #{:FooComponent}]])
        (system (assoc s :reads '(:FooComponent))) => (contains [[:reads #{:FooComponent}]])
        (system (assoc s :writes [:FooComponent])) => (contains [[:writes #{:FooComponent}]])
        (system (assoc s :writes '(:FooComponent))) => (contains [[:writes #{:FooComponent}]])))


(future-fact "serializable?")


;; Deserialize is just calling system on the result of serialize.
(future-fact "serialize")
