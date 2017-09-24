(ns chat.testing.rand-num-server
  (:require [helpers.net-helpers :as nh]
            [clojure.core.async :refer [>! <! thread go-loop chan]])

  (:import [java.net Socket SocketException]))

(def ^:const server-port 5555)

(def users! (atom #{}))
(def running?! (atom true))

(def msg-chan (chan 5))
(def message-q (nh/start-message-loop msg-chan))
(defn q [& messages]
  (apply nh/queue-message msg-chan messages))

(defn add-user! [^Socket socket]
  (q "Recieved a connection from" (nh/pretty-address socket))
  (swap! users! #(conj % socket)))

(defn remove-connection! [^Socket socket]
  (swap! users! #(disj % socket))
  (.close socket))

(defn disconnect-all! []
  (let [close-all! #(doseq [s %] (.close s))]
    (swap! users!
      (fn [us]
        (close-all! us)
        #{}))))

(defn broadcast [^String message]
  (doseq [client @users!]
    (try
      (nh/write client message)

      (catch SocketException se
        (do
          (q "Exception" (.getMessage se))
          (remove-connection! client))))))

(defn accept-handler [^Socket client]
  (add-user! client))

(defn start-broadcaster [max-n message-delay]
  (thread
    (while @running?!
      (broadcast (rand-int max-n))
      (Thread/sleep message-delay))))

(defn shutdown-server [^Socket server-sock]
  (reset! running?! false)
  (disconnect-all!)
  (.close server-sock))

(defn start-server [max-n message-delay]
  (start-broadcaster max-n message-delay)
  (println "Broadcasting. Starting server...")

  (nh/start-simple-server server-port accept-handler
     #(do
        (q (.getMessage ^Exception %2))
        (shutdown-server %)))

  (q "Server Closed."))