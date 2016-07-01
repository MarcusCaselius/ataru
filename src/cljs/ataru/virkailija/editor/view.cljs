(ns ataru.virkailija.editor.view
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync register-handler]]
            [reagent.core :as r]
            [ataru.cljs-util :refer [debounce-subscribe]]
            [ataru.virkailija.editor.core :as c]
            [ataru.virkailija.editor.subs]
            [ataru.virkailija.soresu.component :as component]
            [ataru.virkailija.temporal :refer [time->str]]
            [taoensso.timbre :refer-macros [spy debug]]))

(def wrap-scroll-to
  (with-meta identity {:component-did-mount #(let [node (r/dom-node %)]
                                               (if (.-scrollIntoViewIfNeeded node)
                                                 (.scrollIntoViewIfNeeded node)
                                                 (.scrollIntoView node)))}))

(defn form-row [form selected?]
  [:a.editor-form__row
   {:href  (str "#/editor/" (:id form))
    :class (when selected? "editor-form__selected-row")}
   [:span.editor-form__list-form-name (:name form)]
   [:span.editor-form__list-form-time (time->str (:modified-time form))]
   [:span.editor-form__list-form-editor (:modified-by form)]])

(defn form-list []
  (let [forms            (debounce-subscribe 333 [:state-query [:editor :forms]])
        selected-form-id (subscribe [:state-query [:editor :selected-form-id]])]
    (fn []
      (into [:div.editor-form__list]
            (for [[id form] @forms
                  :let [selected? (= id @selected-form-id)]]
              ^{:key id}
              (if selected?
                [wrap-scroll-to [form-row form selected?]]
                [form-row form selected?]))))))

(defn add-form []
  [:div.editor-form__add-new
   [:a {:on-click (fn [evt]
                    (.preventDefault evt)
                    (dispatch [:editor/add-form]))
        :href "#"}
    "Luo uusi lomake"]])

(defn editor-name []
  (let [form              (subscribe [:editor/selected-form])
        new-form-created? (subscribe [:state-query [:editor :new-form-created?]])
        form-name         (reaction (:name @form))]
    (r/create-class
      {:display-name        "editor-name"
       :component-did-mount (fn [element]
                              (when @new-form-created?
                                (do
                                  (doto (r/dom-node element)
                                    (.focus)
                                    (.select))
                                  (dispatch [:set-state [:editor :new-form-created?] false]))))
       :reagent-render      (fn []
                              [:input.editor-form__form-name-input
                               {:key         (:id @form) ; needed to trigger component-did-update
                                :type        "text"
                                :value       @form-name
                                :placeholder "Lomakkeen nimi"
                                :on-change   #(dispatch-sync [:editor/change-form-name (.-value (.-target %))])}])})))

(defn editor-panel []
  (let [form            (subscribe [:editor/selected-form])]
    (fn []
      (when @form ;; Do not attempt to show form edit controls when there is no selected form (form list is empty)
        [:div.panel-content
         [:div
          [editor-name]]
         [:div.editor-form__link-row
          [:div
           [:span [:a.editor-form__preview-link
                                                {:href   (str js/config.applicant.service_url "/hakemus/" (:id @form))
                                                 :target "_blank"}
                                                "Esikatsele lomake"]]

           [:span.editor-form__link-row--divider "|"]
           [:span
            [:a.editor-form__preview-link
             {:href (str "#/application/" (:id @form))} "Selaa lomakkeen hakemuksia"]]]]
         [c/editor]]))))

(defn editor []
    [:div
     [:div.editor-form__container.panel-content
      [add-form]
      [form-list]]
     [editor-panel]])
