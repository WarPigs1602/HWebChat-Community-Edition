<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1>
<fmt:message key="common.user_management"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<form action="${paths.config_users}&update=true&user=${nick}&change=reg" method="POST" accept-charset="utf-8">${reg_error_if_one}
  <input type="hidden" name="service" value="reg_done">
  <p class="form-section-title">
    <fmt:message key="common.necessary_information"/>
  </p>
  <p>
  <div class="input-group">
  <select class="form-control form-control-sm col-md-1 mb-2" name="reg_gender" size="1">
    <option value="-"${selected_no}><fmt:message key="common.gender"/></option>
    <option value="m"${selected_male}><fmt:message key="console.reg_form.the"/></option>
    <option value="f"${selected_female}><fmt:message key="console.reg_form.the_2"/></option>
  </select>
  <input type="text" size="20" maxlength="20" class="form-control form-control-sm col-md-6 mb-2" name="reg_nick" value="${reg_nick}" placeholder="<fmt:message key="console.reg_form.the_nickname_at_least_two"/>)">
  </div>
    <div class="input-group">
  <input type="text" size="2" maxlength="2" class="form-control form-control-sm col-md-1 mb-1" name="reg_day" value="${reg_day}" placeholder="<fmt:message key="console.reg_form.birthday_day"/>"><input type="text" size="2" maxlength="2" class="form-control form-control-sm col-md-1 mb-1" name="reg_month" value="${reg_month}" placeholder="<fmt:message key="common.month"/>"><input type="text" size="4" maxlength="4" class="form-control form-control-sm col-md-1 mb-1" name="reg_year" value="${reg_year}" placeholder="<fmt:message key="common.year"/>">
  </div>
  <input type="email" size="50" maxlength="255" class="form-control form-control-sm col-md-5 mb-2" name="reg_mail" value="${reg_mail}" placeholder="<fmt:message key="console.reg_form.the_e_mail_address"/>">
  <input type="email" size="50" maxlength="255" class="form-control form-control-sm col-md-5 mb-2" name="reg_mail2" value="" placeholder="<fmt:message key="console.reg_form.the_e_mail_address_repetition"/>)">  
  <input type="password" size="50" maxlength="20" class="form-control form-control-sm col-md-5 mb-2" name="pwd" value="" placeholder="<fmt:message key="console.reg_form.the_password"/>">
  <input type="password" size="50" maxlength="20" class="form-control form-control-sm col-md-5 mb-2" name="pwd2" value="" placeholder="<fmt:message key="console.reg_form.the_password_repetition"/>)">

  </p>
  <p class="form-section-title">
    <fmt:message key="console.reg_form.security_code"/>
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.complete_registration"/>!">
  </p>
</form>
</t:layout>