<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset"><script type="text/javascript">
<!--
  $(document).ready(function() {
    $('#colorpicker').farbtastic('#msg');
  });
-->
</script>
<jsp:include page="/WEB-INF/jsp/includes/navbar-chat.jsp">
  <jsp:param name="registered" value="false"/>
  <jsp:param name="napping" value="false"/>
</jsp:include>
<script>
 function submitMessage(text)
 {
     text = escapeHtml(text);
     var msg = {
        category: "chat",
        message: text,
        target: ""
     };
     socket.send(JSON.stringify(msg));
 }
</script>

<div class="chat-shell container-fluid p-0 d-flex flex-column" style="height: calc(100vh - 56px); min-height: 300px;">
  <div class="chat-main row no-gutters flex-grow-1" style="min-height: 0; flex-direction: row; flex-wrap: nowrap;">
    <div class="chat-frame-container" id="chat_frame_container">
      <div class="chat-tabs" id="chat_tabs"></div>
      <div class="chat_frame" id="chat_window">
        <script type="text/javascript" src="${paths.script_nav}"></script>
      </div>
    </div>
    <div class="right_frame" id="right" style="min-height: 0;">
      <script type="text/javascript" src="${paths.script_userlist}"></script>
    </div>
  </div>
  <div class="post_frame post_field input-group" style="min-height: 64px; flex: 0 0 auto;">
      <input type="text" class="form-control form-control-sm" autocomplete="off" id="message" maxlength="1024" value="" onkeydown="return submitChatInput(event);"></input>
      <div class="input-group-append">
        <input type="button" value="<fmt:message key="common.submission"/>" data-i18n="msg:common.submission" data-i18n-attr="value" class="btn btn-primary btn-sm" onclick="sendText();"></input>
      </div>
      <script type="text/javascript">
    App = {};
    let textarea = document.getElementById("message");
    if (!(App.emojiSelector instanceof Emojilist)) {
        App.emojiSelector = new Emojilist();
    }
    App.emojiSelector.attach({
        node: textarea,
        insert: function(emoji, event) {
            let startBefore = textarea.selectionStart,
            before = textarea.value.substr(0, startBefore),
            after = textarea.value.substr(textarea.selectionEnd);
            textarea.value = before + emoji + after;
            textarea.setSelectionRange(before.length+emoji.length, before.length+emoji.length);
            textarea.focus();
            /*event.preventDefault();
            event.stopPropagation();*/
        },
        categoryclick: function(event) {
            textarea.focus();
            /*event.preventDefault();
            event.stopPropagation();*/
        },
        offset: '15%',
        prepend: true,
        template: '<div class="popover" style="width: 50vw!important; max-width: 50vw!important; min-width: 50vw; height: 50vh!important; max-height: 50vh!important; min-height: 50vh!important;" role="tooltip"><div class="arrow"></div><div class="popover-body" style="position: relative; width: 100%!important; max-width: 100%!important; min-width: 100%; height: 100%!important; max-height: 100%!important; min-height: 100%!important; overflow-y: scroll;"></div></div>',
        pageTitle: function(title) {
            return {
                'Recent':'😁',
                'Smileys & People':'😀',
                'Animals & Nature':'🐱',
                'Food & Drink':'🍟',
                'Travel & Places':'🚄',
                'Activities':'⚽',
                'Objects':'💿',
                'Symbols':'❓',
                'Flags':'🏁'
            }[title];
        },
        container: 'body'
    });
    </script>
  </div>
</div>
  <script type="text/javascript" src="${paths.script_chat}"></script>
  <script type="text/javascript" src="${paths.script_post}"></script>
  <script type="text/javascript" src="${paths.script_color}"></script>
    
<script type="text/javascript">
<!--
  setTimeout(function () {
    $.ajax({
        type: 'GET',
        url: '${paths.webchat_json}',
        dataType: 'json',
        success: function (data) {
         for (var i=0;i<data.length;++i)
         {
            if(data[i].name == "${nick}") {
                $.farbtastic('#colorpicker').setColor("#"+data[i].color);
            }
         }
        }
    });
  },5000);
-->
</script></t:layout>