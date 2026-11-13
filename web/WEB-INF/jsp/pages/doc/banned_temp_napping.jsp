<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping"><h1>
<fmt:message key="common.note_3"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p>
<fmt:message key="common.your_nickname_or_your_ip_2"/>!
</p>
<p>
<fmt:message key="common.the_reason_given_to_you"/> <span style="font-weight: bold">${reason}</span> 
</p>
<p>
<table>
<tr>
<td style="text-align: left; font-weight: bold;">
<fmt:message key="common.the_following_reasons_can_cause"/>
</td>
</tr>
<tr>
<td style="text-align: left;">
<fmt:message key="common.you_have_disturbed_the_chat"/>!<br>
<fmt:message key="common.you_have_violated_applicable_law"/>!<br>
<fmt:message key="common.you_have_done_unwanted_advertising"/>!<br>
<fmt:message key="common.you_have_not_followed_the"/>!<br>
<fmt:message key="common.you_have_built_too_many"/>)!<br>
</td>
</tr>
</table>
</p>
</t:layout>