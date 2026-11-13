<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community_reg"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="card forum-form-card">
        <div class="card-header">
          <h2 class="h4 mb-0"><i class="fa fa-comments"></i> <fmt:message key="common.opening_a_new_forum"/></h2>
        </div>
        <div class="card-body">
          <form method="POST" name="tread" action="${paths.board}&cat=${cat}&add_cat=true" target="_top" accept-charset="utf-8">
            <div class="form-group">
              <label for="board-topic">Name</label>
              <input id="board-topic" class="form-control" name="topic" required autofocus>
            </div>
            <div class="form-group">
              <label for="board-description"><fmt:message key="common.description_2"/></label>
              <textarea id="board-description" class="form-control" name="description" rows="4" required></textarea>
            </div>
            <div class="d-flex justify-content-between">
              <a href="${paths.board}" class="btn btn-outline-secondary"><i class="fa fa-times"></i> <fmt:message key="common.abortions"/></a>
              <button class="btn btn-success" type="submit"><i class="fa fa-plus"></i> <fmt:message key="common.opening_of_the_forum"/></button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>
</t:layout>