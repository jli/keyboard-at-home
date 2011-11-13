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
              ;; [keyboard-at-home.data :as data]
              ))

;;; util

(def html dom/htmlToDocumentFragment)
(def node dom/createDom)

(defn js-alert [& args]
  (let [msg (apply str args)]
    (js* "alert(~{msg})")))

(defn log [& args]
  (let [msg (apply str (goog.date.DateTime.) args)]
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

(defn render-status [{:keys [gen history top workers new in-progress finished]}]
  (let [{:keys [n mean-time]} @stats]
   (node "div" nil
         (node "span" nil "gen " (str gen))
         (html "<br>")
         (node "span" nil "workers " (str workers))
         (html "<br>")
         (node "span" nil "new " (str new))
         (html "<br>")
         (node "span" nil "in-progress " (str in-progress))
         (html "<br>")
         (node "span" nil "finished " (str finished))
         (html "<br>")
         (node "span" nil "boards done " (str n))
         (html "<br>")
         (node "span" nil "average compute time " (str (/ mean-time 1000)) "s")
         )))

(defn update-status [status]
  (dom/removeChildren status-node)
  (dom/appendChild status-node (render-status status)))

(defn new-mean [n cur-val add-n new-val]
  (/ (+ (* n cur-val)
        (* add-n new-val))
     (+ n add-n)))

(defn add-stats [{:keys [n mean-time]} batch-size time]
  {:n (+ n batch-size)
   :mean-time (new-mean n mean-time batch-size time)})

(defn compute-batch [id batch]
  ;; data/fitness
  (let [start (now)
        work (doall (map (fn [kv] [kv (kbd/fitness kv "hey just a test string for now"
                                                   #(. % (toLowerCase)))])
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

(defn work-loop [id]
  (let [work (fn [e]
               (let [batch (event->clj e)]
                 (if-not (empty? batch)
                   (do (log "got " (count batch) " kbds")
                       (compute-batch id batch))
                   (do (log "no work available")
                       (Timer/callOnce #(work-loop id) 1500)))))
        status (fn [e]
                 (update-status (event->clj e))
                 (Xhr/send (work-url id) work))]
    (Xhr/send status-url status)))

(defn ^:export thundercats-are-go []
  (let [id (rand-string 8)]
    (log "worker " id " starting up")
    (work-loop id)))
