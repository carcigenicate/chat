(ns chat.graphics-tests.first
  (:require [chat.graphics-tests.javafx-wrapper :as jw])

  (:import [javafx.stage Stage]
           [javafx.scene.control Button]
           [javafx.scene.layout StackPane]
           [javafx.scene Scene]
           [javafx.application Application])

  (:gen-class :extends javafx.application.Application))

(defn -start [self ^Stage stage]
  (let [^Button b (Button. "Hello World")
        ^StackPane pane (StackPane.)
        ^Scene scene (Scene. pane 500 500)]

    (.setOnAction b
      (jw/event-handler
        (fn [e] (println "HELLO WORLD!"))))

    (jw/add-child pane b)

    (.setScene stage scene)
    (.show stage)))

(defn -main [& args]
  (jw/launch chat.graphics_tests.first))

