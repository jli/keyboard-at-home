(ns keyboard-at-home.js-viewer
    (:require [goog.dom :as dom]
              [goog.net.XhrIo :as Xhr]
              [goog.events.EventType :as EventType]
              [goog.events :as events]
              [goog.date :as date]
              [goog.Timer :as Timer]
              [cljs.reader :as reader]
              [keyboard-at-home.kbd :as kbd]))

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
    ;; (dom/setTextContent (dom/getElement "output") msg)
    (dom/insertChildAt (dom/getElement "output") (node "div" nil msg) 0)
    ))

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

(def status-node (dom/getElement "status"))
(def global-status-node (dom/getElement "global-status"))
(def global-top-node (dom/getElement "global-top"))

(defn ewma
  "Return an exponentially-weighted moving average of xs. The weight w
  [0,1] is applied to new values. Higher values cause the average to
  move more rapidly."
  [xs w]
  (let [inv-w (- 1 w)]
    (reductions (fn [acc x] (+ (* x w) (* acc inv-w)))
                xs)))



;;; protocol

(def status-url "/status")
(def global-status-url "/global-status")



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
  (let [s (kbd/keyvec+score->str kbd score)]
    (node "pre" nil s)))

(defn render-top-kbd [{:keys [kbd score params]}]
  (node "div" nil
        "r" (str (:radiation-level params))
        ", i" (str (:immigrant-rate params))
        (html "<br>")
        (str score)
        (node "pre" nil (kbd/keyvec->str kbd))))

;; loose cljs translation of http://ejohn.org/projects/jspark/
(defn render-sparkline [parent vals]
  (let [canvas (node "canvas" (js* "{\"style\": \"background-color: #fafafa;\"}"))
        ctx (.getContext canvas "2d")
        minv (apply min vals)
        maxv (apply max vals)
        stretch-factor 3
        w (* stretch-factor (count vals))
        h "2em"
        ewma-vals (ewma vals 0.25)]
    (set! (.. parent style display) "inline")
    (set! (.. canvas style width) w)
    (set! (.. canvas style height) h)
    (set! (.width canvas) w)
    (dom/appendChild parent canvas)
    (dom/appendChild parent (html "<br>"))
    (let [offh (.offsetHeight canvas)
          draw (fn [vals color width]
                 (set! (.strokeStyle ctx) color)
                 (set! (.lineWidth ctx) width)
                 (. ctx (beginPath))
                 (doseq [[i v] (index vals)]
                   (let [x (* i (/ w (count vals)))
                         y (- offh (* offh (/ (- v minv)
                                              (- maxv minv))))]
                     (when (zero? i) (.moveTo ctx x y))
                     (.lineTo ctx x y)))
                 (. ctx (stroke)))]
      (set! (.height canvas) offh)
      (draw vals "red" 1.5)
      (draw ewma-vals "blue" 1))))

;; works weirdly because render-sparkline attaches to existing parent
(defn render-summary+sparkline [parent vals]
  (let [to3 (fn [f] (str (.toFixed f 3)))
        summary (node "span" nil (to3 (first vals)) "→" (to3 (last vals)))]
    (dom/appendChild parent summary)
    (render-sparkline parent vals)))

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
                         [(node "tr" (js* "{\"valign\": \"top\"}")
                                (node "td" nil
                                      "r" (str radiation-level) ", i" (str immigrant-rate))
                                (apply node "td" nil
                                       (map (fn [i]
                                              (let [id (params+index->spark-id params i)]
                                                (node "div" (js* "{\"id\": ~{id}}"))))
                                            (range (count histories)))))])
                       status))))

(defn render-global-top [top]
  (node "div" nil
        (node "h2" nil "best so far")
        (node "table" (js* "{\"style\": \"border: solid thin;\"}")
              (apply node "tr" nil
                     (map (fn [top-kbd] (node "td" nil (render-top-kbd top-kbd)))
                          top)))))

;; don't update when nothing new
(def last-status (atom nil))
;; global changed if current evo params not= last-params
(def last-params (atom nil))

(defn update-status [status update-global worker?]
  (when (not= @last-status status)
    (reset! last-status status)
    (when (not= @last-params (:params status))
      (reset! last-params (:params status))
      (update-global))
    (dom/removeChildren status-node)
    (dom/appendChild status-node (render-status status worker?))
    ;; needs to happen after status-node is added to dom, in order to
    ;; get size of inline canvas
    (render-sparkline (dom/getElement spark-id) (reverse (:history status)))))

(defn update-global-status [{:keys [param-history top]}]
  (dom/removeChildren global-top-node)
  (dom/appendChild global-top-node (render-global-top top))
  (let [param-history (radix-sort [(comp :radiation-level first)
                                   (comp :immigrant-rate first)] param-history)]
    (dom/removeChildren global-status-node)
    (dom/appendChild global-status-node (render-global-status param-history))
    (doseq [[params histories] param-history
            [i history] (index histories)]
      (let [div (dom/getElement (params+index->spark-id params i))]
        (render-summary+sparkline div (reverse history))))))

(defn new-mean [n cur-val add-n new-val]
  (/ (+ (* n cur-val)
        (* add-n new-val))
     (+ n add-n)))

(defn add-stats [{:keys [n mean-time]} batch-size time]
  {:n (+ n batch-size)
   :mean-time (new-mean n mean-time batch-size time)})

(def join-button (dom/getElement "join"))

(def web-worker (atom nil))

(defn start-web-worker []
  (log "starting")
  (let [w (window.Worker. "worker.js")]
    (log "created")
    ;; (set (.onmessage w) ???)
    (.postMessage w "start")
    2(log "told to start")
    w))

(defn ^:export thundercats-are-spectating
  ([] (thundercats-are-spectating 1500 5000))
  ([interval global-interval]
     (let [timer (goog.Timer. interval)
           global-xhr (doto (goog.net.XhrIo.)
                        (events/listen goog.net.EventType.COMPLETE
                                       #(update-global-status (event->clj %))))
           update-global #(.send global-xhr global-status-url)
           status-xhr (doto (goog.net.XhrIo.)
                        (events/listen goog.net.EventType.COMPLETE
                                       #(update-status (event->clj %) update-global false)))
           work-toggle (fn []
                         (if-let [w @web-worker]
                           (do ;; (. w (close))
                               (reset! web-worker nil)
                               (dom/setTextContent join-button "join!")
                               (log "thanks for your efforts!"))
                           (do (log "welcome to the force")
                               (reset! web-worker (start-web-worker))
                               (dom/setTextContent join-button "leave!"))))]
       ;; status update loops
       (log "you're tuning in live!")
       (timer-attach timer #(.send status-xhr status-url))
       ;; join the working force
       (events/listen join-button events/EventType.CLICK work-toggle))))
