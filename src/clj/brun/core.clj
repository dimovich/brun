(ns brun.core
  (:require [clojure.string :as string]
            [clojure.edn :as edn]
            [webica.core :as w] ;; must always be first
            [webica.by :as by]
            [webica.web-driver :as driver]
            [webica.keys :as wkeys]
            [webica.chrome-driver :as chrome]
            [webica.web-element :as element]
            [webica.web-driver-wait :as wait]
            [webica.remote-web-driver :as browser])
  (:gen-class))


(def config-file "config.txt")
(def config (atom {}))


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
  ([] (random-sleep 1))
  ([n] (w/sleep (rand n))))


;;
;; slowly-type
;;
(defn slowly-type [el text]
  (doseq [ch text]
    (random-sleep)
    (element/send-keys el (str ch))))


;;
;; move-and-click
;;
(defn move-and-click [el]
  (.mouseMove (browser/get-mouse) (.getCoordinates el))
  (random-sleep)
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
;;
(defn like-items [config]
  (browser/get (:recent-url config))
  
  (let [keyboard (browser/get-keyboard)]
    (loop [covers (browser/find-elements-by-class-name "cover-name-link")
           total-liked 0
           total-seen 0]
      (when (< total-liked (:max-items config))
        (let [covers-sample (random-sample 0.1 covers)
              cover-links (doall (map #(element/get-attribute % "href") covers-sample))
              last-cover-coords (.getCoordinates (last covers))]
          ;; like
          (doseq [cover cover-links]
            ;; TODO: add to seen-db
            (w/sleep 3)
            (browser/get cover)
            (w/sleep 3)

            ;; page-down until we reach the like button
            (let [aprct (- (browser/execute-script
                            "return document.getElementById('appreciation').getBoundingClientRect().top;" nil)
                           100)
                  keyboard (browser/get-keyboard)
                  mouse (browser/get-mouse)]
              (while (< (browser/execute-script "return window.scrollY;" nil)
                        aprct)
                (random-sleep)
                (.sendKeys keyboard (into-array CharSequence [wkeys/PAGE_DOWN])))
              
              ;; click the appreciation
              (move-and-click (browser/find-element-by-id "appreciation"))
              ;
              (w/sleep 5)
              (.back (browser/navigate))))
          
          ;; get new covers
          (w/sleep 3)
          ;;(.mouseMove (browser/get-mouse) (.getCoordinates (browser/find-element-by-link-text elt)))
          (.mouseMove (browser/get-mouse) last-cover-coords)
          (w/sleep 5)

          ;; get next covers and recur
          (let [total-liked (+ total-liked (count covers-sample))
                total-seen (+ total-seen (count covers))]
            (recur (drop total-seen (browser/find-elements-by-class-name "cover-name-link"))
                   total-liked
                   total-seen)))))))


  ;;(.mouseMove (browser/get-mouse) (.getCoordinates el))
  ;; (browser/execute-script "document.getElementById('appreciation').scrollIntoView(true);" nil)
  ;; document.getElementById('appreciation').scrollIntoView(true);
  ;; (browser/execute-script "return window.scrollY;" nil)
;;  (browser/execute-script "return document.getElementById('appreciation').getBoundingClientRect().top;" nil)
;; (.sendKeys keyboard (into-array CharSequence [wkeys/PAGE_DOWN]))

  ;;cover-name-link
  ;;(element/get-text el)
  ;;(element/get-attribute el "href")
  ;;(.back (browser/navigate))
  ;;(element/get-text sticker)


;; get covers
;; like random
;; get next
;; drop size
;; go to last, wait



(defn -main
  [& args]
  ;; read config
  (reset! config (edn/read-string (slurp config-file)))
  
  ;; start chrome
  (chrome/start-chrome "chromedriver.exe")
  #_(if-let [chromepath (:chromepath @config)]
    (chrome/start-chrome chromepath)
    (chrome/start-chrome))

  ;; perform login
  (login @config)
  
  (like-items @config))
