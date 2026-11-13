<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><h1><fmt:message key="common.image_upload"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


  <p class="form-section-title">
    <fmt:message key="common.image_could_not_be_uploaded"/>
  </p>
  <p>
    <fmt:message key="common.your_profile_picture_could_not_2"/> <span style="font-weight: bold;"><fmt:message key="common.file_is_not_an_image"/></span>!
  </p>
  <p class="form-section-title">
    &nbsp;
  </p>
</t:layout>