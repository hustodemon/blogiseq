(ns blogiseq.core
  (:require
    [org.httpkit.server :as server]
    [hiccup.core :as hiccup]
    [clojure.data.json :as json]
    [compojure.core :as compojure]
    [markdown.core :as md]))

; todo list
; + site layout (hiccup)
; - css
; - multipage support (compojure)
; - auto scan of resources

;;;;;;;;;;;;;;; Menu gen
(defn lazy-lines [path]
 (with-open [f (clojure.java.io/reader path)]
  (line-seq f)))

(defn read-meta
  "Reads franky meta data. so far mock todo try clojure spec."
  [path]
  {:title (str "Toz vitajte na: " path) 
   :tags "first,test,franky's new blog!"
   :disqus-id 123})

(defn read-content
  [path])

(defn md-path->link
  "This will generate a link with a reasonable text (extracted from the md)."
  [path]
  (let [franky-meta (read-meta path)]
    [:a {:href path} (:title franky-meta)]))

(defn generate-menu-navi
  "Generates menu navigation structure."
  [path]
  (->> (file-seq (java.io.File. path))
    (filter #(clojure.string/ends-with? % ".md"))
    (map #(.getPath %))
    (map md-path->link)))
;;;;;;;;;;;;;

(defn site
  [content]
  [:div
   [:div "Top"]
   [:div (generate-menu-navi "resources")]
   [:div content]])

(defn detail
  "Todo: fecurity."
  [path]
  (slurp path))

(defn app [req]
  {:status 200
   :headers {"Content-Type"  "text/html"}
   :body (hiccup/html site)})

(defonce server (atom nil))

(compojure/defroutes
  routes
  (compojure/GET "/" [] (hiccup/html (hiccup/html (site (md/md-to-html-string (slurp "resources/index.md"))))))
  (compojure/GET "/resources/articles/:file" [file] (hiccup/html (site (detail (str "resources/articles/" file))))))

(defn start []
  (reset! server (server/run-server (fn [r] (routes r)) {:port 3001})))

(defn stop []
  (if (not (nil? @server))
    (@server)))

(defn restart []
  (stop)
  (start))

  (restart)
