<%@ page contentType="text/css; charset=UTF-8" session="false" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("style", request, response)) { return; } response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); response.setHeader("Pragma", "no-cache"); response.setDateHeader("Expires", 0); %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:css>:root {
  --hwc-primary: ${not empty theme_primary ? theme_primary : '#0d6efd'};
  --hwc-primary-dark: ${not empty theme_primary_dark ? theme_primary_dark : '#0a58ca'};
  --hwc-link: ${not empty theme_link ? theme_link : (not empty theme_primary ? theme_primary : '#0d6efd')};
  --hwc-dark: ${not empty theme_dark ? theme_dark : '#212529'};
  --hwc-muted: ${not empty theme_muted ? theme_muted : '#6c757d'};
  --hwc-border: ${not empty theme_border ? theme_border : '#dee2e6'};
  --hwc-bg: ${not empty theme_bg ? theme_bg : '#f0f2f5'};
  --hwc-surface: ${not empty theme_surface ? theme_surface : '#ffffff'};
  --hwc-radius: ${not empty theme_radius ? theme_radius : '0.75rem'};
  --hwc-font-size: ${not empty theme_font_size ? theme_font_size : '0.925rem'};
  --hwc-font-family: ${not empty theme_font_family ? theme_font_family : 'system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif'};
  --hwc-nav-bg: ${not empty theme_nav_bg ? theme_nav_bg : '#212529'};
  --hwc-nav-text: ${not empty theme_nav_text ? theme_nav_text : '#ffffff'};
  --hwc-success: ${not empty theme_success ? theme_success : '#198754'};
  --hwc-warning: ${not empty theme_warning ? theme_warning : '#ffc107'};
  --hwc-danger: ${not empty theme_danger ? theme_danger : '#dc3545'};
}

html, body {
  height: 100vh;
  margin: 0;
  padding: 0;
  color: var(--hwc-dark);
  background: <c:choose><c:when test="${theme_body_gradient eq '0'}">var(--hwc-bg)</c:when><c:otherwise>linear-gradient(180deg, var(--hwc-bg) 0%, var(--hwc-surface) 100%)</c:otherwise></c:choose>;
  background-color: var(--hwc-bg);
  font-family: var(--hwc-font-family), "Apple Color Emoji", "Segoe UI Emoji", "Noto Color Emoji";
  font-size: var(--hwc-font-size);
  overflow: hidden;
}

body {
  display: flex;
  flex-direction: column;
}

body > .navbar {
  flex: 0 0 auto;
  margin: 0 !important;
  border-radius: 0 !important;
  background: var(--hwc-nav-bg) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 200;
}

body > .navbar .nav-link, body > .navbar .navbar-brand {
  color: var(--hwc-nav-text) !important;
}

body > .navbar .nav-link:hover, body > .navbar .nav-item.active .nav-link {
  color: var(--hwc-nav-text) !important;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 0.4rem;
  text-decoration: none;
}

body > .navbar .nav-link i { color: var(--hwc-nav-text) !important; opacity: 0.85; }

body > .navbar .navbar-brand img {
  height: 36px;
  border-radius: 0.35rem;
}

.chat-shell,
body > .container-fluid.p-0.d-flex.flex-column,
.container-fluid.p-0.d-flex.flex-column {
  flex: 1 1 auto;
  min-height: 0 !important;
  height: auto !important;
  display: flex !important;
  flex-direction: column !important;
  background: var(--hwc-bg);
}

.chat-main,
.chat-shell > .row.no-gutters.flex-grow-1,
body > .container-fluid.p-0.d-flex.flex-column > .row.no-gutters.flex-grow-1,
.row.no-gutters.flex-grow-1 {
  flex: 1 1 auto !important;
  min-height: 0 !important;
  display: flex !important;
  flex-direction: row !important;
  flex-wrap: nowrap !important;
  margin: 0 !important;
  overflow: hidden;
}

