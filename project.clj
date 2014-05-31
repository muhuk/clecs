(defproject clecs "0.2.1-SNAPSHOT"
  :description "Entity-component-system for Clojure."
  :url "https://github.com/muhuk/clecs"
  :license {:name "GNU GPL v3"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[jonase/eastwood "0.1.2"]
            [lein-cloverage "1.0.2"]
            [lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
