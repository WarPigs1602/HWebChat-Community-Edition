<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><div class="card forum-board-card mb-3">
  <div class="card-body">
    <div class="row align-items-center">
      <div class="col-auto">
        <div class="forum-board-icon">
          <i class="fa fa-comments"></i>
        </div>
      </div>
      <div class="col">
        <h5 class="card-title mb-1">
          <a href="${paths.board}&id=${id}" class="text-decoration-none">${topic}</a>
        </h5>
        <p class="card-text text-dark mb-2">${description}</p>
        <div class="forum-board-stats small text-dark">
          <span class="mr-3"><i class="fa fa-file-text-o"></i> <strong>${topics}</strong> <fmt:message key="page.board_boards_moderated.contributions"/></span>
          <span><i class="fa fa-comments-o"></i> <strong>${threads}</strong> Threads</span>
        </div>
      </div>
      <div class="col-auto">
        <div class="btn-group btn-group-sm forum-board-actions" role="group">
          <fmt:message key="page.board_boards_moderated.edit"><fmt:param value="${lock}"/><fmt:param value="${del}"/><fmt:param value="${edit}"/></fmt:message>
        </div>
      </div>
    </div>
  </div>
</div>
