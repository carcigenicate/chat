(ns chat.helpers)

(defn print-fl [& messages]
  (apply print messages)
  (flush))
