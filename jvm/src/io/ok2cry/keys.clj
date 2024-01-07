(ns io.ok2cry.keys
  (:require [io.ok2cry.data :as data])
  (:import (java.security KeyPairGenerator PrivateKey PublicKey SecureRandom Security Signature)
           (java.security.spec ECGenParameterSpec MGF1ParameterSpec)
           (javax.crypto Cipher)
           (javax.crypto.spec OAEPParameterSpec PSource$PSpecified)
           (org.bouncycastle.jce.provider BouncyCastleProvider)))

(set! *warn-on-reflection* true)

(Security/addProvider (BouncyCastleProvider.))

(def default-ttl (or (System/getenv "OK2CRY_TTL")
                     (System/getProperty "ok2cry.ttl") 30000))

(defn ->signing-key-pair
  "Make a signing key pair, not an en|decrypting key pair. Optional parameters:
  ttl - how long to cache the key, default is 30 seconds. Must be a positive number.
  curve - the default curve is secp256r1"
  ([]
   (->signing-key-pair default-ttl))
  ([ttl]
   {:pre [(pos-int? ttl)]}
   (->signing-key-pair ttl "secp256r1"))
  ([ttl ^String curve]
   {:pre [(and (pos-int? ttl) (seq curve))]}
   (let [ps (ECGenParameterSpec. curve)
         kp (-> (doto (KeyPairGenerator/getInstance "EC" BouncyCastleProvider/PROVIDER_NAME)
                  (.initialize ps))
                (.generateKeyPair))]
     {:private-key (.getPrivate kp)
      :public-key (.getPublic kp)
      :key-spec curve
      :ttl ttl
      :expires (+ (System/currentTimeMillis) ttl)})))

(defn ->cry-key-pair
  "Make an en|decrypting key pair, not a signing key pair. Optional parameter:
  ttl - how long to cache the key, default is 30 seconds. Must be a positive number."
  ([]
   (->cry-key-pair default-ttl))
  ([ttl]
   {:pre [(pos-int? ttl)]}
   (let [kp (-> (doto (KeyPairGenerator/getInstance "RSA" BouncyCastleProvider/PROVIDER_NAME)
                  (.initialize 1024 (SecureRandom.)))
                (.generateKeyPair))]
     {:private-key (.getPrivate kp)
      :public-key (.getPublic kp)
      :ttl ttl
      :expires (+ (System/currentTimeMillis) ttl)})))


(defn sign
  "Produce a signature of the edn-data-to-sign using the given private key"
  [{:keys [^PrivateKey private-key]} edn-data-to-sign]
  {:pre [(every? some? [private-key edn-data-to-sign])]}
  (let [data-bytes ^bytes (data/edn->bytes edn-data-to-sign)]
    {:signature (-> (doto (Signature/getInstance "SHA256withECDSA" BouncyCastleProvider/PROVIDER_NAME)
                      (.initSign private-key)
                      (.update data-bytes))
                    (.sign))}))


(defn verify
  "Use the public-key to verify that the signed-data matches the signature"
  [^PublicKey public-key signed-data {:keys [signature algorithm]
                                      :or {algorithm :SHA256withECDSA}}]
  {:pre [(every? some? [public-key signed-data])]}
  (try
    (let [data-bytes ^bytes (data/edn->bytes signed-data)]
      (-> (doto (Signature/getInstance (name algorithm))
            (.initVerify public-key)
            (.update data-bytes))
          (.verify signature)))
    (catch Exception _ false)))


(defn encrypt
  "Use a cipher mechanism that is implemented on many clients"
  [{:keys [^PublicKey public-key]} edn-data]
  {:pre [(every? some? [public-key edn-data])]}
  (let [data (.getBytes (pr-str edn-data) "UTF-8")
        encrypted (-> (doto (Cipher/getInstance "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING")
                        (.init Cipher/ENCRYPT_MODE ^PublicKey public-key))
                      (.doFinal data))]
    {:encrypted encrypted}))


(defn decrypt
  "Use a deciphering configuration that is implemented on many clients"
  [{:keys [^PrivateKey private-key]} {:keys [^bytes encrypted]}]
  {:pre [(every? some? [private-key encrypted])]}
  (let [algo-params (OAEPParameterSpec. "SHA-256" "MGF1" (MGF1ParameterSpec. "SHA-1") PSource$PSpecified/DEFAULT)
        decrypted-bytes (-> (doto (Cipher/getInstance "RSA/ECB/OAEPPadding")
                              (.init Cipher/DECRYPT_MODE ^PrivateKey private-key algo-params))
                            (.doFinal encrypted))]
    {:decrypted (read-string (String. decrypted-bytes))}))


