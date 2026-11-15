<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><main class="start-page">
  <section class="start-overview">
    <div class="start-intro">
      <h1><fmt:message key="page.start.welcome_to"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
    </div>

    <aside class="start-login" aria-labelledby="start-login-title">
      <div class="start-login-heading">
        <span class="start-login-icon"><i class="fa fa-sign-in" aria-hidden="true"></i></span>
        <div>
          <h2 id="start-login-title"><fmt:message key="page.start.log_in"/></h2>
          <p><fmt:message key="page.start.welcome_back"/></p>
        </div>
      </div>
      <form method="POST" name="login" action="${paths.login}" target="_top" accept-charset="utf-8">
        <input type="hidden" name="skin" value="${skin}">
        <div class="form-group">
          <label for="start-nick">Nickname</label>
          <input class="form-control" id="start-nick" maxlength="20" name="nick" type="text" autocomplete="username" required>
        </div>
        <div class="form-group">
          <label for="start-pwd"><fmt:message key="common.password_2"/></label>
          <input class="form-control" id="start-pwd" maxlength="20" name="pwd" type="password" autocomplete="current-password" required>
        </div>
        <button class="btn btn-primary btn-block" type="submit"><i class="fa fa-sign-in" aria-hidden="true"></i> <fmt:message key="page.start.log_in_2"/></button>
      </form>
      <div class="start-login-links">
        <a href="${paths.register}"><i class="fa fa-user-plus" aria-hidden="true"></i> <fmt:message key="page.start.create_an_account"/></a>
        <a href="${paths.password}"><i class="fa fa-key" aria-hidden="true"></i> <fmt:message key="page.start.forgot_password"/></a>
      </div>
      <a class="start-guest-link" href="${paths.login}?skin=${skin}"><i class="fa fa-user-o" aria-hidden="true"></i> <fmt:message key="page.start.continue_as_a_guest"/></a>
    </aside>
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