<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>  <div id="cookie-warning" class="alert alert-warning alert-dismissible fade show" role="alert" style="display:none; margin-top: 10px;">
    <strong><fmt:message key="layout.start.cookie_notice"/></strong> <fmt:message key="layout.start.this_website_uses_cookies_to"/>.
    <button type="button" class="close" onclick="document.cookie='cookie_accepted=1;path=/;max-age='+60*60*24*365;document.getElementById('cookie-warning').style.display='none';">
      <span aria-hidden="true">&times;</span>
    </button>
  </div>
