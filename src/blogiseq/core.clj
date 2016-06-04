(ns blogiseq.core
  (:require
    [org.httpkit.server :as server]
    [hiccup.core :as hiccup]
    [hiccup.page :as hiccup-page]
    [hiccup.element :as hiccup-element]
    [compojure.core :as compojure]
    [compojure.route :as compojure-route]
    [markdown.core :as md])
  (:gen-class))

;;;;;;;;;;;;;;; Menu gen
(defn articles-edn->hiccup
  [edn]
  [:ul
  (map
    (fn [elem] [:li [:a {:href (:href elem)} (:title elem)]])
    edn)])

(defn parse-meta-edn [path]
  (-> path
    slurp
    clojure.edn/read-string))

(defn generate-menu-navi
  "Generates menu navigation structure."
  [path]
  (-> path
    parse-meta-edn
    :articles
    articles-edn->hiccup))

(def left
  [:div
   [:h3 "Franky's blog"]
   [:p "Hi, my name is Franky, I do this and that...ble blehh lorem ipsum."]
   [:p [:i "testing some stuff"]]])

(def include-js-code-highlight
  [:div
   (hiccup-page/include-css "//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.4.0/styles/atelier-dune-light.min.css")
   (hiccup-page/include-js "//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.4.0/highlight.min.js")
   (hiccup-element/javascript-tag "hljs.initHighlightingOnLoad();")])

(defn disqus-js [page-id]
  (str "var disqus_config = function () {
       // todo try if ommiting this will be annoying:
       // this.page.url = 'http://www.franky-canonical-fqdn.com/';
       this.page.identifier = '"page-id"';
       };
       (function() {  // DON'T EDIT BELOW THIS LINE
       var d = document, s = d.createElement('script');

       s.src = '//frankysblogiseq.disqus.com/embed.js';

       s.setAttribute('data-timestamp', +new Date());
       (d.head || d.body).appendChild(s);
       })();"))

(defn embed-disqus [page-id]
  [:div
   [:div.disqus_thread]
   (hiccup-element/javascript-tag (disqus-js page-id))
   [:noscript "Please enable JavaScript to view the <a href=\"https://disqus.com/?ref_noscript\" rel=\"nofollow\">comments powered by Disqus.</a>"]])

;;;;;;;;;;;;;
(defn site
  [content]
  [:div
   (hiccup-page/include-css "/css/franky.css")
   include-js-code-highlight
   [:div.container
    [:div.left left]
    [:div.right (generate-menu-navi "resources/meta.edn")]
    [:div.middle [:div content
                  [:div#disqus_thread]
                  [:div "<script id=\"dsq-count-scr\" src=\"//frankysblogiseq.disqus.com/count.js\" async></script>"]]
    ]]])

(defn detail
  "Todo: fecurity."
  [path]
  (md/md-to-html-string
    (slurp path)))

(defonce server (atom nil))

(compojure/defroutes
  routes
  (compojure/GET "/" [] (hiccup/html (site (md/md-to-html-string (slurp "resources/index.md")))))
  (compojure/GET "/articles/:article/:md-file.md" [article md-file] (hiccup/html (site [:div
                                                                                        (detail (str "resources/articles/" article "/" md-file ".md"))
                                                                                        [:div (embed-disqus (str "franky-very-long-disqus-id-" article))]
                                                                                        ])))
  (compojure-route/resources "/articles" {:root "articles"})
  (compojure-route/resources "/images" {:root "images"})
  (compojure-route/resources "/css" {:root "css"})
  (compojure-route/not-found (hiccup/html (site "Stuff not found."))))

(defn start []
  (reset! server (server/run-server (fn [r] (routes r)) {:port 3001})))

(defn stop []
  (if (not (nil? @server))
    (@server)))

(defn restart []
  (stop)
  (start))

(comment
  (restart))

(defn -main [& args] (start))

