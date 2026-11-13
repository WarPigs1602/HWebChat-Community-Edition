<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="alert alert-danger" role="alert">
        <h4 class="alert-heading"><i class="fa fa-frown-o"></i> <fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h4>
        <p><fmt:message key="page.board_unknown.the_requested_document_does_not"/></p>
        <hr>
        <a href="${paths.board}" class="btn btn-primary btn-sm"><i class="fa fa-arrow-left"></i> <fmt:message key="common.forum_overview"/></a>
      </div>
    </div>
  </div>
</div>
</t:layout>