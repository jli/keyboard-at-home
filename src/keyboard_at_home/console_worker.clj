(ns keyboard-at-home.console-worker
  (:require [keyboard-at-home.kbd :as kbd]
            [keyboard-at-home.data :as data]
            [clj-http.client :as client]))

;;; values

(def no-work-nap-period 2000)



;;; util

(defn rand-string [n]
  (let [hex-str #(Integer/toHexString (rand-int 16))]
    (apply str (repeatedly n hex-str))))

(defn log [& strs] (apply println (java.util.Date.) strs))



;;; protocol

(defn status-url [addr] (str addr "/status"))
(defn work-url [addr id] (str addr "/work?id=" id))
(defn done-url [addr] (str addr "/done"))

(defn print-status [addr]
  (-> (client/get (status-url addr))
      :body
      println))

(defn get-work [addr id]
  (-> (work-url addr id)
      client/get
      :body
      read-string))

(defn submit-work [addr id work]
  (client/post (done-url addr)
               {:form-params
                {:id id :work (pr-str work)}}))

;;; thundercats are go

(defn get-work-loop [addr id]
  (let [res (get-work addr id)]
    (if-not (empty? res)
      res
      (do (log "no work available! napping...")
          (Thread/sleep no-work-nap-period)
          (log "yay, refreshed and trying again")
          (recur addr id)))))

(def fitness-data (kbd/symbol-downcase (.toLowerCase data/fitness)))

(defn work-loop [addr id]
  ;; meh
  ;; (print-status addr)
  (log "getting work...")
  (let [batch (get-work-loop addr id)
        fitted (time (doall
                      (map (fn [kv] [kv (kbd/fitness kv fitness-data)])
                           batch)))]
    (log "submitting" (count fitted) fitted)
    (submit-work addr id fitted)
    (recur addr id)))

(defn safe [f msg]
  (try (f)
       (catch Exception e (log msg e))))

(defn safe-work-loop [addr id]
  (safe #(work-loop addr id) "whoa, exception! chill for a few seconds.")
  (Thread/sleep 5000)
  (recur addr id))

(defn start-worker [addr id]
  (println "worker" id "reporting for duty")
  (client/with-connection-pool nil
    (safe-work-loop addr id)))
