(ns chat.seesaw-tests.multi-test
  (:require [seesaw.core :as sc]
            [seesaw.timer :as st]
            [seesaw.dev :as sd]
            [chat.graphic-client.seesaw-helpers :as sh])

  (:import [java.awt Frame]))

(defn scroll-test []
  (let [t (sc/text :multi-line? true, :font "Arial-100")
        s (sc/scrollable t)
        b (sc/border-panel :center s)
        f (sc/frame :size [1000 :by 1000], :content b)]
    (sc/show! f)))


(defn toggle-showing! [showable]
  (if (sc/visible? showable)
    (sc/hide! showable)
    (sc/show! showable)))

(defn toggle-button [frame]
  (let [callback (fn [_] (toggle-showing! frame))
        text (sc/config frame :title)]
    (sc/button :text text, :listen [:action callback]
               :size [200 :by 200])))

(defn -main []
  (let [f0 (sc/frame :title "MAIN", :size [1800 :by 1800])
        flow (sc/flow-panel)

        f1 (sc/frame :title 1, :size [500 :by 1000])
        f2 (sc/frame :title 2, :size [1000 :by 500])

        toggle-cb (fn [_] (sh/switch-active-frame-to f1 f2))
        toggle-button (sc/button :text "Toggle!", :listen [:action toggle-cb])]

    (sc/add! flow toggle-button)
    (sc/add! f0 flow)

    (sc/pack! f0)
    (sc/show! f0)
    (sc/show! f1)))






