(ns chat.testing.simple-client
  (:require [helpers.net-helpers :as nh]

            [chat.testing.simple-server :as ss]
            [chat.testing.helpers :as ch]

            [clojure.core.async :refer [thread chan]])

  (:import [java.net Socket]))

(def test-address "localhost")
(def test-port ss/test-port)

; TODO: Add a prompt to ask for a message to send

(defrecord User [server-sock username running?!])

(defn new-user [server-sock username]
  (->User server-sock username (atom true)))

(defn recieve-messages [server-sock]
  (loop [acc nil]
    (if (ch/messages-to-recieve? server-sock)
      (recur (str acc (nh/read-line server-sock) "\n"))
      acc)))

(defn connection-handler [server]
  (ch/print-fl "Name?: ")
  (let [entered-name (read-line)
        user (new-user server entered-name)
        {:keys [server-sock running?! username]} user]

    (nh/write server-sock username)

    (while @running?!
      (let [sending-msg (read-line)]
        (when-not (empty? sending-msg)
           (nh/write server-sock sending-msg))

        (when-let [recieved (recieve-messages server-sock)]
            (ss/q recieved))))))

(defn connect [address port]
  (nh/connect-to address port connection-handler))

(defn test-connect []
  (connect test-address test-port))