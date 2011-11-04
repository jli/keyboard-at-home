;;; details of keyboard representation.

(ns keyboard-sex.kbd)

;; keyvecs are 1d vectors of characters.
;;
;; positions are row, column pairs. there are 3 rows. 11 columns on
;; the first 2, 10 on the last.
;;
;; layouts are maps of keys to positions.
;;
;; keyboards defined in core.



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

(defn keyvec->layout [ks] (zipmap ks positions))

(defn layout->keyvec [layout] (map first (sort-by second layout)))

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
