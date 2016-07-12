(ns ataru.virkailija.routes
    (:require-macros [secretary.core :refer [defroute]])
    (:import goog.History)
    (:require [ataru.cljs-util :refer [dispatch-after-state]]
              [secretary.core :as secretary]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [re-frame.core :refer [dispatch]]))

(defonce history (History.))

(defn hook-browser-navigation! []
  (doto history
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn set-history!
  [route]
  (.setToken history route))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" []
    (secretary/dispatch! "/editor"))

  (defroute "/editor" []
    (dispatch [:set-active-panel :editor])
    (dispatch [:editor/refresh-forms])
    (dispatch-after-state
      :predicate
      (fn [db] (not-empty (get-in db [:editor :forms])))
      :handler
      (fn [forms]
        (let [id (-> forms first first)]
          ; prevents dispatching :editor/refresh-form twice
          (.replaceState js/history nil nil (str "#/editor/" id))
          (dispatch [:editor/select-form id])))))

  (defroute #"/editor/(\d+)" [id]
    (dispatch [:set-active-panel :editor])
    (dispatch [:editor/refresh-forms])
    (when-let [parsed-id (js/Number id)]
      (dispatch-after-state
        :predicate
        (fn [db] (not-empty (get-in db [:editor :forms])))
        :handler
        (fn [forms]
          (dispatch [:editor/select-form parsed-id])))))

  (defroute #"/application/(\d+)" [form-id]
    (let [parsed-id (js/Number form-id)]
      (dispatch [:editor/refresh-forms]) ;;Race select-formin kanssa, kato yltä kikkailu ^
      (dispatch [:editor/select-form parsed-id])
      (dispatch [:application/fetch-applications parsed-id]))
    (dispatch [:set-active-panel :application]))

  ;; --------------------
  (hook-browser-navigation!))
