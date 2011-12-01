(ns keyboard-at-home.js-worker
    (:require [goog.dom :as dom]
              [goog.string :as string]
              [goog.array :as array]
              [goog.net.XhrIo :as Xhr]
              [goog.events.EventType :as EventType]
              [goog.events :as events]
              [goog.date :as date]
              [goog.Timer :as Timer]
              [cljs.reader :as reader]
              [keyboard-at-home.kbd :as kbd]
              ;; weird cljs-watch badness
              [keyboard-at-home.data :as data]
              ))

;;; util

(defn index [coll] (map-indexed vector coll))

(defn radix-sort [keys maps]
  (reduce (fn [maps k] (sort-by k maps))
          maps (reverse keys)))

(def html dom/htmlToDocumentFragment)
(def node dom/createDom)

(defn js-alert [& args]
  (let [msg (apply str args)]
    (js* "alert(~{msg})")))

(defn log [& args]
  (let [msg (apply str (goog.date.DateTime.) ": " args)]
    (dom/setTextContent (dom/getElement "output") msg)))

(defn now [] (goog.date.DateTime.))

(defn timer-attach [timer f]
  (f)
  (events/listen timer goog.Timer/TICK f)
  (. timer (start)))

(defn form-params [m]
  (let [pairs (map (fn [[k v]] (str (name k) "=" (js/encodeURIComponent v))) m)]
    (apply str (interpose "&" pairs))))

(defn event->clj [e]
  (-> (.target e)
      (. (getResponseText))
      reader/read-string))

