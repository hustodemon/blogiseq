var disqus_config = function () {
    // todo try if ommiting this will be annoying:
    // this.page.url = 'http://www.franky-canonical-fqdn.com/';
    this.page.identifier = '<PAGE_ID>';
};

(function() {  // DON'T EDIT BELOW THIS LINE
    var d = document, s = d.createElement('script');
    s.src = '//frankysblogiseq.disqus.com/embed.js';
    s.setAttribute('data-timestamp', +new Date());
    (d.head || d.body).appendChild(s);
})();
