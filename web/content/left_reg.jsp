<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("document", request, response)) { return; } %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="left"><div id="left">
<div id="lefttitle">
<fmt:message key="common.commands"/>
</div>
<div id="leftcontent">
      &raquo; <a href="${paths.whois_chat}" target="shadow"><fmt:message key="common.who_is_where"/></a><br>
      &raquo; <a href="${paths.whois_channel}" target="shadow"><fmt:message key="common.who_is_here"/></a><br>
      &raquo; <a href="${paths.clear_chat_window}" target="shadow"><fmt:message key="common.empty_windows"/></a><br>
      &raquo; <a href="${paths.get_my_ip}" target="shadow"><fmt:message key="common.own_host"/></a><br>
      &raquo; <a href="${paths.get_chatinfo}" target="shadow">Chatinfo</a><br>
      &raquo; <a href="${paths.quit_chat}" target="shadow"><fmt:message key="common.log_out"/></a><br>
</div>
</div>
<div id="left">
<div id="blank">
</div>
<div id="lefttitle">
<fmt:message key="common.assistance"/>
</div>
<div id="leftcontent">
      &raquo; <a href="javascript:showChatPanel('${paths.emot}','Emoticons');"><fmt:message key="common.emoticons"/></a><br>
      &raquo; <a href="javascript:showChatPanel('${paths.help}','Hilfe');"><fmt:message key="common.list_of_all_commands"/></a><br>
      &raquo; <a href="javascript:showChatPanel('${paths.account}','Account');"><fmt:message key="doc.left_reg.manage_account"/></a><br>
      &raquo; <a href="javascript:showChatPanel('${paths.memory}','Speicherinfo');"><fmt:message key="common.memory_information"/></a><br>
      &raquo; <a href="javascript:showChatPanel('${paths.ua}','User-Agent');"><fmt:message key="common.user_agent"/></a><br>
</div>
</div>
<div id="left">
<div id="blank">
</div>
<div id="lefttitle">
<fmt:message key="common.colour"/>
</div>
<div id="leftcontent">
<div id="colorpicker"></div>
  <form name="f" action="${paths.input}" method="POST" onsubmit="return submitOpera();" accept-charset="utf-8" target="shadow">
<input type="text" class="color_field" id="msg" name="msg" value="#123456" onkeydown="return submitChatInput(event);"></input>
</form>
</div>
</div></t:layout>