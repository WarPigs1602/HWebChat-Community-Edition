<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><main class="start-page start-page-redesign">
  <section class="start-hero card">
    <div class="card-body start-hero-body">
      <div class="start-hero-copy">
        <p class="start-eyebrow">${SERVER_SOFTWARE}</p>
        <h1><fmt:message key="page.start.welcome_to"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
        <p class="start-lead mb-0"><fmt:message key="page.homepage_extra.meet_in_chat_rooms_or"/></p>
      </div>
      <div class="start-metrics" aria-label="<fmt:message key="page.homepage_extra.chat_at_a_glance"/>">
        <div class="start-metric"><span class="start-metric-value">${homepage_chatters}</span><span class="start-metric-label"><fmt:message key="page.homepage_extra.chatters_online"/></span></div>
        <div class="start-metric"><span class="start-metric-value">${homepage_rooms}</span><span class="start-metric-label"><fmt:message key="page.homepage_extra.active_rooms"/></span></div>
        <div class="start-metric"><span class="start-metric-value">${homepage_registered}</span><span class="start-metric-label"><fmt:message key="page.homepage_extra.registered_users"/></span></div>
        <div class="start-metric"><span class="start-metric-value">${homepage_messages}</span><span class="start-metric-label"><fmt:message key="page.homepage_extra.public_messages"/></span></div>
      </div>
    </div>
  </section>

  <section class="start-grid">
    <c:if test="${modules.chat ne '0'}">
      <aside class="start-login card" id="start-login-panel" aria-labelledby="start-login-title">
        <c:set var="chatLoginAction" value="${directChatLogin ? paths.login_chat : paths.login}"/>
        <div class="card-body">
          <div class="start-login-heading">
            <span class="start-login-icon"><i class="fa fa-comments" aria-hidden="true"></i></span>
            <div>
              <h2 id="start-login-title" class="h4 mb-0"><fmt:message key="page.start.log_in"/></h2>
            </div>
          </div>
          <form method="POST" name="login" action="${chatLoginAction}" target="_top" accept-charset="utf-8">
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
          <a class="start-guest-link" href="${chatLoginAction}?skin=${skin}"><i class="fa fa-user-o" aria-hidden="true"></i> <fmt:message key="page.start.continue_as_a_guest"/></a>
        </div>
      </aside>
    </c:if>

    <div class="start-main-col">
      <c:if test="${modules.chat ne '0' and directChatLogin}">
        <section class="card start-online-card">
          <div class="card-body">
            <h2 class="h5 mb-2"><i class="fa fa-users"></i> <fmt:message key="common.members_online"/></h2>
            <div class="users"><script type="text/javascript" src="${paths.script_room}"></script></div>
          </div>
        </section>
      </c:if>

      <c:if test="${not empty homepage_news_url}">
        <section class="card" aria-labelledby="homepage-news-title">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h2 class="h5 mb-0" id="homepage-news-title"><i class="fa fa-newspaper-o"></i> <fmt:message key="page.homepage_news.latest_news"/></h2>
              <a href="${homepage_news_url}"><fmt:message key="page.homepage_news.all_news"/></a>
            </div>
            <article>
              <h3 class="h6 mb-1">${homepage_news_title}</h3>
              <p class="text-muted small mb-2">${homepage_news_author} &middot; ${homepage_news_published}</p>
              <div class="homepage-news-body">${homepage_news_body}</div>
            </article>
          </div>
        </section>
      </c:if>

      <section class="card">
        <div class="card-body py-2">
          <div class="start-inline-links">
            <a class="btn btn-sm btn-outline-secondary" href="${paths.rules}"><i class="fa fa-gavel"></i> <fmt:message key="page.homepage_extra.chat_guidelines"/></a>
            <a class="btn btn-sm btn-outline-secondary" href="${paths.stats}"><i class="fa fa-bar-chart"></i> <fmt:message key="common.statistics"/></a>
            <c:if test="${modules.board ne '0'}"><a class="btn btn-sm btn-outline-secondary" href="${paths.board}"><i class="fa fa-comments-o"></i> Forum</a></c:if>
          </div>
        </div>
      </section>
    </div>
  </section>
</main></t:layout>