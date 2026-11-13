<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="alert alert-success" role="status">
        <h4 class="alert-heading"><i class="fa fa-check-circle"></i> <fmt:message key="page.board_added.category_created"/></h4>
        <p><fmt:message key="page.board_added.your_category_has_been_successfully"/></p>
        <hr>
        <a href="${paths.board}" class="btn btn-primary btn-sm"><i class="fa fa-arrow-left"></i> <fmt:message key="common.start_page"/></a>
      </div>
    </div>
  </div>
</div>
</t:layout>