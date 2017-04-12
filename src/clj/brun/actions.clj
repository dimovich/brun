(ns brun.actions
  (:require [webica.core :as w]
            [webica.keys :as wkeys]
            [brun.util :refer [press-keys
                               wait]]))

;; page-down
;; 

(def action-list [:page-down
                  :page-up
                  :random-sleep])


(defmulti action (fn [k _] k))

(defmethod action :page-down [_ _]
  (press-keys [wkeys/PAGE_DOWN]))

(defmethod action :random-sleep [_ ctx]
  (let [[t1 t2] (:sleep-range ctx)]
    (wait (+ t1 (inc (rand-int (- t2 t1)))))))


