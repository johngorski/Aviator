(ns aviator-clj.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [aviator-clj.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[aviator-clj started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[aviator-clj has shut down successfully]=-"))
   :middleware wrap-dev})
