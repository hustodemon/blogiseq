(ns blogiseq.site
  (:require
    [blogiseq.utils :as utils]
    [hiccup.core :as hiccup]
    [hiccup.page :as hiccup-page]
    [hiccup.element :as hiccup-element]
    [compojure.core :as compojure]
    [compojure.route :as compojure-route]
    [markdown.core :as md]
    [clojure.walk :refer [postwalk-replace]])
  (:gen-class))

(defn- menu-link [elem]
  [:li
   [:a.w3-hover-black {:href (:href elem)} (:title elem)]])

(defn- articles-edn->hiccup-menu [edn]
  [:ul
   (conj
     (menu-link {:title "Home" :href "/"})
     (map menu-link edn))])

(defn- generate-menu-navi
  "Generates menu navigation structure."
  [path]
  (-> path
    (utils/parse-edn-resource nil)
    :articles
    articles-edn->hiccup-menu))

(defn- disqus-js [page-id]
  (str
    "var disqus_config = function () {
    // todo try if ommiting this will be annoying:
    // this.page.url = 'http://www.franky-canonical-fqdn.com/';
    this.page.identifier = '" page-id "';
    };

    (function() {  // DON'T EDIT BELOW THIS LINE
    var d = document, s = d.createElement('script');
    s.src = '//frankysblogiseq.disqus.com/embed.js';
    s.setAttribute('data-timestamp', +new Date());
    (d.head || d.body).appendChild(s);
    })();"))

(defn- embed-disqus
  "Code for embedding disqus."
  [page-id]
  [:div
   [:div#disqus_thread]
   [:div "<script id=\"dsq-count-scr\" src=\"//frankysblogiseq.disqus.com/count.js\" async></script>"]
   (hiccup-element/javascript-tag
     (disqus-js page-id))
   [:noscript "Please enable JS to see the discussion (disqus)."]])

(defn- include-css-resources []
  (->>
    (utils/filenames-with-extension "resources/css" "css")
    (map #(hiccup-page/include-css (str "/css/" %)))
  ))

(defn- include-js-resources [] ; todo refactor with ^
  (->>
    (utils/filenames-with-extension "resources/js" "js")
    (map #(hiccup-page/include-js (str "/js/" %)))
  ))

(defn fill-template [template menu content]
  (postwalk-replace {:blogiseq-menu-placeholder menu
                                  :blogiseq-content-placeholder content}
                                 template))

(def default-layout ; default layout, if custom isn't found
  [:div.main
   [:div.menu :blogiseq-menu-placeholder]
   [:div.content :blogiseq-content-placeholder]])

(defn- site
  [content]
  [:html
   [:head
    (include-css-resources)
    (include-js-resources)
    (utils/swallow-exceptions
      "Can't parse misc_header.html file, ignoring."
      nil
      (utils/parse-resource "misc_header.html"))] ; include this as-is. this deserves some polishing
   [:body
    (fill-template
      (utils/parse-edn-resource "layout.edn" (str default-layout))
      (generate-menu-navi "meta.edn")
      content)]])

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

