(ns chat.server.server
  (:require [helpers.net.helpers :as nh]
            [helpers.net.buffered-socket :as bs]

            [clojure.core.async :refer [>! <! thread go-loop chan]]
            [chat.messages.message :as m])

  (:import [java.net Socket SocketException]
           [helpers.net.buffered_socket BufferedSocket]))

; TODO: Generalize so it doesn't rely on console IO

(def ^:const test-port 5555)

(def users! (atom {}))
(def running?! (atom true))

(def msg-chan (chan 10))
(def message-q (nh/start-message-loop msg-chan))
(defn q [& messages]
  (apply nh/queue-message msg-chan messages))

(defn add-user! [^String username, ^BufferedSocket b-sock]
  (q "Recieved a connection to" username "from" (nh/pretty-address (:socket b-sock)) "\n")
  (swap! users! #(assoc % username b-sock)))

(defn remove-connection! [^String username, b-sock]
  (swap! users! #(dissoc % username))
  (bs/close b-sock))

(defn disconnect-all! []
  (let [close-all! #(doseq [s %] (bs/close s))]
    (swap! users!
           (fn [us]
             (close-all! us)
             {}))))

(defmacro try-with-user [username, ^Socket user-sock, & body]
  `(try
     ~@body

     (catch SocketException se#
       (do
         (q "Exception for" ~username "-" (.getMessage se#))
         (remove-connection! ~username ~user-sock)))))

(defn broadcast [message]
  (let [{:keys [sender-name sender-address]} message]
    (doseq [[rcvr-name rcvr-sock] @users!]
      (when-not (and (= sender-address (nh/pretty-address rcvr-sock))
                     (= sender-name rcvr-name))

        (try-with-user rcvr-name rcvr-sock
          (bs/write rcvr-sock (m/server-message-to-outgoing message)))))))

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

(defn to-messages [username, ^Socket sender-sock, raw-messages]
  (let [addr (nh/pretty-address sender-sock)]
    (mapv #(m/internal-message username addr %) raw-messages)))

(defn check-users-for-messages []
  (reduce (fn [msgs [u-name c-sock]]
            (try-with-user u-name c-sock
              (if (bs/data-ready? c-sock)
                (let [raw-msgs (bs/read-lines c-sock)]
                  (into msgs (to-messages u-name c-sock raw-msgs)))

                msgs)))
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