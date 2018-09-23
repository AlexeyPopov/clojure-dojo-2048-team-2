(ns ^:figwheel-no-load reagent-frontend.dev
  (:require
    [reagent-frontend.core :as core]
    [devtools.core :as devtools]))


(enable-console-print!)

(devtools/install!)

(core/init!)
