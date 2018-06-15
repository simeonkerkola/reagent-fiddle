(ns reagent-fiddle.core
  (:require [reagent.core :as r :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [cljsjs.react-motion]))

(def state (r/atom {:columns [{:title "Todos"
                               :cards [{:title "Learn about Reagent"}
                                       {:title "Go to sleep"}]
                               :editing true}
                              {:title "Fixes"
                               :cards [{:title "Fix dinner"}
                                       {:title "The Bike"
                                        :editing true}]}]}))

(defn card [card]
   (if (:editing card)
    ; If editing, show input field
    [:div.card.editing [:input {:type "text" :value (:title card)}]]
    [:div.card (:title card)]))

(defn new-card []
  [:div.new-card
   "+ a new card"])

(defn column [{:keys [title cards editing]}]
  [:div.column
   (if editing
     ; If editing, show input field
     [:input {:type "text" :value title}]
     [:h2 title])

   ; Get each individual card
   (for [c cards]
     ^{:key c} [card c])
   [new-card]])

(defn new-column []
  [:div.new-column
   "+ new column"])

(defn board [state]
  [:div.board

   ; Loop thru columns from the state, and make a component for each
   (for [c (:columns @state)]
     ^{:key c} [column c])
   [new-column]])

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
