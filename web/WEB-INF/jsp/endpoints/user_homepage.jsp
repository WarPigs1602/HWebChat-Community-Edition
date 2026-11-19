<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.*, java.util.*, jakarta.servlet.http.*" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%
    Web.start(application);
    if (!Web.isModuleEnabled("user_homepage")) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    if (!"GET".equalsIgnoreCase(request.getMethod())) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    if (!Web.begin("page", request, response)) {
        return;
    }
    var nick = (String) request.getAttribute("user_homepage_nick");
    var db = Bootstrap.getBoot().getConfig().getDb();
    if (nick == null || nick.isBlank() || !db.isRegistered(nick)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    String content;
    try {
        content = db.getUserHomepageContent(nick);
    } catch (java.sql.SQLException ex) {
        Bootstrap.logError(ex);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
    }
    if (content == null || content.isBlank()) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    Web.view("page", request, Map.of("user", nick));
    request.setAttribute("user_homepage_nick", nick);
    request.setAttribute("homepage_content", CustomPageContent.sanitize(content));
%>
<t:layout design="profile">
  <h1><c:out value="${user_homepage_nick}"/></h1>
  <div class="user-homepage-content"><c:out value="${homepage_content}" escapeXml="false"/></div>
</t:layout>
