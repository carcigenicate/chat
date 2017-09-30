(ns chat.main
  (:require [helpers.general-helpers :as g]
            [chat.testing.simple-server :as ss]
            [chat.text-client :as tc]
            [chat.graphic-client.main :as gc])

  (:gen-class))

(def server-message-check-delay 500)

(defn help [mode]
  (println
    (str "Invalid mode \"" mode
         "\". Enter c to run as a client, or s to run as a server.\n"
         "When running as a server, the arguments should be \"s port\".\nWhen running as a client, the arguments should be \"c address port.")))

(defn -main [& [mode address-or-port port?]]
  (if (and mode address-or-port)

    (let [std-mode (Character/toLowerCase ^Character (first mode))]
      (if-let [parsed-port (g/parse-int (or port? address-or-port))]
        (case std-mode
          \c (tc/connect address-or-port parsed-port)
          \s (ss/start-server parsed-port server-message-check-delay)
          \g (gc/main)
          (help mode))

        (println "Invalid port.")))

    (help mode)))