.chat_frame {
  flex: 1 1 0;
  min-width: 0;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  background: var(--hwc-surface);
  border: none;
  border-right: 1px solid var(--hwc-border);
  border-radius: 0;
  margin: 0;
  padding: 0.85rem 1rem;
  box-shadow: none;
  position: relative !important;
  top: auto !important;
  left: auto !important;
  right: auto !important;
  bottom: auto !important;
  text-align: left;
  font-size: 0.925rem;
  line-height: 1.45;
}

.chat_frame p {
  margin: 0.2rem 0;
  word-wrap: break-word;
  overflow-wrap: anywhere;
}

.chat_frame a {
  color: var(--hwc-primary);
  text-decoration: none;
}

.chat_frame a:hover {
  color: var(--hwc-primary-dark);
  text-decoration: underline;
}

.right_frame {
  flex: 0 0 260px;
  width: 260px;
  min-width: 220px;
  max-width: 320px;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  background: var(--hwc-bg);
  border: none;
  border-left: 1px solid var(--hwc-border);
  border-radius: 0;
  margin: 0;
  padding: 0.75rem;
  box-shadow: none;
  position: relative !important;
  top: auto !important;
  right: auto !important;
  bottom: auto !important;
  font-size: 0.875rem;
}

.right_frame a {
  color: var(--hwc-primary);
  text-decoration: none;
}

.right_frame a:hover {
  color: var(--hwc-primary-dark);
  text-decoration: underline;
}

.post_frame {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  height: auto !important;
  min-height: 64px;
  margin: 0 !important;
  padding: 0.65rem 0.85rem !important;
  background: var(--hwc-surface);
  border-top: 1px solid var(--hwc-border);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
  position: relative !important;
  left: auto !important;
  right: auto !important;
  bottom: auto !important;
}

.post_frame .form-control {
  flex: 1 1 auto;
  min-width: 0;
  height: 38px;
  border-radius: 0.5rem;
  border: 1px solid var(--hwc-border);
  background: var(--hwc-bg);
}

.post_frame .form-control:focus {
  background: var(--hwc-surface);
  border-color: var(--hwc-primary);
  box-shadow: 0 0 0 0.15rem rgba(0, 0, 0, 0.1);
}

.post_frame .btn {
  border-radius: 0.5rem;
  min-width: 4.5rem;
}

.post_frame .input-group-append,
.post_frame .input-group-prepend {
  display: flex;
  align-items: center;
}

cw {
  display: block;
  width: 100%;
}

.navbar .dropdown-menu {
  min-width: 220px;
  border-radius: 0.65rem;
  border: 1px solid var(--hwc-border);
  box-shadow: 0 0.5rem 1.25rem rgba(0, 0, 0, 0.12);
}

#colorpicker {
  padding: 0.5rem;
}

.color_field {
  font-family: SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}

@media (max-width: 767.98px) {
  .right_frame {
    flex: 0 0 180px;
    width: 180px;
    min-width: 160px;
    padding: 0.5rem;
  }

  .chat_frame {
    padding: 0.65rem 0.75rem;
  }

  .post_frame {
    padding: 0.5rem !important;
  }
}

.chat-frame-container {
  --tabbar-height: 38px;
  flex: 1 1 0;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--hwc-surface);
  border-right: 1px solid var(--hwc-border);
  position: relative;
}

.chat-frame-container .chat_frame {
  flex: 1 1 0;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  border: none;
  border-radius: 0;
  margin: 0;
  padding: 0.85rem 1rem;
  position: relative;
  background: transparent;
}

.chat-tabs {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.4rem 0.65rem;
  background: var(--hwc-surface);
  border-bottom: 1px solid var(--hwc-border);
  overflow-x: auto;
  min-height: var(--tabbar-height);
  scrollbar-width: none;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.chat-tabs::-webkit-scrollbar {
  display: none;
}

.chat-tab {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.3rem 0.75rem;
  background: var(--hwc-bg);
  border: 1px solid var(--hwc-border);
  border-radius: 0.5rem;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--hwc-dark);
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s ease, border-color 0.15s ease, color 0.15s ease;
  user-select: none;
}

