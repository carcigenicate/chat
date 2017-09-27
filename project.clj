(defproject chat "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [quil "2.6.0"]
                 [helpers "1"]
                 [org.clojure/core.async "0.3.443"]]

  :main chat.main

  :aot :all)

  ;:target-path "target/%s"

  ;:resource-paths ["lib/jfxrt.jar"]

  ;:profiles {:uberjar {:aot :all}})
