(ns ataru.virkailija.application.view
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]))

(defn application []
  (fn []
    [:div "application"]))