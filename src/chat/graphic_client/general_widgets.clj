(ns chat.graphic-client.general-widgets
  (:require [seesaw.core :as sc]
            [seesaw.dev :as sd]
            [seesaw.timer :as st]))

(defn alert-box [& args]
  (apply sc/text
           :class :alert
           :foreground :red
           :editable? false
         args))

(defn send-alert [parent display-length message]
  (let [alerts (sc/select parent [:.alert])]
    (doseq [a alerts]
      (sc/text! a message)

      (st/timer
        (fn [_] (when (= (sc/text a) message)
                  (sc/text! a "")))
        :initial-delay display-length, :repeats? false))))


