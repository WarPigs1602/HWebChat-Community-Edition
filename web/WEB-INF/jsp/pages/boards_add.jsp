<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="card forum-form-card">
        <div class="card-header">
          <h2 class="h4 mb-0"><i class="fa fa-plus-circle"></i> <fmt:message key="page.boards_add.opening_a_new_thread"/></h2>
        </div>
        <div class="card-body">
          <form method="POST" name="thread" action="${paths.board}&id=${id}&new=true&add=true" target="_top" accept-charset="utf-8">
            <div class="form-group">
              <label for="new-topic"><fmt:message key="common.title"/></label>
              <input id="new-topic" class="form-control" name="topic" maxlength="255" required autofocus>
            </div>
            <div class="form-group">
              <label for="new-thread"><fmt:message key="common.contribution"/></label>
              <div class="btn-toolbar mb-2" role="toolbar" aria-label="<fmt:message key="common.bb_code_toolbar"/>">
                <div class="btn-group btn-group-sm mr-2" role="group" aria-label="<fmt:message key="common.text_formatting"/>">
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('new-thread','b')" title="<fmt:message key="common.fat_fat"/>"><i class="fa fa-bold"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('new-thread','i')" title="<fmt:message key="common.italics"/>"><i class="fa fa-italic"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('new-thread','u')" title="<fmt:message key="common.underlined"/>"><i class="fa fa-underline"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('new-thread','s')" title="<fmt:message key="common.crossed_out"/>"><i class="fa fa-strikethrough"></i></button>
                </div>
                <div class="btn-group btn-group-sm mr-2" role="group" aria-label="<fmt:message key="common.alignment_and_special_formatting"/>">
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('new-thread','center')" title="<fmt:message key="common.centred"/>"><i class="fa fa-align-center"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('new-thread','quote')" title="<fmt:message key="common.citation"/>"><i class="fa fa-quote-right"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('new-thread','code')" title="Code"><i class="fa fa-code"></i></button>
                </div>
                <div class="btn-group btn-group-sm" role="group" aria-label="<fmt:message key="common.link_image_color"/>">
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('new-thread','url')" title="Link"><i class="fa fa-link"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('new-thread','img')" title="<fmt:message key="common.picture"/>"><i class="fa fa-image"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('new-thread','email')" title="<fmt:message key="common.e_mail"/>"><i class="fa fa-envelope"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBColor('new-thread')" title="<fmt:message key="common.colour_2"/>"><i class="fa fa-tint"></i></button>
                </div>
              </div>
              <textarea id="new-thread" class="form-control" name="thread" rows="10" required></textarea>
            </div>
            <div class="d-flex justify-content-between">
              <a href="${paths.board}&id=${id}" class="btn btn-outline-secondary">
                <i class="fa fa-times"></i> <fmt:message key="common.abortions"/>
              </a>
              <button class="btn btn-success" type="submit">
                <i class="fa fa-paper-plane"></i> <fmt:message key="page.boards_add.opening_the_thread"/>
              </button>
            </div>
          </form>
        </div>
      </div>
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