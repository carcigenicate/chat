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
            [helpers.general-helpers :as g])

  (:import [java.net SocketException]))

; TODO: Format incoming messages

(def client! (atom nil))

(declare connect-frame chat-frame)

(defn start-handler []
  (let [{:keys [incoming-chan]} @client!
        message-box (sc/select @chat-frame [:#incoming-messages])]
    (go
      (while (and @client! @(:running?! @client!))
        (let [msg (<! incoming-chan)]
          (sc/invoke-later
            (gh/append-line message-box msg)))))))

(defn setup-send-listener []
  (let [send-btn (sc/select @chat-frame [:#send-button])]
    (sc/listen send-btn
       :action
       (fn [_]
         (sc/invoke-later
           (let [msg-box (sc/select @chat-frame [:#compose-message])
                 msg (sc/text msg-box)]

             (c/write @client! msg)
             (sc/config! msg-box :text "")))))))

(defn connect-f [address port-str username]
  (if-let [port (conn-h/parse-port? port-str)]
    (try
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

