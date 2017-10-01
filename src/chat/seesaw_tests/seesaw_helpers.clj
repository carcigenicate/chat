(ns chat.seesaw-tests.seesaw-helpers)

(defn map-dimensions [f dim]
  (let [[x b y] dim]
    [(f x) b (f y)]))