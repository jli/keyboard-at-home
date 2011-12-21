(ns keyboard-at-home.js-worker
    (:require [goog.net.XhrIo :as Xhr]
              [goog.events :as events]
              [goog.date :as date]
              [goog.Timer :as Timer]
              [cljs.reader :as reader]
              [keyboard-at-home.kbd :as kbd]
              [keyboard-at-home.data :as data]))

;;; util

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



;;; protocol

(defn work-url [id] (str "/work?id=" id))
(def done-url "/done")

(defn submit-work [id work k]
  (Xhr/send done-url k "POST" (form-params {:id id :work (pr-str work)})))


;;; THUNDERCATS

(def stats (atom {:n 0 :mean-time 0}))

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
            ;; (log "work done:" (pr-str work) " in " time)
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
                     (do ;; (log "got " (count batch) " kbds")
                         (compute-batch id batch))
                     (do ;; (log "no work available")
                         (Timer/callOnce #(work-loop id) 1500)))))]
      (Xhr/send (work-url id) work))))

(defn ^:export thundercats-are-go []
  (let [id (rand-string 8)]
    ;; (log "worker " id " starting up")
    (reset! working? true)
    (work-loop id)))

(defn ^:export thundercats-should-stop []
  (reset! working? false))

(defn ^:export onmessage [ev]
  (log "fail?")
  (let [type (.data ev)]
    (cond (= type "start") (thundercats-are-go)
          (= type "stop") (thundercats-should-stop))))
