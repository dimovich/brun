(ns brun.core
  (:require [clojure.edn :as edn]
            [brun.util :refer :all])
  (:gen-class))


(def config-file "config.txt")


;;
;; login
;;
(defn login [config]
  (navigate (:main-url config))
  (wait-for-class "js-adobeid-signin")
  
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
      (move-and-click sign-in))))


;;
;; press 4 times page-down (and remember the total number)
;;
(defn like-items [config]
  (navigate (:recent-url config))
  ;;(wait-for-title (:recent-title config))
  (wait-for-class "cover-name-link")

  (random-sleep)
  
  (loop [covers (by-class-all "cover-name-link")
         total-liked 0
         total-seen 0]
    (when (< total-liked (:max-items config))
      (let [sample (random-sample 0.1 covers)
            sample-hrefs (doall (map #(get-attr % "href") sample))
            last-item-text (get-text (last covers))]
        ;;
        ;; like items
        ;;
        (doseq [href sample-hrefs]
          (navigate href)
          (wait-for-id "appreciation")
          (let [el (by-id "appreciation")]
            (get-to el)
            (move-mouse-and-click el))
          (random-sleep)
          (navigate-back))

        ;; get new covers
        (wait-for-class "cover-name-link")
        (get-to (by-link-text last-item-text))
        
        
        (random-sleep [3 5])

        ;; get next covers and recur
        (let [total-liked (+ total-liked (count sample))
              total-seen (+ total-seen (count covers))]
          (recur (drop total-seen (by-class-all "cover-name-link"))
                 total-liked
                 total-seen))))))




(defn -main [& args]
  (let [config (edn/read-string (slurp config-file))]
    (startup chromepath)
    (login config)
    (like-items config)
    (cleanup)))


;;
;; LOGGING
;;
