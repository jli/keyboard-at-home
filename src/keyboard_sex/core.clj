(ns keyboard-sex.core
  (:require [keyboard-sex.data :as data]
            [clojure.set :as set]))

;; keyboards have a keyvec, a layout, and a character pair fitness
;; function. keyvecs and layouts are defined in data. the fitness
;; function is defined here.



;;; util

(let [random (java.util.Random.)]
  (defn rand-bool [] (.nextBoolean random)))

(defn index [xs] (map-indexed vector xs))

;; thought this (or similar) existed already
(defn assoc-with [map key val f]
  (assoc map key (f (get map key) val)))

(defn pairwise
  ([xs] (pairwise xs []))
  ([xs acc]
     (if-let [x (first xs)]
       (let [xpairs (map (partial vector x) (rest xs))]
         (recur (rest xs) (concat xpairs acc)))
       acc)))

(defn downcase [str]
  (map data/symbol-downcase (.toLowerCase str)))

(defn char->position+finger [layout char]
  (let [pos (layout char)
        fing (data/position->finger pos)]
    (concat pos fing)))

(def char-pairs (for [c1 data/charset c2 data/charset] [c1 c2]))



;;; fitness - still needs tweaking

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

(defn layout->fitness [layout]
  (reduce (fn [fitmap cs] (assoc fitmap cs (chars-fitness layout cs)))
          {} char-pairs))



;;; keyboards

(defn keyvec->keyboard [kv]
  (let [layout (data/keyvec->layout kv)]
    {:keyvec kv
     :layout layout
     :chars-fitness (layout->fitness layout)}))

(defn print-keyboard [kbd]
  (println (data/keyvec->str (:keyvec kbd))))

(def qwerty (keyvec->keyboard data/qwerty-keyvec))
(def dvorak (keyvec->keyboard data/dvorak-keyvec))
(def colemak (keyvec->keyboard data/colemak-keyvec))

(defn fitness [kbd text]
  ;; unknown char pairs get 0 cost
  (let [fitmap #(get (:chars-fitness kbd) % 0)]
    (reduce (fn [acc cs] (+ acc (fitmap cs)))
            0 (partition 2 1 (downcase text)))))

(defn test-fitness [kbd text]
  (let [fitmap #(get (:chars-fitness kbd) % 0)
        pairscores (map #(vector % (fitmap %)) (partition 2 1 (downcase text)))]
    (doseq [[pair score] pairscores]
      (println pair score))
    (println (apply + (map second pairscores)))))



;;; it's evolution baby

;; TODO cleanup
(defn sex [kbd1 kbd2]
  (let [[kv1 kv2] (map (comp data/layout->keyvec :layout) [kbd1 kbd2])
        candidate-kv (map (fn [c1 c2] (if (rand-bool) c1 c2))
                          kv1 kv2)
        missing (set/difference data/charset candidate-kv)
        key-positions (reduce (fn [keypos [i k]]
                                (assoc-with keypos k i conj))
                              {}
                              (index candidate-kv))
        dupes (filter (fn [[_k positions]] (> (count positions) 1))
                      key-positions)
        replacements (into {} (map (fn [[_k positions] missing]
                                     [(rand-nth positions) missing])
                                   dupes missing))
        child (map-indexed (fn [i c]
                             (if-let [c2 (replacements i)]
                               c2
                               c))
               candidate-kv)]
    (keyvec->keyboard child)))

(defn rotate [v]
  (if (rand-bool)
    (concat (rest v) [(first v)])
    (concat [(last v)] (butlast v))))

(defn rand-swap [v]
  (let [[i1 i2] [(rand-int (count v))
                 (rand-int (count v))]]
    (map-indexed (fn [i k]
                   (cond (= i i1) (nth v i2)
                         (= i i2) (nth v i1)
                         :default k))
                 v)))

(defn tweak-keyvec [kv]
  (let [mutators [rotate rand-swap]]
    ((rand-nth mutators) kv)))

(defn mutate [keyboard radiation]
  (let [times (int (* radiation 100))
        mutated-kv (nth (iterate tweak-keyvec (:keyvec keyboard)) times)]
    (keyvec->keyboard mutated-kv)))

(defn random-keyboard []
  (keyvec->keyboard (shuffle data/charset)))

(defn index-by [f xs] (map (fn [x] [(f x) x]) xs))

(defn evolve
  "Returns a new population of evolved keyboards. radiation [0,1)
  controls how \"violent\" mutations are. immigrants [0,1] controls
  how many randoms are added to the population."
  [population radiation immigrants text verbose]
  ;; children: each mates with all others and get mutated
  ;; random immigrants: some percent of original population
  ;; candidates: children + original population + immigrants
  ;; n*(n-1)/2 + n + immigrants*n
  ;; next: top n candidates, sorted by fitness
  (let [children (map (partial apply sex) (pairwise population))
        immigrant-limit (int (* immigrants (count population)))
        immigrants (repeatedly immigrant-limit random-keyboard)
        candidates (map #(mutate % radiation)
                        (concat immigrants population children))
        with-scores (sort-by first
                             (index-by #(fitness % text) candidates))
        new-pop (map second (take (count population) with-scores))]
    (when verbose
      (println "children:")
      (doseq [i children] (print-keyboard i) (newline) (flush))
      (println "immigrants:")
      (doseq [i immigrants] (print-keyboard i) (newline) (flush))
      (println "scores:")
      (doseq [[s k] with-scores]
        (print-keyboard k) (println s)))
    new-pop))

(defn genetic [n]
  (loop [pop (repeatedly n random-keyboard)]
    (Thread/sleep 1000)
    (recur (evolve pop 0 0.5 data/brown-humor2 true))))
