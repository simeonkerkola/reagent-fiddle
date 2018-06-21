(ns reagent-fiddle.fiddle
    (:require [reagent.core :as r]))

(def board (r/atom {:columns [{:cards []}
                              {:cards [{:text "Hello World!"}]}]}))

(def cards-cursor
  (r/cursor board [:columns 0 :cards]))

; Adds a new card to the board
(swap! cards-cursor conj {:text "New card"})

; Any extra arguments to swap! are passed through as
; extra arguments to the function, so..
(defn set-text! [board col-idx card-idx text]
  ; swap!, takes an atom and a function to apply to the value in the atom
  (swap! board assoc-in [:columns col-idx :cards card-idx :text] text))

(set-text! board 1 0 "Hello octopi!")


; ; get-in is like a regular get function, but instead of a key, it takes a sequence of keys
; (get-in @board [])                           ;;=> {:columns [{:cards []} {:cards [{:text "Hello Turtles."}]}]}
; (get-in @board [:columns])                   ;;=> [{:cards []} {:cards [{:text "Hello Turtles."}]}]
; (get-in @board [:columns 1])                 ;;=> {:cards [{:text "Hello Turtles."}]}
; (get-in @board [:columns 1 :cards])          ;;=> [{:text "Hello Turtles."}]
; (get-in @board [:columns 1 :cards 0])        ;;=> {:text "Hello Turtles."}
; (get-in @board [:columns 1 :cards 0 :text])  ;;=> "Hello Turtles."
