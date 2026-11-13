<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping_message"><h1><fmt:message key="common.new_from"/> <span style="color: #${color};">${nick}</span></h1>

<fmt:message key="common.you_don_t_have_any"/>
</t:layout>