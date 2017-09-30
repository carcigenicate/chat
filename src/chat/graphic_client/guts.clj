(ns chat.graphic-client.guts

  (:require [chat.client :as c]
            [chat.graphic-client.javafx-wrapper :as jw])

  (:import [javafx.application Application Platform]
           [javafx.stage Stage]
           [javafx.scene.control Button TextField]
           [javafx.scene.layout StackPane Pane BorderPane]
           [javafx.scene Scene]
           [chat.client Client]))

(def test-username "TEST_GRAPHIC_CLIENT")
(def test-address "localhost")
(def test-port 5555)

(def client! (atom nil))

(defn send-button ^Button [^TextField source-field]
  (jw/new-button "Send"
     (fn [_] (if @client!
               (c/write @client! (.getText source-field))
               (println "Not connected!")))))

; Potentially blocks on app thread?
(defn connect-button ^Button []
  (jw/new-button "Connect"
     (fn [_] (reset! client! (c/connect test-username test-address test-port)))))

(defn compose-box ^TextField []
  (let [^TextField tf (TextField.)]
    tf))

(defn messaging-scene []
  (let [^BorderPane sp (BorderPane.)
        ^Scene scene (Scene. sp 500 500)
        compose-tf (compose-box)
        send-btn (send-button compose-box)
        connect-btn (connect-button)]

    (.setTop sp connect-btn)
    (.setCenter sp compose-tf)
    (.setBottom sp send-btn)))

(defn start [^Stage stage]
  (Platform/runLater
    (fn []
      (let [scene (messaging-scene)]
        (.setScene stage scene)
        (.show stage)))))