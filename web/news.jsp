<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" import="java.sql.SQLException, net.midiandmore.chat.Web" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }
    try {
        request.setAttribute("newsArticles", Web.newsArticles());
    } catch (SQLException ex) {
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        return;
    }
%><t:layout design="start"><main class="container">
  <h1><fmt:message key="news.title"/></h1>
  <c:choose>
    <c:when test="${empty newsArticles}"><p><fmt:message key="news.empty"/></p></c:when>
    <c:otherwise><c:forEach items="${newsArticles}" var="article">
      <article class="card mb-3"><div class="card-body">
        <h2 class="h4"><c:out value="${article.title}"/></h2>
        <p class="text-muted"><c:out value="${article.author}"/> &middot; <c:out value="${article.published}"/></p>
        <div style="white-space: pre-wrap"><c:out value="${article.body}"/></div>
      </div></article>
    </c:forEach></c:otherwise>
  </c:choose>
</main></t:layout>
