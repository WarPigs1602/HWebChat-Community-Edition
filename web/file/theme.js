/*
 * Theme switch (dark/light). The theme is kept in the "theme" cookie. The server
 * reads it when it delivers the style sheets, so every page and every frame of
 * the chat uses the same theme. Changing the theme re-loads the style sheets of
 * the page and of its frames, no page reload is needed.
 */
(function () {
  var COOKIE = 'theme';
  var DEFAULT_THEME = 'light';

  function readTheme() {
    var m = document.cookie.match(/(?:^|;\s*)theme=([^;]+)/);
    return m && m[1] === 'dark' ? 'dark' : DEFAULT_THEME;
  }

  function reloadStyles(doc) {
    var links = doc.querySelectorAll('link[rel="stylesheet"]');
    Array.prototype.forEach.call(links, function (link) {
      if (link.href.indexOf('service=style') < 0) {
        return;
      }
      var url = new URL(link.href, doc.baseURI);
      url.searchParams.set('_t', Date.now());
      var fresh = link.cloneNode();
      fresh.href = url.toString();
      // keep the old style sheet until the new one is active (no flicker)
      fresh.onload = function () { link.remove(); };
      fresh.onerror = fresh.onload;
      link.parentNode.insertBefore(fresh, link.nextSibling);
    });
  }

  function markActive(theme) {
    var options = document.querySelectorAll('[data-theme-option]');
    Array.prototype.forEach.call(options, function (option) {
      option.classList.toggle('active', option.getAttribute('data-theme-option') === theme);
    });
  }

  /*
   * Nick colors are chosen for the dark theme (light colors). In the light theme the
   * inline colors of the chat window and of the user list are darkened until they reach
   * a contrast of MIN_CONTRAST to their background; the chosen color is kept in
   * data-hwc-color and restored in the dark theme.
   */
  var MIN_CONTRAST = 4.5;
  var COLOR_ROOTS = '#chat_window, .user-list-nick-text';

  function parseRgb(value) {
    var m = /rgba?\(\s*(\d+)[,\s]+(\d+)[,\s]+(\d+)(?:[,\s/]+([\d.]+))?/.exec(value || '');
    return m ? [+m[1], +m[2], +m[3], m[4] === undefined ? 1 : +m[4]] : null;
  }

  function channel(v) {
    v /= 255;
    return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
  }

  function luminance(c) {
    return 0.2126 * channel(c[0]) + 0.7152 * channel(c[1]) + 0.0722 * channel(c[2]);
  }

  function contrast(a, b) {
    var l1 = luminance(a), l2 = luminance(b);
    return (Math.max(l1, l2) + 0.05) / (Math.min(l1, l2) + 0.05);
  }

  // darkest background of the light theme; the style sheets are not read because they load asynchronously
  var LIGHT_BACKGROUND = [233, 237, 243];

  // moves the color towards black until it is readable on the light background
  function readable(color) {
    for (var t = 0; t <= 1.0001; t += 0.05) {
      var c = [0, 1, 2].map(function (i) { return Math.round(color[i] * (1 - t)); });
      if (contrast(c, LIGHT_BACKGROUND) >= MIN_CONTRAST) {
        return c;
      }
    }
    return [0, 0, 0];
  }

  function adaptElement(el, theme) {
    var current = el.style.color;
    var saved = el.getAttribute('data-hwc-color');
    var ours = saved !== null && current === el.getAttribute('data-hwc-adapted');
    if (theme !== 'light') {
      if (saved !== null) {
        if (ours) {
          el.style.color = saved;
        }
        el.removeAttribute('data-hwc-color');
        el.removeAttribute('data-hwc-adapted');
      }
      return;
    }
    // rooms with their own colors (napping) are not changed
    if (!current || ours || document.documentElement.hasAttribute('data-room-colors')) {
      return;
    }
    var rgb = parseRgb(getComputedStyle(el).color);
    if (!rgb) {
      return;
    }
    var fixed = readable(rgb);
    el.setAttribute('data-hwc-color', current);
    el.style.color = 'rgb(' + fixed.join(',') + ')';
    el.setAttribute('data-hwc-adapted', el.style.color);
  }

  function adaptColors(root, theme) {
    if (!root || root.nodeType !== 1) {
      return;
    }
    var scopes = [];
    if (root.matches(COLOR_ROOTS) || root.closest(COLOR_ROOTS)) {
      scopes.push(root);
    } else {
      scopes = Array.prototype.slice.call(root.querySelectorAll(COLOR_ROOTS));
    }
    scopes.forEach(function (scope) {
      adaptElement(scope, theme);
      Array.prototype.forEach.call(scope.querySelectorAll('[style*="color"]'), function (el) {
        adaptElement(el, theme);
      });
    });
  }

  function currentTheme() {
    return document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : DEFAULT_THEME;
  }

  function watchColors() {
    adaptColors(document.body, currentTheme());
    new MutationObserver(function (changes) {
      var theme = currentTheme();
      changes.forEach(function (change) {
        if (change.type === 'attributes') {
          adaptElement(change.target, theme);
        } else {
          Array.prototype.forEach.call(change.addedNodes, function (node) {
            adaptColors(node, theme);
          });
        }
      });
    }).observe(document.body, {childList: true, subtree: true, attributes: true, attributeFilter: ['style']});
  }

  window.hwcApplyTheme = function (theme) {
    theme = theme === 'dark' ? 'dark' : DEFAULT_THEME;
    document.documentElement.setAttribute('data-theme', theme);
    markActive(theme);
    reloadStyles(document);
    adaptColors(document.body, theme);
    Array.prototype.forEach.call(document.querySelectorAll('iframe'), function (frame) {
      try {
        if (frame.contentWindow && frame.contentWindow.hwcApplyTheme) {
          frame.contentWindow.hwcApplyTheme(theme);
        }
      } catch (ex) {
        // frame of another origin
      }
    });
  };

  window.setTheme = function (theme) {
    theme = theme === 'dark' ? 'dark' : DEFAULT_THEME;
    document.cookie = COOKIE + '=' + theme + ';path=/;max-age=' + 60 * 60 * 24 * 365 + ';SameSite=Lax';
    window.hwcApplyTheme(theme);
  };

  document.addEventListener('DOMContentLoaded', function () {
    markActive(currentTheme());
    watchColors();
  });
})();
