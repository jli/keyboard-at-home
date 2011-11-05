(defproject keyboard-sex "0.0.1-SNAPSHOT"
  :description "Keyboards loving each other over the internet."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.cli "0.1.0"]
                 [ring/ring-core "0.3.11"]
                 [ring/ring-devel "0.3.11"]
                 [ring/ring-jetty-adapter "0.3.11"]
                 [amalloy/ring-gzip-middleware "0.1.0"]
                 [compojure "0.6.5"]
                 [clj-http "0.2.3"]]
  :main keyboard-sex.core)
