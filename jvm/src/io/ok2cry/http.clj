(ns io.ok2cry.http
  (:require
    [ring.adapter.jetty :as jetty]
    [ring.middleware.params :as params]
    [ring.middleware.reload :refer [wrap-reload]]
    [ring.util.response :as resp]))


(defn handler [request]
  (-> (resp/response "Hello Dad")
      (resp/content-type "text/plain")))


(def app
  (-> handler
      params/wrap-params))


(defn run-jetty
  [port]
  (jetty/run-jetty
    (wrap-reload #'app)
    {:port port :join? false}))
