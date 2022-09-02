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
                                :body    (-> (clj->js {:id (str (rand-int 10) "-" (name tag))})
                                             (js/JSON.stringify))})
              result (js/fetch "http://localhost:3000/api/simple" options)
              body (p/-> result .json .-body js/JSON.parse (js->clj :keywordize-keys true))]
        (if unset-others
          (reset! state (hash-map tag body))
          (swap! state (fn [m] (-> (apply dissoc m unset-tags)
                                   (assoc tag body))))))
      (p/catch (fn [error]
                 (js/console.log :error error)))))

(defn fn-request
  [label set-tag & {:keys [unset-tags unset-others]
                    :or   {unset-tags [] unset-others false}}]
  [:div.col-xs-3
   [:button.btn.btn-info.btn-sm
    {:on-click #(post-simple set-tag unset-tags unset-others)} label]
   (when-let [{:keys [id]} (get @state set-tag)]
     [:table.table.table-condensed
      [:tbody
       [:tr [:td [:p.text-uppercase "Result data"]] [:td id]]
       [:tr [:td "Result"] [:td id]]
       [:tr [:td [:p.text-lowercase "Result data"]] [:td id]]
       [:tr [:td "Result"] [:td id]]]])])

;<table class="table table-bordered">
;  ...
;</table>

(defn home-page
  []
  [:div.container
   [:div.row
    [:h2 "API Client " [:small "with features "]
     [:span.glyphicon.glyphicon-exclamation-sign]]]
   [:div.row
    [:h3 "DB aspects " [:small "with some nice things "]
     [:span.glyphicon.glyphicon-remove-circle]]]
   [:div.row
    [fn-request "Request Y" :tag-y :unset-others true]
    [fn-request "Request A" :tag-a :unset-tags [:tag-z :tag-b]]
    [fn-request "Request B" :tag-b :unset-tags [:tag-a]]]
   [:div.row
    [:h3 "API invocations " [:small "with affordances to boot "]
     [:span.glyphicon.glyphicon-user]]]
   [:div.row
    [fn-request "Request Y" :tag-y :unset-others true]
    [fn-request "Request A" :tag-a :unset-tags [:tag-z :tag-b]]
    [fn-request "Request B" :tag-b :unset-tags [:tag-a]]]
   [:div.row
    [:h3 "Simulations " [:small "of things "]
     [:span.glyphicon.glyphicon-ok]]]
   [:div.row
    [fn-request "Request Y" :tag-y :unset-others true]
    [fn-request "Request A" :tag-a :unset-tags [:tag-z :tag-b]]
    [fn-request "Request B" :tag-b :unset-tags [:tag-a]]]])

(rdom/render [home-page] (.getElementById js/document "app"))
