(ns lomake-editori.temporal
  (:require [cljs-time.format :as f]
            [cljs-time.core :as c]
            [goog.date :as gd]))

(def ^:private time-formatter (f/formatter "dd.MM.yyyy HH:mm"))

(def days-finnish
  ["Sunnuntai" "Maanantai" "Tiistai" "Keskiviikko" "Torstai" "Perjantai" "Lauantai"])

(def months-finnish
  ["Tammikuu" "Helmikuu" "Maaliskuu" "Huhtikuu" "Toukokuu" "Kesäkuu" "Heinäkuu" "Elokuu" "Syyskuu" "Lokakuu" "Marraskuu" "Joulukuu"])

(defn dow->dayname [dow]
  (days-finnish dow))

(defn with-dow [google-date]
  (days-finnish (.getDay google-date)))

(defn- time->str [google-date]
  (-> (f/unparse time-formatter google-date)
      (as-> formatted
          (str (with-dow google-date)
               "na " formatted))))
