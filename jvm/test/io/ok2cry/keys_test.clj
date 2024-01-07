(ns io.ok2cry.keys-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [io.ok2cry.cache :as ioc]
    [io.ok2cry.keys :as iok]
    [io.ok2cry.serialization :as srlzn]))

(deftest happy-keys-in-a-cache
  (testing "key pairs are properly created"
    (let [kp (iok/->signing-key-pair)]
      (is (map? kp))))
  (testing "key pairs can be cached"
    (let [{:keys [public-key] :as kp} (iok/->signing-key-pair)
          cached-kp (ioc/>put public-key kp)
          got-kp (ioc/<get public-key)]
      (is (= cached-kp kp got-kp))))
  (testing "key pairs are removed from the cache after expiry"
    (let [{:keys [public-key ttl] :as kp} (iok/->signing-key-pair 10)
          cached-kp (ioc/>put public-key kp)
          got-kp (ioc/<get public-key)
          _ (Thread/sleep ^long (+ 5 ttl))
          miss-kp (ioc/<get public-key)]
      (is (= cached-kp kp got-kp))
      (is (nil? miss-kp)))))

(deftest happy-encrypt-decrypt-roundtrip
  (testing "encrypt / decrypt roundtrip"
    (let [kp (iok/->cry-key-pair)
          data ["abc" 123]
          {:keys [encrypted] :as crypted} (iok/encrypt kp data)
          {:keys [decrypted]} (iok/decrypt kp crypted)]
      (is (not= encrypted decrypted))
      (is (= data decrypted))))
  (testing "roundtrip via JSON serialization"
    (let [kp (iok/->cry-key-pair)
          data ["abc" 123]
          exported-key (srlzn/export-public-key kp)
          imported-key (srlzn/import-public-key exported-key)
          encrypted (iok/encrypt imported-key data)
          {:keys [decrypted]} (iok/decrypt kp encrypted)]
      (is (not= encrypted decrypted))
      (is (= data decrypted))))
  (testing "roundtrip via EDN serialization"
    (let [kp (iok/->cry-key-pair)
          data ["abc" 123]
          exported-key (srlzn/export-public-key kp :output-format :edn-hex)
          imported-key (srlzn/import-public-key exported-key :input-format :edn-hex)
          encrypted (iok/encrypt imported-key data)
          {:keys [decrypted]} (iok/decrypt kp encrypted)]
      (is (not= encrypted decrypted))
      (is (= data decrypted))))
  (testing "roundtrip via Transit serialization"
    (let [kp (iok/->cry-key-pair)
          data ["abc" 123]
          exported-key (srlzn/export-public-key kp :output-format :transit)
          imported-key (srlzn/import-public-key exported-key :input-format :transit)
          encrypted (iok/encrypt imported-key data)
          {:keys [decrypted]} (iok/decrypt kp encrypted)]
      (is (not= encrypted decrypted))
      (is (= data decrypted)))))


(deftest happy-sign-verify-roundtrip
  (testing "sign / verify roundtrip"
    (let [{:keys [public-key] :as kp} (iok/->signing-key-pair)
          data ["abc" 123]
          signed-data (iok/sign kp data)
          verified (iok/verify public-key data signed-data)]
      (is (true? verified))))
  (testing "roundtrip via JSON serialization"
    (let [kp (iok/->signing-key-pair)
          data ["abc" 123]
          {:keys [signature]} (iok/sign kp data)
          exported-sig (srlzn/export-signature signature)
          imported-sig (srlzn/import-signature exported-sig)
          exported-key (srlzn/export-public-key kp)
          {:keys [public-key]} (srlzn/import-signing-public-key exported-key)
          verified (iok/verify public-key data imported-sig)]
      (is (true? verified))))
  (testing "roundtrip via EDN serialization"
    (let [kp (iok/->signing-key-pair)
          data ["abc" 123]
          {:keys [signature]} (iok/sign kp data)
          exported-sig (srlzn/export-signature signature :output-format :edn-hex)
          imported-sig (srlzn/import-signature exported-sig :input-format :edn-hex)
          exported-key (srlzn/export-public-key kp :output-format :edn-hex)
          {:keys [public-key]} (srlzn/import-signing-public-key exported-key :input-format :edn-hex)
          verified (iok/verify public-key data imported-sig)]
      (is (true? verified))))
  (testing "roundtrip via Transit serialization"
    (let [kp (iok/->signing-key-pair)
          data ["abc" 123]
          {:keys [signature]} (iok/sign kp data)
          exported-sig (srlzn/export-signature signature :output-format :transit)
          imported-sig (srlzn/import-signature exported-sig :input-format :transit)
          exported-key (srlzn/export-public-key kp :output-format :transit)
          {:keys [public-key]} (srlzn/import-signing-public-key exported-key :input-format :transit)
          verified (iok/verify public-key data imported-sig)]
      (is (true? verified)))))

