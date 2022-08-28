(ns runner
  (:require
    [encry-test]
    [promesa.core :as p]))


(defn -main
  [_]
  (p/let [;; Run tests in order - simpler reporting
          _ (encry-test/run-tests)
          ]))
