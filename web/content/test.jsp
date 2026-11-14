<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("document", request, response)) { return; } %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><form method="POST" name="login" action="${page_base}/login.jsp" target="_blank" accept-charset="utf-8">
  <input
 maxlength="20" name="nick"  placeholder="Nickname"><br>
  <input maxlength="20" name="pwd"
 type="password"  placeholder="<fmt:message key="common.password_2"/>"><br>
  <input name="room" value="moep" type="hidden">
  <input name="owner" value="Source" type="hidden">
  <input name="skin" value="native" type="hidden">
  <input value="Login" type="submit">
</form>  
<p>  
<a href="${page_base}/login.jsp?skin=native&amp;room=moep&amp;owner=Source"><fmt:message key="common.guest_access"/></a><br>
<a href="${page_base}/register.jsp?skin=native&amp;owner=Source"><fmt:message key="doc.test.registration"/></a><br>
<a href="${page_base}/password.jsp?skin=native&amp;owner=Source"><fmt:message key="doc.test.forget_the_password"/></a>
</p>
<p class="users">
<script type="text/javascript" src="${page_base}/content/script_rooms_napping.jsp?skin=native"></script>
</p></t:layout>