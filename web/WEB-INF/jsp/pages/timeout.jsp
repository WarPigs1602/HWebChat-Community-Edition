<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><h1>${SERVER_SOFTWARE}-Timeout</h1>


<p>
<fmt:message key="page.timeout.your_session_expired_because_of"/>...
</p>
</t:layout>