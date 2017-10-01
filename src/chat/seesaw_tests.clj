(ns chat.seesaw-tests
  (:require [seesaw.core :as sc]
            [seesaw.dev :as sd]))

(defn add-many! [frame & widgets]
  (reduce sc/add! frame widgets))

(defn compose-box []
  (sc/text :multi-line? true, :size [500 :by 50]))

(defn message-box []
  (sc/text :multi-line? true, :size [50 :by 50]
           :text "TEST!"
           :editable? false))

(defn send-button [compose-text]
  (sc/button
    :text "Send"
    :listen [:action (fn [e] (println (sc/text compose-text))
                             (sc/config! compose-text :text ""))]))

(defn send-panel []
  (let [cps-box (compose-box)
        send-btn (send-button cps-box)]

    (sc/flow-panel :items [cps-box send-btn])))

; TODO: Figure out how to fix the sizing. Responize sizing?

(defn -main [& args]
  (let [frame (sc/frame :title "Test")
        panel (sc/border-panel)
        msg-box (message-box)
        send-box (send-panel)]

    (sc/add! frame panel)
    (sc/config! panel :center msg-box, :south send-box)

    (-> frame
      (sc/pack!)
      (sc/show!))))
