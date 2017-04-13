(ns brun.util
  (:require [clojure.string :as string]
            [webica.core :as w] ;; must always be first
            [webica.web-driver-wait :as wait]
            [webica.web-driver :as driver]
            [webica.remote-web-driver :as browser]
            [webica.chrome-driver :as chrome]
            [webica.web-element :as element]
            [webica.keys :as wkeys]))


(defonce state (atom {}))


;; (.perform (.moveToElement a (by-id "login-link"))) (you can chain actions together)
;;
;; (def a (org.openqa.selenium.interactions.Actions. (driver/get-instance)))


(defn runjs
  ([s] (runjs s nil))
  ([s arg] (browser/execute-script s arg)))

(defn startup
  ([]
   (startup nil))
  ([path]
   (chrome/start-chrome path)
   (reset! state {:keyboard (browser/get-keyboard)
                  :mouse (browser/get-mouse)
                  :actions (org.openqa.selenium.interactions.Actions. (driver/get-instance))
                  :width (runjs "return window.innerWidth;")
                  :height (runjs "return window.innerHeight;")})))


(defn cleanup []
  (chrome/quit))


(defn navigate [url]
  (browser/get url))

(defn navigate-back []
  (.back (browser/navigate)))


(defn by-id [id]
  (browser/find-element-by-id id))

(defn by-class [cls]
  (browser/find-element-by-class-name cls))

(defn by-id-all [id]
  (browser/find-elements-by-id id))

(defn by-class-all [cls]
  (browser/find-elements-by-class-name cls))


(defn by-name [s]
  (browser/find-element-by-name s))

(defn by-link-text [s]
  (browser/find-element-by-link-text s))

(defn visible? [el]
  (boolean (element/is-displayed? el)))

(defn wait [t]
  (w/sleep t))

(defn get-attr [el attr]
  (element/get-attribute el attr))

(defn get-text [el]
  (element/get-text el))




(defn press-keys [ks]
  (.sendKeys (:keyboard @state) (into-array CharSequence ks)))


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
       (string/lower-case title))))))


(defn wait-for-id [id]
  (wait/until
   (wait/instance 10)
   (wait/condition
    (fn [driver]
      (try (browser/find-element-by-id id)
           (catch java.lang.reflect.InvocationTargetException e false)
           (finally true))))))


(defn wait-for-class [cls]
  (wait/until
   (wait/instance 10)
   (wait/condition
    (fn [driver]
      (try (browser/find-element-by-class-name cls)
           (catch java.lang.reflect.InvocationTargetException e false)
           (finally true))))))


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
;; move-to
;;
(defn move-mouse-to [el]
  (.perform (.moveToElement (:actions @state) el)))


;;
;; move-and-click
;;
(defn move-mouse-and-click [el]
  (move-mouse-to el)
  (random-sleep)
  (.click el))


(defn move-random [])

(defn get-to [el]
  (let [height (/ (:height @state) 2)
        yf (.y (element/get-location el))]
    (loop [yc (runjs "return window.scrollY;")]
      (when (< (+ yc height) yf)
        (do
          (action (rand-nth [:page-down :arrow-down :arrow-up]))
          (action :random-sleep))))))


;; (runjs "return document.getElementById('appreciation').getBoundingClientRect().top;")
