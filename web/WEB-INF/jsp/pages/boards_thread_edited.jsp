<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="alert alert-success" role="status">
        <h4 class="alert-heading"><i class="fa fa-check-circle"></i> <fmt:message key="page.boards_thread_edited.contribution_stored"/></h4>
        <p><fmt:message key="page.boards_thread_edited.the_contribution_was_successfully_processed"/></p>
        <hr>
        <a href="${paths.board}&pid=${pid}&id=${id}" class="btn btn-primary btn-sm"><i class="fa fa-arrow-right"></i> <fmt:message key="common.contribution_2"/></a>
        <a href="${paths.board}" class="btn btn-outline-secondary btn-sm"><i class="fa fa-home"></i> <fmt:message key="common.forum_overview_2"/></a>
      </div>
    </div>
  </div>
</div>
</t:layout>