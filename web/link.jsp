<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }
    // Only addresses of these kinds are forwarded
    String url = request.getParameter("url");
    if (url == null || !(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://")
            || url.startsWith("irc://") || url.startsWith("mailto:"))) {
        url = "about:blank";
    }
    pageContext.setAttribute("link", Web.parseHtml(Web.decodeHtml(url)));
%><t:layout design="link"><h1><fmt:message key="page.link.forwarding"/></h1>

<p>
  <fmt:message key="page.link.you_will_be_automatically_redirected"><fmt:param value="${link}"/></fmt:message>
</p>
<p>
  <fmt:message key="page.link.if_this_does_not_work"/> <a href="${link}"><fmt:message key="common.here"/></a> 
</p>

<script language="javascript">redirect("${link}");</script>
</t:layout>
