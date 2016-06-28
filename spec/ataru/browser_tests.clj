(ns ataru.browser-tests
  (:require [clojure.string :refer [split join]]
            [clojure.java.shell :refer [sh]]
            [environ.core :refer [env]]
            [speclj.core :refer :all]
            [oph.soresu.common.db :as db]
            [oph.soresu.common.config :refer [config]]
            [ataru.virkailija.virkailija-system :refer [new-system]]
            [ataru.test-utils :refer [login]]
            [com.stuartsierra.component :as component]))

(defn- run-specs-in-system
  [specs]
  (let [system (new-system)]
    (try
      (component/start-system system)
      (specs)
      (finally
        (component/stop-system system)))))

(describe "UI tests /"
          (tags :ui)
          (around-all [specs] (run-specs-in-system specs))

          (it "are successful"
              (let [login-cookie-value (last (split (login) #"="))
                    _ (println "cookie" login-cookie-value)
                    results (sh "node_modules/phantomjs-prebuilt/bin/phantomjs"
                                "--web-security" "false"
                                "bin/phantomjs-runner.js" login-cookie-value)
                    output (:out results)
                    test-report-path "target/tests-output.txt"]
                (println "Tests exit code" (:exit results))
                (spit test-report-path output)
                (println output)
                (.println System/err (:err results))
                (should= 0 (:exit results)))))

(run-specs)