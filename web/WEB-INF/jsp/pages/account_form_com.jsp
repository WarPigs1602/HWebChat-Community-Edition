<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><h1><fmt:message key="common.account"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>

<form action="${paths.account_com}" method="POST" accept-charset="utf-8">
  <p class="form-section-title"><fmt:message key="common.account_2"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.what_do_you_want_to"/></label>
      <select class="form-select form-select-sm" name="service" size="6">
        <option value="account" selected><fmt:message key="common.processing_profile"/></option>
        <option value="napping">Chatnapping</option>
        <option value="picture"><fmt:message key="common.upload_profile_picture"/></option>
        <option value="password"><fmt:message key="common.processing_the_password"/></option>
        <option value="data"><fmt:message key="common.processing_account_data"/></option>
        <option value="delete"><fmt:message key="common.delete_account"/></option>
      </select>
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.continue"/></p>

  <input type="submit" class="btn btn-primary btn-sm" name="OK" value="<fmt:message key="common.continue_to_the_next_page"/>">
</form>
</t:layout>