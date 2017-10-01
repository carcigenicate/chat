(ns chat.seesaw-tests.seesaw-tests
  (:require [seesaw.core :as sc]
            [seesaw.dev :as sd]

            [clojure.core.async :refer [thread]]
            [chat.seesaw-tests.seesaw-helpers :as sh])
  (:import (java.io File)))

(def window-size [1000 :by 1000])
(def send-panel-size [800 :by 500])
(def message-panel-size (sh/map-dimensions #(int (/ % 3))
                                           window-size))

(defn add-many! [frame & widgets]
  (reduce sc/add! frame widgets))

(defn compose-box []
  (sc/text :multi-line? true, :size send-panel-size
           :wrap-lines? true))

(defn message-box []
  (sc/text :multi-line? true, :size [50 :by 50]
           :text "TEST!"
           :editable? false
           :size message-panel-size))

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
  (let [frame (sc/frame :title "Test", :size window-size)
        panel (sc/border-panel)
        msg-box (message-box)
        send-box (send-panel)]

    (sc/add! frame panel)
    (sc/config! panel :center msg-box, :south send-box)

    (-> frame
      #_(sc/pack!)
      (sc/show!))))
