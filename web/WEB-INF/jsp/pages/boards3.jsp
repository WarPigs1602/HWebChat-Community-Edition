<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container-fluid py-4">
  <div class="row">
    <div class="col-12">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="mb-0">${SERVER_SOFTWARE}-Forum</h1>
        <a href="${paths.board}" class="btn btn-outline-secondary btn-sm">
          <i class="fa fa-home"></i> <fmt:message key="common.forum_overview_2"/>
        </a>
      </div>

      <nav aria-label="<fmt:message key="common.forum_navigation"/>" class="mb-3">
        <ul class="pagination justify-content-center">${prev} ${pages} ${next}</ul>
      </nav>

      <div class="card mb-4">
        <div class="card-body">
          <form method="POST" name="login" action="${paths.board}" target="_top" accept-charset="utf-8" class="form-row align-items-center">
            <div class="col-md-9 mb-2 mb-md-0">
              <input class="form-control" name="search" placeholder="<fmt:message key="common.search_the_forum"/>">
            </div>
            <div class="col-md-3">
              <button class="btn btn-primary w-100" type="submit">
                <i class="fa fa-search"></i> <fmt:message key="common.search"/>
              </button>
            </div>
            <input name="skin" value="${skin}" type="hidden">
          </form>
        </div>
      </div>

      <div class="forum-threads mb-4">${boards}</div>

      <nav aria-label="<fmt:message key="common.forum_navigation"/>" class="mt-4">
        <ul class="pagination justify-content-center">${prev} ${pages} ${next}</ul>
      </nav>
    </div>
  </div>
</div>
</t:layout>