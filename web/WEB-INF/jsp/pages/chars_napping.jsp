<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping"><h1>
<fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p>
<fmt:message key="common.please_for_your_nickname"/> <span style="font-weight: bold;">${nick}</span> <fmt:message key="common.only_use_alphanumeric_characters"/>
</p>
</t:layout>