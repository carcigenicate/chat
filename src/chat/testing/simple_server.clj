(ns chat.testing.simple-server
  (:require [helpers.net-helpers :as nh]
            [clojure.core.async :refer [>! <! thread go-loop chan]]
            #_
            [chat.testing.helpers :as ch]
            [chat.buffered-socket :as bs])

  (:import [java.net Socket SocketException]
           [chat.buffered_socket BufferedSocket]))

; TODO: Can recieve connections from clients, and recieve their names.
; TODO: Test if Broadcasting and message listening is working

(def ^:const test-port 5555)

(def users! (atom {}))
(def running?! (atom true))

(def msg-chan (chan 10))
(def message-q (nh/start-message-loop msg-chan))
(defn q [& messages]
  (apply nh/queue-message msg-chan messages))

(defn new-message [sender-name message-text]
  {:sender sender-name
   :text message-text})

(defn add-user! [^String username ^BufferedSocket b-sock]
  (q "Recieved a connection to" username "from" (nh/pretty-address (:socket b-sock)) "\n")
  (swap! users! #(assoc % username b-sock)))

(defn remove-connection! [^String username ^BufferedSocket b-sock]
  (swap! users! #(dissoc % username))
  (bs/close b-sock))

(defn disconnect-all! []
  (let [close-all! #(doseq [s %] (bs/close s))]
    (swap! users!
           (fn [us]
             (close-all! us)
             {}))))

(defn broadcast [message]
  (doseq [[u-name c-sock] @users!]
    (try
      (q "Sending to" u-name "-" message)
      (bs/write c-sock message)

      (catch SocketException se
        (do
          (q "Exception for" u-name "-" (.getMessage se))
          (remove-connection! u-name c-sock))))))

(defn broadcast-many [messages]
  (doseq [msg messages]
    (broadcast msg)))

(defn accept-handler [^Socket client]
  (let [b-sock (bs/new-buffered-socket client)
        username (bs/read-line b-sock)]
    (add-user! username b-sock)))

(defn shutdown-server! [^Socket server-sock]
  (reset! running?! false)
  (disconnect-all!)
  (.close server-sock))

(defn to-messages [username raw-messages]
  (mapv (partial new-message username) raw-messages))

(defn check-users-for-messages []
  (reduce (fn [msgs [u-name c-sock]]
            (if (bs/data-ready? c-sock)
              (let [raw-msgs (bs/read-lines c-sock)]
                (q "Raw messages:" raw-msgs "\n")
                (into msgs (to-messages u-name raw-msgs)))
              msgs))
          []
          @users!))

(defn start-message-listener [check-delay]
  (thread
    (loop []
      (when @running?!
        (q "Checking...")
        (let [msgs (check-users-for-messages)]
          (broadcast-many msgs))

        (Thread/sleep check-delay)
        (recur)))))

(defn start-server [port check-delay]
  (start-message-listener check-delay)
  (println "Starting server...")

  (nh/start-async-server port accept-handler
    #(do
       (q (.getMessage ^Exception %2))
       (shutdown-server! %)))

  (q "Server Closed."))
