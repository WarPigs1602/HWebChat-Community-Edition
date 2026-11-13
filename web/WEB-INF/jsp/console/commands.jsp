<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="console.commands.command_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<span style="font-weight: bold">${success}</span>
<p>
<table>
<form [formGroup]="form" action="${paths.config_cmd}&update=true" method="POST">
${config}
<td>&nbsp;</td><td>
<input type="submit" class="btn btn-secondary btn-sm" name="" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</t:layout>