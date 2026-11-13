<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="console.emot.emoticon_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<span style="font-weight: bold">${success}</span>
<p>
<span style="font-weight: bold"><fmt:message key="common.note"/></span> <fmt:message key="common.an_empty_field_removes_the"/>
<table>
<form action="${paths.config_emot}&update=true" method="POST">
${config}
<td><input type="text" name="name" class="form-control form-control-sm" value=""> </td><td><input type="text" class="form-control form-control-sm" name="value" value=""></td></tr>
<td>&nbsp;</td><td>
<input type="submit" class="btn btn-secondary btn-sm" name="" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</t:layout>