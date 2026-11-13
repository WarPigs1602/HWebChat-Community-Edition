<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="account"><h1><fmt:message key="common.profile_picture_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


  <p class="form-section-title">
    <fmt:message key="common.image_not_uploaded"/>
  </p>
  <p>
    <fmt:message key="common.the_image_could_not_be"/>!
  </p>
  <p class="form-section-title">
    &nbsp;
  </p>
</t:layout>