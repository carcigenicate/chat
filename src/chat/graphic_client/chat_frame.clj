(ns chat.graphic-client.chat-frame
  (:require [seesaw.core :as sc]
            [clojure.core.async :refer [>!!]]
            [chat.graphic-client.seesaw-helpers :as sh]))

(def window-size [1000 :by 1000])
(def send-panel-size [800 :by 500])
(def message-panel-size (sh/map-dimensions #(int (/ % 3))
                                           window-size))

(def message-font "Arial-30")
(def compose-font message-font)
(def send-font "Arial-20")

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
  (let [parent (sc/border-panel)
        cps-box (compose-box)
        send-btn (send-button)]

    (sc/config! parent :center cps-box
                       :south send-btn)

    parent))


(defn chat-frame []
  (let [msg-box (message-box)
        send-box (send-panel)
        main-panel (sc/border-panel :center msg-box
                                    :south send-box
                                    :border 10
                                    :hgap 5)]

    (sc/frame :size window-size, :content main-panel,)))

