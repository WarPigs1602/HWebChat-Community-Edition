<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><main class="start-page">
  <section class="start-intro start-member-intro">
    <p class="start-eyebrow">Community</p>
    <h1><fmt:message key="page.start_logged_in.welcome"><fmt:param value="${homepage_nick}"/></fmt:message></h1>
    <p class="start-lead"><fmt:message key="page.start_logged_in.you_re_signed_in_choose"/></p>
  </section>
  ${homepage_extra}
</main>
</t:layout>