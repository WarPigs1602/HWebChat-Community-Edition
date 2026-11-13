<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><!DOCTYPE html>
<html>
<head>
  <title><fmt:message key="page.chat_header.serve_r_status"><fmt:param value="${nick}"/><fmt:param value="${SERVER_SOFTWARE}"/><fmt:param value="${SERVER_VERSION}"/><fmt:param value="${SERVER_STATUS}"/></fmt:message></title>
  <meta content="${SERVER_VENDOR}" name="author">
  <meta content="&copy;${SERVER_YEAR} by ${SERVER_VENDOR}" name="description">
  <meta content="${SERVER_SOFTWARE} ${SERVER_VERSION}-${SERVER_STATUS}" name="software">
  <meta content="text/html; charset=UTF-8" http-equiv="content-type">
  <meta http-equiv="Content-Style-Type" content="text/css">
  <link rel="stylesheet" href="${paths.style_chat}" type="text/css">
  <link rel="stylesheet" href="${paths.style_iframe}" type="text/css">
  <script type="text/javascript" src="${paths.script_jquery}"></script>
  <script type="text/javascript" src="${paths.script_window}"></script>
  <script type="text/javascript" src="${paths.script_chat}"></script>
  <script language="javascript">
    parent.setSessionId("${sid}");
    parent.setOwnNickName("${nick}");
  </script>
</head>
<body>
<p>
<a href="${paths.redirect}http://${SERVER_HOMEPAGE}/" target="_blank" style="font-weight: bold">${SERVER_SOFTWARE}</a><br>
&copy; ${SERVER_YEAR} by <a href="mailto:${SERVER_MAIL}">${SERVER_VENDOR}</a><br>
All rights reserved.
</p>
