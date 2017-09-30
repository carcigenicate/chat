(ns chat.client
  (:require [helpers.net.helpers :as nh]
            [helpers.net.buffered-socket :as bs]

            [chat.testing.simple-server :as ss]
            [chat.testing.helpers :as ch]

            [clojure.core.async :refer [thread chan go >!! <! alts! timeout]]
            [chat.message :as m])

  (:import [java.net Socket SocketException]))

(def test-address "localhost")
(def test-port ss/test-port)

; FIXME: Obviously a terrible idea
(def client-id (atom (str (rand-int 1000))))

(def message-buffer-size 100)
(def incoming-message-check-delay 500)
(def outgoing-message-check-delay incoming-message-check-delay)

(defrecord User [server-sock username])

(def running?! (atom true))
; TODO: Change to refs of vectors?
(def incoming-message-chan (chan message-buffer-size))
(def outgoing-message-chan (chan message-buffer-size))

(defn new-user [server-sock username]
  (->User server-sock username))

(defn send-out [^String message]
  (>!! outgoing-message-chan message))

; TODO: TEST! Make a simple button that sends, and print incoming.
; TODO: Make a third mode: Graphics client
(defn connection-handler [server-sock]
  (let [user (new-user (bs/new-buffered-socket server-sock) @client-id)
        {:keys [server-sock username]} user]

    ; Outgoing Message Listener
    (go
      (while @running?!
        (let [t-o (timeout outgoing-message-check-delay)
              [result port] (alts! [outgoing-message-chan t-o])]

          (when-not (= port t-o)
            (bs/write server-sock result)))))

    ; Incoming Message Listener
    (thread
      (while @running?!
        (when (bs/data-ready? server-sock)
          (>!! incoming-message-chan
               (bs/read-line server-sock)))

        (Thread/sleep incoming-message-check-delay)))))

(defn connect [address port]
  (nh/connect-to address port connection-handler))

(defn test-connect []
  (connect test-address test-port))