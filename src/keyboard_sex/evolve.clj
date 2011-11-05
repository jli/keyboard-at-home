;;; core of genetic algorithm. includes fitness, mutation, and sex.

(ns keyboard-sex.evolve
  (:require [keyboard-sex.kbd :as kbd]
            [keyboard-sex.data :as data]
            [clojure.set :as set]))

;; keyboards have a keyvec, a layout, and a character pair fitness
;; function. keyvecs and layouts are defined in data. the fitness
;; function is defined here.

;; the central evolution server doesn't calculate fitness (except for
;; the initial random population), but does constantly send keyboards
;; across the wire as well as mutate/mate keyvecs. so go for
;; compactness and store everything as keyvecs.

;; there are 2 main bits of state:
;;
;; * evolution state
;; ** generation number
;; ** current keyboard population
;; ** top n keyboards seen so far
;; ** (estimated) number of workers
;; ** history
;; *** list of each past generation's top n keyboards
;;
;; * current population fitness testing
;; ** finished
;; ** in-progress (assigned to a worker). have timestamps and worker ids.
;; ** new (unassigned)
;;
;; there are also worker stats, containing per-worker summary stats of
;; compute time



;;; util

;; thx chouser
(defn pipe [& args] (reduce #(%2 %1) args))

(def | pipe)

(defn update [map k f & args]
  (apply update-in map [k] f args))

;; like partition, except will return pieces smaller than n and
;; doesn't leave anything out
(defn batch [n coll] (partition n n nil coll))

(defn filter-split [pred xs]
  (reduce (fn [[ts fs] x] (if (pred x)
                            [(conj ts x) fs]
                            [ts (conj fs x)]))
          [[] []] xs))

(defn now [] (.getTime (java.util.Date.)))

(let [random (java.util.Random.)]
  (defn rand-bool [] (.nextBoolean random)))

(defn index [xs] (map-indexed vector xs))

(defn pindex-by [f xs] (pmap (fn [x] [(f x) x]) xs))

(defn average [xs] (float (/ (apply + xs) (count xs))))

;; thought this (or similar) existed already
(defn assoc-with [map key val f & args]
  (assoc map key (apply f (get map key) val args)))

(defn pairwise
  ([xs] (pairwise xs []))
  ([xs acc]
     (if-let [x (first xs)]
       (let [xpairs (map (partial vector x) (rest xs))]
         (recur (rest xs) (concat xpairs acc)))
       acc)))

(defn downcase [str]
  (map kbd/symbol-downcase (.toLowerCase str)))

(defn char->position+finger [layout char]
  (let [pos (layout char)
        fing (kbd/position->finger pos)]
    (concat pos fing)))

(def char-pairs (for [c1 kbd/charset c2 kbd/charset] [c1 c2]))



;;; enterprise plumbing

(defn log [& args]
  ;; pretty terrible
  (let [timestamp (-> (java.util.Date.) .getTime java.sql.Timestamp. .toString)]
    ;; replace stdout log with sweet interprize solution
    (apply println timestamp "|" args)))



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

(defn keyvec->chars-fitness [kv]
  (let [layout (kbd/keyvec->layout kv)]
   (reduce (fn [fitmap cs] (assoc fitmap cs (chars-fitness layout cs)))
           {} char-pairs)))



;;; keyboards

(defn print-keyvec [kv] (println (kbd/keyvec->str kv)))

(defn keyvec+score->str [kv score]
  (let [[a b c] (.split (kbd/keyvec->str kv) "\n")
        a (str a "     " score)]
    (apply str (interpose "\n" [a b c]))))

(def qwerty kbd/qwerty-keyvec)
(def dvorak kbd/dvorak-keyvec)
(def colemak kbd/colemak-keyvec)

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



;;; it's evolution baby

(defn local-fitness [population text]
  (sort-by first (pindex-by #(fitness % text) population)))

(defonce keyboard-work (atom nil))
(defonce worker-stats (atom {}))

(def empty-stats {:n 0 :mean 0
                  ;; :stddev nil :25 nil :50 nil :75 nil :95 nil
                  })

(defn add-stats [{:keys [n mean] :as stats} add-n val]
  (let [new-mean (/ (+ (* n mean)
                       (* add-n val))
                    (+ n add-n))]
    (assoc stats
      :n (+ add-n n)
      :mean new-mean)))

(defn add-worker-stats [id n time]
  (swap! worker-stats (fn [stats]
                        (let [cur (stats id empty-stats)]
                          (assoc stats id (add-stats cur n time))))))

;; in-progress work units have the keyvecs to be worked on, the id of
;; the worker, and when the work was assigned.
(defn in-progress-batch [kvs id]
  {:work kvs
   :id id
   :timestamp (now)})

(defn in-progress-disj [in-progress-coll kvs id]
  (let [[unit rest] (filter-split #(and (= (:id %) id)
                                        (= (:work %) kvs))
                                  in-progress-coll)]
    (if (= (count unit) 1)
      (let [unit (first unit)
            time (- (now) (:timestamp unit))]
        (add-worker-stats id (count (:work unit)) time))
      (log "in-progress-disj found non-1 work units with kvs and id" kvs id ":" unit))
    rest))


;; terrible. how should this actually work? can atoms work at all for
;; this?
(defn get-work [id]
  (let [hack (atom nil)
        dispense (fn [{:keys [new] :as work-state}]
                   (if-let [kvs (first new)]
                     (do (reset! hack kvs) ; ugh
                         (| work-state
                            #(update % :new rest)
                            #(update % :in-progress conj
                                     (in-progress-batch kvs id))))
                     work-state))]
    (swap! keyboard-work dispense)
    @hack))

;; work is [[<kv> <score>] ...]. kvs in original order.
(defn work-done [id work]
  (println "yo got this work" id work)
  (let [kvs (map first work)
        _ (println "kvs is " work kvs)
        add-done (fn [state]
                   (| state
                      #(update % :in-progress in-progress-disj kvs id)
                      #(update % :finished into work)))]
    (println "work is " @keyboard-work)
    (swap! keyboard-work add-done)))

;; frobbify state so workers can display something interesting
(defn status []
  )

;; let the internet share in our love
;; rough as hell first cut. I mean, uh, "minimum viable product"
(defn distributed-fitness [population nworkers]
  (reset! keyboard-work {:new (batch (max nworkers 1) population)
                         :in-progress #{}
                         :finished []})
  (let [start (java.util.Date.)
        size (count population)]
    ;; fire off work-reaper here!
    (loop []
      (let [{:keys [new in-progress finished]} @keyboard-work]
        (if (= size (count finished))
          (do (println "done! started" start ", now" (java.util.Date.))
              (reset! keyboard-work nil) ; crud!
              finished)
          (do (println "not done yet." (count in-progress) "in progress."
                       (count new) "undone.")
              (Thread/sleep 10000)
              (recur)))))))

;; for each key position, randomly select a character from one of the
;; parents. this can result in duplicate keys and missing keys.
;; randomly replace duplicates with missing.
(defn sex [kv1 kv2]
  (let [candidate-kv (map (fn [c1 c2] (if (rand-bool) c1 c2))
                          kv1 kv2)
        key-positions (reduce (fn [keypos [i k]] (assoc-with keypos k i conj))
                              {}
                              (index candidate-kv))
        dupes (filter (fn [[_key positions]] (> (count positions) 1))
                      key-positions)
        missing (shuffle (set/difference kbd/charset candidate-kv))
        replacements (into {} (map (fn [[_k positions] missing]
                                     [(rand-nth positions) missing])
                                   dupes missing))
        child (map-indexed (fn [i c] (get replacements i c))
                           candidate-kv)]
    child))

;; this can almost certainly be better
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
                         :else k))
                 v)))

(defn tweak-keyvec [kv]
  (let [mutators [rotate rand-swap]]
    ((rand-nth mutators) kv)))

(defn mutate [kv radiation]
  (let [times (int (* radiation 100))
        mutated-kv (nth (iterate tweak-keyvec kv) times)]
    mutated-kv))

(defn random-keyvec [] (shuffle kbd/charset))

;; random immigrants: some percent of original population
;; parents: current population + immigrants
;; children: each parent mates with all others
;; next-gen: parents + children, mutated
;; n*(n-1)/2 + n + immigrants*n
;; new population: top n of next-gen, sorted by fitness
(defn evolve
  "Returns a new population of evolved keyboards. radiation [0,1)
  controls how \"violent\" mutations are. immigrants [0,1] controls
  how many randoms are added to the population."
  [population radiation immigrants nworkers]
  (let [immigrant-limit (int (* immigrants (count population)))
        immigrants (repeatedly immigrant-limit random-keyvec)
        parents (concat immigrants population)
        children (time (doall (map (partial apply sex) (pairwise parents))))
        next-gen (time (doall (map #(mutate % radiation) (concat parents children))))
        scored (time (distributed-fitness next-gen nworkers))
        next-pop (map second (take (count population) scored))]
    {:scored scored
     :next-population next-pop}))

(defonce state-atom (atom nil))

;; state has generation number, fitness test data, current population,
;; top organisms so far, and history of population fitness.
(defn genetic-loop [{:keys [gen workers population top history] :as state}]
  (reset! state-atom state)
  (let [{:keys [scored next-population]} (evolve population 0.01 0.10 workers)
        ave-score (fn [xs] (average (map first xs)))
        topn (count top)
        gen-top (take topn scored)
        new-top (take topn (sort-by first (concat scored top)))]
    (println "======== generation" (inc gen))
    (println "history:" (map average (take 10 history)))
    (println "    gen ave:" (ave-score scored))
    (println "gen top ave:" (ave-score gen-top))
    (println "    top ave:" (ave-score new-top))
    (doseq [[score kv] gen-top] (println (keyvec+score->str kv score)))
    ;; (Thread/sleep 1000)
    (recur {:gen (inc gen)
            :workers workers
            :population next-population
            :top new-top
            :history (conj history (map first gen-top))})))

(defn initial-gen [n topn text]
  (let [pop (repeatedly n random-keyvec)
        ;; score initial gen locally
        scored (local-fitness pop text)
        top (take topn scored)]
    {:gen 0
     :workers 0
     :population pop
     :top top
     :history (list (map first top))}))

(defn genetic [n topn]
  (genetic-loop (initial-gen n topn data/fitness)))
