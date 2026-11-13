<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="common.room_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<p>
<fmt:message key="common.the_room"/> <span style="font-weight: bold">${room}</span> <fmt:message key="console.rooms_exist.already_exists"/><br>
<fmt:message key="console.rooms_exist.you_can"/> <a href="${paths.config_rooms}&update=true&change=delete&room=${room}"><fmt:message key="common.here"/><a/> <fmt:message key="console.rooms_exist.click_to_remove_it"/>
</p>
</t:layout>