(ns chat.graphic-client.main

  (:require [chat.client :as c]
            [chat.graphic-client.guts :as gg]
            [chat.graphic-client.javafx-wrapper :as jw])

  (:gen-class :extends javafx.application.Application))

(defn -start [self stage]
  (println "Starting!")
  (jw/initialize-swing)
  (gg/start stage))

(defn main [& args]
  (println "Main!")
  (jw/launch chat.graphic_client.main))