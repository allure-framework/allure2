;(function () {
  'use strict';
  // Iframe resizer
  function measureIframe(iframe) {
    try {
      var doc = iframe.contentDocument || (iframe.contentWindow && iframe.contentWindow.document);
      if (!doc) return;
      var body = doc.body, html = doc.documentElement;
      var h = Math.max(
        (body && body.scrollHeight) || 0,
        (html && html.scrollHeight) || 0,
        (body && body.offsetHeight) || 0,
        (html && html.offsetHeight) || 0
      );
      iframe.style.width = '100%';
      var fh = Math.max(h || 0, 20);
      if (iframe.style.height !== fh + 'px') iframe.style.height = fh + 'px';
    } catch (e) {
      // ignore cross-origin access errors
    }
  }

  function measureAll() {
    try {
      var all = document.querySelectorAll('.attachment__iframe');
      for (var i = 0; i < all.length; i++) {
        measureIframe(all[i]);
      }
    } catch (e) {}
  }

  function attachListeners() {
    try {
      var iframes = document.querySelectorAll('.attachment__iframe');
      for (var i = 0; i < iframes.length; i++) {
        var f = iframes[i];
        if (f.dataset._resizerAttached) continue;
        f.dataset._resizerAttached = '1';
        try {
          f.addEventListener('load', function (ev) {
            measureIframe(ev.currentTarget);
            // transient polling in case the iframe applies late layout
            var tries = 0, max = 6;
            var iv = setInterval(function () { try { measureIframe(ev.currentTarget); } catch (e) {} tries++; if (tries >= max) clearInterval(iv); }, 400);
          });
        } catch (e) {}
      }
    } catch (e) {}
  }

  function observeDom() {
    try {
      if (document.body) {
        var mo = new MutationObserver(function (entries) {
          entries.forEach(function (en) {
            try {
              if (en.addedNodes && en.addedNodes.length) {
                attachListeners();
                measureAll();
              }
            } catch (e) {}
          });
        });
        mo.observe(document.body, { childList: true, subtree: true });
      }
    } catch (e) {}
  }

  // initial wiring
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () { attachListeners(); observeDom(); measureAll(); });
  } else {
    attachListeners(); observeDom(); measureAll();
  }

})();
