(ns reagent-fiddle.prod
  (:require [reagent-fiddle.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
