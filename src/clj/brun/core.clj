(ns brun.core
  (:require [clojure.string :as string]
            [webica.core :as w] ;; must always be first
            [webica.by :as by]
            [webica.web-driver :as driver]
            [webica.keys :as wkeys]
            [webica.chrome-driver :as chrome]
            [webica.web-element :as element]
            [webica.web-driver-wait :as wait]
            [webica.remote-web-driver :as browser]))


(def config (atom {:main-url "https://www.behance.net/"
                   :recent-url "https://www.behance.net/search?content=projects&sort=published_date&time=all"
                   :recent-title "Most recent projects on Behance"
                   :login-title "Sign in - Adobe ID"
                   :main-title "Online Portfolios on Behance"
                   :max-items 30
                   :user ""
                   :pass ""}))


(defn char-sequence [char-seqable]
  (let [char-seq (seq char-seqable)]
    (reify CharSequence
      (charAt   [this i] (nth char-seq i))
      (length   [this]   (count char-seq))
      (toString [this]   (String. (char-array char-seq))))))

;;
;; wait-for-title
;;
(defn wait-for-title [title]
  (wait/until
   (wait/instance 10)
   (wait/condition
    (fn [driver]
      (string/starts-with?
       (string/lower-case (driver/get-title driver))
       (string/lower-case title)))))
  (w/sleep (rand 5)))


(defn random-sleep
  ([] (random-sleep 5))
  ([n] (w/sleep (rand n))))


;;
;; slowly-type
;;
(defn slowly-type [el text]
  (doseq [ch text]
    (element/send-keys el (str ch))
    (w/sleep (rand 1))))


;;
;; move-and-click
;;
(defn move-and-click [el]
  (.mouseMove (browser/get-mouse) (.getCoordinates el))
  (w/sleep (rand 5))
  (.click el))


;;
;; login
;;
(defn login [config]
  (browser/get (:main-url config))
  (wait-for-title (:main-title config))
  
  (let [ham (browser/find-element-by-id "hamburger-button")
        logins (browser/find-elements-by-class-name "js-adobeid-signin")]
    
    ;; make sure at least one login is visible
    (when (boolean (element/is-displayed? ham))
      (element/click ham))

    (w/sleep 5)
    
    ;; click on login
    (move-and-click
     (->> logins
          (filter #(boolean (element/is-displayed? %)))
          first))


    (wait-for-title (:login-title config))
    
    ;; enter credentials
    (let [user (browser/find-element-by-name "username")
          pass (browser/find-element-by-name "password")
          sign-in (browser/find-element-by-id "sign_in")]
      (slowly-type user (:user config))
      (slowly-type pass (:pass config))
      (random-sleep)
      (move-and-click sign-in))))


;;
;; (.sendKeys keyboard (into-array CharSequence [wkeys/PAGE_DOWN]))
;;
(defn like-random-items [config]
  
  (loop [covers (browser/find-elements-by-class-name "cover-name-link")
         total-liked 0]
    (when (< total-liked (:max-items config))
      (let [total-covers (count covers)
            liked (random-sample covers)]
        ;; like
        (doseq [item liked]
          (.click item)
          ;;wait for it to load
          ;;appreciate
          ;;escape
          
          ))))

  ;;cover-name-link
  ;;(element/get-text el)
  ;;(element/get-attribute el "href")
  ;;(.back (browser/navigate))
  ;;(element/get-text sticker)
  )

;; get covers
;; like random
;; get next
;; drop size
;; go to last, wait


(defn -main
  [& args]
  (chrome/start-chrome "chromedriver.exe")
  (login @config)
  (browser/get (:recent-url @config)))
