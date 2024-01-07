(ns decry-test
  (:require
    [cljs.test :as t :refer [async deftest is testing]]
    [common-data]
    [promesa.core :as p]))


(deftest card-pin-access
  (testing "that we can obtain the card PIN"
    #_(async done
           (-> (p/let [{:keys [id]} (create/create-card common-data/card-account common-data/sample-data)
                       {:keys [activated]} (activate/call-modulr-api id)
                       {:keys [body]} (pin/call-modulr-api id)
                       _clean-up (and id (aws-secrets/delete-management-token id))]
                 (is (true? activated))
                 (is (seq (:pin body)))
                 (is (number? (parse-long (:pin body)))))
               (p/finally done)))))


(defn run-tests
  []
  (t/run-tests 'encry-test))
