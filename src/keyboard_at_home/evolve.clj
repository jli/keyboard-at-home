;;; core of genetic algorithm. includes fitness, mutation, and sex.

(ns keyboard-at-home.evolve
  (:require [keyboard-at-home.kbd :as kbd]
            [keyboard-at-home.data :as data]
            [clojure.set :as set]))

;; keyboards have a keyvec, a layout, and a character pair fitness
;; function. keyvecs and layouts are defined in data. the fitness
;; function is defined here.

;; the central evolution server doesn't calculate fitness (except for
;; the initial random population), but does constantly send keyboards
;; across the wire as well as mutate/mate keyvecs. so go for
;; compactness and store everything as keyvecs.

;; there are 3 main pieces of state:
;;
;; * evolution state
;; ** generation number
;; ** current keyboard population
;; ** top n keyboards seen so far
;; ** (estimated) number of workers (move to worker state?)
;; ** history
;; *** list of each past generation's top n keyboards
;;
;; * current population fitness testing
;; ** finished
;; ** in-progress (assigned to a worker). have timestamps and worker ids.
;; ** new (unassigned)
;;
;; * worker stats, containing per-worker summary stats of
;; compute time
;;
;; todo: different parameters with top score curves



;;; util

;; thx chouser
(defn pipe [& args] (reduce #(%2 %1) args))

;; don't think this is technically an allowed char :)
(def | pipe)

(defn update
  "Update the map's value for k with f."
  [map k f & args]
  (apply update-in map [k] f args))

(defn filter-split
  "Return a pair of seqs. All in the first seq tested true and all in
  the second tested false via the given pred."
  [pred xs]
  (reduce (fn [[ts fs] x] (if (pred x)
                            [(conj ts x) fs]
                            [ts (conj fs x)]))
          [[] []] xs))

(defn now [] (.getTime (java.util.Date.)))

(let [random (java.util.Random.)]
  (defn rand-bool [] (.nextBoolean random)))

(defn index
  "Return a seq that has each element paired with its index (index
  appears first)."
  [xs] (map-indexed vector xs))

(defn ppair-with [f xs]
  "Return a seq with each element paired with the result of applying
f (in parallel)."
  (pmap (fn [x] [x (f x)]) xs))

(defn average [xs] (float (/ (apply + xs) (count xs))))

(defn round-up [x] (Math/round (+ x 0.5)))

(defn bounded
  "Ensure value is between lo and hi (inclusive)."
  [x lo hi]
  (max (min x hi) lo))

(defn quad-eq [a b c]
  (let [discrim-root (Math/sqrt (- (Math/pow b 2)
                                   (* 4 a c)))
        soln+ (/ (+ (- b) discrim-root)
                 2 a)
        soln- (/ (- (- b) discrim-root)
                 2 a)]
    [soln+ soln-]))

(defn dotime [coll] (time (doall coll)))

(defn pairwise
  "Return each pair of elements in the collection."
  ([xs] (pairwise xs []))
  ([xs acc]
     (if-let [x (first xs)]
       (let [xpairs (map (partial vector x) (rest xs))]
         (recur (rest xs) (concat xpairs acc)))
       acc)))



;;; enterprise plumbing

(defn log [& args]
  ;; pretty terrible
  (let [timestamp (-> (java.util.Date.) .getTime java.sql.Timestamp. .toString)]
    ;; replace stdout log with sweet interprize solution
    (apply println timestamp "|" args)))



;;; it's evolution baby

(def work-batch-size "number of keyvecs in a batch" 5)
(def radiation-level "how many mutations keyvecs are subject to" 2)
(def immigrant-rate "random kbds added to population as a fraction of population" 0.10)
(def work-batch-ttl "how long a work-batch can be unfinished before reassignment"
     (* 15 1000))
(def worker-ttl "how long before a worker is considered dead"
     (* 120 1000))
(def reaper-period "how long the reaper sleeps"
     (* 3 1000))

(defn local-fitness [population text]
  (sort-by second (ppair-with #(kbd/fitness % text) population)))

(defonce keyboard-work (atom nil))
(defonce worker-stats (atom {}))

(def empty-stats {:n 0 :mean 0.
                  :pinged nil ; last-heard-from time
                  ;; :stddev nil :25 nil :50 nil :75 nil :95 nil
                  })

(defn add-stats [{:keys [n mean] :as stats} add-n val]
  (let [new-mean (/ (+ (* n mean)
                       (* add-n val))
                    (+ n add-n))]
    (assoc stats
      :n (+ add-n n)
      :mean new-mean
      :pinged (now))))

(defn add-worker-stats [id n time]
  (swap! worker-stats (fn [stats]
                        (let [cur (get stats id empty-stats)]
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

;; external! serve new work.
(defn get-work [id]
  ;; :new-in-progress in keyboard-work has the keyvecs for this
  ;; worker. if no work is left, dissoc :new-in-progress so the same
  ;; work isn't served twice
  (let [dispense (fn [{:keys [new] :as work-state}]
                   (if-let [kvs (first new)]
                     (-> work-state
                         (update :new rest)
                         (update :in-progress conj (in-progress-batch kvs id))
                         (assoc :new-in-progress kvs))
                     (dissoc work-state :new-in-progress)))
        new-state (swap! keyboard-work dispense)]
    (:new-in-progress new-state)))

;; external! save finished work.
;; work is [[<kv> <score>] ...]. kvs in original order.
(defn work-done [id work]
  (let [kvs (map first work)
        add-done (fn [state]
                   (-> state
                       (update :in-progress in-progress-disj kvs id)
                       (update :finished into work)))]
    (swap! keyboard-work add-done)))

;; external! frobbify state so workers can display something
;; interesting.
(defn status []
  "todo")

;; move abandoned work from in-progress to new
;; add to worker stats?
(defn work-reaper [work-timeout worker-timeout sleep]
  (let [work-too-old (fn [in-progress-batch]
                       (> (- (now) (:timestamp in-progress-batch))
                          work-timeout))
        reap-work (fn [{:keys [in-progress] :as work-state}]
                    (let [[reaped still-okay] (filter-split work-too-old in-progress)]
                      (if (empty? reaped)
                        work-state
                        (do (log "reaped" (count reaped) "work units:" (map :id reaped))
                            (-> work-state
                                (update :new concat (map :work reaped))
                                (assoc :in-progress still-okay))))))
        worker-dead (fn [worker]
                       (> (- (now) (:pinged worker))
                          worker-timeout))
        reap-workers (fn [worker-stats]
                       (let [dead (->> worker-stats
                                       (filter (fn [[id stats]] (worker-dead stats)))
                                       (map first))]
                         (when-not (empty? dead)
                           (log "reaped" (count dead) "workers:" dead))
                         (apply dissoc worker-stats dead)))]
    (swap! keyboard-work reap-work)
    (swap! worker-stats reap-workers)
    (Thread/sleep sleep)
    (recur work-timeout worker-timeout sleep)))

;; let the internet share in our love
(defn distributed-fitness [population]
  (reset! keyboard-work {:start (now)
                         :new (partition-all work-batch-size population)
                         :in-progress #{}
                         ;; :finished is a set, in case of duped work
                         :finished #{}})

  (loop []
    (let [{:keys [start new in-progress finished]} @keyboard-work]
      (if (= (count population) (count finished))
        (do (println "done! started" start ", now" (now))
            (sort-by second finished))
        (do (println (count in-progress) "in progress," (count new) "undone")
            (Thread/sleep 1000)
            (recur))))))

;; for each key position, randomly select a character from one of the
;; parents. this can result in duplicate keys and missing keys.
;; randomly replace duplicates with missing.
(defn sex [kv1 kv2]
  (let [candidate-kv (map (fn [c1 c2] (if (rand-bool) c1 c2))
                          kv1 kv2)
        key-positions (reduce (fn [keypos [i k]] (update keypos k conj i))
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

(defn mutate [kv times]
  (let [mutated-kv (nth (iterate tweak-keyvec kv) times)]
    mutated-kv))

(defn random-keyvec [] (shuffle kbd/charset))

;; pop-size (solution) = n
;; immigrant-rate = i
;; immigrants = in
;; parents = n + immigrants = n + in = (i+1)n
;; children = parents * (parents-1)/2
;; target-size = parents + children
;;             = (i+1)n + (i+1)n*((i+1)n - 1) / 2
;;              = ... = (i^2/2 + i + .5)n^2 + (i+1)/2*n
;; 0 = (i^2/2 + i + .5)*n^2 + (i+1)/2*n - target-size
;; solve with quadratic equation
(defn population-needed
  "How big does the population need to be for the target size?"
  [target-size immigrant-rate]
  (let [a (+ (/ (Math/pow immigrant-rate 2) 2)
             immigrant-rate
             0.5)
        b (/ (inc immigrant-rate) 2)
        c (- target-size)
        solns (quad-eq a b c)
        [soln] (filter pos? solns)]
    (when soln
      (round-up soln))))

;; random immigrants: some percent of original population
;; parents: current population + immigrants
;; children: each parent mates with all others
;; next-gen: parents + children, mutated
;; new population: top n of next-gen, sorted by fitness
(defn evolve
  "Returns a new population of evolved keyboards. radiation (int)
  controls how many mutations happen. immigrants [0,1] controls how
  many randoms are added to the population."
  [population radiation immigrant-rate]
  (let [immigrants (repeatedly (round-up (* immigrant-rate (count population)))
                               random-keyvec)
        parents (concat immigrants population)
        children (dotime (map (partial apply sex) (pairwise parents)))
        next-gen (dotime (map #(mutate % radiation) (concat parents children)))
        scored (time (distributed-fitness next-gen))
        ;; resize worker population
        nworkers (count @worker-stats)
        target-size (* work-batch-size nworkers)
        pop-needed (bounded (population-needed target-size immigrant-rate) 10 300)
        next-pop (map first (take pop-needed scored))]
    (when (not= pop-needed (count population))
      (log "population size changed from" (count population) "to" pop-needed))
    {:scored scored
     :next-population next-pop}))

(defonce state-atom (atom nil))

(defn genetic-loop [{:keys [gen population top history] :as state}]
  (reset! state-atom state)
  (let [{:keys [scored next-population]} (evolve population radiation-level immigrant-rate)
        ave-score (fn [xs] (average (map second xs)))
        topn (count top)
        gen-top (take topn scored)
        new-top (take topn (sort-by second (concat scored top)))]
    (println "======== generation" (inc gen))
    (println "history:" (map average (take 10 history)))
    (println "   nworkers:" (count @worker-stats))
    (println "    gen ave:" (ave-score scored))
    (println "gen top ave:" (ave-score gen-top))
    (println "    top ave:" (ave-score new-top))
    (println "  best ever:")
    (println (apply kbd/keyvec+score->str (first new-top)))
    (doseq [[kv score] gen-top]
      (println (str "---" (kbd/keyvec+score->str kv score))))
    (recur {:gen (inc gen)
            :population next-population
            :top new-top
            :history (conj history (map second gen-top))})))

(defn initial-gen [n topn text]
  (let [pop (repeatedly n random-keyvec)
        ;; score initial gen locally
        scored (local-fitness pop text)
        top (take topn scored)]
    {:gen 0
     :population pop
     :top top
     :history (list (map second top))}))

(defn genetic [n topn]
  (.start (Thread. #(work-reaper work-batch-ttl worker-ttl reaper-period)))
  (genetic-loop (initial-gen n topn data/fitness)))
