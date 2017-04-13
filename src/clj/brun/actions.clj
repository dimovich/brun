(ns brun.actions
  (:require [webica.core :as w]
            [webica.keys :as wkeys]
            [brun.util :refer :all]))

;; page-down
;;

(def action-list [:page-down
                  :page-up
                  :random-sleep])


(defmulti action (fn [k & args] k))

(defmethod action :page-down [_]
  (press-keys [wkeys/PAGE_DOWN]))

(defmethod action :page-up [_]
  (press-keys [wkeys/PAGE_UP]))

(defmethod action :arrow-down [_]
  (press-keys [wkeys/ARROW_DOWN]))

(defmethod action :arrow-up [_]
  (press-keys [wkeys/ARROW_UP]))

(defmethod action :random-sleep [_ cfg]
  (let [[t1 t2] (:wait-range cfg)]
    (wait (+ t1 (inc (rand-int (- t2 t1)))))))
