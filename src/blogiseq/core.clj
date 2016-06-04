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

; todo list
; + site layout (hiccup)
; + css
; + multipage support (compojure)
; - auto scan of resources - nope - we'll rely on meta.edn

;;;;;;;;;;;;;;; Menu gen
(defn menu-edn->hiccup
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

(defn embed-disqus [page-id]
  [:div
   (str
     "<div id=\"disqus_thread=\"></div>
     <script>
     var disqus_config = function () {
     //this.page.url = 'http://www.franky-canonical-fqdn.com/';// todo try if ommiting this will be annoying
     this.page.identifier = '"page-id"';
     };
     (function() {  // DON'T EDIT BELOW THIS LINE
     var d = document, s = d.createElement('script');

     s.src = '//frankysblogiseq.disqus.com/embed.js';

     s.setAttribute('data-timestamp', +new Date());
     (d.head || d.body).appendChild(s);
     })();
     </script>
     <noscript>Please enable JavaScript to view the <a href=\"https://disqus.com/?ref_noscript\" rel=\"nofollow\">comments powered by Disqus.</a></noscript>
     ")])

;;;;;;;;;;;;;
(defn site
  [content]
  [:div
   (hiccup-page/include-css "/css/franky.css")
   (include-js-code-highlight)
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

(defn app [req]
  {:status 200
   :headers {"Content-Type"  "text/html"}
   :body (hiccup/html site)})

(defonce server (atom nil))

(defn copy-path-if-not-exists
  "this is rather lame"
  [src tgt]
  (let [tgt-file (clojure.java.io/file tgt)
        src-file (clojure.java.io/file src)]
    (if-not (.exists tgt-file)
      (clojure.java.io/copy src-file tgt-file))))

(defn ensure-miniature [path]
  (let [mini-name (str path "_mini")]
    (if-not (.exists (clojure.java.io/file mini-name))
      (copy-path-if-not-exists path mini-name))))

(defn is-image? [path]
  (not (nil? (re-find #"jpg$|png$|gif$" (clojure.string/lower-case path)))))

(is-image? "resources/photowalls/usa")

(defn dir-to-photowall [path]
  (let [f (clojure.java.io/file path)
        files (file-seq f)
        images (filter (fn [f] (is-image? (.getName f))) files)
        paths (map #( .getPath %) images)]
    [:div
     [:h1 "lame photogallery"]
     "this is just some attemt to create autogenerated gallery."
     [:div
      (map
        (fn [path]
          (let [enhanced-path (str "/" path)]
            [:a {:href enhanced-path} [:img.thumb {:src enhanced-path :width 160}]]))
        paths)]]))

(defn photowalls-index
  "lame - use metadata instead"
  [path]
  [:div
   [:h1 "My photowalls"]
   "Here you can find list of my memories."
   (map (fn [p] [:p [:a {:href (str "/photowalls/" (.getName p))} p]])
        (.listFiles (clojure.java.io/file path)))])

(compojure/defroutes
  routes
  ;(compojure-route/resources "photowalls/" {:root "photowalls/"}) ; all photowalls resources (photos)
  ;(compojure/GET "/photowalls/index" [] (hiccup/html (site (photowalls-index "resources/photowalls"))))
  ;(compojure/GET "/photowalls/:name" [name] (hiccup/html (site (dir-to-photowall (str "resources/photowalls/" name)))))

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

