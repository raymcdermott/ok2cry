(ns encry-test
  (:require
    [client-crypto :as client]
    [cljs.test :refer [async deftest is testing]]
    [lambda.encry :as encry]
    [promesa.core :as p]))


(deftest encryption-keys-from-client
  (testing "that we can get data encrypted with a client public key"
    (async done
      (-> (p/let [payload {:jwt "fake-jwt" :data {:anything :at-all}}
                  response (-> payload
                               (client/->api-gateway-signed-request-data :jwt)
                               (encry/handler)
                               (js->clj))]
            (is (= 1 response)))
          (p/finally done)))))
