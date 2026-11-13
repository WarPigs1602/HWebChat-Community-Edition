<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping"><h1>
<fmt:message key="common.note_3"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p style="text-align: left; font-weight: bold;">
<fmt:message key="common.you_were_removed_from_the"/>
</p>
<p>
<fmt:message key="common.this_measure_was_automatically_initiated"/>!
</p>
</t:layout>