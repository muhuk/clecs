(defproject clecs "0.2.2-SNAPSHOT"
  :description "Entity-component-system for Clojure."
  :url "https://github.com/muhuk/clecs"
  :license {:name "GNU GPL v3"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[codox "0.8.8"]
            [jonase/eastwood "0.1.4"]
            [lein-cloverage "1.0.2"]
            [lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]
                                  [org.clojure/test.check "0.5.9"]]}}
  :codox {:defaults {:doc/format :markdown}
          :src-dir-uri "http://github.com/muhuk/clecs/blob/master/"
          :src-linenum-anchor-prefix "L"
          :output-dir "target/doc/api"}
  :deploy-repositories {"releases" :clojars}
  :aliases {"quickcheck" ["run" "-m" "clecs.world.check"]})
