<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="common.user_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<p>
<fmt:message key="common.the_user"/> <span style="font-weight: bold">${nick}</span> <fmt:message key="common.does_not_exist"/><br>
<fmt:message key="common.please_click"/> <a href="${paths.config_users}&update=true&user=${nick}&change=reg"><fmt:message key="common.here"/></a> <fmt:message key="console.users_fail.to_create_the_user"/>
</p>
</t:layout>