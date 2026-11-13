<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><form method="POST" name="login" action="http://localhost:8080/HWebChat/Start?page=login" target="_blank" accept-charset="utf-8">
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
<a href="http://localhost:8080/HWebChat/Start?page=login&skin=native&room=moep&owner=Source"><fmt:message key="common.guest_access"/></a><br>
<a href="http://localhost:8080/HWebChat/Start?page=register&skin=native&owner=Source"><fmt:message key="doc.test.registration"/></a><br>
<a href="http://localhost:8080/HWebChat/Start?page=password&skin=native&owner=Source"><fmt:message key="doc.test.forget_the_password"/></a>
</p>
<p class="users">
<script type="text/javascript" src="http://localhost:8080/HWebChat/Start?page=content&service=js&skin=native&doc=<fmt:message key="doc.test.rooms_nappi"/></t:layout>