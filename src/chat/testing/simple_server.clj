(ns chat.testing.simple-server
  (:require [helpers.net-helpers :as nh]
            [clojure.core.async :refer [>! <! thread go-loop chan]]
            [chat.testing.helpers :as ch])

  (:import [java.net Socket SocketException]))

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

(defn add-user! [^String username ^Socket socket]
  (q "Recieved a connection to" username "from" (nh/pretty-address socket))
  (swap! users! #(assoc % username socket)))

(defn remove-connection! [^String username ^Socket socket]
  (swap! users! #(dissoc % username))
  (.close socket))

(defn disconnect-all! []
  (let [close-all! #(doseq [s %] (.close s))]
    (swap! users!
           (fn [us]
             (close-all! us)
             {}))))

(defn broadcast [message]
  (q "Broadcasting" message)

  (doseq [[u-name c-sock] @users!]
    (try
      (nh/write c-sock message)

      (catch SocketException se
        (do
          (q "Exception for" u-name "-" (.getMessage se))
          (remove-connection! u-name c-sock))))))

(defn broadcast-many [messages]
  (doseq [msg messages]
    (broadcast msg)))

(defn accept-handler [^Socket client]
  (let [username (nh/read-line client)]
    (add-user! username client)))

(defn shutdown-server! [^Socket server-sock]
  (reset! running?! false)
  (disconnect-all!)
  (.close server-sock))

(defn to-messages [username raw-messages]
  (mapv (partial new-message username) raw-messages))

(defn check-users-for-messages []
  (reduce (fn [msgs [u-name c-sock]]
            (if (ch/messages-to-recieve? c-sock)
              (let [raw-msgs (ch/read-lines c-sock)]
                (into msgs (to-messages u-name raw-msgs)))
              msgs))
          []
          @users!))

(defn start-message-listener [check-delay]
  (thread
    (loop []
      (when @running?!
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