.chat-tab:hover {
  background: var(--hwc-bg);
  border-color: var(--hwc-border);
}

.chat-tab.active {
  background: var(--hwc-primary);
  border-color: var(--hwc-primary);
  color: var(--hwc-primary-text, #ffffff);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.chat-tab-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.1rem;
  height: 1.1rem;
  padding: 0;
  margin: 0 -0.15rem 0 0.1rem;
  background: transparent;
  border: none;
  border-radius: 50%;
  color: inherit;
  font-size: 1rem;
  line-height: 1;
  cursor: pointer;
  opacity: 0.75;
  transition: opacity 0.15s ease, background 0.15s ease;
}

.chat-tab-close:hover {
  opacity: 1;
  background: rgba(0, 0, 0, 0.15);
}

.chat-panel-overlay {
  position: absolute;
  top: var(--tabbar-height);
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--hwc-surface);
  z-index: 50;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 0;
}

.chat-panel-iframe {
  flex: 1 1 auto;
  width: 100%;
  border: none;
  min-height: 0;
  background: var(--hwc-surface);
}

/* ---------- System-Broadcast (/sys) im Chatstream ---------- */
.chat-sys-broadcast {
  margin: 0.5rem 0;
  border: 1px solid #b6d4fe;
  border-left: 4px solid var(--hwc-primary);
  border-radius: var(--hwc-radius);
  background: #eef5ff;
  box-shadow: 0 1px 3px rgba(13, 110, 253, 0.08);
  overflow: hidden;
}

.chat-sys-broadcast-header {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.7rem;
  background: var(--hwc-primary);
  color: #ffffff;
  font-weight: 600;
  font-size: 0.85rem;
}

.chat-sys-broadcast-body {
  padding: 0.55rem 0.75rem;
  color: #0b2e59;
  line-height: 1.45;
  word-wrap: break-word;
  overflow-wrap: anywhere;
}

.chat-sys-broadcast-body a {
  color: var(--hwc-primary-dark);
  text-decoration: underline;
}

/* ---------- Info-Karte (/info) im Chatstream ---------- */
.chat-info-card {
  margin: 0.5rem 0;
  border: 1px solid #b6d4fe;
  border-radius: var(--hwc-radius);
  background: var(--hwc-surface);
  box-shadow: 0 1px 3px rgba(13, 110, 253, 0.08);
  overflow: hidden;
}

.chat-info-card-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.55rem 0.85rem;
  background: linear-gradient(90deg, var(--hwc-primary) 0%, var(--hwc-primary-dark) 100%);
  color: #ffffff;
}

.chat-info-card-copyright {
  color: #495057;
  background: #f6f9ff;
}

.chat-info-card-section {
  border-top: 1px solid #e3e9f2;
}

.chat-info-card-label {
  color: var(--hwc-primary);
}

.chat-info-card-body {
  color: #0b2e59;
}

.chat-info-card-empty {
  color: #6c757d;
  font-style: italic;
}

.chat-info-card a {
  color: #0a58ca;
  text-decoration: underline;
}

.chat-info-card-header a {
  color: #ffffff;
}

html,
body {
  background-color: #f3f6fa;
}

body > .navbar {
  background: var(--hwc-nav-bg) !important;
  border-bottom: 1px solid rgba(148, 163, 184, 0.25);
}

body > .navbar .nav-link {
  border-radius: 0.55rem;
  transition: background-color 0.15s ease, color 0.15s ease;
}

body > .navbar .nav-link:focus-visible {
  outline: 2px solid #93c5fd;
  outline-offset: 2px;
}

.chat-shell,
.chat-privchat-shell,
body > .container-fluid.p-0.d-flex.flex-column {
  background: var(--hwc-bg);
}

.chat-frame-container {
  background: var(--hwc-surface);
  border-right-color: var(--hwc-border);
}

