(ns brun.core
  (:require [clojure.edn :as edn]
            [webica.core :as w] ;; must always be first
            [webica.by :as by]
            [webica.web-driver :as driver]
            [webica.keys :as wkeys]
            [webica.chrome-driver :as chrome]
            [webica.web-element :as element]
            [webica.remote-web-driver :as browser]
            [brun.actions :refer action])
  (:gen-class))


(def config-file "config.txt")
(def config (atom {}))


;;
;; login
;;
(defn login [config]
  (navigate (:main-url config))
  ;;(wait-for-title (:main-title config))

  (wait-for-class "js-adobeid-signin")
  
  (let [ham (by-id "hamburger-button")
        logins (by-class "js-adobeid-signin")]
    
    ;; make sure at least one login is visible
    (when (visible? ham)
      (move-and-click ham))

    (wait 5)
    
    ;; click on login
    (move-and-click
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
  (wait 5)
  
  (let [keyboard (browser/get-keyboard)
        mouse (browser/get-mouse)]
    (loop [covers (by-class "cover-name-link")
           total-liked 0
           total-seen 0]
      (when (< total-liked (:max-items config))
        (let [sample (random-sample 0.1 covers)
              sample-hrefs (doall (map #(get-attr % "href") sample))
              last-item-text (get-text (last covers))]
          ;;
          ;; like
          ;;
          (doseq [href sample-hrefs]
            ;; TODO: add to seen-db
            (wait 7)
            (navigate href)
            (wait 5)

            ;; page-down until we reach the like button
            (let [aprct (- (runjs "return document.getElementById('appreciation').getBoundingClientRect().top;")
                           200)]
              (while (< (runjs "return window.scrollY;")
                        aprct)
                (random-sleep)
                (press-keys [wkeys/PAGE_DOWN]))
              
              ;; click the appreciation
              (move-and-click (browser/find-element-by-id "appreciation"))
              
              (w/sleep 5)
              (.back (browser/navigate))))

          ;; get new covers
          (w/sleep 3)
          (.mouseMove
           (browser/get-mouse)
           (.getCoordinates
            (browser/find-element-by-link-text
             last-item-text)))
          
          
          (w/sleep 5)

          ;; get next covers and recur
          (let [total-liked (+ total-liked (count sample))
                total-seen (+ total-seen (count covers))]
            (recur (drop total-seen (browser/find-elements-by-class-name "cover-name-link"))
                   total-liked
                   total-seen)))))))




(defn -main
  [& args]
  ;; read config
  (reset! config (edn/read-string (slurp config-file ;;:encoding "Unicode"
                                         )))
  
  ;; start chrome
  (if-let [chromepath (:chromepath @config)]
    (startup chromepath)
    (startup))

  ;; perform login
  (login @config)
  
  (like-items @config)

  (cleanup))




;; TODO
;;
;; scrollitemintoview before clicking (use link-text), so the browser returns back correctly
;;
;; use PAGEDOWN maybe?
