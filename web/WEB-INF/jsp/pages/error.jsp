<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><h1>
<fmt:message key="page.error.error_404"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p style="font-weight: bold;">
<fmt:message key="page.error.unfortunately_the_indicated_page_does"/> 
</p>
<p>
<fmt:message key="page.error.if_you_have_entered_the"/>. 
</p>
</t:layout>