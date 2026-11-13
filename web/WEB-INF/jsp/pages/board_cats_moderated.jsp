<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><section class="card forum-category-card mb-4">
  <div class="card-header bg-dark text-white">
    <h3 class="h5 mb-0"><i class="fa fa-folder-open"></i> ${topic}</h3>
  </div>
  <div class="card-body p-0">
    <div class="forum-category-description px-3 py-2 text-muted border-bottom">${description}</div>
    <div class="forum-category-boards p-3">${boards}</div>
  </div>
</section>
