(ns chat.client
  (:require [helpers.net.helpers :as nh]
            [helpers.net.buffered-socket :as bs]

            [chat.testing.simple-server :as ss]
            [chat.helpers :as ch]

            [clojure.core.async :refer [thread chan go >!! <! alts! timeout]]
            [chat.message :as m])

  (:import [java.net Socket SocketException]))

(def test-address "localhost")
(def test-port ss/test-port)
(def test-username (delay (str "TEST_USER" (rand-int 100))))

(def message-buffer-size 100)
(def incoming-message-check-delay 500)
(def outgoing-message-check-delay incoming-message-check-delay)

(defrecord Client [server-sock username incoming-chan outgoing-chan running?!])

(defn new-message-channel []
  ; TODO: Change to refs of vectors?
  (chan message-buffer-size))

(defn new-client [server-sock username]
  (->Client server-sock username
            (new-message-channel) (new-message-channel)
            (atom true)))

(defn write [^Client client ^String message]
  (>!! (:outgoing-chan client) message))

(defmacro while-running [^Client client & body]
  `(while @(:running?! ~client)
     ~@body))

(defn stop [client]
  (reset! (:running?! client) false))

(defn write [^Client client message]
  (>!! (:outgoing-chan client) message))

; TODO: TEST! Make a simple button that sends, and print incoming.
; TODO: Make a third mode: Graphics client
(defn connection-handler [username server-sock]
  (let [client (new-client (bs/new-buffered-socket server-sock) username)
        {:keys [server-sock username outgoing-chan incoming-chan]} client]

    (bs/write server-sock username)

    ; Outgoing Message Listener
    ; FIXME: Do we need the timeout?
    (go
      (while-running client
        (let [t-o (timeout outgoing-message-check-delay)
              [result port] (alts! [outgoing-chan t-o])]

          (when-not (= port t-o)
            (bs/write server-sock result)))))

    ; Incoming Message Listener
    (thread
      (while-running client
        (when (bs/data-ready? server-sock)
          (>!! incoming-chan
               (bs/read-line server-sock))

          (Thread/sleep incoming-message-check-delay))))

    client))

(defn connect [username address port]
  (let [client (nh/connect-to address port
                              (partial connection-handler username))]
    client))

(defn test-connect []
  (connect @test-username test-address test-port))