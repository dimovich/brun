(ns brun.core
  (:require [clojure.edn :as edn]
            [brun.util :refer :all]
            [taoensso.timbre :as timbre :refer [info]])
  (:gen-class))


(def config-file "config.txt")

(timbre/set-config!
 {:level :info
  :appenders {:println (timbre/println-appender {:stream :auto})}})


(defn login [config]
  (info "sign in...")
  (navigate (:main-url config))
  (wait-for-id "hamburger-button")
  
  (when-not (empty? (by-class-all "js-adobeid-signin"))
    (let [ham (by-id "hamburger-button")
          logins (by-class-all "js-adobeid-signin")]
    
      ;; make sure at least one login is visible
      (when (visible? ham)
        (move-mouse-and-click ham))

      (wait 5)
    
      ;; click on login
      (move-mouse-and-click
       (->> logins
            (filter #(visible? %))
            first))

      (wait-for-id "sign_in")
    
      ;; enter credentials
      (let [user (by-name "username")
            pass (by-name "password")
            sign-in (by-id "sign_in")]
        (slowly-type user (:user config))
        (slowly-type pass (:pass config))
        (random-sleep)
        (move-mouse-and-click sign-in)))))


(defn zoom-random-item [])


(defn explore-item [el]
  (info "exploring item [" (get-text el) "]")
  (let [aprct (rand-nth [0 1])]
    (get-to el)
    (move-mouse-and-click el)
    ;;explore
    (dotimes [_ (inc (rand-int 5))]
      (random-sleep [1 2])
      ((rand-nth [page-down zoom-random-item])))
    (when (pos? aprct)
      (let [el (wait-for-id "appreciation")]
        (get-to el)
        (move-mouse-and-click el)))
    (random-sleep)
    (navigate-back)
    aprct))



(defn like-items [config]
  (info "liking items...")
  (navigate (:recent-url config))
  (wait-for-class "cover-name-link")

  (loop [total-liked 0]
    (when (< total-liked (:max-items config))
      (info "looking around...")
      (dotimes [_ (inc (rand-int 7))]
        (random-sleep)
        ((rand-nth [page-down page-down page-down page-up
                    arrow-down arrow-up f5])))
      (let [item (rand-nth (by-class-all "cover-name-link"))]
        (recur (+ total-liked (explore-item item)))))))




(defn -main [& args]
  (let [config (edn/read-string (slurp config-file))]
    (info "starting up with config: \n" config)
    (startup config)
    (login config)
    (like-items config)
    (cleanup)))



;; zoom-in
;; hide window
;; pause
;; navigate out/in
