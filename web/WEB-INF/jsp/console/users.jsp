<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="common.user_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<p>
<form action="${paths.config_users}&update=true" method="POST">
<fmt:message key="console.users.search_for_chatter"/> <input type="text" name="user" class="form-control form-control-sm" value="">
<input type="submit" name="" class="btn btn-secondary btn-sm" value="<fmt:message key="common.submit"/>">
</form>
</p>
</t:layout>