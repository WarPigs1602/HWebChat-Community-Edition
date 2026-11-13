<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="chat_messages_box"><div class="chat-messages-box">
  <div class="chat-messages-box-header"><fmt:message key="page.chat_messages_box.you_hav_e_received_a"><fmt:param value="${count}"/></fmt:message>:</div>
  <div class="chat-messages-box-body">${messages}</div>
</div>
</t:layout>