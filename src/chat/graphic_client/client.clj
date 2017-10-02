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
            [chat.graphic-client.general-widgets :as gw]

            [helpers.general-helpers :as g]
            [chat.messages.message :as m])

  (:import [java.net SocketException]))

; TODO: - Add error message boxes to connection and chat frames
; TODO:   - When an error is to be displayed, use a Timeout to auto-reset it after
; TODO: - Setup enter key to trigger "connect" and "send"
; TODO: - Force the connection frame to start restored and give the address field focus

(def alert-length 3000)

(def client! (atom nil))

(declare connect-frame chat-frame)

(defn append-message
  "Adds the message to the message pane."
  [message]
  (let [message-box (sc/select @chat-frame [:#incoming-messages])]

    (gh/append-line message-box
                    (str (m/format-message message) "\n"))))

(defn alert [parent & messages]
  (gw/send-alert parent alert-length (apply str messages)))

(defn username-valid? [username]
  (and (<= 3 (count username) 20)))

(defn assert-alert [parent conditon message]
  (when-not conditon
    (alert parent message)))

(defn send-current-message []
  (let [msg-box (sc/select @chat-frame [:#compose-message])
        msg-text (sc/text msg-box)
        self-msg (m/outgoing-message (:username @client!) msg-text)]

    (if (m/valid-message-text? msg-text)
      (do
        (c/write @client! msg-text)
        (append-message self-msg)
        (sc/text! msg-box ""))

      (alert @chat-frame "Invalid Message."))))

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
  (if-let [port (and (username-valid? username)
                     (conn-h/parse-port? port-str))]

    (thread
      (try
        (let [client (c/connect username address port)]
          (reset! client! client)

          (sc/invoke-later
            (sh/switch-active-frame-to @connect-frame @chat-frame)
            (sc/request-focus! @chat-frame)
            (setup-send-listener)
            (start-handler)))

        (catch SocketException se
          (alert @connect-frame (.getMessage se)))))

    (alert @connect-frame "Invalid username/port")))

(def chat-frame (delay (chat-f/chat-frame)))
(def connect-frame (delay (conn-f/connection-frame connect-f)))

(defn -main [& args]
  (sc/show! @connect-frame)
  (sc/request-focus! (sc/select @connect-frame [:#address-input]))
  nil)

