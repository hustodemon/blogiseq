(ns blogiseq.utils)

(defmacro swallow-exceptions [msg default & forms]
  `(try
     (do ~@forms)
     (catch Exception e# (println "*" ~msg
                                "\n  Cause:  "
                                (.getMessage e#)
                                "\n  Returning: " ~default)
                         ~default))
  )

(defn parse-resource [path]
  (-> path
    clojure.java.io/resource
    slurp)
  )

(defn parse-edn-resource [path]
  (-> path
    parse-resource
    clojure.edn/read-string))

(defn filenames-with-extension [dir ext]
  (->>
    (file-seq (clojure.java.io/file dir))
    (map #(.getName %))
    (filter #(clojure.string/ends-with? % (str "." ext)))
  )
)
