<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><h1>
${SERVER_SOFTWARE} <fmt:message key="page.pass_form2.reset_the_password"/>
</h1>
<form action="${paths.password}" method="POST">${reg_error_if_one}
  <input type="hidden" name="service" value="pass_accept">
  <input type="hidden" name="reg_nick" value="${reg_nick}">
  <input type="hidden" name="reg_mail" value="${reg_mail}">
  <input type="hidden" name="reg_pwd" value="${reg_pwd}">
  <input type="hidden" name="reg_reminder_question" value="${reg_reminder_question}">
  <input type="hidden" name="reg_reminder_answer" value="${reg_reminder_answer}">
  <input type="hidden" name="reg_captcha" value="${reg_captcha}">
  <input type="hidden" name="reg_cid" value="${reg_cid}">
  <input type="hidden" name="reg_day" value="${reg_day}">
  <input type="hidden" name="reg_month" value="${reg_month}">
  <input type="hidden" name="reg_year" value="${reg_year}">
  <p class="form-section-title">
    <fmt:message key="common.necessary_information"/>
  </p>
  <p>
  <fmt:message key="page.pass_form2.your_security_question_for"/> <strong>${reg_nick}</strong>:<br />
  <span style="font-weight: bold;">${reg_reminder_question}</span><br /><br />
  <fmt:message key="page.pass_form2.your_answer"/><br />
  <input type="text" size="50" maxlength="255" class="form-control form-control-sm mb-2" name="reg_reminder_answer" value="${reg_reminder_answer}"><br />
  <fmt:message key="page.pass_form2.your_password"/><br />
  <span class="form-hint"><fmt:message key="common.should_be_at_least_four"/></span>
  <input type="password" size="10" maxlength="30" class="form-control form-control-sm mb-2" name="reg_pwd" value="${reg_pwd}"><br />
  <fmt:message key="page.pass_form2.password_repetition"/><br />
  <span class="form-hint"><fmt:message key="page.pass_form2.to_make_sure_you_re"/></span>
  <input type="password" size="10" maxlength="30" class="form-control form-control-sm mb-2" name="reg_pwd2" value="">
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done_2"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="page.pass_form2.send_request"/>">
  </p>
</form>
</t:layout>