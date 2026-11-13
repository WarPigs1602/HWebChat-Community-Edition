<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg">
<h1>${SERVER_SOFTWARE} <fmt:message key="page.account_form_password_com.password"/></h1>

<form action="${paths.account_com}" method="POST" accept-charset="utf-8">${account_error_if_one}
  <input type="hidden" name="service" value="password">
  <input type="hidden" name="data" value="change">

  <p class="form-section-title"><fmt:message key="common.change_password"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.old_password"/></label>
      <input type="password" class="form-control form-control-sm" name="old_pwd" value="" placeholder="<fmt:message key="common.old_password"/>">
      <span class="form-hint"><fmt:message key="common.should_be_at_least_four"/></span>
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.new_password"/></label>
      <input type="password" class="form-control form-control-sm" name="new_pwd" value="" placeholder="<fmt:message key="common.new_password"/>">
      <span class="form-hint"><fmt:message key="common.should_be_at_least_four"/></span>
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.repeat_new_password"/></label>
      <input type="password" class="form-control form-control-sm" name="new_pwd2" value="" placeholder="<fmt:message key="common.repeat_new_password"/>">
      <span class="form-hint"><fmt:message key="common.to_make_sure_you_re"/></span>
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.save"/></p>

  <input type="submit" class="btn btn-primary btn-sm" name="OK" value="<fmt:message key="common.submit_change"/>">
</form>
</t:layout>