(ns chat.testing.simple-client
  (:require [helpers.net.helpers :as nh]
            [helpers.net.buffered-socket :as bs]

            [chat.testing.simple-server :as ss]
            [chat.helpers :as ch]

            [clojure.core.async :refer [thread chan]]
            [chat.message :as m])

  (:import [java.net Socket SocketException]))

(def test-address "localhost")
(def test-port ss/test-port)

; TODO: Add a prompt to ask for a message to send

(defrecord User [server-sock username running?!])

(defn new-user [server-sock username]
  (->User server-sock username (atom true)))

(defn ask-for-message []
  (ch/print-fl ">: ")
  (read-line))

(defn parse-format-message [raw-message]
  (let [{:keys [sender text]} (m/parse-message raw-message)]
    (str sender ": " text)))

(defn connection-handler [server]
  (ch/print-fl "Name?: ")
  (let [b-sock (bs/new-buffered-socket server)
        entered-name (read-line)
        user (new-user b-sock entered-name)
        {:keys [server-sock running?! username]} user]

    (bs/write server-sock username)

    (try
      (while @running?!
        (let [sending-msg (ask-for-message)]

          (when (> (count sending-msg) 1)
             (bs/write server-sock sending-msg))

          (when-let [recieved (bs/read-lines server-sock)]
            (doseq [msg recieved]
              (println "\t" (parse-format-message msg))))))

      (catch SocketException se
        (println "Exception:" (.getMessage se) "\nClosing..."))

      (finally
        (bs/close server-sock)))))

(defn connect [address port]
  (nh/connect-to address port connection-handler))

(defn test-connect []
  (connect test-address test-port))