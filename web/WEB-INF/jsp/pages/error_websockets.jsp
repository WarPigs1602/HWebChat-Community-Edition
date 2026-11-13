<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><h1
${SERVER_SOFTWARE}-Fehler 400
</h1>
<p style="font-weight: bold;">
<fmt:message key="page.error_websockets.invalid_requirement"/>! 
</p>
<p>
<fmt:message key="page.error_websockets.the_url_you_requested_uses"/>!
</p>
</t:layout>