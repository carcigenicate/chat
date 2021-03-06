(ns chat.messages.message
  (:require [clojure.edn :as edn]))

(defn internal-message [sender-name sender-address message-text]
  {:sender sender-name
   :sender-address sender-address
   :message-text message-text})

(defn outgoing-message [sender-name message-text]
  {:sender sender-name
   :message-text message-text})

(defn server-message-to-outgoing [server-message]
  (dissoc server-message :sender-address))

(defn parse-message [message]
  (edn/read-string message))

(defn format-message [message]
  (let [{:keys [sender sender-address message-text]} message
        addr-str (if sender-address (str "(" sender-address ")") "")]

    (str sender addr-str ": " message-text)))

(defn valid-message-text? [raw-message]
  (and (>= (count raw-message) 2)
       (not (every? #(Character/isWhitespace ^Character %) raw-message))))