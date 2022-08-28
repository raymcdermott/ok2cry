(ns interactive.server.web
  (:require ["@tinyhttp/app" :as app]
            [common-data]
            [lambda.encry :as encry]
            ["milliparsec" :as milliparsec]))

(def app (app/App.))

(def prologue "<!DOCTYPE html>
  <html>
  <head>
    <script src=\"https://cdn.jsdelivr.net/npm/scittle@0.2.8/dist/scittle.js\" type=\"application/javascript\"></script>
    <script crossorigin src=\"https://unpkg.com/react@17/umd/react.production.min.js\"></script>
    <script crossorigin src=\"https://unpkg.com/react-dom@17/umd/react-dom.production.min.js\"></script>
    <script src=\"https://cdn.jsdelivr.net/npm/scittle@0.2.8/dist/scittle.reagent.js\" type=\"application/javascript\"></script>
    <script type=\"application/x-scittle\">")

(def epilogue "</script>
            </head>
            <body>
              <div id=\"app\"></div>
            </body>
          </html>")

(def call-lambda-1
  "(require '[reagent.core :as r]
            '[reagent.dom :as rdom])

   (def state (r/atom {:clicks 0}))

   (defn post-encrypt-request
     []
     (let [options (clj->js {:method  \"POST\"
                             :headers {:Content-Type \"application/json\"}
                             :body    (-> (clj->js {:encryption-key \"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAl9h8cxneWVtXe4Q5+4c2dSCc0WTHr8qns5HochwX2kgEgiGimDJWQzJu6lVEwqQSQvGaKqtG1VtJdasFf0HRwBRb9UKqJV6dua2ohWxq5Z1Dq4IcEnY5sRngXnJBZNZKo5VMFW0Gtqhm3vdJFHD8wLYYx/3FWTRmw+MlD3DnH8dwBN6GzDVvOKhvfUWPxm/5EOtOcwUYUPKreiskgY30RmPYQdfRqf3MN8oLUFvKnBjTbstKQ5979w8SuMZrtNqBREwo0JToM5L5d/p40xol+ear05nW3YLPjQlKlX/OMWiR93KPpWexQYeFxBagBWeh2RZqYqUnWmQN+4netSyQtjtc3TImfNAP4ETHpX5/Jk563XscImWE06/mebcWm2BIxvX58ZtgXNKm2YMk3XI/Q6ypssCT6Kgc8NTpsp5wYs2V9StY9ONz3PiUon3G2tS9l+NgvUdZC6KbB9r+BCl5UC355SlTEQkcwdK8c7G4Vsd1kCju7mow8wanQaAkWQJwkpvfdTMQW1TLGdW8Hug+AlXRQsP7YcgLrKk5xXKbSdq269SUBtQBAHGQfYPDvsTHZjEIwmnElKS1I4NqP+rXrG9F4A/YzhXqX5alg5w4u9HquiRztgTK4PoS2B7IcuOUSidMNuBe4LsRi6tOiZaxG+6tkS/AAFmNbBjwZh1wJc0CAwEAAQ==\"
                                                    :signing-key    \"MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEs8O566mgXgtF6ykYn2ISzDJAcbZMPZ63YvVevSDndDIYTyfczY+EczL9nEuIsp0GDGB99TzbcthRrNHrMBLaChorUgzO9Vgk0riaNPwS/o913bkKJgx/PJNgJTuWQSrm\"
                                                    :signature      \"cd2f4137e7b6ddaa156ad04d12a84eeb3759929b783fe49bc435fc52a136f241dfffabc5fca79faa073c75215740e6b5ae107e81a6686c5167f7a486cc68cb089bc49a684d2545b8e6bfa9746ccec25cf40d0b3d7e9c60ae80c1435643b70054\"})
                                          (js/JSON.stringify))})]
       (js/console.log \"OPTIONS\" options)
       (-> (js/fetch \"http://localhost:3000/api/encrypt\" options)
           (.then #(.json %))
           (.then #(js/console.log \"OK:\" %))
           (.catch #(js/console.error \"FAIL:\" %)))))

   (defn my-component []
    [:div
      [:p [:button {:on-click post-encrypt-request}
           \"Fetch secret data\"]]])

   (rdom/render [my-component] (.getElementById js/document \"app\"))")

(defn post-encrypt-request
  []
  (let [options (clj->js {:method  "POST"
                          :headers {:Content-Type "application/json"}
                          :body    (js/JSON.stringify (clj->js {:a 1}))})]
    (-> (js/fetch "http://localhost:3000/api/encrypt" options)
        (.then #(.text %))
        (.then #(js/alert %)))))


(defn -main
  [_]
  (-> app
      (.use (milliparsec/json))
      (.get "/" (fn [_req res]
                  (.send res "<h1>Hello world</h1>")))
      (.get "/page/:page/" (fn [_ res]
                             (-> res
                                 (.status 200)
                                 (.send (str prologue call-lambda-1 epilogue)))))
      (.post "/api/encrypt" (fn [req res]
                              (-> res
                                  (.status 200)
                                  ;; stringify the JSON. Might seem weird, but it's to emulate API GW.
                                  (.send (encry/handler (clj->js {:body (js/JSON.stringify (.-body req))}))))))
      (.listen 3000 (fn [] (js/console.log "Listening on http://localhost:3000")))))
