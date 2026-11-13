<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><section class="card forum-category-card mb-4">
  <div class="card-header bg-dark text-white">
    <div class="d-flex justify-content-between align-items-center flex-wrap">
      <h3 class="h5 mb-0" style="color: #fff !important;"><i class="fa fa-folder-open" style="color: #fff !important;"></i> ${topic}</h3>
      <div class="btn-group btn-group-sm mt-2 mt-sm-0" role="group">${edit} ${delete}</div>
    </div>
  </div>
  <div class="card-body p-0">
    <div class="forum-category-description px-3 py-2 text-dark border-bottom">${description}</div>
    <div class="forum-category-boards p-3">${boards}</div>
  </div>
  <div class="card-footer bg-light border-top-0">
    <a href="${paths.board}&add_cat=true&cat=${id}" class="btn btn-success btn-sm">
      <i class="fa fa-plus"></i> <fmt:message key="common.creating_a_new_forum"/>
    </a>
  </div>
</section>
