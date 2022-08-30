(ns api-client
  (:require [promesa.core :as p]
            [reagent.dom :as rdom]))

(defn post-encrypt-request
  []
  (let [options (clj->js {:method  "POST"
                          :headers {:Content-Type "application/json"}
                          :body    (-> (clj->js {:encryption-key "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAl9h8cxneWVtXe4Q5+4c2dSCc0WTHr8qns5HochwX2kgEgiGimDJWQzJu6lVEwqQSQvGaKqtG1VtJdasFf0HRwBRb9UKqJV6dua2ohWxq5Z1Dq4IcEnY5sRngXnJBZNZKo5VMFW0Gtqhm3vdJFHD8wLYYx/3FWTRmw+MlD3DnH8dwBN6GzDVvOKhvfUWPxm/5EOtOcwUYUPKreiskgY30RmPYQdfRqf3MN8oLUFvKnBjTbstKQ5979w8SuMZrtNqBREwo0JToM5L5d/p40xol+ear05nW3YLPjQlKlX/OMWiR93KPpWexQYeFxBagBWeh2RZqYqUnWmQN+4netSyQtjtc3TImfNAP4ETHpX5/Jk563XscImWE06/mebcWm2BIxvX58ZtgXNKm2YMk3XI/Q6ypssCT6Kgc8NTpsp5wYs2V9StY9ONz3PiUon3G2tS9l+NgvUdZC6KbB9r+BCl5UC355SlTEQkcwdK8c7G4Vsd1kCju7mow8wanQaAkWQJwkpvfdTMQW1TLGdW8Hug+AlXRQsP7YcgLrKk5xXKbSdq269SUBtQBAHGQfYPDvsTHZjEIwmnElKS1I4NqP+rXrG9F4A/YzhXqX5alg5w4u9HquiRztgTK4PoS2B7IcuOUSidMNuBe4LsRi6tOiZaxG+6tkS/AAFmNbBjwZh1wJc0CAwEAAQ=="
                                                 :signing-key    "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEs8O566mgXgtF6ykYn2ISzDJAcbZMPZ63YvVevSDndDIYTyfczY+EczL9nEuIsp0GDGB99TzbcthRrNHrMBLaChorUgzO9Vgk0riaNPwS/o913bkKJgx/PJNgJTuWQSrm"
                                                 :signature      "cd2f4137e7b6ddaa156ad04d12a84eeb3759929b783fe49bc435fc52a136f241dfffabc5fca79faa073c75215740e6b5ae107e81a6686c5167f7a486cc68cb089bc49a684d2545b8e6bfa9746ccec25cf40d0b3d7e9c60ae80c1435643b70054"})
                                       (js/JSON.stringify))})]
    (-> (p/let [result (js/fetch "http://localhost:3000/api/encrypt" options)
                body (.-body result)]
          (js/console.log "result" result))
        (p/catch (fn [error]
                   (js/console.log :error error))))))

(defn my-component []
  [:div
   [:p [:button {:on-click post-encrypt-request}
        "Post an encryption request"]]])

(rdom/render [my-component] (.getElementById js/document "app"))
