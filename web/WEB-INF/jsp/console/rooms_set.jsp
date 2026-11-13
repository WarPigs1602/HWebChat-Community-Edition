<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="common.room_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<p>
<fmt:message key="console.rooms_set.space"/> <span style="font-weight: bold">${room}</span> <fmt:message key="console.rooms_set.it_was_created"/>
</span>
</t:layout>