(ns chat.graphic-client.connection-frame
  (:require [chat.graphic-client.seesaw-helpers :as sh]
            [seesaw.core :as sc]))

(def input-length 20)

(def input-font "Arial-30")
(def label-font "Arial-40")
(def connect-font "Arial-20")

(def connect-btn-text "connect")
(def frame-title "Connect to...")
(def input-texts ["Address" "Port" "Username"])

(defn input-box [label-text]
  (let [input-box (sc/text :font input-font,
                           :halign :center
                           :class :connect-input
                           :columns input-length)

        label (sc/label :text label-text
                        :font label-font)]

    (sc/border-panel :north label, :center input-box
                     :border 30)))

(defn get-input-text [input-box]
  (let [selection (vec (sc/select input-box [:.connect-input]))]
    (sc/text (selection 0))))

(defn connect-button [connect-f]
  (let [connect-btn (sc/button :text connect-btn-text,
                         :listen [:action connect-f]
                         :font connect-font)

        ; So the button is centered correctly.
        btn-wrapper (sc/flow-panel :items [connect-btn])]

    btn-wrapper))

(defn connection-frame [connect-f]
  (let [addr-input (input-box (input-texts 0))
        port-input (input-box (input-texts 1))
        user-input (input-box (input-texts 2))

        connect-cb (fn [_] (connect-f (get-input-text addr-input)
                                      (get-input-text port-input)
                                      (get-input-text user-input)))

        wrapped-btn (connect-button connect-cb)
        #_ ; Will need to think of how it will work if were passing in connect-f.
           ; ID
        (status-lbl (sc/label :text ""))

        panel (sc/vertical-panel
                :items [addr-input port-input user-input wrapped-btn]
                :border 10)

        frame (sc/frame :title frame-title, :content panel)]

    (sc/pack! frame)

    (sc/config! addr-input :id :address-input)

    frame))



