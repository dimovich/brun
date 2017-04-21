(ns brun.core
  (:require [clojure.edn :as edn]
            [brun.util :refer :all]
            [taoensso.timbre :as timbre :refer [info]])
  (:gen-class))


(def config-file "config.txt")

(timbre/set-config!
 {:level :info
  :output-fn (fn [{:keys [timestamp_ level msg_]}]
               (str
                (second (clojure.string/split (force timestamp_) #" ")) " "
                ;;(clojure.string/upper-case (name level)) " "
                (force msg_)))
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


(defn zoom-random-item []
  (let [elxs (by-class-all "lightbox-link")]
    (when-not (empty? elxs)
      (let [el (rand-nth elxs)]
        (info "zooming random item...")
        (get-to el)
        (move-mouse-and-click el)
        ;;        (wait-for-class "zoomable")
        (random-sleep [5 20])
        (esc)))))


(defn explore-item [el]
  (let [aprct (rand-nth [0 1])]
    (get-to el)
    (info "exploring item" (str "[" (get-text el) "]"))
    (move-mouse-and-click el)
    ;;explore
    (info "looking around...")
    (dotimes [_ (rand-int 5)]
      (random-sleep [1 2])
      ((rand-nth [page-down page-down page-up zoom-random-item])))
    (when (pos? aprct)
      (info "appreciating...")
      (let [el (wait-for-id "appreciation")]
        (get-to el)
        (move-mouse-and-click el)))
    (random-sleep)
    (navigate-back)
    aprct))



(defn like-items [config]
  (navigate (:like-url config))
  (wait-for-class "cover-name-link")

  (info "liking items...")
  (loop [total-liked 0]
    (when (< total-liked (:max-likes config))
      (when (coin)
        (do (info "zavison...")
            (random-sleep (:long-wait config))))
      (info "looking around...")
      (dotimes [_ (inc (rand-int 7))]
        (random-sleep)
        ((rand-nth [page-down page-down page-down page-up
                    arrow-down arrow-up f5 random-thought])))
      (let [item (rand-nth (by-class-all "cover-name-link"))]
        (recur (+ total-liked (explore-item item)))))))




(defn -main [& args]
  (let [config (into {:long-wait [5 10]
                      :wait [1 3]
                      :max-likes 10}
                     (edn/read-string (slurp config-file)))]
    (info "starting up with config: \n" config)
    (startup config)
    (login config)
    (random-sleep)
    (like-items config)
    (cleanup)))



;; pause
;; navigate out/in
;;    context? (page url)
;;    exception handling
;;    
