;;; details of keyboard representation.

(ns keyboard-at-home.kbd)

;; keyvec: 1d vector of characters
;;
;; position: row, column pair. there are 3 rows. 11 columns on first
;; 2, 10 on last.
;;
;; layout: (key -> position) map



;;; low-level representation

;; simplifying lies: numbers and `[]=\ ignored

;; top row: 11 keys (rightmost two ignored)
;; middle: 11
;; bottom: 10
(def positions
     (for [[row ncols] [[0 11] [1 11] [2 10]]
           col (range ncols)]
       [row col]))

(defn position->finger [[row col]]
  (let [hand (if (<= col 4) :left :right)
        digit (case col
                    (0 9 10) :pinky
                    (1 8) :ring
                    (2 7) :salute
                    (3 6) :index
                    (4 5) :index-stretch)]
    [hand digit]))

(defn char->position+finger [layout char]
  (let [pos (layout char)
        fing (position->finger pos)]
    (concat pos fing)))

(defn keyvec->layout [ks] (zipmap ks positions))

(defn keyvec->str [keys]
  (let [[row0 next] (split-at 11 keys)
        [row1 row2] (split-at 11 next)
        tostr #(apply str (interpose \space %))]
    (format "%s\n%s\n%s"
            (tostr row0) (tostr row1) (tostr row2))))

;; lie: - moved down into /'s position
(def qwerty-keyvec
     [\q \w \e \r \t \y \u \i \o \p \-
      \a \s \d \f \g \h \j \k \l \; \'
      \z \x \c \v \b \n \m \, \. \/])

(def dvorak-keyvec
     [\' \, \. \p \y \f \g \c \r \l \/
      \a \o \e \u \i \d \h \t \n \s \-
      \; \q \j \k \x \b \m \w \v \z])

;; lie: - moved down into /'s position
(def colemak-keyvec
     [\q \w \f \p \g \j \l \u \y \; \-
      \a \r \s \t \d \h \n \e \i \o \'
      \z \x \c \v \b \k \m \, \. \/])

;; same as dvorak and colemak
(def charset (set qwerty-keyvec))

(def char-pairs (for [c1 charset c2 charset] [c1 c2]))

;; "lowercasing" of symbols in our charset
(def symbol-downcase-map
     {\" \'
      \< \,
      \> \.
      \? \/
      \_ \-
      \: \;})

(defn symbol-downcase [c]
  (get symbol-downcase-map c c))

(defn downcase [str]
  (map symbol-downcase (.toLowerCase str)))



;;; fitness layer - still needs tweaking

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

(defn keyvec->chars-fitness [kv]
  (let [layout (keyvec->layout kv)]
   (reduce (fn [fitmap cs] (assoc fitmap cs (chars-fitness layout cs)))
           {} char-pairs)))



;;; high-level

(defn print-keyvec [kv] (println (keyvec->str kv)))

(defn keyvec+score->str [kv score]
  (let [[a b c] (.split (keyvec->str kv) "\n")
        a (str a "     " score)]
    (apply str (interpose "\n" [a b c]))))

(defn fitness [kv text]
  ;; unknown char pairs get 0 cost
  (let [chars-fitness (keyvec->chars-fitness kv)
        cost #(get chars-fitness % 0)]
    (reduce (fn [acc cs] (+ acc (cost cs)))
            0
            (partition 2 1 (downcase text)))))

(defn test-fitness [kv text]
  (let [chars-fitness (keyvec->chars-fitness kv)
        cost #(get chars-fitness % 0)
        pairscores (map #(vector % (cost %)) (partition 2 1 (downcase text)))]
    (doseq [[pair score] pairscores]
      (println pair score))
    (println (apply + (map second pairscores)))))
