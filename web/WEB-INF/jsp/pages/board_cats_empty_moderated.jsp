<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><div class="alert alert-light text-center py-4" role="status">
  <i class="fa fa-folder-open-o fa-2x text-muted mb-2"></i>
  <p class="mb-2 text-muted"><fmt:message key="page.board_cats_empty_moderated.there_are_no_categories_yet"/></p>
  <a href="${paths.board}&add_board=true" class="btn btn-success btn-sm">
    <i class="fa fa-plus"></i> <fmt:message key="page.board_cats_empty_moderated.create_a_new_category"/>
  </a>
</div>
