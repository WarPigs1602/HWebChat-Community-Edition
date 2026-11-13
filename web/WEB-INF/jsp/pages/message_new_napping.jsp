<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping_message"><h1><fmt:message key="common.news"/></h1>

<p style="font-weight: bold">${com.message_new_header}</p>
${messages}
</t:layout>