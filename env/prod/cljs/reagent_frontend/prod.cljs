(ns reagent-frontend.prod
  (:require
    [reagent-frontend.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
