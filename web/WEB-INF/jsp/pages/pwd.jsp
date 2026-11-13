<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><h1><fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


<p>
<fmt:message key="common.the_login_for_the_name"/> <span style="font-weight: bold;">${nick}</span> <fmt:message key="common.failed_because_of_a_wrong"/>
</p>
</t:layout>