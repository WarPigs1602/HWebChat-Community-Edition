<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="common.room_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<p>
<fmt:message key="common.the_room"/> <span style="font-weight: bold">${room}</span> <fmt:message key="common.does_not_exist"/><br>
<fmt:message key="common.please_click"/> <a href="${paths.config_rooms}&update=true&change=add_room&room=${room}"><fmt:message key="common.here"/></a> <fmt:message key="console.rooms_fail.to_create_the_room"/>
</p>
</t:layout>