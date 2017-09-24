(ns chat.testing.helpers
  (:import [java.net Socket]))

(defn messages-to-recieve? [^Socket server-sock]
  (.available (.getInputStream server-sock)))

(defn print-fl [& messages]
  (apply print messages)
  (flush))