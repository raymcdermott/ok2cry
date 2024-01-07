(ns lambda.encry
  "Encrypt data using a public key from the other party"
  (:require
    [cljs-bean.core :as cb]
    [io.ok2cry.cry :as cry]
    [lambda.common :refer [respond]]
    [nbb.core :refer [time]]
    [promesa.core :as p]))


(defn obtain-secret-data
  [encryption-key]
  (p/let [pin-data (str (rand-nth (range 1337 7331)))]      ; fake API call
    (->> pin-data
         (cry/string->encoded-data)
         (cry/encrypt encryption-key))))


(defn ->encrypted-response
  [{:keys [signing-key encryption-key signature signed-property request-data]}]
  (p/let [signing-key' (cry/import-signing-key signing-key)
          _ (tap> :imported-signing-key)
          encryption-key' (cry/import-crypto-key encryption-key)
          _ (tap> :imported-cry-key)
          signature' (cry/hex->array-buffer signature)
          _ (tap> :imported-signature)
          request-data' (cry/hex-string->edn request-data)
          _ (tap> [:request-data request-data'])
          signed-data (request-data' (keyword signed-property))
          _ (tap> [:signed-data signed-data])
          verified? (cry/verify signing-key' signature' (cry/string->encoded-data signed-data))
          _ (tap> [:verified verified?])
          encrypted-data (when verified? (obtain-secret-data encryption-key'))]
    (when encrypted-data
      {:pin (cry/array-buffer->hex encrypted-data)})))


(defn event->body-data
  [event]
  (some-> event cb/->clj :body js/JSON.parse cb/->clj))

(defn handler
  [event & _ctx]
  (p/-> event
        event->body-data
        ->encrypted-response
        respond))

;; expose the Lambda for access in the ES Module (see encry.mjs at the root of the project)
#js {:handler handler}


(comment
  (def fake-json
    {"encryption-key" "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAl9h8cxneWVtXe4Q5+4c2dSCc0WTHr8qns5HochwX2kgEgiGimDJWQzJu6lVEwqQSQvGaKqtG1VtJdasFf0HRwBRb9UKqJV6dua2ohWxq5Z1Dq4IcEnY5sRngXnJBZNZKo5VMFW0Gtqhm3vdJFHD8wLYYx/3FWTRmw+MlD3DnH8dwBN6GzDVvOKhvfUWPxm/5EOtOcwUYUPKreiskgY30RmPYQdfRqf3MN8oLUFvKnBjTbstKQ5979w8SuMZrtNqBREwo0JToM5L5d/p40xol+ear05nW3YLPjQlKlX/OMWiR93KPpWexQYeFxBagBWeh2RZqYqUnWmQN+4netSyQtjtc3TImfNAP4ETHpX5/Jk563XscImWE06/mebcWm2BIxvX58ZtgXNKm2YMk3XI/Q6ypssCT6Kgc8NTpsp5wYs2V9StY9ONz3PiUon3G2tS9l+NgvUdZC6KbB9r+BCl5UC355SlTEQkcwdK8c7G4Vsd1kCju7mow8wanQaAkWQJwkpvfdTMQW1TLGdW8Hug+AlXRQsP7YcgLrKk5xXKbSdq269SUBtQBAHGQfYPDvsTHZjEIwmnElKS1I4NqP+rXrG9F4A/YzhXqX5alg5w4u9HquiRztgTK4PoS2B7IcuOUSidMNuBe4LsRi6tOiZaxG+6tkS/AAFmNbBjwZh1wJc0CAwEAAQ==",
     "signing-key" "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEs8O566mgXgtF6ykYn2ISzDJAcbZMPZ63YvVevSDndDIYTyfczY+EczL9nEuIsp0GDGB99TzbcthRrNHrMBLaChorUgzO9Vgk0riaNPwS/o913bkKJgx/PJNgJTuWQSrm", "signature" "cd2f4137e7b6ddaa156ad04d12a84eeb3759929b783fe49bc435fc52a136f241dfffabc5fca79faa073c75215740e6b5ae107e81a6686c5167f7a486cc68cb089bc49a684d2545b8e6bfa9746ccec25cf40d0b3d7e9c60ae80c1435643b70054",
     "signed-property" "token",
     "request-data" "7b3a637573746f6d65722d6964202231346134383361302d656436342d343333662d626330352d663936653939313139636631222c203a746f6b656e202237313862613836652d633130382d343666642d393632362d333330323565623464303961227d"})

  (def fake-api-request
    (clj->js (assoc {:requestContext {:httpMethod "POST"}} :body (js/JSON.stringify (clj->js fake-json)))))

  (p/let [x (handler fake-api-request "0")]
    (println x)))

