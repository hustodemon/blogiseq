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

(defn slurp-nil
  "Slurp path. If there's some exception, don't freak out, but return nil
  instead."
  [path]
  (swallow-exceptions
    (str "Can't read " path)
    nil
    (slurp path)))

(defn parse-resource
  "Firstly parse path in the `resources/` dir.
  If not successful, tries loading this from resources.
  If even this is not succesful and default is provided, returns the provided
  default.
  Otherwise return nil."
  ([path default]
   (first
     (filter
       some?
       (list
         (slurp-nil (str "resources/" path))
         (slurp-nil (clojure.java.io/resource path))
         default))))
  ([path]
   (parse-resource path nil)))

(defn parse-edn-resource [path default]
  (-> path
    (parse-resource default)
    clojure.edn/read-string))

(defn filenames-with-extension [dir ext]
  (->>
    (file-seq (clojure.java.io/file dir))
    (map #(.getName %))
    (filter #(clojure.string/ends-with? % (str "." ext)))
  )
)
