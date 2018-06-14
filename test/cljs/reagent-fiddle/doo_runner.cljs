(ns reagent.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [reagent.core-test]))

(doo-tests 'reagent.core-test)
