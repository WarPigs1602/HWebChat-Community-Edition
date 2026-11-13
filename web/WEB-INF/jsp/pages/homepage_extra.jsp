<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><section class="my-4" aria-labelledby="homepage-overview-title">
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
${homepage_news}
