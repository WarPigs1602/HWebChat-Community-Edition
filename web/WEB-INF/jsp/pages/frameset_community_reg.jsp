<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><main class="start-page">
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
  <section class="my-4" aria-labelledby="homepage-overview-title">
  <h2 class="h4" id="homepage-overview-title"><fmt:message key="page.homepage_extra.chat_at_a_glance"/></h2>
  <div class="row">
    <div class="col-6 col-lg-3 mb-3">
      <div class="card h-100"><div class="card-body">
        <div class="h4">${homepage_chatters}</div><div><fmt:message key="page.homepage_extra.chatters_online"/></div>
      </div></div>
    </div>
    <div class="col-6 col-lg-3 mb-3">
      <div class="card h-100"><div class="card-body">
        <div class="h4">${homepage_rooms}</div><div><fmt:message key="page.homepage_extra.active_rooms"/></div>
      </div></div>
    </div>
    <div class="col-6 col-lg-3 mb-3">
      <div class="card h-100"><div class="card-body">
        <div class="h4">${homepage_registered}</div><div><fmt:message key="page.homepage_extra.registered_users"/></div>
      </div></div>
    </div>
    <div class="col-6 col-lg-3 mb-3">
      <div class="card h-100"><div class="card-body">
        <div class="h4">${homepage_messages}</div><div><fmt:message key="page.homepage_extra.public_messages"/></div>
      </div></div>
    </div>
  </div>
</section>
<section class="my-4">
  <h2 class="h4"><fmt:message key="page.homepage_extra.about_the_chat"/></h2>
  <p><fmt:message key="page.homepage_extra.meet_in_chat_rooms_or"/></p>
  <p><a href="${paths.rules}"><fmt:message key="page.homepage_extra.chat_guidelines"/></a> &middot; <a href="${paths.stats}"><fmt:message key="common.statistics"/></a></p>
</section>
<c:if test="${not empty homepage_news_url}">
<section class="my-4" aria-labelledby="homepage-news-title">
  <h2 class="h4" id="homepage-news-title"><fmt:message key="page.homepage_news.latest_news"/></h2>
  <article class="card">
    <div class="card-body">
      <h3 class="h5">${homepage_news_title}</h3>
      <p class="text-muted">${homepage_news_author} &middot; ${homepage_news_published}</p>
      <div class="homepage-news-body">${homepage_news_body}</div>
      <p class="mt-3 mb-0"><a href="${homepage_news_url}"><fmt:message key="page.homepage_news.all_news"/></a></p>
    </div>
  </article>
</section>
</c:if>
</main>
</t:layout>