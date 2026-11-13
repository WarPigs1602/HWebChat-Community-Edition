<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><h1>
<fmt:message key="common.registration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
  <p>
    <fmt:message key="common.you_became_successful_as"/> <span style="font-weight: bold;">${reg_nick}</span> <fmt:message key="common.registered"/>
  </p>
  <p>
    <fmt:message key="common.your_password_was_successfully_sent"/>!
  </p>
</t:layout>