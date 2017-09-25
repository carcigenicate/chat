(ns chat.testing.helpers
  (:require [clojure.edn :as edn])

  (:import [java.net Socket]
           [java.io InputStreamReader BufferedReader]
           (java.util.stream Collectors)
           (java.util List)))

(defn messages-to-recieve? [^Socket socket]
  (pos? (.available (.getInputStream socket))))

(defn print-fl [& messages]
  (apply print messages)
  (flush))

(defn parse-message [message]
  (edn/read-string message))

(defn read-lines [^Socket socket]
  (let [^BufferedReader br
        (BufferedReader. (InputStreamReader. (.getInputStream socket)))

        ^List raw-messages
        (.collect (.lines br) (Collectors/toList))

        vec-msgs (vec raw-messages)]

    (println "RL1:" (.size raw-messages))
    (println "RL2:" vec-msgs)

    vec-msgs))
