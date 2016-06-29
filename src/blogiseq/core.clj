(ns blogiseq.core
  (:require
    [blogiseq.site :as site]
    [org.httpkit.server :as server]
    [hiccup.core :as hiccup]
    [hiccup.page :as hiccup-page]
    [hiccup.element :as hiccup-element]
    [compojure.core :as compojure]
    [compojure.route :as compojure-route]
    [markdown.core :as md])
  (:gen-class))

(compojure/defroutes
  routes
  (compojure/GET "/" []
                 (site/render (md/md-to-html-string (slurp "resources/required/index.md"))))
  (compojure/GET "/*.md" [:as request]  ; why /:resource{\\.md} doesn't work?
                 (let [resource (:* (:params request))
                       article (slurp (str "resources/" resource ".md"))]
                   (site/render-with-disqus (md/md-to-html-string article) nil)))
  (compojure-route/resources "/articles" {:root "articles"})
  (compojure-route/resources "/css" {:root "css"})
  (compojure-route/resources "/js" {:root "js"})
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

