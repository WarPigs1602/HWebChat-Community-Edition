<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1>
<fmt:message key="common.user_management"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
  <p>
    <fmt:message key="console.reg_form_success.you_have"/> <span style="font-weight: bold;">${reg_nick}</span> <fmt:message key="console.reg_form_success.successfully_registered"/>
  </p>
</t:layout>