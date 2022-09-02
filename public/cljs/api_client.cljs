(ns api-client
  (:require [promesa.core :as p]
            [reagent.core :as r]
            [reagent.dom :as rdom]))

(defn post-simple
  [component-state]
  (-> (p/let [options (clj->js {:method  "POST"
                                :headers {:Content-Type "application/json"}
                                :body    (-> (clj->js {:id (str (random-uuid))})
                                             (js/JSON.stringify))})
              result (js/fetch "http://localhost:3000/api/simple" options)
              body (p/-> result .json .-body js/JSON.parse (js->clj :keywordize-keys true))]
        (reset! component-state body))
      (p/catch (fn [error]
                 (js/console.log :error error)))))

(defn message [txt]
  [:div (str "Result: " txt)])

(defn fn-request1
  []
  (let [result (r/atom nil)]
    (fn []
      [:div
       [:p [:button {:on-click #(post-simple result)}
            "Post a fn-1 request"]]
       [:div (str "Result: " (:id @result))]])))

(defn fn-request2
  []
  (let [result (r/atom nil)]
    (fn []
      [:div
       [:p [:button {:on-click #(post-simple result)}
            "Post a fn-2 request"]]
       [:div (str "Result: " (:id @result))]])))

(defn home-page
  []
  [:div
   [fn-request1]
   [fn-request2]])

(rdom/render [home-page] (.getElementById js/document "app"))
