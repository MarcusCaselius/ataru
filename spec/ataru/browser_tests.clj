(ns ataru.browser-tests
  (:require [clojure.data.json :as json]
            [clojure.string :refer [split join]]
            [clojure.java.shell :refer [sh]]
            [environ.core :refer [env]]
            [speclj.core :refer :all]
            [oph.soresu.common.db :as db]
            [oph.soresu.common.config :refer [config]]
            [com.stuartsierra.component :as component]
            [ataru.test-utils :refer [login]]
            [ataru.virkailija.virkailija-system :as virkailija-system]
            [ataru.fixtures.db.browser-test-db :refer [init-db-fixture]])
  (:import (java.util.concurrent TimeUnit)))

(defn- run-specs-in-system
  [specs]
  (let [system (virkailija-system/new-system)]
    (try
      (component/start-system system)
      (init-db-fixture) ;; Has to be done "this late" in the process because start-system runs migrations (creates db structure)
      (specs)
      (finally
        (component/stop-system system)))))

(defn sh-timeout [timeout-secs & args]
  (.get
    (future-call #(apply sh args))
    timeout-secs
    (TimeUnit/SECONDS)))

(describe "UI tests /"
          (tags :ui)
          (around-all [specs]
                      (db/clear-db! :db (-> config :db :schema))
                      (run-specs-in-system specs))
          (it "are successful"
              (let [login-cookie-value (last (split (login) #"="))
                    results (sh-timeout 600 "node_modules/phantomjs-prebuilt/bin/phantomjs"
                                "--web-security" "false"
                                "bin/phantomjs-runner.js" login-cookie-value)]
                (println (:out results))
                (.println System/err (:err results))
                (should= 0 (:exit results)))))

(run-specs)
