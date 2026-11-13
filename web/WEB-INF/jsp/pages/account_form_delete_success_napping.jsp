<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping_account"><h1>${SERVER_SOFTWARE}<fmt:message key="common.delete_account_2"/></h1>

  <p class="form-section-title">
    <fmt:message key="common.your_account_has_been_deleted"/>!
  </p>
  <div class="card bg-light border-0 shadow-sm mb-3">
    <div class="card-body">
      <p class="mb-0"><fmt:message key="common.you_have_successfully_deleted_your"/> <strong>${nick}</strong><fmt:message key="common.gel_scht"/></p>
    </div>
  </div>
</t:layout>