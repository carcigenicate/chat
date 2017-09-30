(ns chat.graphic-client.javafx-wrapper
  (:import [javafx.application Application Platform]
           [javafx.event EventHandler]
           [javafx.scene Parent]
           [java.util List]
           [javafx.collections ObservableList]
           [javafx.embed.swing JFXPanel]
           [javafx.scene.control Button]))

(defn initialize-swing
  "No idea why this is necessary, but it must be run before multiple javafx.scene.controls can be imported.
  Should only be needed when using Swing in a JavaFX app, but apparently javafx.scene.controls are Swing?
  Stolen from https://stackoverflow.com/questions/28140324/clojure-javafx-toolkit-not-initialized-error/35269506"
  []
  ;; initialze the environement
  (JFXPanel.)
  ;; ensure I can keep reloading and running without restarting JVM every time
  (Platform/setImplicitExit false))

(defn event-handler [handler]
  (reify EventHandler
    (handle [self event] (handler event))))

(defn launch [cs & args]
  (Application/launch cs (into-array String args)))

; FIXME: Difficult to annotate parent. It's a member of javafx.scene.Parent, but the getChildren method of that class is protected.
(defn get-children
  "Returns the observable list of children of the parent."
  ^ObservableList [parent]
  (.getChildren parent))

(defn add-child
  "parent must have a getChildren method."
  [parent child]
  (.add (get-children parent) child))

(defn add-children [parent & children]
  (.addAll (get-children parent) children))

(defn new-button ^Button [^String label, callback]
  (let [^Button b (Button. "Send!")]
    (.setOnAction b
      (event-handler callback))
    b))
