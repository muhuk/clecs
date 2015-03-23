(defproject clecs "2.0.1-SNAPSHOT"
  :description "Entity-component-system for Clojure."
  :url "https://github.com/muhuk/clecs"
  :license {:name "GNU GPL v3"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]]
  :plugins [[codox "0.8.10"]
            [jonase/eastwood "0.2.1"]
            [lein-asciidoctor "0.1.14"]
            [lein-cloverage "1.0.2"]
            [lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}}
  :codox {:defaults {:doc/format :markdown}
          :src-dir-uri "http://github.com/muhuk/clecs/blob/master/"
          :src-linenum-anchor-prefix "L"
          :output-dir "target/doc/api"}
  :asciidoctor {:extract-css true
                :format :html5
                :source-highlight true
                :sources "doc/*.adoc"
                :to-dir "target/doc/user_guide"
                :toc :left}
  :deploy-repositories {"releases" :clojars
                        "snapshots" :clojars}
  :aliases {"all" ["with-profile" "dev:dev,1.6"]
            "docs" ["do"
                    "doc,"
                    "asciidoctor,"
                    "cloverage" "-o" "target/doc/coverage"]})
