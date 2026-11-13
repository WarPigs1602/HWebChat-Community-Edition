<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><h1><fmt:message key="common.profile_picture_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


  <p class="form-section-title">
    <fmt:message key="common.image_could_not_be_uploaded"/>
  </p>
  <p>
    <fmt:message key="common.your_profile_picture_could_not_2"/> <span style="font-weight: bold;">${message}</span>!
  </p>
  <p class="form-section-title">
    &nbsp;
  </p>
</t:layout>