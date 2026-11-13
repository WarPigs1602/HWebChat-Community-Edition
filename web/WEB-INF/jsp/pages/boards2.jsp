<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container-fluid py-4">
  <div class="row">
    <div class="col-12">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="mb-0">${SERVER_SOFTWARE}-Forum</h1>
        <a href="${paths.board}" class="btn btn-outline-secondary btn-sm">
          <i class="fa fa-arrow-left"></i> <fmt:message key="page.boards2.back"/>
        </a>
      </div>

      <nav aria-label="<fmt:message key="page.boards2.thread_navigation"/>" class="mb-3">
        <ul class="pagination justify-content-center">${prev} ${pages} ${next}</ul>
      </nav>

      <div class="card mb-4">
        <div class="card-body">
          <form method="POST" name="login" action="${paths.board}" target="_top" accept-charset="utf-8" class="input-group">
            <input class="form-control" name="search" placeholder="<fmt:message key="page.boards2.in_this_forum_search"/>">
            <input name="skin" value="${skin}" type="hidden">
            <div class="input-group-append">
              <button class="btn btn-primary" type="submit"><i class="fa fa-search"></i> <fmt:message key="common.search"/></button>
            </div>
          </form>
        </div>
      </div>

      <div class="forum-threads mb-4">${boards}</div>

      <div class="card forum-reply-card">
        <div class="card-header">
          <h5 class="mb-0"><i class="fa fa-reply"></i> <fmt:message key="page.boards2.response"/></h5>
        </div>
        <div class="card-body">
          <form method="POST" name="thread" action="${paths.board}&id=${id}&answer=true&pid=${pid}" target="_top" accept-charset="utf-8">
            <div class="form-group">
              <label for="reply-topic"><fmt:message key="page.boards2.subject"/></label>
              <input id="reply-topic" class="form-control" name="topic" value="${re}${topic}" required>
            </div>
            <div class="form-group">
              <label for="reply-content"><fmt:message key="page.boards2.message"/></label>
              <div class="btn-toolbar mb-2" role="toolbar" aria-label="<fmt:message key="common.bb_code_toolbar"/>">
                <div class="btn-group btn-group-sm mr-2" role="group" aria-label="<fmt:message key="common.text_formatting"/>">
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('reply-content','b')" title="<fmt:message key="common.fat_fat"/>"><i class="fa fa-bold"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('reply-content','i')" title="<fmt:message key="common.italics"/>"><i class="fa fa-italic"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('reply-content','u')" title="<fmt:message key="common.underlined"/>"><i class="fa fa-underline"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('reply-content','s')" title="<fmt:message key="common.crossed_out"/>"><i class="fa fa-strikethrough"></i></button>
                </div>
                <div class="btn-group btn-group-sm mr-2" role="group" aria-label="<fmt:message key="common.alignment_and_special_formatting"/>">
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('reply-content','center')" title="<fmt:message key="common.centred"/>"><i class="fa fa-align-center"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('reply-content','quote')" title="<fmt:message key="common.citation"/>"><i class="fa fa-quote-right"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('reply-content','code')" title="Code"><i class="fa fa-code"></i></button>
                </div>
                <div class="btn-group btn-group-sm" role="group" aria-label="<fmt:message key="common.link_image_color"/>">
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('reply-content','url')" title="Link"><i class="fa fa-link"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('reply-content','img')" title="<fmt:message key="common.picture"/>"><i class="fa fa-image"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('reply-content','email')" title="<fmt:message key="common.e_mail"/>"><i class="fa fa-envelope"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBColor('reply-content')" title="<fmt:message key="common.colour_2"/>"><i class="fa fa-tint"></i></button>
                </div>
              </div>
              <textarea id="reply-content" class="form-control" name="thread" rows="6" required></textarea>
            </div>
            <button class="btn btn-success" type="submit">
              <i class="fa fa-paper-plane"></i> <fmt:message key="page.boards2.submit_a_reply"/>
            </button>
          </form>
        </div>
      </div>

      <nav aria-label="<fmt:message key="page.boards2.thread_navigation"/>" class="mt-4">
        <ul class="pagination justify-content-center"><fmt:message key="page.boards2.text"><fmt:param value="${prev}"/><fmt:param value="${pages}"/><fmt:param value="${next}"/></fmt:message></ul>
      </nav>
    </div>
  </div>
</div>
<script>
(function() {
  function getArea(areaId) {
    var area = document.getElementById(areaId);
    return area && area.tagName.toLowerCase() === 'textarea' ? area : null;
  }
  function insertAtCursor(area, before, after) {
    var start = area.selectionStart || 0;
    var end = area.selectionEnd || 0;
    var text = area.value || '';
    var selected = text.substring(start, end);
    area.value = text.substring(0, start) + before + selected + after + text.substring(end);
    area.focus();
    var pos = start + before.length;
    if (selected) {
      pos = start + before.length + selected.length + after.length;
    }
    area.setSelectionRange(pos, pos);
  }
  window.insertBBCode = function(areaId, tag) {
    var area = getArea(areaId);
    if (!area || !tag) return;
    tag = String(tag).toLowerCase();
    var open, close, value;
    switch (tag) {
      case 'url':
        var url = prompt('<fmt:message key="common.enter_url"/>:', 'https://');
        if (url === null) return;
        url = url.trim() || 'https://';
        var start = area.selectionStart || 0;
        var end = area.selectionEnd || 0;
        var text = area.value || '';
        var selected = text.substring(start, end);
        var linkText = selected || url;
        insertAtCursor(area, '[url=' + url + ']', '[/url]');
        return;
      case 'img':
        var imgUrl = prompt('<fmt:message key="common.enter_picture_url"/>:', 'https://');
        if (imgUrl === null) return;
        imgUrl = imgUrl.trim();
        if (!imgUrl) return;
        insertAtCursor(area, '[img]' + imgUrl + '[/img]', '');
        return;
      case 'email':
        var mail = prompt('<fmt:message key="common.enter_e_mail_address"/>:', '');
        if (mail === null) return;
        mail = mail.trim();
        if (!mail) return;
        insertAtCursor(area, '[email]' + mail + '[/email]', '');
        return;
      default:
        open = '[' + tag + ']';
        close = '[/' + tag + ']';
    }
    insertAtCursor(area, open, close);
  };
  window.insertBBColor = function(areaId) {
    var area = getArea(areaId);
    if (!area) return;
    var color = prompt('<fmt:message key="common.enter_colour_as_hex_value"/>. ff0000):', 'ff0000');
    if (color === null) return;
    color = color.replace(/^#/, '').trim() || '000000';
    insertAtCursor(area, '[color=' + color + ']', '[/color]');
  };
})();
</script>
</t:layout>