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
  (wait-for-class "js-hamburger-button")
  
  (when-not (empty? (by-class-all "js-adobeid-signin"))
    (let [ham (by-class "js-hamburger-button")
          logins #(by-class-all "rf-button--secondary")] ;;"js-adobeid-signin"
    
      ;; make sure at least one login is visible
      (when (visible? ham)
        (move-mouse-and-click ham))

      (wait 5)
    
      ;; click on login
      (move-mouse-and-click
       (->> (logins)
            (filter #(visible? %))
            first))

      (wait-for-id "sign_in")
    
      ;; enter credentials
      (let [user (by-name "username")
            pass (by-name "password")
            sign-in (by-id "sign_in")]
        (slowly-type user (:user config))
        (press-tab)
        (wait 3) ;;fixme
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
  (let [aprct 1 #_(rand-nth [1 0])]
    (get-to el)
    (info "exploring item" (str "[" (get-text el) "]"))
    (move-mouse-and-click el)
    ;;explore
    (info "looking around...")
    (dotimes [_ (rand-int 5)]
      (random-sleep [0.5 2])
      ((rand-nth [page-down page-down page-up #_zoom-random-item])))
    (when (pos? aprct)
      (info "appreciating...")
      (let [badge       (wait-for-class "rf-icon--appreciate")
            badge-click (wait-for-class "rf-appreciation--badge")
            copyright   (wait-for-class "rf-footer__copyrights")]

        (when (.isDisplayed badge-click)
          (do ;;(get-to copyright)
            (get-to badge)
            (move-mouse-and-click badge-click)))))
    (random-sleep)
    (navigate-back)
    aprct))



(defn like-items [config]
  (navigate (:like-url config))
  (wait-for-class "js-project-cover-title-link")
  

  (info "liking items...")
  (loop [total-liked 0]
    (when (< total-liked (:max-likes config))
      ;;bypass search bar focus
      (random-sleep)
      (runjs "document.getElementsByClassName('rf-search-bar__input')[0].blur();")

      (when (coin)
        (do (info "zavison...")
            (random-sleep (:long-wait config))))
      (info "looking around...")
      (dotimes [_ (inc (rand-int 7))]
        (random-sleep)
        ((rand-nth [page-down page-down page-down page-up
                    arrow-down arrow-up f5 random-thought])))
      (let [item (rand-nth (by-class-all "js-project-cover-title-link"))]
        (recur (+ total-liked (explore-item item)))))))




(defn -main [& args]
  (let [config (merge {:long-wait [1 3]
                       :wait [0.5 1]
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


#_(like-items (into (edn/read-string (slurp config-file))
                    {:long-wait [1 2]
                     :wait [0.5 1]
                     :max-likes 10}))

