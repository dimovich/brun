(require '[etaoin.api :as et]
         '[etaoin.keys :as ek]
         '[brun.util :as bu])


(def config-file "config.edn")
(def config (merge {:long-wait [1 3]
                    :wait [0.5 1]
                    :max-likes 10
                    :size [740 740]}
                   (clojure.edn/read-string (slurp config-file))))

(def index-hamburger {:css "a.js-hamburger-button.js-hamburger-button-basement.rf-primary-nav__hamburger-button"})
(def index-hamburger-signin {:css "a.form-button.js-rf-button.rf-button.rf-button--secondary.js-adobeid-signin"})
(def signin-user {:css "input#adobeid_username"})
(def signin-pass {:css "input#adobeid_password"})
(def signin-button {:css "button#sign_in"})
(def gallery-content {:css "div#content-wrapper"})



(def ff (et/firefox {:size (:size config)}))

(doto ff
  (et/go (:main-url config))
  
  (et/wait-visible index-hamburger)

  (bu/random-sleep)

  (et/click index-hamburger)
  (et/click index-hamburger-signin)

  (et/wait-visible signin-user)
  (et/fill-human signin-user (:user config))
  
  (et/click signin-pass)
  (bu/random-sleep)
  (et/fill-human signin-pass (:pass config))

  (et/wait-visible signin-button)
  (et/click signin-button)

  ;; LIKE
  (et/go (:like-url config))
  )

(et/wait-visible ff gallery-content)

(et/js-execute ff "document.getElementsByClassName('rf-search-bar__input')[0].blur();")


(et/quit ff)





(defn blur-search-bar
  "Bypass search bar focus."
  [driver]
  (try
    (et/js-execute
     driver
     (str "var el=document.getElementsByClassName('rf-search-bar__input')[0];"
          "if(el){ el.remove();}"))
    (catch Exception e nil)))









(require 'brun.core)
(in-ns 'brun.core)

(def config (merge {:max-likes 10}
                   (edn/read-string (slurp config-file))))

(def driver (startup config))

(def opts {:driver driver
           :config config})

(login opts)


(et/go (:driver opts) (:like-url config))

(def items (remove @seen-links
                   (et/query-all
                    (:driver opts) sr/gallery-item-link)))

(def driver (:driver opts))

(get-to driver (second items))

(et/click-el driver (second items))

(et/wait-visible driver author)

(empty? (et/query-all driver author))

(like-item (first (et/query-all
                   (:driver opts) sr/gallery-item-link)) opts)


(def images {:css "div.project-module-image"})
;; get to
(def author {:css "div.project-module-text"})


;; click
(def like-button {:css "#primary-project-content div.js-appreciate"})

(def thanks {:css "div.thanks"})

(get-to driver (et/query driver like-button))

(et/click-el driver (et/query driver like-button))

(et/query-all driver author)
(et/query-all driver like-button)

(et/visible? driver thanks)
(et/query-all driver thanks)

(et/query-all driver sr/index-hamburger-signin)
(def logged-in {:css "[alt=Me]"})
(et/query-all driver {:css "[alt=Me]"})



(-main)

