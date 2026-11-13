<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><h1>
<fmt:message key="page.internal_error.error_500"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<fmt:message key="page.internal_error.there_was_an_internal_server"/><br>
<fmt:message key="page.internal_error.the_following_error_message_was"/>...<br>
Guru Meditation: <span sytle="font-weight: bold>${error_message}</span>
</p>
<p>
<fmt:message key="page.internal_error.if_this_error_occurs_repeatedly"/>
</p>
</t:layout>