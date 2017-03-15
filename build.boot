(set-env!
 :source-paths #{"src/clj"}
 :dependencies '[[webica "3.0.0-beta2-clj0"]])


(require 'boot.repl)

(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.15.0-SNAPSHOT"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)


(deftask dev
  []
  (comp
   (watch)
   (repl :server true)
   (target :dir #{"target"})))



