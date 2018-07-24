(ns reagent-fiddle.core
  (:require [reagent.core :as r]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [cljsjs.react-motion]
            [reagent-fiddle.fiddle]))

(def state (r/atom {:columns [{:id (random-uuid)
                               :title "Todos"
                               :cards [{:id (random-uuid)
                                        :title "Learn about Reagent"}
                                       {:id (random-uuid)
                                        :title "Go to sleep"}]}
                              {:id (random-uuid)
                               :title "Buy"
                               :cards [{:id (random-uuid)
                                        :title "Groceries"}
                                       {:id (random-uuid)
                                        :title "What ever"}]}]}))

(defn auto-focus-input [props]
  (r/create-class
   {:display-name "auto-focus-input"
    :component-did-mount (fn [component]
                           (.focus (r/dom-node component)))
    :reagent-render (fn [props]
                      [:input props])}))

(defn- update-title [cursor title]
  (swap! cursor assoc :title title)
  (println title))

(defn- stop-editing [cursor]
  (swap! cursor dissoc :editing)
  (println "New state is:" @state))

(defn- start-editing [cursor]
  (swap! cursor assoc :editing true))

(defn editable [el cursor]
  (let [{:keys [editing title]} @cursor]
    (if editing
      ; If editing, show input field
      [el {:className "editing"}
       [auto-focus-input {:type "title"
                           :value title
                           :on-change #(update-title cursor (.. % -target -value))
                           ; When the title field is not selected anymore
                           :on-blur #(stop-editing cursor)
                           ; When hit enter, stop editing
                           :on-key-press #(if (= (.-charCode %) 13)
                                             (stop-editing cursor))}]]
      [el {:on-click #(start-editing cursor)} title])))

(defn card [cursor]
  [editable :div.card cursor])

(defn- add-new-card [col-cur]
  (swap! col-cur update :cards conj {:id (random-uuid)
                                     :title ""
                                     :editing true}))

(defn new-card [col-cur]
  [:div.new-card
   {:on-click #(add-new-card col-cur)}
   "+ a new card"])

(defn column [col-cur]
  (let [{:keys [title cards]} @col-cur]
    [:div.column
     ^{:key title}[editable :h2 col-cur]

      ; Get each individual card
     (map-indexed (fn [idx {id :id}]
                    ; Creating a cursor based on another cursor, the path of the new cursor
                    ; is now relative to the path of the old one
                    (let [card-cur (r/cursor col-cur [:cards idx])]
                      ^{:key id} [card card-cur]))
                  cards)
     [new-card col-cur]]))

(defn- add-new-column [board]
  (swap! state update :columns conj {:id (random-uuid)
                                     :title ""
                                     :cards []
                                     :editing true}))

(defn new-column [board]
  [:div.new-column
   {:on-click #(add-new-column board)}
   "+ new column"])

(defn board [board]
  [:div.board
   (map-indexed (fn [idx {id :id}]
                  (let [col-cur (r/cursor board [:columns idx])]
                    ^{:key id} [column col-cur]))
                (:columns @board))
   [new-column board]])
   ; ; Loop thru columns from the state, and make a component for each
   ; (for [c (:columns @state)]
   ;   ^{:key c} [column c])
   ; [new-column]))

;; -------------------------
;; Views


(defn home-page []
  [:div
   [board state]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About reagent-fiddle"]
   [:div [:a {:href "/"} "go to the home page"]]])

;; -------------------------
;; Routes

(defonce page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
                    (reset! page #'home-page))

(secretary/defroute "/about" []
                    (reset! page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (secretary/dispatch! path))
    :path-exists?
    (fn [path]
      (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
