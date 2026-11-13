<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="common.user_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


<form action="${paths.config_users}&update=true&user=${nick}&change=password" method="POST" accept-charset="utf-8">${account_error_if_one}
  <p class="form-section-title">
    <fmt:message key="common.necessary_information"/>
  </p>
  <p>
  <fmt:message key="console.users_pwd.the_new_password"/><br />
  <span class="form-hint"><fmt:message key="common.should_be_at_least_four"/></span><br />
  <input type="password" size="10" maxlength="30" class="form-control form-control-sm col-md-5 mb-2" name="new_pwd" value="">
  <fmt:message key="console.users_pwd.repeat_new_password"/><br />
  <span class="form-hint"><fmt:message key="console.users_pwd.to_make_sure_you_are"/></span><br />
  <input type="password" size="10" maxlength="30" class="form-control form-control-sm col-md-5 mb-2" name="new_pwd2" value="">
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.send_a_change"/>!!">
  </p>
</form>
</t:layout>