(ns lambda.simple
  (:require
    [cljs-bean.core :as cb]
    [promesa.core :as p]))

(defn event->body-data
  [event]
  (some-> event cb/->clj :body js/JSON.parse cb/->clj))


(defn handler
  "echo inputs"
  [event _ctx]
  (p/let [response (event->body-data event)]
    (println "response" response)
    (if response
      (clj->js {:statusCode 200
                :body       (js/JSON.stringify (clj->js response))})
      (clj->js {:statusCode 500}))))

;; exports
#js {:handler handler}
