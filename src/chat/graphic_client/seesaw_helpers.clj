(ns chat.graphic-client.seesaw-helpers
  (:require [seesaw.core :as sc])
  (:import (java.awt Component IllegalComponentStateException Point)))

(defn map-dimensions [f dim]
  (let [[x b y] dim]
    [(f x) b (f y)]))

(defn screen-position [^Component component]
  (let [^Point p (.getLocationOnScreen component)]
    [(.x p) (.y p)]))

(defn set-screen-position [^Component component x y]
  (.setLocation component x y))

(defn switch-active-frame-to [current-frame new-frame]
  (let [[x y] (screen-position current-frame)]
    (sc/hide! current-frame)

    (set-screen-position new-frame x y)
    (sc/show! new-frame)))