(ns blogiseq.core
  (:require
    [blogiseq.site :as site]
    [blogiseq.utils :as utils]
    [org.httpkit.server :as server]
    [hiccup.core :as hiccup]
    [compojure.core :as compojure]
    [compojure.route :as compojure-route]
    [markdown.core :as md])
  (:gen-class))

(compojure/defroutes routes
  ; index
  (compojure/GET "/" []; ha! based on file type we could employ various renderers!
                 (-> "index.md"
                   (utils/parse-resource "Welcome to blogiseq engine. README.md file says: 'read me'.")
                   md/md-to-html-string
                   site/render))
  ; markdown renderer
  (compojure/GET "/*.md" [:as request]  ; why /:resource{\\.md} doesn't work?
                 (let [resource (:* (:params request))
                       article (utils/parse-resource (str resource ".md"))]
                   (site/render-with-disqus (md/md-to-html-string article) nil)))
  ; everything in `resources/`
  (compojure-route/resources "/" {:root ""})
  ; 404
  (compojure-route/not-found (hiccup/html (site/render "Stuff not found."))))

(defonce server (atom nil))

(defn start []
  (reset! server (server/run-server (fn [r] (routes r)) {:port 3000})))

(defn stop []
  (if (not (nil? @server))
    (@server)))

(defn restart []
  (stop)
  (start))

(comment
  (restart))

(defn -main [& args] (start))

