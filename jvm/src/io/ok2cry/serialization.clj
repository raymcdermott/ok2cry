(ns io.ok2cry.serialization
  "Provide functions to create, emit and consume keys and cryptographically signed and / or
  encrypted material that are suitable for sending over or receiving from a network."
  (:require [clojure.data.json :as json]
            [cognitect.transit :as transit]
            [io.ok2cry.data :as data])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)
           (java.security KeyFactory PublicKey Security)
           (java.security.spec X509EncodedKeySpec)
           (org.bouncycastle.jce.provider BouncyCastleProvider)))

(set! *warn-on-reflection* true)

(Security/addProvider (BouncyCastleProvider.))              ;


(defn data->export
  [export-data & {:keys [output-format]
                  :or   {output-format :json}}]
  (condp = output-format
    :edn-hex (data/edn->hex export-data)
    :transit (let [out (ByteArrayOutputStream. 4096)]
               (-> out
                   (transit/writer :json)
                   (transit/write export-data))
               out)
    :json (json/write-str export-data)))

(defn data->import
  [import-data & {:keys [input-format]
                  :or   {input-format :json}}]
  (condp = input-format
    :edn-hex (data/hex->edn import-data)
    :transit (let [in     (-> ^ByteArrayOutputStream import-data
                              (.toByteArray)
                              (ByteArrayInputStream.))
                   reader (transit/reader in :json)]
               (transit/read reader))
    :json (json/read-str import-data :key-fn keyword)))

(defn export
  [data export-fn options]
  (-> (export-fn data)
      (data->export options)))

(defn key-pair->public-key-hex
  "Export the public key as hex with relevant metadata"
  [{:keys [public-key key-spec]}]
  {:public-key (-> ^PublicKey public-key
                   (.getEncoded)
                   (data/bytes->hex-string))
   :key-spec   key-spec})

(defn export-public-key
  [key-pair & {:keys [output-format] :as options
               :or   {output-format :json}}]
  (export key-pair key-pair->public-key-hex options))

(defn signature->hex
  "Export the signature as hex with relevant metadata"
  [signature & {:keys [algorithm]
                :or   {algorithm :SHA256withECDSA}}]
  {:signature (data/bytes->hex-string signature)
   :algorithm algorithm})

(defn export-signature
  [signature & {:keys [output-format] :as options
                :or   {output-format :json}}]
  (export signature signature->hex options))


(defn import-public-key
  [serialized-key & {:keys [input-format] :as options
                     :or   {input-format :json}}]
  (let [{:keys [public-key]} (data->import serialized-key options)
        key-bytes        (data/hex-string->bytes public-key)
        encoded-key-spec (X509EncodedKeySpec. key-bytes)]
    {:public-key (-> (KeyFactory/getInstance "RSA" BouncyCastleProvider/PROVIDER_NAME)
                     (.generatePublic encoded-key-spec))}))

(defn import-signing-public-key
  [serialized-key & {:keys [input-format] :as options
                     :or   {input-format :json}}]
  (let [{:keys [public-key]} (data->import serialized-key options)
        key-bytes        (data/hex-string->bytes public-key)
        encoded-key-spec (X509EncodedKeySpec. key-bytes)]
    {:public-key (-> (KeyFactory/getInstance "EC" BouncyCastleProvider/PROVIDER_NAME)
                     (.generatePublic encoded-key-spec))}))

(defn hex->signature
  [hex-data]
  (data/hex-string->bytes hex-data))

(defn import-signature
  [serialized-sig & {:keys [input-format] :as options
                     :or   {input-format :json}}]
  (let [{:keys [signature] :as sig-data} (data->import serialized-sig options)]
    (assoc sig-data :signature (hex->signature signature))))
