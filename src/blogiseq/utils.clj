(ns blogiseq.utils)

(defn parse-edn [path]
  (-> path
    slurp
    clojure.edn/read-string))

(defn filenames-with-extension [dir ext]
  (->>
    (file-seq (clojure.java.io/file dir))
    (map #(.getName %))
    (filter #(clojure.string/ends-with? % (str "." ext)))
  )
)
