(ns chat.graphics-tests.button-client
  (:require [chat.client :as c]
            [chat.graphics-tests.javafx-wrapper :as jw])

  (:import [javafx.application Application Platform]
           (javafx.stage Stage)
           (javafx.scene.control Button TextField)
           (javafx.scene.layout StackPane Pane BorderPane)
           (javafx.scene Scene)
           (javafx.embed.swing JFXPanel))

  (:gen-class :extends javafx.application.Application))

(jw/initialize-swing)

(defn send-button ^Button []
  (let [^Button b (Button. "Send!")]
    (.setOnAction b
      (jw/event-handler
        (fn [e] (println "Nothing yet!"))
        #_
        (fn [e] (c/send-out "Test!"))))

    b))

(defn message-box ^TextField [width height]
  (let [^TextField tf (TextField.)]
    tf))

(defn -start [self ^Stage stage]
  (let [^BorderPane sp (BorderPane.)
        ^Scene scene (Scene. sp 500 500)
        b (send-button)
        tf (message-box 100 100)]

    ; Set to setTop, setBottom
    (.setBottom sp b)
    (.setCenter sp tf)

    (.setScene stage scene)
    (.show stage)))

(defn -main [& args]
  (jw/launch chat.graphics_tests.button_client))