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
(defn md-path->link
  "This will generate a link with a reasonable text (extracted from the md)."
  [path]
  (let [franky-meta (read-meta path)]
    [:a {:href path} (:title franky-meta)]))

(defn menu-edn->hiccup
  [edn]
  (map
    (fn [elem] [:a {:href (:href elem)} (:title elem)])
    edn))

(defn generate-menu-navi
  "Generates menu navigation structure."
  [path]
  (-> path
    slurp
    clojure.edn/read-string
    menu-edn->hiccup))

;;;;;;;;;;;;;
(defn site
  [content]
  [:div
   [:div "Top"]
   [:div (generate-menu-navi "resources/meta.edn")]
   [:div content]])

(defn detail
  "Todo: fecurity."
  [path]
  (md/md-to-html-string
    (slurp path)))

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
