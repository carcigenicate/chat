(ns chat.server.helpers
  (:import (java.net NetworkInterface InetAddress)
           (java.util Collection Collections)))

(defn get-local-addresses []
  (let [to-vec #(vec (Collections/list %))
        addrs (to-vec (NetworkInterface/getNetworkInterfaces))]
    (for [^NetworkInterface a, addrs]
      (mapv #(.getHostName ^InetAddress %) (to-vec (.getInetAddresses a))))))
