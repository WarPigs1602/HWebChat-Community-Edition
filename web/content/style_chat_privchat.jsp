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
  background-color: var(--hwc-bg);
  font-family: var(--hwc-font-family), "Apple Color Emoji", "Segoe UI Emoji", "Noto Color Emoji";
  font-size: var(--hwc-font-size);
  overflow: hidden;
}

body {
  display: flex;
  flex-direction: column;
}

.chat-shell,
body > .container-fluid.p-0.d-flex.flex-column {
  flex: 1 1 auto;
  min-height: 0;
  display: flex !important;
  flex-direction: column !important;
  background: var(--hwc-bg);
}

.chat_frame {
  flex: 1 1 0;
  min-width: 0;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  background: var(--hwc-surface);
  border: none;
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
  font-size: var(--hwc-font-size);
  line-height: 1.45;
}

.chat_frame p {
  margin: 0.2rem 0;
  word-wrap: break-word;
  overflow-wrap: anywhere;
}

.chat_frame a {
  color: var(--hwc-link);
  text-decoration: none;
}

.chat_frame a:hover {
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
  border-color: var(--hwc-link);
  box-shadow: 0 0 0 0.15rem rgba(13, 110, 253, 0.15);
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

html,
body {
  background-color: #f3f6fa;
}

body > .navbar {
  background: linear-gradient(90deg, #1a1f25 0%, #2b3035 100%) !important;
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
  background: #eef2f7;
}

.chat_frame {
  scrollbar-color: #c4cfdd transparent;
  scrollbar-width: thin;
}

.chat_frame::-webkit-scrollbar,
.right_frame::-webkit-scrollbar {
  width: 8px;
}

.chat_frame::-webkit-scrollbar-thumb,
.right_frame::-webkit-scrollbar-thumb {
  background: #c4cfdd;
  border: 2px solid transparent;
  border-radius: 8px;
  background-clip: padding-box;
}

.right_frame {
  background: #f7f9fc;
  border-left-color: #dce3ed;
}

.post_frame {
  gap: 0.65rem;
  padding: 0.75rem 1rem !important;
  background: var(--hwc-surface);
  border-top-color: #dce3ed;
  box-shadow: 0 -4px 14px rgba(27, 39, 55, 0.06);
}

.post_frame .form-control {
  background: #f7f9fc;
}

.post_frame .btn {
  min-height: 2.4rem;
  font-weight: 600;
}

@media (prefers-reduced-motion: reduce) {
  .post_frame .btn {
    transition: none;
  }
}

@media (max-width: 767.98px) {
  .chat_frame {
    padding: 0.65rem 0.75rem;
  }

  .post_frame {
    padding: 0.5rem !important;
  }
}

/* Theme overrides */
body { background: <c:choose><c:when test="${theme_body_gradient eq '0'}">var(--hwc-bg)</c:when><c:otherwise>linear-gradient(180deg, var(--hwc-bg) 0%, var(--hwc-surface) 100%)</c:otherwise></c:choose> !important; }
a { color: var(--hwc-link); }
.btn-primary, .btn.btn-primary { background-color: var(--hwc-primary) !important; border-color: var(--hwc-primary) !important; color: #fff !important; }
.btn-primary:hover, .btn.btn-primary:hover { background-color: var(--hwc-primary-dark) !important; border-color: var(--hwc-primary-dark) !important; }
.btn-secondary, .btn.btn-secondary { background-color: var(--hwc-primary) !important; border-color: var(--hwc-primary) !important; color: #fff !important; }
.btn-secondary:hover, .btn.btn-secondary:hover { background-color: var(--hwc-primary-dark) !important; border-color: var(--hwc-primary-dark) !important; }
.btn-success, .btn.btn-success { background-color: var(--hwc-success) !important; border-color: var(--hwc-success) !important; color: #fff !important; }
.btn-warning, .btn.btn-warning { background-color: var(--hwc-warning) !important; border-color: var(--hwc-warning) !important; color: #212529 !important; }
.btn-danger, .btn.btn-danger { background-color: var(--hwc-danger) !important; border-color: var(--hwc-danger) !important; color: #fff !important; }
.post_frame .btn { border-radius: calc(var(--hwc-radius) * 0.67); }
.post_frame .form-control { border-color: var(--hwc-border); background: var(--hwc-bg); color: var(--hwc-dark); }
.post_frame .form-control:focus { border-color: var(--hwc-primary); }
<c:if test="${not empty theme_custom_css}">${theme_custom_css}</c:if>

</t:css>
