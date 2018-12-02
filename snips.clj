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
