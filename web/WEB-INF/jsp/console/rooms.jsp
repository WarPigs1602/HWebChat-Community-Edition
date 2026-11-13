<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="common.room_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<p>
<form action="${paths.config_rooms}&update=true&change=search" method="POST">
<fmt:message key="console.rooms.search_for_space"/> <input type="text" class="form-control form-control-sm" name="room" value="">
<input type="submit" name="" class="btn btn-secondary btn-sm" value="<fmt:message key="common.submit"/>">
</form>
</p>
</t:layout>