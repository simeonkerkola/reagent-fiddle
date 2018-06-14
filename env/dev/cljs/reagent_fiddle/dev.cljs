(ns ^:figwheel-no-load reagent-fiddle.dev
  (:require
    [reagent-fiddle.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
