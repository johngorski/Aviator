(ns aviator-clj.core)

(-> (.getElementById js/document "content")
    (.-innerHTML)
    (set! "Hello, World!"))
