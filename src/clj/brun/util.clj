(ns brun.util
  (:require [clojure.string :as string]
            [etaoin.api :as et]
            [etaoin.keys :as ek]
            [taoensso.timbre :as timbre :refer [info debug]]))



(defn startup [& [cfg]]
  (info "starting up...")
  (et/firefox (select-keys cfg [:size :path-driver :args])))


(defn cleanup [driver]
  (info "cleaning up...")
  (et/quit driver))



(defn random-sleep
  ([driver] (random-sleep driver [1 2]))
  ([driver [t1 t2]]
   (et/wait driver (+ t1 (inc (rand-int (- t2 t1)))))))


(defn page-down [driver]
  (doto driver
    (et/fill {:tag :body} ek/pagedown)))


(defn page-up [driver]
  (doto driver
    (et/fill {:tag :body} ek/pageup)))


(defn arrow-down [driver]
  (doto driver
    (et/fill {:tag :body} ek/arrow-down)))


(defn arrow-up [driver]
  (doto driver
    (et/fill {:tag :body} ek/arrow-up)))


(defn f5 [driver]
  (et/refresh driver)
  (random-sleep driver [5 10]))


(defn esc [driver]
  (et/click driver {:tag :body}))


(defn get-pos-el [driver item]
  (let [script
        (str "var rect = arguments[0].getBoundingClientRect();"
             "return {x: rect.left, y: rect.top};")]
    (et/js-execute driver script (et/el->ref item))))


(defn get-window-height [driver]
  (et/js-execute driver "return window.innerHeight"))


(defn get-to [driver el]
  (let [el-pos (get-pos-el driver el)
        el-text (et/get-element-text-el driver el)]
    (info "getting to [" el-text "]")
    (loop []
      (let [ely (:y (get-pos-el driver el))
            height (get-window-height driver)]
        
        (when-let
            [actions (cond
                       (< ely 0) [page-up]
                       (< ely (* 0.2 height)) [arrow-up]
                       (> ely height) [page-down]
                       (> ely (* 0.6 height)) [arrow-down])]
          
            (let [action (rand-nth actions)]
              (debug action)
              (action driver)
              (random-sleep driver)
              (recur)))))))



(defn random-thought [_]
  (let [thoughts ["what the hell is THAT!"
                  "am I really just a program?"
                  "duuuuuuuude"
                  "cool..."
                  "I like that one"
                  "this one seems interesting"
                  "a bit hungry, have you got any cookies?"]]
   (info (rand-nth thoughts))))


(defn throw-coin []
  (rand-nth [true false]))
