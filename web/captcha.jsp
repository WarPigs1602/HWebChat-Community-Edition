<%@ page session="false" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.Web" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%
    Web.start(application);
    if (!Web.begin("plain", request, response)) {
        return;
    }
    // The captcha is an image: nothing else may be written when it was drawn
    if (!Web.drawCaptcha(request, response)) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("text/html; charset=UTF-8");
%><!DOCTYPE html>
<html lang="<fmt:message key="common.lang"/>">
<head><meta charset="UTF-8"><title><fmt:message key="system.captcha_unavailable.title"/></title></head>
<body><h1><fmt:message key="system.captcha_unavailable.title"/></h1><p><fmt:message key="system.captcha_unavailable.text"/></p></body>
</html>
<%
    }
%>