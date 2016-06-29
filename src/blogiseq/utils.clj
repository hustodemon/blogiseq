(ns blogiseq.utils)

(defn parse-edn [path]
  (-> path
    slurp
    clojure.edn/read-string))

