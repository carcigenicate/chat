(ns chat.text-client
  (:require [chat.client :as c]
            [chat.helpers :as ch]

            [clojure.core.async :refer [thread >!! <! go]]))

(defn- start-incoming-handler [client]
  (let [{:keys [incoming-chan]} client]
    (go
      (c/while-running client
        (println "\n\t" (<! incoming-chan))))))

(defn get-username []
  (ch/print-fl "Name?:")
  (read-line))

(defn connect [address port]
  (let [username (get-username)
        client (c/connect username address port)]
    (println "Client created...")

    (start-incoming-handler client)

    (c/while-running client
      (ch/print-fl ">:")

      (if-let [msg (read-line)]
        (c/write client msg)
        (c/stop client)))))