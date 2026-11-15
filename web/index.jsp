<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fn" uri="jakarta.tags.functions" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><c:choose><c:when test="${not empty cookie.lang.value}"><c:set var="requestedLang" value="${cookie.lang.value}"/></c:when><c:when test="${not empty param.lang}"><c:set var="requestedLang" value="${param.lang}"/></c:when><c:when test="${fn:startsWith(fn:toLowerCase(header['Accept-Language']), 'de')}"><c:set var="requestedLang" value="de"/></c:when><c:otherwise><c:set var="requestedLang" value="en"/></c:otherwise></c:choose><c:set var="lang" value="${fn:toLowerCase(requestedLang) eq 'de' ? 'de' : 'en'}" scope="request"/><fmt:setLocale value="${lang}" scope="request"/><fmt:setBundle basename="i18n.messages" scope="request"/>
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
