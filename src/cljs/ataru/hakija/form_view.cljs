(ns ataru.hakija.form-view
  (:require [ataru.hakija.banner :refer [banner]]
            [re-frame.core :refer [subscribe]]
            [cljs.core.match :refer-macros [match]]))

(defn render-field
  [content]
   (match [content]
          [{:fieldClass "wrapperElement"
            :children   children}] (into [:div.application__wrapper] (mapv render-field children))
          [{:fieldClass "formField" :fieldType "textField"}] [:div.application__form-field [:label (-> content :label :fi)]]))

(defn render-fields [form-data]
  (if form-data
    (mapv render-field (:content form-data))
    nil))

(defn application-contents []
  (let [form (subscribe [:state-query [:form]])]
    (into [:div] (render-fields @form))))

(defn form-view []
  [:div
   [banner]
   [application-contents]])
