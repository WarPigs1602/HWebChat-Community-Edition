<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><main class="start-page">
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
  ${homepage_extra}
</main>
</t:layout>