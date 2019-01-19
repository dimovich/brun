(ns brun.core
  (:require [clojure.edn :as edn]
            [etaoin.api :as et]
            [brun.util :refer :all]
            [brun.selectors :as sr]
            [taoensso.timbre :as timbre :refer [info]]))


(def config-file "config.edn")
(def total-liked (atom 0))
(def seen-links (atom #{}))

(timbre/set-config!
 {:level :info
  :output-fn
  (fn [{:keys [timestamp_ level msg_]}]
    (str
     (second (clojure.string/split (force timestamp_) #" ")) " "
     (force msg_)))
  :appenders {:println (timbre/println-appender {:stream :auto})}})



(defn login [{:as opts :keys [driver config]}]
  (info "signing in...")
  (doto driver
    (et/go (:login-url config))
  
    (et/wait-visible sr/index-hamburger)

    (random-sleep [5 10])
    (et/click-visible sr/index-hamburger)

    (random-sleep [5 10]))
  

  ;; check if already logged in
  (when (empty? (et/query-all driver sr/logged-in))
    (doto driver
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
      (et/click sr/signin-button))))




(defn like-item [item {:as opts :keys [driver config]}]
  (let [aprct 1 #_(rand-nth [1 0])]
    (try
      (doto driver
        (get-to item)
        (et/click-el item)
        (random-sleep [2 3])
        (et/wait-exists sr/like-button))

      (when (pos? aprct)
        (when-let [like-button (first (et/query-all driver sr/like-button))]
          ;; check if we didn't like already
          (when (empty? (et/get-element-text-el driver like-button))
            (random-sleep driver)
            (get-to driver like-button)
            (info "liking [" (inc @total-liked) "/" (:max-likes config) "]")
            (et/click-el driver like-button)
            aprct)))

      (catch Exception e (info "something's fishy here..."))
      
      (finally
        (random-sleep driver)
        (et/back driver)))))



(defn explore-items [{:as opts :keys [driver config]}]
  (et/go driver (:like-url config))
  (et/wait-visible driver sr/gallery-content)
  
  (info "exploring items...")
  (loop []
    (when (< @total-liked (:max-likes config))
      (random-sleep driver)
      
      (info "looking around...")
      (dotimes [_ (inc (rand-int 5))]
        (doto driver
          ((rand-nth [page-down page-down page-down
                      page-down page-down page-down
                      f5 random-thought]))
          (random-sleep)))

      (let [items (remove @seen-links
                          (et/query-all
                           driver sr/gallery-item-link))]
        (when-not (empty? items)
          (let [item (rand-nth items)]
            (some->> (like-item item opts)
                     (swap! total-liked +))
            (swap! seen-links conj item)))
        (recur)))))



(defn run [{:as opts :keys [driver]}]
  (let [tear-msg #(info "ERROR! You're Tearing me Apart!!")]
    (try
      (login opts)
      (random-sleep driver)
      (explore-items opts)
      (catch Exception e (tear-msg)))))



(defn -main [& args]
  (if-let [config (merge {:max-likes 10}
                         (edn/read-string (slurp config-file)))]
    (do
      (info "config:")
      (clojure.pprint/pprint config)
    
      (loop []
        (when-let [driver (startup config)]
          (run {:driver driver :config config})
          (cleanup driver)
          (when (< @total-liked (:max-likes config))
            (Thread/sleep 5000)
            (recur)))))

    (info "Error! Could not find config.edn")))
