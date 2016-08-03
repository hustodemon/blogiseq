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

(defn- menu-link [elem]
  [:li
   [:a.w3-hover-black {:href (:href elem)} (:title elem)]])

(defn- articles-edn->hiccup-menu
  [edn]
  [:ul
   (conj
     (menu-link {:title "Home" :href "/"})
     (map menu-link edn))])

(defn- generate-menu-navi
  "Generates menu navigation structure."
  [path]
  (-> path
    utils/parse-edn
    :articles
    articles-edn->hiccup-menu))

(defn- embed-disqus [page-id]
  [:div
   [:div#disqus_thread]
   [:div "<script id=\"dsq-count-scr\" src=\"//frankysblogiseq.disqus.com/count.js\" async></script>"]
   (hiccup-element/javascript-tag
     (clojure.string/replace
       (slurp "resources/js/disqus.js")
       "<PAGE_ID>"
       page-id))
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

(defn- site ; would be cool to externalize this too (to support user-defined layouts)
  [content]
  [:html
   [:head
    (include-css-resources)
    (include-js-resources)
    (slurp "resources/misc_header.html") ; include this as-is. this deserves some polishing
    [:body
     [:div.w3-content.w3-light-yellow
      [:nav#mySidenav.w3-sidenav.w3-light-yellow.w3-collapse {:style "z-index:3;width:250px;margin-top:51px;background-color:#fffbc7;"}
       [:a {:title "Close menu"
            :class "w3-right w3-xlarge w3-padding-large w3-hover-black w3-hide-large"
            :onclick "w3_close()"
            :href "javascript:void(0)"}
        "Close menu"]
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
                                                    "MENU"]]
                                                  content]]
      ]]]])

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

