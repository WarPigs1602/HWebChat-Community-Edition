<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="account"><div id="pagetitle">
<fmt:message key="page.account_error_message_mail_invalid.de_lete_profile_picture"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>!! ::
</div>
<div id="pagecontent">
  <p class="form-section-title">
    <fmt:message key="common.delete_profile_picture"/>!
  </p>
  <p>
    <fmt:message key="common.please_click_2"/> <a href="${page_base}/account.jsp?service=picture_del&amp;data=delete&amp;skin=${skin}"><fmt:message key="common.here"/></a> <fmt:message key="common.to_continue_the_deletion_process"/>!
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done"/>
  </p>
</div></t:layout>