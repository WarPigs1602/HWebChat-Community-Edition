<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1>${SERVER_SOFTWARE}-401</h1>
<p>
<fmt:message key="console.401.you_are_not_authorized_to"/><br>
<fmt:message key="console.401.in_order_to_use_this"/>!
</p>
</t:layout>