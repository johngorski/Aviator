(ns user
  (:require [mount.core :as mount]
            aviator-clj.core))

(defn start []
  (mount/start-without #'aviator-clj.core/repl-server))

(defn stop []
  (mount/stop-except #'aviator-clj.core/repl-server))

(defn restart []
  (stop)
  (start))


