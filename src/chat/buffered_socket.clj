(ns chat.buffered-socket
  (:require [clojure.java.io :as io])

  (:import [java.net Socket]
           [java.io BufferedReader BufferedWriter InputStream])

  (:refer-clojure :exclude [read-line]))

(defrecord BufferedSocket [^Socket socket, ^BufferedReader reader])

(defn new-buffered-socket
  ([^Socket socket]
   (->BufferedSocket socket (io/reader socket)))

  ([^String address ^long port]
   (new-buffered-socket (Socket. address port))))

(defn write [^BufferedSocket b-sock ^String message]
  "Writes the message to the buffered socket.
  Terminates the message with a newline, then flushes the stream."
  (let [sock (:socket b-sock)
        ^BufferedWriter w (io/writer sock)
        nl-terminated-ms  (str message "\n")]

    (.write w nl-terminated-ms)
    (.flush w)))

(defn read-line
  "Reads a line from the socket."
  [^BufferedSocket b-sock]
  (let [reader (:reader b-sock)]
    (.readLine reader)))

(defn data-ready? [^BufferedSocket b-sock]
  #_
  (pos? (.available ^InputStream (.getInputStream (:socket b-sock))))
  (.ready (:reader b-sock)))

(defn read-lines [^BufferedSocket b-sock]
  (loop [acc nil]
    (if (data-ready? b-sock)
      (recur (conj (or acc []) (read-line b-sock)))
      acc)))

(defn close [^BufferedSocket b-sock]
  (.close
    ^Socket (:socket b-sock)))
