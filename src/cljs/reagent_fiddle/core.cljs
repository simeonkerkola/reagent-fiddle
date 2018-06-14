(ns reagent-fiddle.core
    (:require [reagent.core :as r :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [cljsjs.react-motion]))

(def Motion js/ReactMotion.Motion)
(def spring js/ReactMotion.spring)

(def toggled? (r/atom false))

(defn green-button [txt]
  (fn [txt]
    [:button txt]))
;; -------------------------
;; Views



(defn home-page []
  [:div
   [:h2 "Welcome"]
   [green-button "moi"]

   [:div [:a {:href "/about"} "go to about page"]]])
   ; [:div
   ;  [:button {:on-mouse-down #(swap! toggled? not)} "Toggle"]
   ;  ; Smiley bird :> Motion is same as using (r/adapt-react-class Motion)
   ;  [:> Motion {:style #js {:x (spring (if @toggled? 400 0))}} ; #js == js reader tag
   ;   (fn [style]
   ;     (let [x (.-x style)] ; style is js-object, so easiest way to get the x property out is .-x
   ;       (r/as-element ; Turn hiccup into a plain React element, which the Motion will then render
   ;         [:div.slider
   ;          [:div.slider-block
   ;           {:style {:transform (str "translate3d(" x "px, 0, 0)")}}]])))]]])



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
