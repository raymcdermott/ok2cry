(ns encry-test
  (:require
    [cljs.test :as t :refer [async deftest is testing]]
    [common-data]
    [lambda.encry :as encry]
    [promesa.core :as p]))


(deftest encryption-keys-from-client
  (testing "that we can get data encrypted with a client public key"
    (async done
           (-> (p/let [response (encry/handler common-data/fake-api-request)]
                 (is (= 1 response)))
               (p/finally done)))))


(defn run-tests
  []
  (t/run-tests 'encry-test))
