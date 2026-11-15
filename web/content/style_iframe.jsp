<%@ page contentType="text/css; charset=UTF-8" session="false" isELIgnored="true" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("style", request, response)) { return; } %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:css>body {
  height: 100%;
  overflow-x: hidden;
  overflow-y: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
}</t:css>