(deftest happy-encrypt-sign-verify-roundtrip
  (testing "sign / verify roundtrip"
    (let [{:keys [public-key] :as skp} (iok/->signing-key-pair)
          ekp (iok/->cry-key-pair)
          data ["abc" 123]
          {:keys [encrypted] :as crypted} (iok/encrypt ekp data)
          signature (iok/sign skp encrypted)
          verified (iok/verify public-key encrypted signature)
          {:keys [decrypted]} (iok/decrypt ekp crypted)]
      (is (true? verified))
      (is (= decrypted data))))
  (testing "roundtrip via JSON serialization"
    (let [skp (iok/->signing-key-pair)
          ekp (iok/->cry-key-pair)
          data ["abc" 123]
          exported-ekp (srlzn/export-public-key ekp)
          imported-ekp (srlzn/import-public-key exported-ekp)
          {:keys [encrypted] :as crypted} (iok/encrypt imported-ekp data)
          {:keys [signature]} (iok/sign skp encrypted)
          exported-sig (srlzn/export-signature signature)
          imported-sig (srlzn/import-signature exported-sig)
          exported-skp (srlzn/export-public-key skp)
          {:keys [public-key]} (srlzn/import-signing-public-key exported-skp)
          {:keys [decrypted]} (iok/decrypt ekp crypted)
          verified (iok/verify public-key encrypted imported-sig)]
      (is (true? verified))
      (is (= decrypted data))))
  (testing "roundtrip via EDN serialization"
    (let [skp (iok/->signing-key-pair)
          ekp (iok/->cry-key-pair)
          data ["abc" 123]
          exported-ekp (srlzn/export-public-key ekp :output-format :edn-hex)
          imported-ekp (srlzn/import-public-key exported-ekp :input-format :edn-hex)
          {:keys [encrypted] :as crypted} (iok/encrypt imported-ekp data)
          {:keys [signature]} (iok/sign skp encrypted)
          exported-sig (srlzn/export-signature signature :output-format :edn-hex)
          imported-sig (srlzn/import-signature exported-sig :input-format :edn-hex)
          exported-skp (srlzn/export-public-key skp :output-format :edn-hex)
          {:keys [public-key]} (srlzn/import-signing-public-key exported-skp :input-format :edn-hex)
          {:keys [decrypted]} (iok/decrypt ekp crypted)
          verified (iok/verify public-key encrypted imported-sig)]
      (is (true? verified))
      (is (= decrypted data))))
  (testing "roundtrip via Transit serialization"
    (let [skp (iok/->signing-key-pair)
          ekp (iok/->cry-key-pair)
          data ["abc" 123]
          exported-ekp (srlzn/export-public-key ekp :output-format :transit)
          imported-ekp (srlzn/import-public-key exported-ekp :input-format :transit)
          {:keys [encrypted] :as crypted} (iok/encrypt imported-ekp data)
          {:keys [signature]} (iok/sign skp encrypted)
          exported-sig (srlzn/export-signature signature :output-format :transit)
          imported-sig (srlzn/import-signature exported-sig :input-format :transit)
          exported-skp (srlzn/export-public-key skp :output-format :transit)
          {:keys [public-key]} (srlzn/import-signing-public-key exported-skp :input-format :transit)
          {:keys [decrypted]} (iok/decrypt ekp crypted)
          verified (iok/verify public-key encrypted imported-sig)]
      (is (true? verified))
      (is (= decrypted data)))))
