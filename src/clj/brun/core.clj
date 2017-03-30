(ns brun.core
  (:require [clojure.string :as string]
            [webica.core :as w] ;; must always be first
            [webica.by :as by]
            [webica.web-driver :as driver]
            [webica.chrome-driver :as chrome]
            [webica.web-element :as element]
            [webica.web-driver-wait :as wait]
            [webica.remote-web-driver :as browser]))


(def config (atom {:main-url "https://www.behance.net/"
                   :login-title "Sign in - Adobe ID"
                   :main-title "Online Portfolios on Behance"
                   :user ""
                   :pass ""}))


(defn wait-for-title [title]
  (wait/until
   (wait/instance 10)
   (wait/condition
    (fn [driver]
      (string/starts-with?
       (string/lower-case (driver/get-title driver))
       (string/lower-case title)))))
  (w/sleep 5))


(defn slowly-type [el text]
  (doseq [ch text]
    (element/send-keys el (str ch))
    (w/sleep (inc (rand-int 2)))))

(defn login [config]
  (browser/get (:main-url config))
  (wait-for-title (:main-title config))
  
  (let [ham (browser/find-element-by-id "hamburger-button")
        logins (browser/find-elements-by-class-name "js-adobeid-signin")]
    
    ;; make sure at least one login is visible
    (when (element/is-displayed? ham)
      (element/click ham))

    (w/sleep 5)
    
    ;; click on login
    (element/click
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
      (element/click sign-in))))


(defn -main
  [& args]
  (chrome/start-chrome)
  (login @config))


