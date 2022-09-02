(ns api-client
  (:require
    [promesa.core :as p]
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(defonce state (r/atom nil))

(defn post-simple
  [tag unset-tags unset-others]
  (-> (p/let [options (clj->js {:method  "POST"
                                :headers {:Content-Type "application/json"}
                                :body    (-> (clj->js {:id (str (random-uuid) "-" (name tag))})
                                             (js/JSON.stringify))})
              result (js/fetch "http://localhost:3000/api/simple" options)
              body (p/-> result .json .-body js/JSON.parse (js->clj :keywordize-keys true))]
        (if unset-others
          (reset! state (hash-map tag body))
          (swap! state (fn [m] (-> (assoc m tag body)
                                   (#(apply dissoc % unset-tags)))))))
      (p/catch (fn [error]
                 (js/console.log :error error)))))

(defn fn-request
  [label set-tag & {:keys [unset-tags unset-others]
                    :or   {unset-tags [] unset-others false}}]
  [:div.col-xs-2
   [:p [:button.btn.btn-info
        {:on-click #(post-simple set-tag unset-tags unset-others)} label]]
   (when-let [data (get @state set-tag)]
     [:div (str "Result: " data)])])

(defn home-page
  []
  [:div.container
   [:div.row
    [:h1 "API Client"]]
   [:div.row
    [fn-request "Request Y" :tag-y :unset-others true]
    [fn-request "Request Z" :tag-z :unset-tags [:tag-a]]
    [fn-request "Request A" :tag-a :unset-tags [:tag-z :tag-b]]
    [fn-request "Request B" :tag-b :unset-tags [:tag-a]]]])

(rdom/render [home-page] (.getElementById js/document "app"))
