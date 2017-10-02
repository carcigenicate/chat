(ns chat.graphic-client.chat-frame
  (:require [seesaw.core :as sc]
            [clojure.core.async :refer [>!!]]
            [chat.graphic-client.seesaw-helpers :as sh]
            [chat.graphic-client.general-widgets :as gw]))

(def window-size [1200 :by 1200])

(def message-font "Arial-40")
(def compose-font message-font)
(def send-font "Arial-30")
(def alert-font "Arial-Bold-35")

(defn alert-box []
  (gw/alert-box :font alert-font))

(defn message-box []
  (sc/scrollable
    (sc/text :multi-line? true,
             :font message-font
             :editable? false
             :id :incoming-messages)))

(defn compose-box []
  (sc/scrollable
    (sc/text :multi-line? true, :wrap-lines? true
             :font compose-font
             :id :compose-message
             :columns 100
             :rows 10)))

(defn send-button []
  (sc/button
    :text "Send"
    :font send-font
    :id :send-button))

(defn send-panel []
  (sc/border-panel
    :north (alert-box)
    :center (compose-box)
    :south (send-button)))

(defn chat-frame []
  (let [main-panel (sc/border-panel
                     :center (message-box)
                     :south (send-panel)
                     :border 10
                     :hgap 5)]

    (sc/frame :size window-size, :content main-panel,)))

