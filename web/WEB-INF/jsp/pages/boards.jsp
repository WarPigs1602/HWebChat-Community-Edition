<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container-fluid py-4">
  <div class="row">
    <div class="col-12">
      <h1 class="mb-4">${SERVER_SOFTWARE}-Forum</h1>
      
      <!-- Pagination oben -->
      <nav aria-label="<fmt:message key="common.forum_navigation"/>" class="mb-3">
        <ul class="pagination justify-content-center">
          ${prev} ${pages} ${next}
        </ul>
      </nav>
      
      <!-- Suchleiste -->
      <div class="card mb-4">
        <div class="card-body">
          <form method="POST" name="login" action="${paths.board}" target="_top" accept-charset="utf-8" class="form-row align-items-center">
            <div class="col-md-8">
              <input class="form-control" name="search" placeholder="<fmt:message key="common.search_the_forum"/>">
            </div>
            <div class="col-md-2">
              <button class="btn btn-primary w-100" type="submit">
                <i class="fa fa-search"></i> <fmt:message key="common.search"/>
              </button>
            </div>
            <div class="col-md-2">
              <a href="${paths.board}&id=${id}&new=true" class="btn btn-success w-100">
                <i class="fa fa-plus"></i> <fmt:message key="page.boards.new_contribution"/>
              </a>
            </div>
            <input name="skin" value="${skin}" type="hidden">
          </form>
        </div>
      </div>
      
      <!-- Boards-Liste -->
      <div class="boards-list">
        ${boards}
      </div>
      
      <!-- Pagination unten -->
      <nav aria-label="<fmt:message key="common.forum_navigation"/>" class="mt-4">
        <ul class="pagination justify-content-center">
          ${prev} ${pages} ${next}
        </ul>
      </nav>
    </div>
  </div>
</div>
</t:layout>