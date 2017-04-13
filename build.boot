(set-env!
 :source-paths #{"src/clj"}
 :resource-paths #{"resource"}
 :dependencies '[[org.clojure/clojure "1.8.0" :scope "provided"]
                 [webica "3.0.0-beta2-clj0"]
                 [com.taoensso/timbre "4.8.0"]])

;;(require 'boot.repl)

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


(deftask build
  "Builds an uberjar of this project that can be run with java -jar"
  []
  (comp
   (aot :namespace #{'brun.core})
   (uber)
   (jar :file "brun.jar" :main 'brun.core)
   (sift :include #{#"brun.jar" #"config.txt"})
   (target :dir #{"target"})))

