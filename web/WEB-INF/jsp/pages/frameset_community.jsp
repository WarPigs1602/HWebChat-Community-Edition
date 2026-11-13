<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community"><main class="start-page">
  <section class="start-intro community-welcome">
    <p class="start-eyebrow">Community</p>
    <h1><fmt:message key="common.welcome"><fmt:param value="${homepage_nick}"/></fmt:message></h1>
    <p class="start-lead"><fmt:message key="common.you_re_signed_in_to"/></p>
  </section>
  <section class="card my-4">
    <div class="card-body">
      <h2 class="h5"><fmt:message key="common.members_online"/></h2>
      <div class="users"><script type="text/javascript" src="${paths.script_room}"></script></div>
    </div>
  </section>
  ${homepage_extra}
</main>
</t:layout>