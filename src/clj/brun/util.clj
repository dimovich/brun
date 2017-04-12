(ns brun.util
  (:require [clojure.string :as string]
            [webica.core :as w] ;; must always be first
            [webica.web-driver-wait :as wait]
            [webica.remote-web-driver :as browser]
            [webica.chrome-driver :as chrome]
            [webica.web-element :as element]))


(defn startup
  ([] (chrome/start-chrome))
  ([path] (chrome/start-chrome path)))


(defn cleanup []
  (chrome/quit))


(defn navigate [url]
  (browser/get url))


(defn by-id [id]
  (browser/find-element-by-id id))

(defn by-class [cls]
  (browser/find-elements-by-class-name cls))

(defn by-name [s]
  (browser/find-element-by-name s))

(defn visible? [el]
  (boolean (element/is-displayed? el)))

(defn wait [t]
  (w/sleep t))

(defn get-attr [el attr]
  (element/get-attribute el attr))

(defn get-text [el]
  (element/get-text el))

(defn runjs
  ([s] (runjs s nil))
  ([s arg] (browser/execute-script s arg)))


(def keyboard (let [keyboard (atom nil)]
                (fn []
                  (if (@keyboard)
                    keyboard
                    (try
                      (let [k (browser/get-keyboard)]
                        (reset! keyboard k)
                        (catch java.lang.RuntimeException e "couldn't get keyboard")))))))

(defn press-keys [ks]
  (.sendKeys (keyboard) (into-array CharSequence )))

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
;; move-and-click
;;
(defn move-and-click [el]
  (.mouseMove (browser/get-mouse) (.getCoordinates el))
;;  (browser/execute-script "document.getElementById('appreciation').scrollIntoView(true);" nil)
  (random-sleep)
  (.click el))
