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

a {
  color: var(--hwc-link);
  text-decoration: none;
}

a:hover {
  color: var(--hwc-primary-dark);
  text-decoration: underline;
}

p {
  margin: 0.5rem 0;
}

p a {
  color: var(--hwc-link);
}

p a:hover {
  color: var(--hwc-primary-dark);
  text-decoration: underline;
}

input, textarea, .form-control {
  border: 1px solid var(--hwc-border);
  border-radius: 0.5rem;
  padding: 0.45rem 0.75rem;
  font-size: 0.9rem;
  color: var(--hwc-dark);
  background: var(--hwc-surface);
}

input:focus, textarea:focus, .form-control:focus {
  border-color: var(--hwc-link);
  outline: 0;
  box-shadow: 0 0 0 0.15rem rgba(13, 110, 253, 0.15);
}

textarea {
  resize: vertical;
  width: 100%;
  min-height: 12rem;
}

h1 {
  font-weight: 700;
  font-size: 1.5rem;
  margin: 0.5rem 0;
}

h2 {
  font-weight: 600;
  font-size: 1.25rem;
  margin: 0.5rem 0;
}

h3 {
  font-weight: 600;
  font-size: 1rem;
  margin: 0.5rem 0;
}

.container-fluid.p-0.d-flex.flex-column {
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

.post_field {
  border: none;
  font-size: 0.875rem;
  text-align: left;
  width: 100%;
}

.post_button {
  border-radius: 0.5rem;
  font-size: 0.875rem;
  background-color: var(--hwc-link);
  color: #fff;
  border: 1px solid var(--hwc-primary);
  padding: 0.45rem 0.75rem;
}

.post_button:hover {
  background-color: var(--hwc-primary-dark);
  border-color: var(--hwc-primary-dark);
}

.emoji-list-group {
  display: contents;
}

.emoji-wrapper {
  font-family: system-ui, "Apple Color Emoji", "NotoColorEmoji", "TwitterColorEmoji", sans-serif;
}

.emoji {
  font-size: 1.5rem;
  width: 3rem;
  height: 3rem;
  padding: 0.25rem 0.5rem;
  margin: 0;
  border: none;
  background: transparent;
  border-radius: 0.4rem;
  cursor: pointer;
}

.emoji:hover {
  background: #eef4ff;
}

.color_field {
  width: 6rem;
  font-family: SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}

.hidden {
  display: none !important;
}

.no-select, .tooltip, .bigbaddragons, .dialog:not(.active) {
  -webkit-user-select: none;
  user-select: none;
}

.allow-select {
  -webkit-user-select: text;
  user-select: text;
}

#pagecontent {
  font-size: 0.95rem;
  width: min(1100px, calc(100% - 2rem));
  margin: 0 auto 1.25rem;
  padding: 1.5rem;
  text-align: left;
  color: var(--hwc-dark);
  background: var(--hwc-surface);
  border: 1px solid var(--hwc-border);
  border-radius: var(--hwc-radius);
  box-shadow: 0 0.25rem 0.75rem rgba(33, 37, 41, 0.08);
}

@media (max-width: 767.98px) {
  .chat_frame {
    padding: 0.65rem 0.75rem;
  }

  .post_frame {
    padding: 0.5rem !important;
  }
}

cw {
  display: block;
  width: 100%;
}

#pagecontent > h1:first-child,
#profilecontent > h1:first-child {
  margin-top: 0;
  margin-bottom: 1.25rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--hwc-border);
}

#pagecontent .card,
#profilecontent .card {
  overflow: hidden;
  border: 1px solid var(--hwc-border);
  border-radius: var(--hwc-radius);
  box-shadow: 0 2px 8px rgba(33, 37, 41, 0.05);
}

#pagecontent .card-header,
#profilecontent .card-header {
  padding: 0.85rem 1rem;
  background: #f7f9fc;
  border-bottom-color: var(--hwc-border);
}

#pagecontent .btn,
#profilecontent .btn {
  border-radius: 0.5rem;
  font-weight: 600;
  transition: background-color 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;
}

#pagecontent a:focus-visible,
#profilecontent a:focus-visible,
#pagecontent .btn:focus-visible,
#profilecontent .btn:focus-visible {
  outline: 2px solid var(--hwc-primary);
  outline-offset: 2px;
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
