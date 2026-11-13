<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><p>
<fmt:message key="common.your_nickname_or_your_ip_3"/>
</p>
<p>
<fmt:message key="common.the_reason_given_to_you"/> <span style="font-weight: bold">${reason}</span> 
</p>
<p>
<table>
<tr>
<td style="text-align: left; font-weight: bold;">
<fmt:message key="common.this_can_have_several_causes"/>
</td>
</tr>
<tr>
<td style="text-align: left;">
<fmt:message key="common.you_have_disturbed_the_chat"/>!<br>
<fmt:message key="common.you_have_violated_applicable_law"/>!<br>
<fmt:message key="common.you_have_done_unwanted_advertising"/>!<br>
<fmt:message key="common.you_have_repeatedly_flooded_the"/>!<br>
<fmt:message key="common.you_have_become_a_victim"/>)
</td>
</tr>
</table>
</p>
<p>
<fmt:message key="common.please_try_again_later"/>!
</p>