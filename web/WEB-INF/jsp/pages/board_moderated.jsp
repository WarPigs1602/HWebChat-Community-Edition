<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container-fluid py-4">
  <div class="row">
    <div class="col-12">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="mb-0">${SERVER_SOFTWARE}-Forum</h1>
        <a href="${paths.board}&add_board=true" class="btn btn-success btn-sm">
          <i class="fa fa-plus"></i> <fmt:message key="page.board_moderated.new_category"/>
        </a>
      </div>
      <div class="forum-content">
        ${boards}
      </div>
    </div>
  </div>
</div>
</t:layout>