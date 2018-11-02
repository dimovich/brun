(require '[etaoin.api :as et])
(require '[etaoin.keys :as k])


(def index-hamburger {:css "a.js-hamburger-button.js-hamburger-button-basement.rf-primary-nav__hamburger-button"})
(def index-hamburger-signin {:css "a.form-button.js-rf-button.rf-button.rf-button--secondary.js-adobeid-signin"})
(def signin-user {:css "input#adobeid_username"})
(def signin-pass {:css "input#adobeid_password"})
(def signin-button {:css "button#sign_in"})

(def driver (et/firefox))


(doto driver
  (et/set-window-size 740 740)

  (et/go "https://www.behance.net/")

  (et/wait-visible index-hamburger)

  (et/click index-hamburger)
  (et/click index-hamburger-signin)

  (et/wait-visible signin-user)
  (et/fill-human signin-user "dimovich")
  (et/fill-human signin-pass "hello")

  (et/wait-visible signin-button)
  (et/click signin-button))

(et/quit driver)
