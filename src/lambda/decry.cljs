(ns lambda.decry
  (:require
    [clojure.string :as string]
    [promesa.core :as p]))


(def signing-key-params
  {:name "ECDSA" :namedCurve "P-384"})


(def signing-params
  {:name "ECDSA" :hash {:name "SHA-384"}})


(def encryption-key-params
  {:name "RSA-OAEP" :hash "SHA-384"})


(def encryption-params
  {:name "RSA-OAEP"})


(defn verify
  [public-key signature data]
  (js/crypto.subtle.verify
    (clj->js signing-params)
    public-key
    signature
    data))


(defn encrypt
  [crypto-key data]
  (js/crypto.subtle.encrypt
    (clj->js encryption-params)
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
        abuf (js/ArrayBuffer. (count decoded))
        view (js/Uint8Array. abuf)]
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
  (let [size (/ (count hex) 2)
        abuf (js/ArrayBuffer. size)
        view (js/Uint8Array. abuf)
        hex-numbers (map string/join (partition 2 hex))]
    (doseq [index (range size)]
      (aset view index (js/parseInt (nth hex-numbers index) 16)))
    abuf))


(defn hex-string->edn
  [hex-string]
  (-> hex-string
      (hex->array-buffer)
      (encoded-data->string)
      (read-string)))


(defn import-signing-key
  [key-str]
  (let [key-arr (string->array-buffer key-str)]
    (js/crypto.subtle.importKey
      "spki"
      key-arr
      (clj->js signing-key-params)
      false
      ["verify"])))


(defn import-crypto-key
  [key-str]
  (let [key-arr (string->array-buffer key-str)]
    (js/crypto.subtle.importKey
      "spki"
      key-arr
      (clj->js encryption-key-params)
      false
      ["encrypt"])))


(defn obtain-secret-data
  [encryption-key]
  (p/let [pin-data (str (rand-nth (range 1337 7331)))]      ; fake API call
    (->> pin-data
         (string->encoded-data)
         (encrypt encryption-key))))


(defn ->encrypted-response
  [{:keys [signing-key encryption-key signature signed-property request-data] :as request}]
  (p/let [t0 (js/Date.now)
          signing-key' (import-signing-key signing-key)
          encryption-key' (import-crypto-key encryption-key)
          signature' (hex->array-buffer signature)
          request-data' (hex-string->edn request-data)
          signed-data (request-data' (keyword signed-property))
          verified? (verify signing-key' signature' (string->encoded-data signed-data))
          encrypted-data (when verified? (obtain-secret-data encryption-key'))]
    (when encrypted-data
      {:pin (array-buffer->hex encrypted-data)})))


(defn parse-event
  [event]
  (let [clj-event (js->clj event :keywordize-keys true)]
    (if (get clj-event :requestContext)                     ; invoked via API Gateway. Better signal?
      (-> clj-event (get :body) (js/JSON.parse) (js->clj :keywordize-keys true))
      clj-event)))


(defn handler
  [event _ctx]
  (js/console.log event)
  (p/let [time-start (js/Date.now)
          response (-> event
                       (parse-event)
                       (->encrypted-response))]
    (js/console.log "Elapsed: " (- (js/Date.now) time-start) "ms")
    (if response
      (clj->js {:statusCode 200
                :body       (js/JSON.stringify (clj->js response))})
      (clj->js {:statusCode 500}))))


(comment

  (def fake-json
    {"encryption-key"  "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAl9h8cxneWVtXe4Q5+4c2dSCc0WTHr8qns5HochwX2kgEgiGimDJWQzJu6lVEwqQSQvGaKqtG1VtJdasFf0HRwBRb9UKqJV6dua2ohWxq5Z1Dq4IcEnY5sRngXnJBZNZKo5VMFW0Gtqhm3vdJFHD8wLYYx/3FWTRmw+MlD3DnH8dwBN6GzDVvOKhvfUWPxm/5EOtOcwUYUPKreiskgY30RmPYQdfRqf3MN8oLUFvKnBjTbstKQ5979w8SuMZrtNqBREwo0JToM5L5d/p40xol+ear05nW3YLPjQlKlX/OMWiR93KPpWexQYeFxBagBWeh2RZqYqUnWmQN+4netSyQtjtc3TImfNAP4ETHpX5/Jk563XscImWE06/mebcWm2BIxvX58ZtgXNKm2YMk3XI/Q6ypssCT6Kgc8NTpsp5wYs2V9StY9ONz3PiUon3G2tS9l+NgvUdZC6KbB9r+BCl5UC355SlTEQkcwdK8c7G4Vsd1kCju7mow8wanQaAkWQJwkpvfdTMQW1TLGdW8Hug+AlXRQsP7YcgLrKk5xXKbSdq269SUBtQBAHGQfYPDvsTHZjEIwmnElKS1I4NqP+rXrG9F4A/YzhXqX5alg5w4u9HquiRztgTK4PoS2B7IcuOUSidMNuBe4LsRi6tOiZaxG+6tkS/AAFmNbBjwZh1wJc0CAwEAAQ==",
     "signing-key"     "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEs8O566mgXgtF6ykYn2ISzDJAcbZMPZ63YvVevSDndDIYTyfczY+EczL9nEuIsp0GDGB99TzbcthRrNHrMBLaChorUgzO9Vgk0riaNPwS/o913bkKJgx/PJNgJTuWQSrm", "signature" "cd2f4137e7b6ddaa156ad04d12a84eeb3759929b783fe49bc435fc52a136f241dfffabc5fca79faa073c75215740e6b5ae107e81a6686c5167f7a486cc68cb089bc49a684d2545b8e6bfa9746ccec25cf40d0b3d7e9c60ae80c1435643b70054",
     "signed-property" "token", "request-data" "7b3a637573746f6d65722d6964202231346134383361302d656436342d343333662d626330352d663936653939313139636631222c203a746f6b656e202237313862613836652d633130382d343666642d393632362d333330323565623464303961227d"})
  (def fake-request
    (clj->js fake-json))

  (def fake-api-request
    (clj->js (assoc {:requestContext {:httpMethod "POST"}} :body (js/JSON.stringify (clj->js fake-json)))))

  (handler fake-request "0")

  (handler fake-api-request "1")

  )


;; exports
#js {:handler handler}
