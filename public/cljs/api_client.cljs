(ns api-client
  (:require [promesa.core :as p]
            [reagent.core :as reagent]
            [reagent.dom :as rdom]))

(defonce state (reagent/atom (str (random-uuid))))

(defn post-simple
  []
  (-> (p/let [options (clj->js {:method  "POST"
                                :headers {:Content-Type "application/json"}
                                :body    (-> (clj->js {:id (str (random-uuid))})
                                             (js/JSON.stringify))})
              result (js/fetch "http://localhost:3000/api/simple" options)
              body (p/-> result .json .-body js/JSON.parse (js->clj :keywordize-keys true))]
        (reset! state body))
      (p/catch (fn [error]
                 (js/console.log :error error)))))

(defn message [txt]
  [:div (str "Result: " txt)])

(defn my-component
  []
  (let [{:keys [id]} @state]
    [:div
     [:p [:button {:on-click post-simple}
          "Post a fn request"]]
     [:div (str "Result: " id)]]))

(rdom/render [my-component] (.getElementById js/document "app"))