.chat_frame {
  scrollbar-color: var(--hwc-border) transparent;
  scrollbar-width: thin;
}

.chat_frame::-webkit-scrollbar,
.right_frame::-webkit-scrollbar {
  width: 8px;
}

.chat_frame::-webkit-scrollbar-thumb,
.right_frame::-webkit-scrollbar-thumb {
  background: var(--hwc-border);
  border: 2px solid transparent;
  border-radius: 8px;
  background-clip: padding-box;
}

.right_frame {
  background: var(--hwc-bg);
  border-left-color: var(--hwc-border);
}

.chat-tabs {
  padding: 0.45rem 0.75rem;
  background: var(--hwc-bg);
  border-bottom-color: var(--hwc-border);
}

.chat-tab {
  min-height: 2rem;
  border-radius: 0.55rem;
}

.post_frame {
  gap: 0.65rem;
  padding: 0.75rem 1rem !important;
  background: var(--hwc-surface);
  border-top-color: var(--hwc-border);
  box-shadow: 0 -4px 14px rgba(27, 39, 55, 0.06);
}

.post_frame .form-control {
  background: var(--hwc-bg);
}

.post_frame .btn {
  min-height: 2.4rem;
  font-weight: 600;
}

@media (prefers-reduced-motion: reduce) {
  .chat-tab,
  .chat-tab-close,
  .post_frame .btn {
    transition: none;
  }
}

/* Theme overrides */
a { color: var(--hwc-link); }
a:hover { color: var(--hwc-primary-dark); }
body > .navbar, .navbar.bg-dark, .navbar-dark.bg-dark { background: var(--hwc-nav-bg) !important; background-color: var(--hwc-nav-bg) !important; }
body > .navbar .nav-link, .navbar-dark .navbar-nav .nav-link, .navbar-dark .navbar-brand, body > .navbar .navbar-brand { color: var(--hwc-nav-text) !important; }
.btn-primary, .btn.btn-primary, .post_frame .btn.btn-primary { background-color: var(--hwc-primary) !important; border-color: var(--hwc-primary) !important; color: #fff !important; }
.btn-primary:hover, .btn.btn-primary:hover, .btn-primary:focus, .btn-primary:active { background-color: var(--hwc-primary-dark) !important; border-color: var(--hwc-primary-dark) !important; color: #fff !important; }
.btn-secondary, .btn.btn-secondary { background-color: var(--hwc-primary) !important; border-color: var(--hwc-primary) !important; color: #fff !important; }
.btn-secondary:hover, .btn.btn-secondary:hover { background-color: var(--hwc-primary-dark) !important; border-color: var(--hwc-primary-dark) !important; color: #fff !important; }
.btn-success, .btn.btn-success { background-color: var(--hwc-success) !important; border-color: var(--hwc-success) !important; color: #fff !important; }
.btn-warning, .btn.btn-warning { background-color: var(--hwc-warning) !important; border-color: var(--hwc-warning) !important; color: #212529 !important; }
.btn-danger, .btn.btn-danger { background-color: var(--hwc-danger) !important; border-color: var(--hwc-danger) !important; color: #fff !important; }
.btn-outline-primary { color: var(--hwc-primary) !important; border-color: var(--hwc-primary) !important; background: transparent !important; }
.btn-outline-primary:hover { background-color: var(--hwc-primary) !important; color: #fff !important; }
input[type="button"].btn, input[type="submit"].btn, button.btn, .btn { border-radius: calc(var(--hwc-radius) * 0.67); }
.chat-tab.active { background: var(--hwc-primary) !important; border-color: var(--hwc-primary) !important; color: #fff !important; }
.post_frame .form-control { border-color: var(--hwc-border); background: var(--hwc-bg); color: var(--hwc-dark); }
.post_frame .form-control:focus { border-color: var(--hwc-primary); }
.text-muted { color: var(--hwc-muted) !important; }
<c:if test="${not empty theme_custom_css}">${theme_custom_css}</c:if>

</t:css>
