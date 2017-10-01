(ns chat.failed-javafx.main

  (:require [chat.client :as c]
            [chat.failed-javafx.guts :as gg]
            [chat.failed-javafx.javafx-wrapper :as jw])

  (:gen-class :extends javafx.application.Application))

(defn -start [self stage]
  (println "Starting!")
  (jw/initialize-swing)
  (gg/start stage))

(defn main [& args]
  (println "Main!")
  (jw/launch chat.graphic_client.main))