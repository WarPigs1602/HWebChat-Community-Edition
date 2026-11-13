<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping_account"><form action="${paths.account}" method="POST" accept-charset="utf-8">
  <p class="form-section-title">
    <fmt:message key="page.account_form_napping2.here_you_can_manage_your"/>
  </p>
  <p>
  <fmt:message key="page.account_form_napping2.what_do_you_want_to"/></br>
  <select class="form-control form-control-sm col-md-6 mb-3" name="service" size="6">
    <option value="account" selected><fmt:message key="common.processing_profile"/></option>
    <option value="napping">Chatnapping</option>
    <option value="picture"><fmt:message key="common.upload_profile_picture"/></option>
    <option value="password"><fmt:message key="common.processing_the_password"/></option>
    <option value="data"><fmt:message key="common.processing_account_data"/></option>
    <option value="delete"><fmt:message key="common.delete_account"/></option>
  </select>
  </p>
  <input type="hidden" name="owner" value="${owner}">
  <p class="form-section-title">
    <fmt:message key="page.account_form_napping2.and_off_we_go"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.continue_to_the_next_page"/>">
  </p>
</form></t:layout>