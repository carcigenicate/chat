(ns chat.graphics-tests.button-client
  (:require [chat.client :as c]
            [chat.graphics-tests.javafx-wrapper :as jw])

  (:import [javafx.application Application]
           (javafx.stage Stage)
           (javafx.scene.control Button)
           (javafx.scene.layout StackPane)
           (javafx.scene Scene))

  (:gen-class :extends javafx.application.Application))

(defn -start [self ^Stage stage]
  (let [^Button b (Button. "Send!")
        ^StackPane sp (StackPane.)
        ^Scene scene (Scene. sp 500 500)]

    (.setOnAction b
      (jw/event-handler (fn [e] (println "Nothing yet!"))))

    (jw/add-child sp b)

    (.setScene stage scene)
    (.show stage)))

(defn -main [& args]
  (jw/launch chat.graphics_tests.button_client))