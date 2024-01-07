(ns lambda.common)

(defn respond
  [response]
  (if response
    (clj->js {:statusCode 200
              :body       (-> response clj->js js/JSON.stringify)})
    (clj->js {:statusCode 500})))
