(defproject clecs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[jonase/eastwood "0.1.0"]
            [lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
