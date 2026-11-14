<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("document", request, response)) { return; } %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping"><h1>
<fmt:message key="common.note_3"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p>
<fmt:message key="common.there_was_a_relogin_and"/>...
</p>
<p>
<fmt:message key="common.if_this_message_occurs_unexpectedly"/><br>
In <fmt:message key="common.this_case_please_immediately_change"/>!
</p>
</t:layout>