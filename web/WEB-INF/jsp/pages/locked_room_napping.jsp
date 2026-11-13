<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping"><h1><fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


<p>
<fmt:message key="common.the_room"/> <span style="font-weight: bold;">${room}</span> <fmt:message key="common.could_not_be_entered_reason"/>: <span style="font-weight: bold;">${reason}</span>)
</p>
<p>
<fmt:message key="common.please_choose_another_room"/>!
</p>
</t:layout>