(ns ataru.hakija.hakija-ajax
  (:require [re-frame.core :refer [register-handler dispatch]]
            [cljs.core.match :refer-macros [match]]
            [ajax.core :refer [GET POST]])
  (:refer-clojure :exclude [get]))

(def ^:private json-params {:format :json :response-format :json :keywords? true})

(defn- params [handler-kw error-handler-kw]
  (merge
    {:handler (fn [response] (dispatch [(or handler-kw :application/default-http-ok-handler) response]))
     :error-handler (fn [response] (dispatch [(or error-handler-kw :application/default-handle-error) response]))}
    json-params))

(defn get [path & [handler-kw error-handler-kw]]
  (GET path (params handler-kw error-handler-kw)))

(defn post [path post-data & [handler-kw error-handler-kw]]
  (POST path (merge {:params post-data} (params handler-kw error-handler-kw))))
