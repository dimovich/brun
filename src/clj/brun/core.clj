(ns brun.core
  (:require [clojure.edn :as edn]
            [etaoin.api :as et]
            [brun.util :refer :all]
            [brun.selectors :as sr]
            [taoensso.timbre :as timbre :refer [info]])
  (:gen-class))


(def config-file "config.edn")

(timbre/set-config!
 {:level :info
  :output-fn (fn [{:keys [timestamp_ level msg_]}]
               (str
                (second (clojure.string/split (force timestamp_) #" ")) " "
                ;;(clojure.string/upper-case (name level)) " "
                (force msg_)))
  :appenders {:println (timbre/println-appender {:stream :auto})}})



(defn login [driver config]
  (info "signing in...")
  (doto driver
    (et/go (:main-url config))
  
    (et/wait-visible sr/index-hamburger)

    (random-sleep [5 10])

    (et/click sr/index-hamburger)
    (et/click sr/index-hamburger-signin)

    (et/wait-visible sr/signin-user)
    (et/fill-human sr/signin-user (:user config))
  
    (et/click sr/signin-pass)
    (random-sleep)
    (et/fill-human sr/signin-pass (:pass config))

    (et/wait-visible sr/signin-button)
    (et/click sr/signin-button)))




(defn like-item [driver item]
  (let [aprct 1 #_(rand-nth [1 0])
        el-text (et/get-element-text-el driver item)]

    (get-to driver item)

    (info "exploring item" (str "[" el-text "]"))
    (et/click-el driver item)

    (et/wait-visible driver sr/gallery-item-appreciate)
    
    (info "looking around...")
    (dotimes [_ (rand-int 3)]
      (random-sleep driver [0.5 2])
      ((rand-nth [page-down page-down page-up]) driver))
    
    (when (pos? aprct)
      (info "appreciating...")
      (let [badge (et/query driver sr/gallery-item-appreciate)]
        (when (empty? (et/get-element-text-el driver badge))
          (get-to driver badge)
          (et/click-el driver badge))))
    
    (random-sleep driver)
    (et/back driver)
    aprct))



(defn explore-items [driver config]
  (et/go driver (:like-url config))
  (et/wait-visible driver sr/gallery-content)
  
  (info "liking items...")
  (loop [total-liked 0]
    (when (< total-liked (:max-likes config))
      ;;bypass search bar focus
      (random-sleep driver)
      

      (when (throw-coin)
        (do (info "zavison...")
            (random-sleep driver (:long-wait config))))
      (info "looking around...")
      (dotimes [_ (inc (rand-int 7))]
        (et/js-execute driver "document.getElementsByClassName('rf-search-bar__input')[0].blur();")
        (random-sleep driver)
        ((rand-nth [page-down page-down page-down page-up
                    arrow-down arrow-up f5 random-thought])
         driver))
      (let [item (rand-nth (et/query-all driver sr/gallery-item-link))]
        (recur (+ total-liked (like-item driver item)))))))




(defn -main [& args]
  (let [config (merge {:long-wait [1 3]
                       :wait [0.5 1]
                       :max-likes 10}
                      (edn/read-string (slurp config-file)))]
    (info "config: \n" config)
    (let [driver (startup config)]
      (doto driver
        (login config)
        (random-sleep)
        (explore-items config)
        (cleanup)))))



;; pause
;; navigate out/in
;;    context? (page url)
;;    exception handling
;;    


#_(like-items (into (edn/read-string (slurp config-file))
                    {:long-wait [1 2]
                     :wait [0.5 1]
                     :max-likes 10}))

