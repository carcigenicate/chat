(ns chat.graphic-client.client
  (:require [seesaw.core :as sc]
            [seesaw.dev :as sd]

            [clojure.core.async :refer [thread go <! >!!]]

            [chat.graphic-client.seesaw-helpers :as sh]
            [chat.graphic-client.chat-frame :as chat-f]
            [chat.graphic-client.connection-frame :as conn-f]
            [chat.graphic-client.connection-helpers :as conn-h]
            [chat.graphic-client.helpers :as gh]
            [chat.client :as c]
            [helpers.general-helpers :as g]
            [chat.messages.message :as m])

  (:import [java.net SocketException]))

; TODO: - Add error message boxes to connection and chat frames
; TODO:   - When an error is to be displayed, use a Timeout to auto-reset it after
; TODO: - Setup enter key to trigger "connect" and "send"
; TODO: - Force the connection frame to start restored and give the address field focus

(def client! (atom nil))

(declare connect-frame chat-frame)

(defn append-message
  "Adds the message to the message pane."
  [message]
  (let [message-box (sc/select @chat-frame [:#incoming-messages])]

    (gh/append-line message-box
                    (str (m/format-message message) "\n"))))

(defn message-ok-to-send? [raw-message]
  (and (>= (count raw-message) 2)
       (not (every? #(Character/isWhitespace ^Character %) raw-message))))

(defn send-current-message []
  (let [msg-box (sc/select @chat-frame [:#compose-message])
        msg-text (sc/text msg-box)
        self-msg (m/outgoing-message (:username @client!) msg-text)]

    (println (type self-msg) self-msg)

    (if (message-ok-to-send? msg-text)
      (do
        (c/write @client! msg-text)
        (append-message self-msg)
        (sc/text! msg-box ""))

      ()))) ; FIXME: Alert the Chat Frame that there was an error.

(defn start-handler []
  (let [{:keys [incoming-chan]} @client!]
    (go
      (while (and @client! @(:running?! @client!))
        (let [msg (<! incoming-chan)
              p-msg (m/parse-message msg)]

          (sc/invoke-later
            (append-message p-msg)))))))

(defn setup-send-listener []
  (let [send-btn (sc/select @chat-frame [:#send-button])]
    (sc/listen send-btn
       :action
       (fn [_]
         (sc/invoke-later (send-current-message))))))

(defn connect-f [address port-str username]
  (if-let [port (conn-h/parse-port? port-str)]
    (try
      ; Try to connect on a seperate thread?
      (let [client (c/connect username address port)]
        (reset! client! client)

        (sc/invoke-later
          (sh/switch-active-frame-to @connect-frame @chat-frame)
          (sc/request-focus! @chat-frame)
          (setup-send-listener)
          (start-handler)))

      (catch SocketException se
        (println (.getMessage se)))) ; Alert chat frame that the connection failed

    (println "Invalid port:" port-str))) ; Alert chat frame that port was invalid

(def chat-frame (delay (chat-f/chat-frame)))
(def connect-frame (delay (conn-f/connection-frame connect-f)))

(defn -main [& args]
  (sc/show! @connect-frame)
  (sc/request-focus! (sc/select @connect-frame [:#address-input]))
  nil)

