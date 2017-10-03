(ns chat.server.server
  (:require [helpers.net.helpers :as nh]
            [helpers.net.buffered-socket :as bs]

            [clojure.core.async :refer [>! <! thread go-loop chan]]
            [chat.messages.message :as m]
            [clojure.string :as s])

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

(defn client-address [^BufferedSocket b-sock]
  (nh/pretty-address (:socket b-sock)))

(defn add-user! [^String username, ^BufferedSocket b-sock]
  (q "Recieved a connection to" username "from" (client-address b-sock)) "\n"
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
  (let [{:keys [sender sender-address]} message]
    (doseq [[rcvr-name rcvr-sock] @users!
            :let [rcvr-addr (client-address rcvr-sock)]]
      (when-not (and (= sender-address)
                     (= sender rcvr-name))

        (q "Sending" message "to" rcvr-name)
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

(defn process-message [raw-message-text]
  (-> raw-message-text
      (s/trim)
      (s/replace "\r\n" "\n")
      (s/replace "\n" "  ")))

(defn pre-process-messages [raw-messages]
  (for [m raw-messages
        :let [proc (process-message m)]
        :when (m/valid-message-text? proc)]
    proc))

; TODO: Getting expensive?
(defn to-messages [username, ^BufferedSocket sender-sock, raw-messages]
  (let [addr (client-address sender-sock)
        proc-msgs (pre-process-messages raw-messages)]
    (map #(m/internal-message username addr %) proc-msgs)))

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

(defn add-shutdown-hook []
  (.addShutdownHook (Runtime/getRuntime)
    (Thread. ^Runnable
             (fn []
               (disconnect-all!)
               (println "Shutdown run!")))))

(defn start-server [port check-delay]
  (start-message-listener check-delay)
  (println "Starting server...")

  (add-shutdown-hook)

  (nh/start-async-server port accept-handler
                         #(do
                            (q (.getMessage ^Exception %2))
                            (shutdown-server! %))))
