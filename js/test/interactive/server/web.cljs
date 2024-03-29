(ns interactive.server.web
  (:require ["@tinyhttp/app" :as app]
            ["fs" :as fs]
            [lambda.simple :as simple]
            ["@tinyhttp/logger" :as logger]
            ["path" :as path]
            [promesa.core :as p]
            ["milliparsec" :as milliparsec]))

(def app (app/App.))

(def document-root "public")

(defn send-file
  [res path]
  (.sendFile res path #js {:root document-root}))

(defn stream-file
  "This keeps FireFox happy"
  [res path]
  (let [content (->> path (str document-root) path/resolve fs/readFileSync)]
    (-> res
        (.type "text/plain")
        (.send content))))

(defn req->api-gateway-data
  "Model the data request data in a similar manner to API Gateway"
  [req]
  (clj->js {:body (js/JSON.stringify (.-body req))}))

(defn call-handler
  [req res]
  (p/let [api (keyword (subs (.-path req) (count "/api/")))
          request-body (req->api-gateway-data req)
          result (condp = api
                   :simple (simple/handler request-body nil)
                   "Ooops")]
    (js/console.log "result" result)
         (-> res
             (.status 200)
             (.send result))))

(defn start-server
  [port]
  (-> app
      (.use (milliparsec/json))
      (.use (logger/logger))
      (.get "/"
            (fn [_req res] (send-file res "/index.html")))
      (.get "/css/:sheet"
            (fn [req res]
              (send-file res (.-path req))))
      (.get "/cljs/:ns"
            (fn [req res]
              (stream-file res (.-path req))))
      (.get "/js/:package"
            (fn [req res]
              (send-file res (.-path req))))
      (.post "/api/:path"
             (fn [req res] (call-handler req res)))
      (.listen port
               (fn []
                 (js/console.log "Listening on http://localhost:" port)))))

(defn -main
  [_]
  (start-server 3000))
