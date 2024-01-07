(ns client-crypto
  (:require
    [io.ok2cry.cry :as cry]
    [promesa.core :as p]))

(defn- request->wire-format
  [{:keys [signing-key encryption-key signed-property signature payload]}]
  (p/let [exported-signing-key (cry/export-key (.-publicKey signing-key))
          exported-signature (cry/array-buffer->hex signature)
          exported-crypto-key (cry/export-key (.-publicKey encryption-key))
          hex-payload (cry/edn->hex-string payload)]
    {:encryption-key exported-crypto-key
     :signing-key exported-signing-key
     :signature exported-signature
     :signed-property signed-property
     :request-data hex-payload}))


(defn request->api-gateway-request
  [request]
  (p/let [request-body (request->wire-format request)]
    (clj->js (-> {:requestContext {:httpMethod "POST"}}
                 (assoc :body (-> request-body clj->js js/JSON.stringify))))))

(defn ->api-gateway-signed-request-data
  [payload property-to-sign]
  (p/let [signing-key (cry/->signing-key)
          signature (cry/sign signing-key (-> payload property-to-sign cry/string->encoded-data))
          encryption-key (cry/->encryption-key)]
    (request->api-gateway-request {:signing-key signing-key
                                   :signature signature
                                   :encryption-key encryption-key
                                   :decryption-key encryption-key
                                   :signed-property property-to-sign
                                   :payload payload})))

