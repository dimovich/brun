(ns brun.actions)

;; page-down
;; 



(defmulti action (fn [k _] k))

(defmethod action :page-down [_ ctx]
  (println "Hello"))
