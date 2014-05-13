(defproject clecs "0.2.1-SNAPSHOT"
  :description "Entity-component-system for Clojure."
  :url "https://github.com/muhuk/clecs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[jonase/eastwood "0.1.2"]
            [lein-cloverage "1.0.2"]
            [lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
