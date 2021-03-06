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

;; parameters only contain the radiation-level and immigrant-rate. the
;; population size that survives each iteration is not a parameter,
;; for now.

;; there are 4 main pieces of state:
;;
;; * current evolution state
;; ** generation number
;; ** simulation parameters (described above)
;; ** current keyboard population
;; ** top n keyboards seen so far
;; ** top n keyboards from the previous generation
;; ** history: list of each past generation's top n keyboard scores
;;
;; * current population fitness testing
;; ** finished
;; ** in-progress (assigned to a worker). have timestamps and worker ids.
;; ** new (unassigned)
;;
;; * worker stats, containing per-worker summary stats of compute
;;   time. # entries is estimate of worker population.
;;
;; * global evolution history
;; ** top n keyboards ever. each item has :kbd, :score, :params.
;; ** map from parameters to list of evolution states (similar to the
;;    state described above, but don't contain population or previous
;;    top keyboards).




;;; util

;; thx chouser
(defn pipe [& args] (reduce #(%2 %1) args))

;; don't think this is technically an allowed char :)
(def | pipe)

(defn update
  "Update the map's value for k with f."
  [map k f & args]
  (apply update-in map [k] f args))

(defn decimal-places [f places]
  (let [mult (Math/pow 10 places)]
    (/ (Math/round (* f mult))
       mult)))

(defn pretty-range [start end step places]
  (map #(decimal-places % places) (range start end step)))

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

(def work-batch-size "number of keyvecs in a batch" 2)
(def work-batch-ttl "how long a work-batch can be unfinished before reassignment"
     (* 15 1000))
(def worker-ttl "how long before a worker is considered dead"
     (* 120 1000))
(def reaper-period "how long the reaper sleeps"
     (* 3 1000))

;; gak, floats. this actually includes 1.0 (range includes 0.99999, prettied to 1.0)
(def radiation-level-range "probability of mutation occuring"
     (pretty-range 0 1.0 0.1 2))
;; 0% to half the population
(def immigrant-rate-range "random kbds added to population as a fraction of population"
     (pretty-range 0 0.6 0.1 2))

(defn local-fitness [population text]
  (sort-by second (ppair-with #(kbd/fitness % text)
                              population)))

(defonce keyboard-work (atom nil))
(defonce worker-stats (atom {}))
(defonce evo-state (atom nil))
(defonce global-state (atom {}))

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

;; used to update keyboard-work when we get completed work
(defn in-progress->finished [{:keys [in-progress finished] :as state} id work]
  (let [kvs (map first work)
        [unit rest] (filter-split #(and (= (:id %) id)
                                        (= (:work %) kvs))
                                  in-progress)]
    (if (= (count unit) 1) ; expected case
      (let [unit (first unit)
            time (- (now) (:timestamp unit))]
        (add-worker-stats id (count (:work unit)) time)
        (assoc state :in-progress rest, :finished (into finished work)))
      (do ;; if unit count = 0, can be because client was slow and
          ;; sent a work unit that got reaped and completed by someone
          ;; else. if unit count > 1, pretty weird bug...
        (log "in-progress->finished found non-1 work units with kvs and id" kvs id ":" unit)
        ;; don't change anything
        state))))

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
  (swap! keyboard-work (fn [state] (in-progress->finished state id work))))

;; external! frobbify state so workers can display something
;; interesting.
(defn status []
  (let [{:keys [gen params top prev-gen-top history]} @evo-state
        {:keys [new in-progress finished]} @keyboard-work]
    {:gen gen
     :params params
     :top top
     :prev-gen-top prev-gen-top
     :history history
     :workers (count @worker-stats)
     :new (* work-batch-size (count new))
     :in-progress (* work-batch-size (count in-progress))
     :finished (count finished)
     }))

;; external!
(defn global-status []
  (let [{:keys [param-states top]} @global-state
        param-history (map (fn [[params evo-states]]
                             [params (map :history evo-states)])
                           param-states)]
    {:param-history param-history
     :top top}))

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
                        (do (log "reaped" (count reaped) "work units:" reaped)
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

  (loop [{prev-new :new prev-in-progress :in-progress} nil]
    (let [{:keys [start new in-progress finished] :as state} @keyboard-work]
      (if (= (count population) (count finished))
        (do (println "done! started" start ", now" (now))
            (sort-by second finished))
        (do (when (or (not= prev-new new)
                      (not= prev-in-progress in-progress))
              (println (count in-progress) "in progress," (count new) "undone"))
            (Thread/sleep 3000)
            (recur state))))))

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

(defn mutate [kv level]
  (if (< (rand) level)
    (apply str (tweak-keyvec kv))
    kv))

(defn random-keyvec [] (apply str (shuffle kbd/charset)))

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
      (Math/ceil soln))))

