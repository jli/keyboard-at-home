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

(def html dom/htmlToDocumentFragment)
(def node dom/createDom)

(defn js-alert [& args]
  (let [msg (apply str args)]
    (js* "alert(~{msg})")))

(defn log [& args]
  (let [msg (apply str (goog.date.DateTime.) ": " args)]
    (dom/setTextContent (dom/getElement "output") msg)))

(defn now [] (goog.date.DateTime.))

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



;;; protocol

(def status-url "/status")
(defn work-url [id] (str "/work?id=" id))
(def done-url "/done")

(defn submit-work [id work k]
  (Xhr/send done-url k "POST" (form-params {:id id :work (pr-str work)})))


;;; THUNDERCATS

(def stats (atom {:n 0 :mean-time 0}))

(defn render-progress-bar [finished in-progress new]
  (apply str (concat (repeat finished "=")
                     (repeat in-progress "+")
                     (repeat new "-"))))

(defn render-history [hist]
  (apply str (interpose \→ (reverse hist))))

(defn render-kbd+score [[kbd score]]
  (node "pre" nil (kbd/keyvec+score->str kbd score)))

;; loose cljs translation of http://ejohn.org/projects/jspark/
(defn render-sparklines [parent vals]
  (let [vals (js->clj vals)
        canvas (node "canvas" (js* "{\"style\": \"background-color: #fafafa;\"}"))
        ctx (.getContext canvas "2d")
        minv (apply min vals)
        maxv (apply max vals)
        stretch-factor 5
        w (* stretch-factor (count vals))
        h (atom "1.5em")]
    (set! (.. parent style display) "inline")
    (set! (.. canvas style width) w)
    (set! (.. canvas style height) @h)
    (set! (.width canvas) w)
    (dom/appendChild parent canvas)
    (reset! h (.offsetHeight canvas))
    (set! (.height canvas) @h)
    (set! (.strokeStyle ctx) "red")
    (set! (.lineWidth ctx) 1.5)
    (. ctx (beginPath))
    (doseq [[v i] (map vector vals (range))]
      (let [x (* i (/ w (count vals)))
            y (- @h (* @h (/ (- v minv)
                             (- maxv minv))))]
        (when (zero? i) (.moveTo ctx x y))
        (.lineTo ctx x y)))
    (. ctx (stroke))))

(def spark-id "sparkspan")

(defn render-status [{:keys [gen history top prev-gen-top
                             workers new in-progress finished]}
                     worker?]
  (let [{:keys [n mean-time]} @stats]
    (node "div" nil
          (node "span" nil "generation: " (str gen))
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
                      (node "td" nil (html "top keyboards from<br>previous generation:"))
                      (node "td" nil (html "top keyboards ever:")))
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

;; don't update when nothing new
(def last-status (atom nil))

(defn update-status [status worker?]
  (when (not= @last-status status)
    (reset! last-status status)
    (dom/removeChildren status-node)
    (dom/appendChild status-node (render-status status worker?))
    ;; needs to happen after status-node is added to dom, in order to
    ;; get size of inline canvas
    (render-sparklines (dom/getElement spark-id) (reverse (:history status)))))


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
                         (Timer/callOnce #(work-loop id) 1500)))))
          status (fn [e]
                   (update-status (event->clj e) true)
                   (when @working?
                     (Xhr/send (work-url id) work)))]
      (Xhr/send status-url status))))

(defn ^:export thundercats-are-go []
  (let [id (rand-string 8)]
    (log "worker " id " starting up")
    (work-loop id)))

(def join-button (dom/getElement "join"))

(defn ^:export thundercats-are-spectating
  ([] (thundercats-are-spectating 1000))
  ([interval]
     (let [timer (goog.Timer. interval)
           status (fn [] (Xhr/send status-url
                                   #(update-status (event->clj %) false)))
           work-toggle (fn []
                         (if @working?
                           (do (reset! working? false)
                               (log "thanks for your efforts!")
                               (dom/setTextContent join-button "join!")
                               (. timer (start)))
                           (do (reset! working? true)
                               (. timer (stop))
                               (dom/setTextContent join-button "leave!")
                               (thundercats-are-go))))]
       ;; status update loop
       (log "you're tuning in live!")
       (events/listen timer goog.Timer/TICK status)
       (. timer (start))
       ;; join the working force
       (events/listen join-button events/EventType.CLICK work-toggle))))
