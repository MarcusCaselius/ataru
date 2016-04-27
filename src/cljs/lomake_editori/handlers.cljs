(ns lomake-editori.handlers
    (:require [re-frame.core :as re-frame :refer [register-handler dispatch]]
              [ajax.core :refer [GET POST PUT DELETE]]
              [lomake-editori.db :as db]
              [lomake-editori.util :as util]
              [lomake-editori.temporal :refer [coerce-timestamp]]
              [cljs-time.core :as c]
              [cljs-time.format :as f]
              [goog.date :as gd]
              [cljs.core.match :refer-macros [match]]
              [taoensso.timbre :refer-macros [spy debug]]))

(def formatter (f/formatter "EEEE dd.MM.yyyy HH:mm"))

(defn http [method path handler-or-dispatch & [override-args]]
  (let [f (case method
            :get    GET
            :post   POST
            :put    PUT
            :delete DELETE)]
    (dispatch [:flasher {:loading? true
                         :message  (str "Tietoja "
                                        (case method
                                          :get    "haetaan."
                                          :post   "tallennetaan."
                                          :put    "tallennetaan."
                                          :delete "poistetaan."))}])
    (f path
       (merge {:response-format :json
               :format          :json
               :keywords?       true
               :error-handler   #(dispatch [:flasher {:loading? false
                                                      :message (str "Virhe "
                                                                    (case method
                                                                      :get "haettaessa."
                                                                      :post "tallennettaessa."
                                                                      :put "tallennettaessa."
                                                                      :delete "poistettaessa."))
                                                      :detail %}])
               :handler         (fn [response]
                                  (println "foo")
                                  (dispatch [:flasher {:loading? false
                                                       :message (str "Kaikki tiedot "
                                                                     (case method
                                                                       :get "haettu."
                                                                       :post "tallennettu."
                                                                       :put "tallennettu."
                                                                       :delete "poistettu."))}])
                                  (match [handler-or-dispatch]
                                         [(dispatch-keyword :guard keyword?)] (dispatch [dispatch-keyword response])
                                         :else (fn [response]
                                                 (dispatch [:state-update (fn [db] (handler-or-dispatch db response))]))))}
              override-args))))

(defn post [path params handler-or-dispatch]
  (http :post path handler-or-dispatch {:params params}))

(defn with-author [form]
  (assoc form :author {:last "Turtiainen" :first "Janne"}))

(register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(register-handler
  :set-state
  (fn [db [_ path args]]
    (assert (or (vector? path)
                (seq? path)))
    (if (map? args)
      (update-in db path merge args)
      (assoc-in db path args))))

(register-handler
  :state-update
  (fn [db [_ f]]
    (or (f db)
        db)))

(defn sorted-by-time [m]
  (into (sorted-map-by
          (fn [k1 k2]
            (let [v1 (-> (get m k1) :modified-time)
                  v2 (-> (get m k2) :modified-time)]
              (match [v1 v2]
                     [nil nil] 0
                     [_ nil] 1
                     [nil _] -1
                     :else (c/after? v1 v2)))))
        m))

(register-handler
  :handle-get-forms
  (fn [db [_ forms-response]]
    (-> (assoc-in db [:editor :forms] (-> (util/group-by-first
                                            :id (mapv (comp with-author (coerce-timestamp :modified-time))
                                                      (:forms forms-response)))
                                          (sorted-by-time)))
        (update-in [:editor] dissoc :selected-form))))

(register-handler
  :handle-error
  (fn [db [_ error]]
    ;; TODO make a generic error message panel in UI which is shown when
    ;; :error-message exists and has a control for emptying it
    (assoc db :error-message "Lomakelistan hakeminen epäonnistui")))

(register-handler
  :fetch-initial-data
  (fn [db _]
    (dispatch [:editor/refresh-forms])
    db))

(register-handler
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(register-handler
  :flasher
  (fn [db [_ flash]]
    (assoc db :flasher flash)))