(defn rand-string [n]
  (let [hex-str #(rand-nth "0123456789abcdef")]
    (apply str (repeatedly n hex-str))))

(def status-node (dom/getElement "status"))
(def global-status-node (dom/getElement "global-status"))



;;; protocol

(def status-url "/status")
(def global-status-url "/global-status")
(defn work-url [id] (str "/work?id=" id))
(def done-url "/done")

(defn submit-work [id work k]
  (Xhr/send done-url k "POST" (form-params {:id id :work (pr-str work)})))


;;; THUNDERCATS

(def stats (atom {:n 0 :mean-time 0}))

(defn render-progress-bar [finished in-progress new]
  ;; use about 50 chars
  (let [scale (/ 50.0 (+ finished in-progress new))
        [finished in-progress new] (map (partial * scale)
                                        [finished in-progress new])]
   (apply str (concat (repeat finished "=")
                      (repeat in-progress "+")
                      (repeat new "-")))))

(defn render-history [hist]
  (apply str (interpose \→ (reverse hist))))

(defn render-kbd+score [[kbd score]]
  (node "pre" nil (kbd/keyvec+score->str kbd score)))

;; loose cljs translation of http://ejohn.org/projects/jspark/
(defn render-sparklines [parent vals]
  (let [canvas (node "canvas" (js* "{\"style\": \"background-color: #fafafa;\"}"))
        ctx (.getContext canvas "2d")
        minv (apply min vals)
        maxv (apply max vals)
        stretch-factor 2
        w (* stretch-factor (count vals))
        h (atom "1.5em")]
    (set! (.. parent style display) "inline")
    (set! (.. canvas style width) w)
    (set! (.. canvas style height) @h)
    (set! (.width canvas) w)
    (dom/appendChild parent canvas)
    (let [offh (.offsetHeight canvas)]
      (set! (.height canvas) offh)
      (set! (.strokeStyle ctx) "red")
      (set! (.lineWidth ctx) 1.5)
      (. ctx (beginPath))
      (doseq [[i v] (index vals)]
        (let [x (* i (/ w (count vals)))
              y (- offh (* offh (/ (- v minv)
                                   (- maxv minv))))]
          (when (zero? i) (.moveTo ctx x y))
          (.lineTo ctx x y)))
      (. ctx (stroke)))))

(def spark-id "sparkspan")

;; true horror
(defn render-status [{:keys [gen params history top prev-gen-top
                             workers new in-progress finished]}
                     worker?]
  (let [{:keys [n mean-time]} @stats]
    (node "div" nil
          (node "h2" nil "current evolution status")
          (node "span" nil "generation: " (str gen))
          (html "<br>")
          (node "span" nil "radiation level: " (str (:radiation-level params)))
          (html "<br>")
          (node "span" nil "immigrant rate: " (str (:immigrant-rate params)))
          (html "<br>")
          (node "span" nil "# workers: " (str workers))
          (html "<br>")
          (node "span" nil "finished/in-progress/new: "
                (str finished) "/" (str in-progress) "/" (str new))
          (html "<br>")
          (node "span" (js* "{\"style\": \"font-family: monospace\"}")
                (render-progress-bar finished in-progress new))
          (html "<br>")
          (node "span" nil "ave. score for previous 5 generations: "
                (render-history (take 5 history)))
          (html "<br>")
          (node "div" nil "scores for all " (str (count history)) " generations: "
                (node "span" (js* "{\"id\": ~{spark-id}}")))
          (html "<p>")
          ;; needs srs work
          (node "table" (js* "{\"style\": \"border: solid thin;\"}")
                (node "tr" nil
                      (node "td" nil (html "best from<br>previous generation:"))
                      (node "td" nil (html "best from<br>current simulation:")))
                (node "tr" nil
                      (node "td" nil
                            (apply node "div" nil (map render-kbd+score prev-gen-top)))
                      (node "td" nil
                            (apply node "div" nil (map render-kbd+score top)))))
          (html "<br>")
          (when worker?
            (node "span" nil "boards done " (str n))
            (html "<br>")
            (node "span" nil "average compute time " (str (/ mean-time 1000)) "s")))))

(defn params+index->spark-id [{:keys [radiation-level immigrant-rate]} i]
  (str "spark-r" radiation-level "-i" immigrant-rate "-" i))

;; true horror
(defn render-global-status [status]
  (node "div" nil
        (node "h2" nil "global evolution history")
        (apply node "table" (js* "{\"style\": \"border: solid thin;\"}")
               (mapcat (fn [[{:keys [radiation-level immigrant-rate] :as params} histories]]
                         [(node "tr" nil
                                (node "td" nil
                                      "r" (str radiation-level) "i" (str immigrant-rate))
                                (apply node "td" nil
                                       (map (fn [i]
                                              (let [id (params+index->spark-id params i)]
                                                (node "div" (js* "{\"id\": ~{id}}"))))
                                            (range (count histories)))))])
                       status))))

;; don't update when nothing new
(def last-status (atom nil))
(def last-params (atom nil))
;; global changed if current evo params not= last-params. initially true for first update.
(def global-changed? (atom true))

(defn update-status [status worker?]
  (when (not= @last-status status)
    (reset! last-status status)
    (when (not= @last-params (:params status))
      (reset! global-changed? true)
      (reset! last-params (:params status)))
    (dom/removeChildren status-node)
    (dom/appendChild status-node (render-status status worker?))
    ;; needs to happen after status-node is added to dom, in order to
    ;; get size of inline canvas
    (render-sparklines (dom/getElement spark-id) (reverse (:history status)))))

(defn update-global-status [status]
  (let [status (radix-sort [(comp :radiation-level first)
                            (comp :immigrant-rate first)] status)]
    (when @global-changed?
      (reset! global-changed? false)
      (dom/removeChildren global-status-node)
      (dom/appendChild global-status-node (render-global-status status))
      (doseq [[params histories] status
              [i history] (index histories)]
        (let [div (dom/getElement (params+index->spark-id params i))]
          (render-sparklines div (reverse history)))))))

(defn new-mean [n cur-val add-n new-val]
  (/ (+ (* n cur-val)
        (* add-n new-val))
     (+ n add-n)))

(defn add-stats [{:keys [n mean-time]} batch-size time]
  {:n (+ n batch-size)
   :mean-time (new-mean n mean-time batch-size time)})

(def fitness-data (kbd/symbol-downcase (. data/fitness (toLowerCase))))

(defn compute-batch [id batch]
  (let [start (now)
        work (doall (map (fn [kv] [kv (kbd/fitness kv fitness-data)])
                         batch))
        stop (now)
        time (- stop start)
        k (fn [e]
            (log "work done:" (pr-str work) " in " time)
            (swap! stats add-stats (count batch) time)
            ;; breather
            ;; (Timer/callOnce #(work-loop id) 100)
            (work-loop id)
            )]
    (submit-work id work k)))

(def working? (atom false))

(defn work-loop [id]
  (when @working?
    (let [work (fn [e]
                 (let [batch (event->clj e)]
                   (if-not (empty? batch)
                     (do (log "got " (count batch) " kbds")
                         (compute-batch id batch))
                     (do (log "no work available")
                         (Timer/callOnce #(work-loop id) 1500)))))]
      (Xhr/send (work-url id) work))))

(defn ^:export thundercats-are-go []
  (let [id (rand-string 8)]
    (log "worker " id " starting up")
    (work-loop id)))

(def join-button (dom/getElement "join"))

(defn ^:export thundercats-are-spectating
  ([] (thundercats-are-spectating 2000 5000))
  ([interval global-interval]
     (let [global-timer (goog.Timer. global-interval)
           timer (goog.Timer. interval)
           global-status (fn [] (Xhr/send global-status-url
                                          #(update-global-status (event->clj %))))
           status (fn [] (Xhr/send status-url
                                   #(update-status (event->clj %) false)))
           work-toggle (fn []
                         (if @working?
                           (do (reset! working? false)
                               (dom/setTextContent join-button "join!")
                               (log "thanks for your efforts!"))
                           (do (reset! working? true)
                               (dom/setTextContent join-button "leave!")
                               (thundercats-are-go))))]
       ;; status update loops
       (log "you're tuning in live!")
       (timer-attach global-timer global-status)
       (timer-attach timer status)
       ;; join the working force
       (events/listen join-button events/EventType.CLICK work-toggle))))
