(ns chat.graphics-tests.javafx-wrapper
  (:import (javafx.application Application)
           (javafx.event EventHandler)
           (javafx.scene Parent)))

(defn event-handler [handler]
  (reify EventHandler
    (handle [self event] (handler event))))

(defn launch [cs & args]
  (Application/launch cs (into-array String args)))

; FIXME: Difficult to annotate parent. It's a member of javafx.scene.Parent, but the getChildren method of that class is protected.
(defn add-child
  "parent must have a getChildren method."
  [parent child]
  (.add (.getChildren parent) child))