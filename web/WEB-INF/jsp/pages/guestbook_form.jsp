<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><div class="guestbook-form mt-3">
  <form action="${paths.profile}" method="get" accept-charset="utf-8">
    <input type="hidden" name="user" value="${user}">
    <input type="hidden" name="skin" value="${skin}">
    <input type="hidden" name="owner" value="${owner}">
    <input type="hidden" name="guestbook" value="1">
    <div class="input-group">
      <textarea class="form-control form-control-sm" name="guestbook_text" rows="2" placeholder="<fmt:message key="page.guestbook_form.write_a_guestbook_entry"/>"></textarea>
      <button class="btn btn-primary btn-sm" type="submit"><fmt:message key="page.guestbook_form.submit"/></button>
    </div>
  </form>
</div>
