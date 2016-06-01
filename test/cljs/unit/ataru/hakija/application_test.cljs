(ns ataru.hakija.application-test
  (:require [cljs.test :refer-macros [deftest is]]
            [ataru.hakija.application :refer [create-initial-answers answers->valid-status]]))

(def form1
  {:id 37,
   :name "uusi lomake",
   :modified-time "x",
   :modified-by "DEVELOPER",
   :content [{:id "G__1",
              :label {:fi "osio1", :sv "Avsnitt namn"},
              :children [{:id "G__2",
                          :label {:fi "kenttä1", :sv ""},
                          :params {:size "S"},
                          :required true,
                          :fieldType "textField",
                          :fieldClass "formField"}
                         {:id "G__14",
                          :label {:fi "kenttä2", :sv ""},
                          :params {:size "M"},
                          :required false,
                          :fieldType "textField",
                          :fieldClass "formField"}],
              :fieldType "fieldset",
              :fieldClass "wrapperElement"}
             {:id "G__25",
              :label {:fi "ulkokenttä", :sv ""},
              :params {:size "L"},
              :required false,
              :fieldType "textField",
              :fieldClass "formField"}]})

(deftest correct-validity-for-nested-form
  (let [initial-answers (create-initial-answers form1)]
    (is (= {:G__2 {:valid false}, :G__14 {:valid true}, :G__25 {:valid true}} initial-answers))))

(deftest answers->valid-status-gives-false-when-one-answer-is-not-valid
  (let [result (answers->valid-status {:one {:valid false}, :two {:valid true}, :three {:valid true}})]
    (is (= {:valid false} result))))

(deftest answers->valid-status-gives-false-for-empty-map
  (is (= {:valid false} (answers->valid-status {}))))

(deftest answers->valid-status-gives-true-for-all-valid
  (let [result (answers->valid-status {:one {:valid true}, :two {:valid true}, :three {:valid true}})]
    (is (= {:valid true} result))))
