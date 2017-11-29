(set-env!
 :source-paths #{"src/clj"}
 :resource-paths #{"resource"}
 :dependencies '[[org.clojure/clojure "1.8.0" :scope "provided"]
                 ;;[adzerk/boot-reload  "0.5.2" :scope "test"]
                 [webica "3.0.0-beta2-clj0"]
                 [com.taoensso/timbre "4.10.0"]
                 [clojure-lanterna "0.9.7"]])


#_(require
   '[adzerk.boot-reload    :refer [reload]])


(task-options!  repl {:port   3311
                      :server true}
                target    {:dir #{"target"}})


(deftask dev
  []
  (comp
   (cider)
   (watch)
;;   (reload)
   (repl)
   (target)))


(deftask prod
  []
  (comp
   (aot :namespace #{'brun.core})
   (uber)
   (jar :file "brun.jar" :main 'brun.core)
   (sift :include #{#"brun.jar" #"config.txt"})
   (target :dir #{"target"})))
