(defproject blogiseq "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [markdown-clj "0.9.89"]
                 [http-kit "2.1.18"]
                 [hiccup "1.0.5"]
                 [compojure "1.5.0"]]
  :profiles  {:uberjar  {:aot :all}}
  :main blogiseq.core)
