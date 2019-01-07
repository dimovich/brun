(ns brun.core
  (:require [clojure.edn :as edn]
            [etaoin.api :as et]
            [brun.util :refer :all]
            [brun.selectors :as sr]
            [taoensso.timbre :as timbre :refer [info]]))


(def config-file "config.edn")
(def total-liked (atom 0))

(timbre/set-config!
 {:level :info
  :output-fn (fn [{:keys [timestamp_ level msg_]}]
               (str
                (second (clojure.string/split (force timestamp_) #" ")) " "
                ;;(clojure.string/upper-case (name level)) " "
                (force msg_)))
  :appenders {:println (timbre/println-appender {:stream :auto})}})



(defn login [{:as opts :keys [driver config]}]
  (info "signing in...")
  (doto driver
    (et/go (:login-url config))
  
    (et/wait-visible sr/index-hamburger)

    (random-sleep [5 10])
    (et/click-visible sr/index-hamburger)

    (random-sleep [5 10])
    (et/click-visible sr/index-hamburger-signin)

    (et/wait-visible sr/signin-user)
    (et/click sr/signin-user)
    (random-sleep)
    (et/fill-human sr/signin-user (:user config))

    (et/wait-visible sr/signin-pass)
    (et/click sr/signin-pass)
    (random-sleep)
    (et/fill-human sr/signin-pass (:pass config))

    (et/wait-visible sr/signin-button)
    (et/click sr/signin-button)))




(defn like-item [item {:as opts :keys [driver config]}]
  (let [aprct 1 #_(rand-nth [1 0])
        el-text (et/get-element-text-el driver item)]

    (get-to driver item)
    (et/click-el driver item)
    
    (if (et/exists? driver sr/gallery-item-appreciate)
      (do
        (et/wait-visible driver sr/gallery-item-appreciate)
      
        (dotimes [_ (rand-int 3)]
          (random-sleep driver)
          ((rand-nth [page-down page-down page-up]) driver))
    
        (when (pos? aprct)
          (when-let [badge (et/query driver sr/gallery-item-appreciate)]
            (when (empty? (et/get-element-text-el driver badge))
              (random-sleep driver)
              (get-to driver badge)
              (info "liking [" (inc @total-liked) "/" (:max-likes config) "]")
              (et/click-el driver badge))))

        (random-sleep driver)
        (et/back driver)
        aprct)
      (do
        (random-sleep driver)
        (et/back driver)
        0))))



(defn blur-search-bar
  "Bypass search bar focus."
  [driver]
  (try
    (et/js-execute
     driver
     (str "var el=document.getElementsByClassName('rf-search-bar__input')[0];"
          "if(el){ el.remove();}"))
    (catch Exception e nil)))



(defn explore-items [{:as opts :keys [driver config]}]
  (et/go driver (:like-url config))
  (et/wait-visible driver sr/gallery-content)
  
  (info "exploring items...")
  (loop []
    (when (< @total-liked (:max-likes config))

      (random-sleep driver)
      (blur-search-bar driver)
      
      (when (throw-coin)
        (do (info "zavison...")
            (random-sleep driver [5 10])))
      
      (info "looking around...")
      (dotimes [_ (inc (rand-int 7))]
        (blur-search-bar driver)
        (random-sleep driver)
        ((rand-nth [page-down page-down page-down page-up
                    arrow-down arrow-up f5 random-thought])
         driver))

      (blur-search-bar driver)
      
      (let [item (rand-nth (et/query-all driver sr/gallery-item-link))]
        (swap! total-liked + (like-item item opts))
        (recur)))))



(defn run [{:as opts :keys [driver]}]
  (let [tear-msg #(info "ERROR! You're Tearing me Apart!!")]
    (try
      (login opts)
      (random-sleep driver)
      (explore-items opts)
      (catch Exception e (tear-msg)))))



(defn -main [& args]
  (let [config (merge {:max-likes 10}
                      (edn/read-string (slurp config-file)))]
    (info "config:")
    (clojure.pprint/pprint config)
    
    (loop []
      (when-let [driver (startup config)]
        (run {:driver driver :config config})
        (cleanup driver)
        (when (< @total-liked (:max-likes config))
          (Thread/sleep 5000)
          (recur))))))

