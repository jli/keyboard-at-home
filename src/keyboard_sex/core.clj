(ns keyboard-sex.core
  (:use [compojure.core]
        [compojure.route :only [resources]]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.util.response :only [response file-response]]
        [ring.middleware.params :only [wrap-params]]
        [ring.middleware.reload :only [wrap-reload]]
        [ring.middleware.stacktrace :only [wrap-stacktrace]]
        [ring.middleware.gzip :only [wrap-gzip]]
        [clojure.tools.cli :only [cli optional]])
  (:require [swank.swank]
            [keyboard-sex.evolve :as evolve])
  (:gen-class))

(defroutes base
  (GET "/work" [id] (response (prn-str (evolve/get-work id))))
  (POST "/done" [id res] (do (evolve/work-done id (read-string res))
                             (response "ok")))
  (GET "/status" [] (response (prn-str (evolve/status))))
  (GET "/love" [] (response "<3"))
  (GET "/" [] (file-response "resources/public/index.html"))
  (resources "/")
  (ANY "*" [] (file-response "resources/public/index.html")))

(def app
     (-> base
         wrap-params
         wrap-gzip
         ;; hm, doesn't seem to interact well with defonce
         ;; (wrap-reload '(keyboard-sex.core keyboard-sex.evolve keyboard-sex.kbd keyboard-sex.data))
         wrap-stacktrace))

(defn -main [& args]
  (let [opts (cli args
                  (optional ["-j" "--jetty-port" :default 8080] #(Integer. %))
                  (optional ["-s" "--swank-port" :default 8081] #(Integer. %))
                  (optional ["-ns" "--no-swank" :default false]))]
    (when-not (:no-swank opts)
      (swank.swank/start-server :port (:swank-port opts)))
    (println "starting jetty...")
    (run-jetty #'app {:port (:jetty-port opts)})))
