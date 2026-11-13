<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><article class="card forum-thread-card mb-3 border-secondary">
  <div class="card-header bg-light">
    <div class="d-flex justify-content-between align-items-center flex-wrap">
      <small class="text-muted">
        <i class="fa fa-clock-o"></i> ${posted} &middot;
        <i class="fa fa-user"></i> <strong style="color: #${color}">${user}</strong>
      </small>
      <div class="forum-thread-actions btn-group btn-group-sm" role="group">${delete}</div>
    </div>
  </div>
  <div class="card-body forum-thread-content text-muted">
    <i class="fa fa-trash-o"></i> <fmt:message key="common.this_entry_has_been_deleted"/>.
  </div>
</article>
