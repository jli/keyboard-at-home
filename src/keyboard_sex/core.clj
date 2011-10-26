(ns keyboard-sex.core
  (:require [keyboard-sex.data :as data]))

;; keyboards are: :name, :layout, :chars-fitness

;; layouts are maps of char -> position (row, column)
;; (derived: char->finger (left/right, digit number (1-4 is index-pinky)))
;; shifted keys don't vary

;; what are sensible costs?
;; terrible on dvorak:
;; equipment

;; adjust:
;; twistiness worse. (eq qu)
;; finger strength less bad (smaller, closer)
;; same finger worse (ui)


;;; util

(defn downcase [str]
  (map data/symbol-downcase (.toLowerCase str)))

(defn char->position+finger [layout char]
  (let [pos (layout char)
        fing (data/position->finger pos)]
    (concat pos fing)))

(def char-pairs (for [c1 data/charset c2 data/charset] [c1 c2]))

;; punish using middle keys - index stretching painful
;; reward home row
(defn finger-strength-cost [[row _col hand digit]]
  (let [hand-cost (if (= hand :left) 1 0)
        digit-cost (case digit
                         (:index :salute) 1
                         :ring 2
                         :pinky 3
                         :index-stretch 4)
        position-cost (case row
                            0 2
                            1 0
                            2 3)]
    (+ hand-cost digit-cost position-cost)))


;; 0 for same row or hand alternation. high for adjacent fingers.
(defn row-cross-cost [[r1 c1 h1 d1] [r2 c2 h2 d2]]
  (if (or (= r1 r2) (not= h1 h2))
    0
    ;; range of abs(c1-c2), given h1=h2: 01234
    (let [col-cost (- 5 (Math/abs (- c1 c2)))
          row-diff (Math/abs (- r1 r2))]
      (* row-diff col-cost))))

;; repeated finger use punished (except for same char).
(defn repeat-finger-cost [[r1 c1 h1 d1] [r2 c2 h2 d2]]
  ;; special case for index, since it can be classified as
  ;; index-stretch
  (let [same-finger? (and (= h1 h2)
                          (or (= d1 d2)
                              (= #{d1 d2} #{:index :index-stretch})))
        same-key? (and (= r1 r2) (= c1 c2))]
    (if (or (not same-finger?) same-key?)
      0
      (let [row-diff (Math/abs (- r1 r2))
            ;; column jumping is truly terrible (ui, ip)
            col-diff (* 3 (Math/abs (- c1 c2)))]
        (+ row-diff col-diff)))))

;; if not alternating, prefer moving from weak to strong fingers.
(defn roll-cost [[r1 c1 h1 d1] [r2 c2 h2 d2]]
  (cond
   (not= h1 h2) 0
   (and (= h1 :left)  (> c2 c1)) 0
   (and (= h1 :right) (> c1 c2)) 0
   :default 1))

;; sanity test scoring
(comment

  ;; each char
  (doseq [[c cost]
         (->>
          (map #(char->position+finger data/dvorak-layout %) data/charset)
          (map finger-strength-cost)
          (map vector data/charset)
          (sort-by second))]
    (println c cost))

  ;; row and roll
  (doseq [[pair costs]
          (->> (map (fn [pair]
                      (map #(char->position+finger data/dvorak-layout %)
                           pair))
                    char-pairs)
               (map (fn [p] [(apply row-cross-cost p)
                             (apply repeat-finger-cost p)
                             (apply roll-cost p)]))
               (map vector char-pairs)
               (sort-by second))]
    (println pair costs))
)


;; not symmetric! reward weak->strong sequences
(defn chars-fitness [layout chars]
  (let [positions+fingers (map #(char->position+finger layout %) chars)
        strength (apply + (map finger-strength-cost positions+fingers))
        ;; directly applying row-cross-cost and roll-cost to
        ;; positions+fingers means they must be pairs! could be fancy
        ;; and map them after (partition 2 1 ...), but this is slow
        ;; enough.
        row (apply row-cross-cost positions+fingers)
        rep (apply repeat-finger-cost positions+fingers)
        roll (apply roll-cost positions+fingers)]
    ;; (println (format "chars,str,row,roll: %s %d %d %d" chars strength row roll))
    ;; [strength row roll]
    (+ strength row rep roll)))

(defn gen-chars-fitness [layout]
  (reduce (fn [fitmap cs] (assoc fitmap cs (chars-fitness layout cs)))
          {} char-pairs))

(def qwerty
     {:name "qwerty"
      :layout data/qwerty-layout
      :chars-fitness (gen-chars-fitness data/qwerty-layout)})

(def dvorak
     {:name "dvorak"
      :layout data/dvorak-layout
      :chars-fitness (gen-chars-fitness data/dvorak-layout)})

(def colemak
     {:name "colemak"
      :layout data/colemak-layout
      :chars-fitness (gen-chars-fitness data/colemak-layout)})

(defn fitness [kbd text]
  (let [fitmap #(get (:chars-fitness kbd) % 0)]
    (reduce (fn [acc cs] (+ acc (fitmap cs)))
            0 (partition 2 1 (downcase text)))))

(defn test-fitness [kbd text]
  (let [fitmap #(get (:chars-fitness kbd) % 0)]
    (doseq [pair (partition 2 1 (downcase text))]
      (println pair (fitmap pair)))))
