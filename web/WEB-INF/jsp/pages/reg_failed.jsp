<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>  <p class="form-section-title">
    <fmt:message key="page.reg_failed.registration_is_not_possible"/>!
  </p>
  <p style="font-weight: bold; color: #ff0000">
    ${error_message}
  </p>