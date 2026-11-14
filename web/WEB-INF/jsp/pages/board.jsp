<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container-fluid py-4 forum-page">
  <div class="row">
    <div class="col-12">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="mb-0">${SERVER_SOFTWARE}-Forum</h1>
        <a href="${paths.board}" class="btn btn-outline-secondary btn-sm">
          <i class="fa fa-home"></i> <fmt:message key="page.board.forum_overview"/>
        </a>
      </div>
      <div class="forum-content">
        ${boards}
      </div>
    </div>
  </div>
</div>
</t:layout>