<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping"><h1>${SERVER_SOFTWARE} &ndash; <fmt:message key="common.delete_account"/></h1>

<form action="${paths.account}" method="POST" accept-charset="utf-8">${account_error_if_one}
  <input type="hidden" name="service" value="delete">
  <input type="hidden" name="data" value="delete">

  <p class="form-section-title"><fmt:message key="common.required_information"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.your_password"/></label>
      <input type="password" class="form-control form-control-sm" name="pwd" value="" placeholder="<fmt:message key="common.password_2"/>">
      <span class="form-hint"><fmt:message key="common.should_be_at_least_four"/></span>
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.repeat_your_password"/></label>
      <input type="password" class="form-control form-control-sm" name="pwd2" value="" placeholder="<fmt:message key="common.repeat_password"/>">
      <span class="form-hint"><fmt:message key="common.to_make_sure_you_didn"/></span>
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.confirm"/></p>

  <input type="submit" class="btn btn-danger btn-sm" name="OK" value="<fmt:message key="common.delete_account"/>">
</form>
</t:layout>