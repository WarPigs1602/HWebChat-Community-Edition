<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="console.help.help_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<span style="font-weight: bold">${success}</span>
<p>
<table>
<form action="${paths.config_help}&update=true" method="POST">
${config}
<td>&nbsp;</td><td>
<input type="submit" name="" class="btn btn-secondary btn-sm" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</t:layout>