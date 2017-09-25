(ns chat.testing.helpers)

(defn print-fl [& messages]
  (apply print messages)
  (flush))
