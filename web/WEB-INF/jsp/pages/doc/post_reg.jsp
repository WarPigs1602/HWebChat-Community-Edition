<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="post">  <form name="f" action="${paths.input}" method="POST" onsubmit="return submitOpera();" accept-charset="utf-8" target="shadow">
    <p style="clear: both; padding:0px; margin:0px;">
      <textarea size="64" class="post_field" id="message" name="msg" value="" maxlength="255" onkeydown="return submitChatInput(event);"></textarea>
    </p>
  </form></t:layout>