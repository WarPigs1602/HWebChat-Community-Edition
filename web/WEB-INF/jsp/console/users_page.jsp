<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="common.user_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<form action="${paths.config_users}&update=true&user=${nick}" method="POST" accept-charset="utf-8">
  <p class="form-section-title">
    <fmt:message key="console.users_page.here_you_can_edit_the"><fmt:param value="${nick}"/></fmt:message>
  </p>
  <p>
  <fmt:message key="console.users_page.what_do_you_want_to"/></br>
  <select class="form-control form-control-sm col-md-6 mb-3" name="change" size="4">
    <option value="<fmt:message key="console.users_page.profile"/>" selected><fmt:message key="common.processing_profile"/></option>
    <option value="password"><fmt:message key="common.processing_the_password"/></option>
    <option value="<fmt:message key="console.users_page.data"/>"><fmt:message key="common.processing_account_data"/></option>
    <option value="delete"><fmt:message key="common.delete_account"/></option>
  </select>
  </p>
  <p class="form-section-title">
    <fmt:message key="console.users_page.and_then_we_go_on"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.continue_to_the_next_page"/>">
  </p>
</form>
</t:layout>