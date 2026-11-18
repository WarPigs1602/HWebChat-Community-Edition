<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.*, java.util.*, jakarta.servlet.http.HttpServletResponse" %><%
    Web.start(application);
    if (!Web.begin("document", request, response)) {
        return;
    }
    var slug = request.getParameter("slug");
    if (!CustomPageContent.isValidSlug(slug)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    var pages = Bootstrap.getBoot().getConfig().loadDataFromJSON("pages.json", "slug", "data");
    var data = pages.get(slug);
    if (data == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    String title;
    String content;
    try (var reader = jakarta.json.Json.createReader(new java.io.StringReader(data))) {
        var pageData = reader.readObject();
        title = pageData.getString("title");
        content = CustomPageContent.sanitize(pageData.getString("content"));
    } catch (RuntimeException ex) {
        Bootstrap.logError(ex);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
    }
    request.setAttribute("page_title", title);
    request.setAttribute("page_content", content);
%><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start">
  <h1><c:out value="${page_title}"/></h1>
  <c:out value="${page_content}" escapeXml="false"/>
</t:layout>
