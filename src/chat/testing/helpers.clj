(ns chat.testing.helpers
  (:require [clojure.edn :as edn]
            [helpers.net-helpers :as nh]
            [clojure.java.io :as io])

  (:import [java.net Socket]
           [java.io InputStreamReader BufferedReader]
           [java.util.stream Collectors]
           [java.util List]))

(defn messages-to-recieve? [^Socket socket]
  (pos? (.available (.getInputStream socket))))

(defn print-fl [& messages]
  (apply print messages)
  (flush))

(defn parse-message [message]
  (edn/read-string message))

(defn read-lines [^Socket socket]
  (let [^BufferedReader br (io/reader socket)]
    (loop [acc nil]
      (if (messages-to-recieve? socket)
        (do
          (println "Reading from socket...")
          (recur (conj (or acc [])
                       (.readLine br))))
        acc))))

; FIXME: Seems to be dropping messages.
#_
(defn read-messages [^Socket socket]
  (loop [acc nil]
    (if (messages-to-recieve? socket)
      (recur (conj (if acc acc [])
                   (nh/read-line socket)))
      acc)))

; FIXME: Horribly broken. .collect blocks forever.
#_
(defn read-lines [^Socket socket]
  (println "Starting to read message...")
  (let [^BufferedReader br (io/reader socket)

        _ (println "BR Created")

        ls (.lines br)

        _ (println "Lines Created")

        lst (.collect ls (Collectors/joining "\n"))

        _ (println "List Collected")

        vec-msgs (vec lst)]

    (println "RL1:" (.size lst))
    (println "RL2:" vec-msgs)

    vec-msgs))
