<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><h1>
${SERVER_SOFTWARE} &ndash; Chatnapping
</h1>
<p>
  <fmt:message key="common.you_have_changed_the_room"/> <strong>${room2}</strong> <fmt:message key="common.and_can_add_the_room_2"/>:
</p>
<textarea readonly class="prism-live line-numbers language-html fill"><form method="POST" name="login" action="${host_http}${paths.login_chat}" target="_blank" accept-charset="utf-8">
  <input maxlength="20" name="nick" placeholder="Nickname"><br>
  <input maxlength="20" name="pwd" type="password" placeholder="<fmt:message key="common.password_2"/>"><br>
  <input name="room" value="${room2}" type="hidden">
  <input name="owner" value="${nick}" type="hidden">
  <input name="skin" value="${skin}" type="hidden">
  <input value="Login" type="submit">
</form>
<p>
  <a href="${host_http}${paths.login_chat}&skin=${skin}&room=${room2}&owner=${nick}"><fmt:message key="common.guest_access"/></a><br>
  <a href="${host_http}${paths.register}&owner=${nick}"><fmt:message key="common.register"/></a><br>
  <a href="${host_http}${paths.password}&owner=${nick}"><fmt:message key="common.forgot_password"/></a>
</p>
<p class="users">
<script type="text/javascript" src="${host_http}${paths.script_room_napping}"></script>
</p>
</textarea>
</t:layout>