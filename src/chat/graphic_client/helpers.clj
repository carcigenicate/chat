(ns chat.graphic-client.helpers
  (:require [seesaw.core :as sc]))

(defn append-line [text-box line]
  (sc/text! text-box
     (str (sc/text text-box) "\n" line)))