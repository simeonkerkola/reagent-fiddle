(ns reagent-fiddle.core
  (:require [reagent.core :as r]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [cljsjs.react-motion]
            [reagent-fiddle.fiddle]))

(def state (r/atom {:columns [{:id (random-uuid)
                               :title "Todos"
                               :cards [{:id (random-uuid)
                                        :text "Learn about Reagent"}
                                       {:id (random-uuid)
                                        :text "Go to sleep"}]}
                              {:id (random-uuid)
                               :title "Buy"
                               :cards [{:id (random-uuid)
                                        :text "Groceries"}
                                       {:id (random-uuid)
                                        :text "What ever"}]}]}))

(defn- update-text [card-cur text]
  (swap! card-cur assoc :text text)
  (println text))

(defn- stop-editing [card-cur]
  (swap! card-cur dissoc :editing)
  (println "New state is:" @state))

(defn- start-editing [card-cur]
  (swap! card-cur assoc :editing true))

(defn card [card-cur]
  (let [{:keys [editing text]} @card-cur]
    (if editing
      ; If editing, show input field
      [:div.card.editing [:input {:type "text"
                                  :value text
                                  :autoFocus true
                                  :on-change #(update-text card-cur (.. % -target -value))
                                  ; When the text field is not selected anymore
                                  :on-blur #(stop-editing card-cur)
                                  ; When hit enter, stop editing
                                  :on-key-press #(if (= (.-charCode %) 13)
                                                   (stop-editing card-cur))}]]
      [:div.card {:on-click #(start-editing card-cur)} text])))

(defn new-card []
  [:div.new-card
   "+ a new card"])

(defn column [col-cur]
  (let [{:keys [title cards editing]} @col-cur]
    [:div.column
     (if editing
       ; If editing, show input field
       [:input {:type "text" :value title}]
       [:h2 title])

      ; Get each individual card
     (map-indexed (fn [idx {id :id}]
                    ; Creating a cursor based on another cursor, the path of the new cursor
                    ; is now relative to the path of the old one
                    (let [card-cur (r/cursor col-cur [:cards idx])]
                      ^{:key id} [card card-cur]))
                  cards)
     [new-card]]))

(defn new-column []
  [:div.new-column
   "+ new column"])

(defn board [board]
  [:div.board
   (map-indexed (fn [idx {id :id}]
                  (let [col-cur (r/cursor board [:columns idx])]
                    ^{:key id} [column col-cur]))
                (:columns @board))
   [new-column]])
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
