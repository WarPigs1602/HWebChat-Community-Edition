<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="alert alert-danger" role="status">
        <h4 class="alert-heading"><i class="fa fa-ban"></i> <fmt:message key="page.boards_ban.user_banned"/></h4>
        <p><fmt:message key="common.the_user"/> <strong style="color: #${color}">${user}</strong> <fmt:message key="page.boards_ban.they_were_banned_from_the"/></p>
        <hr>
        <a href="${paths.board}" class="btn btn-primary btn-sm"><i class="fa fa-arrow-left"></i> <fmt:message key="common.forum_overview"/></a>
      </div>
    </div>
  </div>
</div>
</t:layout>