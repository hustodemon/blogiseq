(ns blogiseq.core
  (:require
    [org.httpkit.server :as server]
    [hiccup.core :as hiccup]
    [hiccup.page :as hiccup-page]
    [hiccup.element :as hiccup-element]
    [clojure.data.json :as json]
    [compojure.core :as compojure]
    [compojure.route :as compojure-route]
    [markdown.core :as md]))

; todo list
; + site layout (hiccup)
; - css
; - multipage support (compojure)
; - auto scan of resources

;;;;;;;;;;;;;;; Menu gen
(defn menu-edn->hiccup
  [edn]
  [:ul
  (map
    (fn [elem] [:li [:a {:href (:href elem)} (:title elem)]])
    edn)])

(defn generate-menu-navi
  "Generates menu navigation structure."
  [path]
  (-> path
    slurp
    clojure.edn/read-string
    menu-edn->hiccup))

(def left
  [:div
   [:h3 "Franky's blog"]
   [:p "Hi, my name is Franky, I do this and that...ble blehh lorem ipsum."]
   [:p [:i "testing some stuff"]]])

(defn include-js-code-highlight [] ; todo use def
  [:div ; do i have to use wrapping div?
   (hiccup-page/include-css "//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.4.0/styles/atelier-dune-light.min.css")
   ;(hiccup-page/include-css "//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.4.0/styles/gruvbox-light.min.css")
   [:script {:src "//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.4.0/highlight.min.js"}]
   (hiccup-element/javascript-tag "hljs.initHighlightingOnLoad();")])

(defn disqus-embed []
  [:div
   "<div id=\"disqus_thread=\"></div>
   <script>
   var disqus_config = function () {
   this.page.url = this.page.url = 'http://example.com/helloworld.html'; // todo
   this.page.identifier = 'single-id-so-far'; // todo
   };
   */
   (function() {  // DON'T EDIT BELOW THIS LINE
   var d = document, s = d.createElement('script');

   s.src = '//frankysblogiseq.disqus.com/embed.js';

   s.setAttribute('data-timestamp', +new Date());
   (d.head || d.body).appendChild(s);
   })();
   </script>
   <noscript>Please enable JavaScript to view the <a href=\"https://disqus.com/?ref_noscript\" rel=\"nofollow\">comments powered by Disqus.</a></noscript>
   "])

;;;;;;;;;;;;;
(defn site
  [content]
  [:div
   (hiccup-page/include-css "/resources/css/franky.css")
   (include-js-code-highlight)
   (disqus-embed)
   [:div.container
    [:div.left left]
    [:div.right (generate-menu-navi "resources/meta.edn")]
    [:div.middle [:div content
                  [:div "<script id=\"dsq-count-scr\" src=\"//frankysblogiseq.disqus.com/count.js\" async></script>"]]
    ]]])

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
  (compojure/GET "/" [] (hiccup/html (site (md/md-to-html-string (slurp "resources/index.md")))))
  (compojure/GET "/resources/articles/:file" [file] (hiccup/html (site (detail (str "resources/articles/" file)))))
  (compojure-route/resources "/resources/images" {:root "images"})
  (compojure-route/resources "/resources/css" {:root "css"}))

(defn start []
  (reset! server (server/run-server (fn [r] (routes r)) {:port 3001})))

(defn stop []
  (if (not (nil? @server))
    (@server)))

(defn restart []
  (stop)
  (start))

  (restart)
