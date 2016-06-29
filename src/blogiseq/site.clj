(ns blogiseq.site
  (:require
    [blogiseq.utils :as utils]
    [hiccup.core :as hiccup]
    [hiccup.page :as hiccup-page]
    [hiccup.element :as hiccup-element]
    [compojure.core :as compojure]
    [compojure.route :as compojure-route]
    [markdown.core :as md])
  (:gen-class))

(defn articles-edn->hiccup-menu
  [edn]
  [:ul
   (map
     (fn [elem] [:li [:a {:href (:href elem)} (:title elem)]])
     edn)])

(defn generate-menu-navi
  "Generates menu navigation structure."
  [path]
  (-> path
    utils/parse-edn
    :articles
    articles-edn->hiccup-menu))

(def about
  (->
    (slurp "resources/required/left-column.md")
    (md/md-to-html-string)))

(def include-js-code-highlight
  [:div
   (hiccup-page/include-css "//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.4.0/styles/atelier-dune-light.min.css")
   (hiccup-page/include-js "//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.4.0/highlight.min.js")
   (hiccup-element/javascript-tag "hljs.initHighlightingOnLoad();")])

(def include-js-utils
  (hiccup-page/include-js "/js/utils.js"))

(defn embed-disqus [page-id]
  [:div
   [:div#disqus_thread]
   [:div "<script id=\"dsq-count-scr\" src=\"//frankysblogiseq.disqus.com/count.js\" async></script>"]
   (hiccup-element/javascript-tag
     (clojure.string/replace
       (slurp "resources/js/disqus.js")
       "<PAGE_ID>"
       page-id))
   [:noscript "Please enable JS to see the discussion (disqus)."]])

(defn site ; would be cool to externalize this too (to support user-defined layouts)
  [content]
  [:div
   (hiccup-page/include-css "/css/franky.css")
   include-js-code-highlight
   include-js-utils
   [:div.container
    [:div#about about]
    [:div#menu
     [:h3#menu-header "MENU"]
     [:div#menu-inner (generate-menu-navi "resources/meta.edn")]]
    [:div#main [:div content]
     ]]
   ])

(defn render
  "Renders whole site. Takes chunks of hiccup and renders them in standalone
  divs in the main page section."
  [& content]
  (-> (map (fn [el] [:div el]) content)
    site
    hiccup/html))

(defn render-with-disqus [content disqus-id]
  (render
    content
    (embed-disqus (str "disqus-id" disqus-id))))