;; random immigrants: some percent of original population
;; parents: current population + immigrants
;; children: each parent mates with all others
;; next-gen: parents + children, mutated
;; new population: top n of next-gen, sorted by fitness
(defn evolve
  "Returns a new population of evolved keyboards. radiation-level
  [0,1] controls the probability of mutations. immigrant-rate [0,1]
  controls how many randoms are added to the population."
  [population {:keys [radiation-level immigrant-rate]}]
  (let [immigrants (repeatedly (Math/ceil (* immigrant-rate (count population)))
                               random-keyvec)
        parents (concat immigrants population)
        children (dotime (map (partial apply sex) (pairwise parents)))
        next-gen (dotime (map #(mutate % radiation-level) (concat parents children)))
        ;; set, to uniquify population
        scored (time (distributed-fitness (set next-gen)))
        ;; resize worker population
        nworkers (count @worker-stats)
        target-size (* work-batch-size nworkers)
        pop-needed (bounded (population-needed target-size immigrant-rate) 10 300)
        next-pop (map first (take pop-needed scored))]
    (when (not= pop-needed (count population))
      (log "population size changed from" (count population) "to" pop-needed))
    {:scored scored
     :next-population next-pop}))

;; get rid of data that's uninteresting at the global level
(defn globalify [evo-state]
  (dissoc evo-state :gen :params :population :prev-gen-top))

(defn genetic-loop [iters {:keys [gen params population top history] :as state}]
  (reset! evo-state state)
  (let [{:keys [scored next-population]} (evolve population params)
        ave-score (fn [xs] (average (map second xs)))
        topn (count top)
        gen-top (take topn scored)
        new-top (take topn (sort-by second (set (concat scored top))))]
    (println "======== generation" (inc gen) params)
    (println "history:" (take 10 history))
    (println "   nworkers:" (count @worker-stats))
    (println "    gen ave:" (ave-score scored))
    (println "gen top ave:" (ave-score gen-top))
    (println "    top ave:" (ave-score new-top))
    (println "  best ever:")
    (println (apply kbd/keyvec+score->str (first new-top)))
    (doseq [[kv score] gen-top]
      (println (str "---\n" (kbd/keyvec+score->str kv score))))
    (let [next-state {:gen (inc gen)
                      :params params
                      :population next-population
                      :top new-top
                      :prev-gen-top gen-top
                      :history (conj history (average (map second gen-top)))}]
      (if (>= (:gen next-state) iters)
        (globalify next-state)
        (recur iters next-state)))))

(defn initial-gen [n topn params text]
  (let [pop (repeatedly n random-keyvec)
        ;; score initial gen locally
        scored (local-fitness pop text)
        top (take topn scored)]
    {:gen 0
     :params params
     :population pop
     :top top
     :prev-gen-top nil
     :history (list (average (map second top)))}))

;; work-reaper thread
(defonce reaper (atom nil))

(defn start-reaper []
  (let [start (fn [t]
                (if (nil? t)
                  (.start (Thread. #(work-reaper work-batch-ttl worker-ttl
                                                 reaper-period)))
                  (do (log "reaper already running") t)))]
    (swap! reaper start)))

;; clj, cljs interop sadness
(def fitness-data (kbd/symbol-downcase (.toLowerCase data/fitness)))

(defn genetic [iters n topn params]
  (start-reaper)
  (genetic-loop iters (initial-gen n topn params fitness-data)))

(defn update-global [{:keys [param-states top] :as state} topn params new-evo-state]
  (let [param-states (update param-states params conj new-evo-state)
        latest-top (map (fn [[k s]] {:kbd k :score s :params params})
                        (:top new-evo-state))
        top (take topn (sort-by :score (set (concat top latest-top))))]
    {:param-states param-states
     :top top}))

;; up the ladder of abstraction
(defn global-genetic [iters n topn]
  (let [all-params (for [r radiation-level-range
                         i immigrant-rate-range]
                     {:radiation-level r
                      :immigrant-rate i})]
    (doseq [params (cycle all-params)]
      (log "running genetic with" params)
      (let [evo-result (time (genetic iters n topn params))]
        (swap! global-state update-global topn params evo-result)))))

(defn start-global
  ([iters n] (start-global iters n 5))
  ([iters n topn]
     (.start (Thread. #(global-genetic iters n topn)))))
