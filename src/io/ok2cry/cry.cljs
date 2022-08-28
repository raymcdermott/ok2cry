(ns io.ok2cry.cry
  (:require
    #?@(:org.babashka/nbb [["crypto" :as crypto]
                           ["buffer" :as buffer]]
        :cljs             [])
    [clojure.string :as string]
    [clojure.edn :as edn]
    [promesa.core :as p]
    [clojure.string :as string]))

#?(:org.babashka/nbb
   (def subtle (.. crypto -webcrypto -subtle))
   :cljs
   (def subtle js/crypto.subtle))


(defn verify
  [public-key signature data]
  (.verify subtle
           (clj->js {:name "ECDSA" :hash {:name "SHA-384"}})
           public-key
           signature
           data))


(defn encrypt
  [crypto-key data]
  (.encrypt subtle
            (clj->js {:name "RSA-OAEP"})
            crypto-key
            data))


(defn string->encoded-data
  [string]
  (let [encoder (js/TextEncoder.)]                          ; Always UTF-8
    (.encode encoder string)))


(defn encoded-data->string
  [data]
  (let [decoder (js/TextDecoder. "utf-8")]
    (.decode decoder data)))


(defn string->array-buffer
  [s]
  (let [decoded (.toString (js/Buffer.from s "base64") "binary")
        abuf    (js/ArrayBuffer. (count decoded))
        view    (js/Uint8Array. abuf)]
    (doseq [index (range (count decoded))]
      (aset view index (.charCodeAt decoded index)))
    abuf))


(defn array-buffer->hex
  [array-buffer]
  (->> (js/Array.from (js/Uint8Array. array-buffer))
       (map #(.padStart (.toString % 16) 2 "0"))
       (apply str)))


(defn hex->array-buffer
  [hex]
  (let [size        (/ (count hex) 2)
        abuf        (js/ArrayBuffer. size)
        view        (js/Uint8Array. abuf)
        hex-numbers (map string/join (partition 2 hex))]
    (doseq [index (range size)]
      (aset view index (js/parseInt (nth hex-numbers index) 16)))
    abuf))


(defn hex-string->edn
  [hex-string]
  (-> hex-string
      (hex->array-buffer)
      (encoded-data->string)
      (edn/read-string)))


(defn to-hex
  [^js/Uint8Array array]
  #?(:org.babashka/nbb
     (.toString (buffer/Buffer.from array) "hex")
     :cljs
     (->> (js/Array.from array)
          (map #(.padStart (.toString % 16) 2 "0"))
          (apply str))))

(defn from-base64
  [base64]
  #?(:org.babashka/nbb
     (js/Uint8Array.from (buffer/Buffer.from base64 "base64"))
     :cljs
     (let [decoded (.toString (js/Buffer.from base64 "base64") "binary")
           abuf    (js/ArrayBuffer. (count decoded))
           view    (js/Uint8Array. abuf)]
       (doseq [index (range (count decoded))]
         (aset view index (.charCodeAt decoded index)))
       abuf)))


(defn edn->hex-string
  [edn-data]
  (-> edn-data
      (pr-str)
      (string->encoded-data)
      (to-hex)))


(defn import-signing-key
  [key-str]
  (let [key-arr (from-base64 key-str)]
    (println :has-array)
    (.importKey subtle
                "spki"
                key-arr
                (clj->js {:name "ECDSA" :namedCurve "P-384"})
                false
                #js ["verify"])))


(defn import-crypto-key
  [key-str]
  (let [key-arr (from-base64 key-str)]
    (.importKey subtle
                "spki"
                key-arr
                (clj->js {:name "RSA-OAEP" :hash "SHA-384"})
                false
                #js ["encrypt"])))

(defn string->encoded-data
  [string]
  (let [encoder (js/TextEncoder.)]                          ;; Always UTF-8
    (.encode encoder string)))

(defn encoded-data->string
  [data]
  (let [decoder (js/TextDecoder. "utf-8")]
    (.decode decoder data)))

(defn- array-buffer->hex
  [array-buffer]
  (->> (js/Array.from (js/Uint8Array. array-buffer))
       (map #(.padStart (.toString % 16) 2 "0"))
       (apply str)))

(defn- hex->array-buffer
  [hex]
  {:pre [(even? (/ (count hex) 2))]}
  (let [size        (/ (count hex) 2)
        abuf        (js/ArrayBuffer. size)
        view        (js/Uint8Array. abuf)
        hex-numbers (map string/join (partition 2 hex))]
    (doseq [index (range size)]
      (aset view index (js/parseInt (nth hex-numbers index) 16)))
    abuf))

(defn- decrypt
  [crypto-key ciphertext]
  (.decrypt subtle
            (clj->js {:name "RSA-OAEP"})
            crypto-key
            ciphertext))

; TODO for export-key
; could add these to make it more standard but not gonna bother for this spike - just more fluff code
; cos the header / footer are always stripped
; -----BEGIN PUBLIC KEY-----\nBase64Key\n-----END PUBLIC KEY-----

(defn- export-key
  [key]
  (p/let [exported (.exportKey subtle "spki" key)]
         (-> (js/String.fromCharCode.apply nil (js/Uint8Array. exported))
             (js/btoa))))

(defn sign
  [signing-key data]
  (.sign subtle
         (clj->js {:name "ECDSA" :hash {:name "SHA-384"}})
         (.-privateKey signing-key)
         data))

(defn ->encryption-key
  []
  (.generateKey subtle
                (clj->js {:name           "RSA-OAEP"
                          :modulusLength  4096
                          :publicExponent (js/Uint8Array. [1 0 1])
                          :hash           "SHA-384"})
                true                                        ;; Extractable
                #js ["encrypt" "decrypt"]))

(defn ->signing-key
  []
  (.generateKey subtle
                (clj->js {:name "ECDSA" :namedCurve "P-384"})
                true                                        ;; Extractable
                #js ["sign", "verify"]))

