(ns reagent-frontend.core
    (:require
      [reagent.core :as r]))

(defonce game-state (r/atom nil))

;; -------------------------
;; Views
(defn board []
  (let [current-state @game-state]
    (doall
     (loop [current-line (take 4 current-state)
           next-line (drop 4 current-state)
           dom [:div {:class "container"}]]
       (if (empty? current-line)
         dom
         (recur (take 4 next-line)
                (drop 4 next-line)
                (conj dom (for [element current-line]
                            [:div {:class "element"} element]))))))))

(defn result-array-shifter [row]
  (defn shift [row]
    (->> row
         (reduce (fn [acc item]
                   (if (= item 0)
                     (conj [0] acc)
                     (conj acc item)))
                 [])
         (flatten)))
  (->>
   row
   (shift)
   (partition-by identity)
   (map (fn [seq]
          (->> seq
               (partition 2 2 nil)
               (reverse)
               (map (fn [sub-sq] (if (= (count sub-sq) 2)
                                   (sequence [0 (reduce + sub-sq)])
                                   sub-sq))))))
   (flatten)
   (shift)))

(defn transpose [m]
  (apply map list m))

(defn rotate [dir s]
  (let [parted (partition 4 s)
        transposed (transpose parted)
        result (if (= :up dir)
                 (map reverse transposed)
                 transposed)
        ] result))

(defn direction->scheme
  [dir]
  (get {:up #(rotate :up %)
        :down #(rotate :down %)
        :left #(partition 4 (reverse %))
        :right #(partition 4 %)}
       dir
       :default))

(defn find-all [coll thing]
  (map first
       (filter #(= (second %) thing)
               (map-indexed vector coll))))

(defn rand-place [coll]
  (assoc coll (rand-nth (find-all coll 0))
         (if (> (rand) 0.9) 4 2)))

(defn randomize-board
  []
  (reset! game-state [0 0 0 0
                      0 2 0 0
                      0 0 0 2
                      0 0 2 2])) ;; TODO Дописать функцию инициализации доски

(defn add-keypress-event-listener
  []
  (let [events {37 :left
                38 :up
                39 :right
                40 :down}]
    (-> js/document
        (.addEventListener "keydown"
                           (fn [e] (let [key-code (get events (-> e .-keyCode))]
                                    (if-not (nil? key-code)
                                      (reset! game-state
                                              (into []
                                                    (flatten (map #(if-not (nil? (get [:down :left] key-code))
                                                                     (reverse (result-array-shifter %))
                                                                     (result-array-shifter %))
                                                                  ((direction->scheme key-code) @game-state))))))))
                           false))))

(defn home-page []
  [:div [:h2 "2048"]
   [board]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root)
  (randomize-board)
  (add-keypress-event-listener))

(comment
  (board)

  (test-board)

  (add-keypress-event-listener)

  (into [] (flatten (map result-array-shifter ((direction->scheme :down) @game-state))))

  (reset! game-state [5 1 2 3
                      1 3 4 5
                      6 3 5 2
                      7 2 3 4])

  (randomize-board)

  (.addEventListener js/document "keydown" (fn [e] (prn (aget e "keyCode"))) false)

  (-> js/document
      (.addEventListener "keydown"
                         (fn [e] (js/console.log (-> e .-keyCode)))
                         false))

  )

