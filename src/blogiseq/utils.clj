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

(defn- parse-resource
  "Firstly parse path in the `resources/` dir. If not successful,
  tries loading this from resources."
  [path]
  (if-let [ext-content (slurp (str "resources/" path))]
    ext-content
    (slurp (clojure.java.io/resource path))
    )
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
