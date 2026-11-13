<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><h1>
<fmt:message key="common.registration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
  <p>
    <fmt:message key="common.your_new_password_has_been"/>
	<small>
	  <fmt:message key="common.your_old_password_will_remain"/>!
	</small>
  </p>
  <p>
     <fmt:message key="common.the_arrival_of_the_e"/>!
  </p></t:layout>