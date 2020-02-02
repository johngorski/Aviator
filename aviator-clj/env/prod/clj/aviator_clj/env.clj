(ns aviator-clj.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[aviator-clj started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[aviator-clj has shut down successfully]=-"))
   :middleware identity})
