(ns chat.message
  (:require [clojure.edn :as edn]))

(defn new-message [sender-name message-text]
  {:sender sender-name
   :text message-text})

(defn parse-message [message]
  (edn/read-string message))