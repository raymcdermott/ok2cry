(ns io.ok2cry.data-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [io.ok2cry.data :as data]))

(deftest happy-edn-bytes-roundtrip
  (testing "edn to bytes and bytes to edn"
    (let [data ["abc" 123 {:foo "bar"}]]
      (is (= data (-> (data/edn->bytes data)
                      (data/bytes->edn)))))))


(deftest happy-base64-bytes-roundtrip
  (testing "bytes to base64 and base64 to bytes"
    (let [data       ["abc" 123 {:foo "bar"}]
          data-bytes (data/edn->bytes data)]
      (is (= data (-> (data/bytes->base64 data-bytes)
                      (data/base64->bytes)
                      (data/bytes->edn)))))))


(deftest happy-hexify-roundtrip
  (testing "base64 to hex and hex to base64"
    (let [data       ["abc" 123 {:foo "bar"}]
          data-bytes (data/edn->bytes data)
          b64-string (data/bytes->base64 data-bytes)]
      (is (= b64-string (-> (data/string->hex-string b64-string)
                            (data/hex-string->base64-string))))
      (is (= data (-> (data/edn->hex data)
                      (data/hex->edn)))))))