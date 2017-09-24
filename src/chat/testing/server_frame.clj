(ns chat.testing.server-frame)

(def users! (atom {}))

(defn user-registered? [username]
  (@users! username))

(defn unsafe-add-user
  "Will overwrite any existing users with the same name, leaking the old socket."
  [username ^Socket socket]
  (swap! users! #(assoc % username socket)))

(defn add-user
  "Adds the user to the system. Fails and returns nil if the username is already registered."
  [username ^Socket socket]
  (when-not (user-registered? username)
    (unsafe-add-user username socket)))

(defn disconnect-user
  "Disconnects the user, and closes the socket IF the user exists. Fails and returns nil otherwise."
  [username]
  (when (user-registered? username)
    (swap! users! #(dissoc % username))
    (.close ^Socket (@users! username))))