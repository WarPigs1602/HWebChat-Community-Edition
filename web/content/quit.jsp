<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("document", request, response)) { return; } %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><h1>
<fmt:message key="common.note_3"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p>
<fmt:message key="common.you_left_the_chat"/>
</p>
<p>
<fmt:message key="common.as_a_reason_you_left"/> <span style="font-weight: bold">${reason}</span> 
</p>
</t:layout>