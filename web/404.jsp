<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ include file="/WEB-INF/jsp/includes/lang.jspf" %>
<% response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND); %>
<c:set var="errorTitleKey" value="web.error404.title"/>
<c:set var="errorHeadingKey" value="web.error404.heading"/>
<c:set var="errorTextKey" value="web.error404.text"/>
<%@ include file="/WEB-INF/jsp/includes/error-page.jspf" %>
