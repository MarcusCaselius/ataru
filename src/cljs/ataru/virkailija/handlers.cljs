(ns ataru.virkailija.handlers
    (:require [re-frame.core :as re-frame :refer [register-handler dispatch]]
              [ajax.core :refer [GET POST PUT DELETE]]
              [ataru.virkailija.db :as db]
              [ataru.virkailija.temporal :as temporal]
              [cljs-time.core :as c]
              [cljs-time.format :as f]
              [cljs.core.match :refer-macros [match]]
              [taoensso.timbre :refer-macros [spy debug]]))

(defonce formatter (f/formatter "EEEE dd.MM.yyyy HH:mm"))

(defn http [method path handler-or-dispatch & {:keys [override-args handler-args]}]
  (let [f (case method
            :get    GET
            :post   POST
            :put    PUT
            :delete DELETE)]
    (dispatch [:flasher {:loading? true
                         :message  (match [method]
                                          [:post] "Tietoja tallennetaan"
                                          [:delete] "Tietoja poistetaan"
                                          :else nil)}])
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
               :handler         (comp (fn [response]
                                        (dispatch [:flasher {:loading? false
                                                             :message
                                                             (match [method]
                                                                    [:post] "Kaikki muutokset tallennettu"
                                                                    [:delete] "Tiedot poistettu"
                                                                    :else nil)}])
                                        (match [handler-or-dispatch]
                                               [(dispatch-keyword :guard keyword?)] (dispatch [dispatch-keyword response handler-args])
                                               [nil] nil
                                               :else (dispatch [:state-update (fn [db] (handler-or-dispatch db response handler-args))])))
                                      temporal/parse-times)}
              override-args))))

(defn post [path params & [handler-or-dispatch]]
  (http :post path handler-or-dispatch :override-args {:params params}))

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

(register-handler
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(register-handler
  :flasher
  (fn [db [_ flash]]
    (assoc db :flasher flash)))