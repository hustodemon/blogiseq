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
     (fn [elem] [:a.w3-hover-black {:href (:href elem)} (:title elem)])
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
  [:html
   [:head
    (hiccup-page/include-css "/css/franky.css")
     include-js-code-highlight
     include-js-utils
     (hiccup-page/include-css "http://www.w3schools.com/lib/w3.css")
     (hiccup-page/include-css "http://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css")
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     [:link {:rel "icon"
             :type "image/png"
             :href "/images/favicon.png"}]
    ]
   [:body
    [:div.w3-content.w3-light-yellow
     [:nav#mySidenav.w3-sidenav.w3-light-yellow.w3-collapse {:style "z-index:3;width:250px;margin-top:51px;background-color:#fffbc7;"}
      [:a {:title "Close menu"
           :class "w3-right w3-xlarge w3-padding-large w3-hover-black w3-hide-large"
           :onclick "w3_close()"
           :href "javascript:void(0)"}
       "close menu"
       [:i {:class "fa fa-remove"}]
       ] 
      [:div about]
      [:div (generate-menu-navi "resources/meta.edn")]]
     [:div {:id "myOverlay",
            :title "close side menu",
            :style "cursor:pointer",
            :onclick "w3_close()",
            :class "w3-overlay w3-hide-large"}]
     [:div.w3-main {:style "margin-left:300px"} [:div 
                                                 [:header {:class "w3-container"} 
                                                  [:span {:onclick "w3_open()"
                                                          :class "w3-opennav w3-hide-large w3-xxlarge w3-hover-text-grey"} 
                                                   [:i {:class  "fa fa-bars"}]]]
                                                 content]]
     ]]])

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
