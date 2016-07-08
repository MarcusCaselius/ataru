(ns ataru.applications.excel-export
  (:import [org.apache.poi.ss.usermodel Row]
           [java.io ByteArrayOutputStream]
           [org.apache.poi.xssf.usermodel XSSFWorkbook])
  (:require [ataru.forms.form-store :as form-store]
            [ataru.applications.application-store :as application-store]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.string :refer [trim]]
            [clojure.core.match :refer [match]]
            [clojure.java.io :refer [input-stream]]
            [taoensso.timbre :refer [spy]]))

(defn- update-row-cell! [sheet row column value]
  (when-let [v (not-empty (trim (str value)))]
    (-> (or (.getRow sheet row)
            (.createRow sheet row))
        (.getCell column Row/CREATE_NULL_AS_BLANK)
        (.setCellValue v)))
  sheet)

(defn- make-writer [sheet offset]
  (fn [row column value]
    (update-row-cell!
      sheet
      (+ offset row)
      column
      value)
    [sheet offset row column value]))

(defn- write-headers! [writer headers]
  (doseq [header headers]
    (writer 0 (:column header) (:header header))))

(defn- write-application! [writer application headers]
  (doseq [answer (:answers application)]
    (let [column (:column (first (filter #(= (:label answer) (:header %)) headers)))
          value  (:value answer)]
      (writer 0 column value))))

(defn- extract-headers
  [applications]
  (->> (reduce
         (fn [form-headers application]
           (into form-headers
             (reduce
               (fn [application-headers answer]
                 (conj application-headers (:label answer)))
               []
               (:answers application))))
         []
         applications)
       distinct
       (map-indexed (fn [idx header] {:header header :column idx}))))

(defn export-all-applications [form-id & {:keys [language] :or {language :fi}}]
  (let [workbook               (XSSFWorkbook.)
        form                   (form-store/fetch-form form-id)
        sheet                  (.createSheet workbook "Hakemukset")
        applications           (application-store/fetch-applications
                                 form-id
                                 {:limit 100 :lang (name language)})
        headers                (extract-headers applications)]
    (when (and (not-empty form) (not-empty applications))
      (do
        (write-headers! (make-writer sheet 0) headers)
        (dorun (map-indexed
                 (fn [idx application]
                   (let [writer (make-writer sheet (inc idx))]
                     (write-application! writer application headers)))
                 applications))
        (.createFreezePane sheet 0 1 0 1)))
    (with-open [stream (ByteArrayOutputStream.)]
      (.write workbook stream)
      (.toByteArray stream))))


