(ns chat.testing.rand-num-client
  (:require [helpers.net-helpers :as nh]
            [chat.testing.rand-num-server :as cs]

            [clojure.core.async :refer [thread chan]]))

(def running?! (atom true))

(defn connection-handler [server]
  (thread
    (while @running?!
      (let [message (nh/read-line server)]
        (cs/q (.getId (Thread/currentThread)) "recieved" message)))))

(defn connect []
  (let [port cs/server-port]
    (nh/connect-to "localhost" port connection-handler)))

(defn connect-many [n]
  (doseq [m (range n)]
    (connect)))