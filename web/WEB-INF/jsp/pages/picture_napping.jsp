<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping_account"><h1>${SERVER_SOFTWARE} <fmt:message key="common.profile_picture"/></h1>


<form action="UploadFile" method="post" enctype="multipart/form-data"><input type="hidden" name="upload_context" value="chat">  
  <input type="hidden" name="service" value="password">
  <input type="hidden" name="data" value="change">
  <input type="hidden" name="owner" value="${owner}">
  <p class="form-section-title">
    <fmt:message key="common.choose_a_file_to_upload"/>!
  </p>
  <p>
  <input type="file" name="file" size="50" class="form-control-file mb-2"/>
  <a href="${path_hwebchat}?page=account&service=picture_del&data=delete&skin=${skin}"><fmt:message key="common.click_here_to_delete_an"/>!</a>
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done_2"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.submit_change"/>">
  </p>
</form>
</t:layout>