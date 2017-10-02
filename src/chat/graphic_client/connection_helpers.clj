(ns chat.graphic-client.connection-helpers
  (:require [helpers.general-helpers :as g]))

(def highest-port 65535)

(defn parse-port? [port-str]
  (when-let [port (g/parse-int port-str)]
    (when (<= 0 port highest-port)
      port)))