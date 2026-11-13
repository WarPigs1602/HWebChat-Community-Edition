<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><!DOCTYPE html>
<html lang="<fmt:message key="common.lang"/>">
<head>
  <meta charset="UTF-8">
  <title><fmt:message key="system.setup_required.title"/></title>
</head>
<body style="font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #f0f9ff;">
<div style="background: white; padding: 40px; border-radius: 15px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); max-width: 600px; margin: 0 auto;">
<h1 style="color: #e94560;">&#9881;&#65039; <fmt:message key="system.setup_required.title"/></h1>
<p style="margin: 20px 0; color: #666;"><fmt:message key="system.setup_required.text"/></p>
<p style="margin: 20px 0;"><a href="${setup_url}" style="background: #e94560; color: white; padding: 12px 30px; text-decoration: none; border-radius: 8px; font-weight: bold;"><fmt:message key="system.setup_required.start"/> &rarr;</a></p>
<p style="font-size: 0.8em; color: #888; margin-top: 20px;">${SERVER_SOFTWARE} | &copy;${SERVER_YEAR} by ${SERVER_VENDOR}</p>
</div>
</body>
</html>
