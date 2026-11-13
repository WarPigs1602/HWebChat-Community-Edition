<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="account"><h1>${SERVER_SOFTWARE}-Account!</h1>

  <p class="form-section-title">
    <fmt:message key="common.account_data_for_successfully_changed"><fmt:param value="${nick}"/></fmt:message>!
  </p>
  <div class="card bg-light border-0 shadow-sm mb-3">
    <div class="card-body">
      <p class="mb-0"><fmt:message key="common.account_data_successfully_updated"/></p>
    </div>
  </div>
</t:layout>