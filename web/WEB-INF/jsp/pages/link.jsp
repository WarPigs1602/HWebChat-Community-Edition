<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="link"></h1><fmt:message key="page.link.forwarding"/></h1>

<p>
  <fmt:message key="page.link.you_will_be_automatically_redirected"><fmt:param value="${link}"/></fmt:message>
</p>
<p>
  <fmt:message key="page.link.if_this_does_not_work"/> <a href="${link}"><fmt:message key="common.here"/></a> 
</p>

<script language="javascript">redirect("${link}");</script>
</t:layout>