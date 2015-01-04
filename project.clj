(defproject clecs "1.1.0-SNAPSHOT"
  :description "Entity-component-system for Clojure."
  :url "https://github.com/muhuk/clecs"
  :license {:name "GNU GPL v3"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha2"]]
  :plugins [[codox "0.8.10"]
            [jonase/eastwood "0.2.1"]
            [lein-cloverage "1.0.2"]
            [lein-midje "3.1.3"]]
  :profiles {:dev {:source-paths ["check"]
                   :dependencies [[midje "1.6.3"]
                                  [org.clojure/test.check "0.6.2"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}}
  :codox {:defaults {:doc/format :markdown}
          :src-dir-uri "http://github.com/muhuk/clecs/blob/master/"
          :src-linenum-anchor-prefix "L"
          :output-dir "target/doc/api"}
  :deploy-repositories {"releases" :clojars}
  :aliases {"all" ["with-profile" "dev:dev,1.6"]
            "quickcheck"
            ^{:doc (str "Run quick-check to compare a backend against clecs.backend.atom-world\n"
                        "\n"
                        "    lein quickcheck $PATH.TO.WORLD/INITIALIZER $SAMPLES")}
            ["run" "-m" "clecs.world.check"]})
