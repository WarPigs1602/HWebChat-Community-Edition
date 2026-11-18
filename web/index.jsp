<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" import="net.midiandmore.chat.Web" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><% request.setAttribute("lang", Web.language(request)); %><fmt:setLocale value="${lang}" scope="request"/><fmt:setBundle basename="i18n.messages" scope="request"/>
<c:url var="startUrl" value="/start.jsp">
    <c:if test="${not empty param.lang}"><c:param name="lang" value="${param.lang}"/></c:if>
</c:url>
<!DOCTYPE html>
<html lang="${lang}">
    <head>
        <title><fmt:message key="web.index.title"/></title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="refresh" content="0; url=<c:out value="${startUrl}"/>">
    </head>
    <body>
        <script>
            self.location.href = "<c:out value="${startUrl}"/>";
        </script>
        <noscript>
            <p><fmt:message key="web.index.text"/> <a href="<c:out value="${startUrl}"/>"><fmt:message key="web.index.link"/></a></p>
        </noscript>
    </body>
</html>
