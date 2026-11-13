<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community"><div class="toplist">
  <div class="toplist-header">
    <h1><fmt:message key="common.top_list"/></h1>
    <p class="toplist-meta">
      <fmt:message key="common.it_is_in_this_chat"/> <strong>${reg_count}</strong> <fmt:message key="common.chatter_registered"/>
    </p>
  </div>
  <div class="toplist-table-wrap">
    <table class="toplist-table">
      <thead>
        <tr>
          <th class="col-rank"><fmt:message key="common.range"/></th>
          <th class="col-nick">Nick</th>
          <th class="col-points"><fmt:message key="common.points_2"/></th>
          <th class="col-reg"><fmt:message key="common.registered_since"/></th>
          <th class="col-login"><fmt:message key="common.last_login_2"/></th>
          <th class="col-online">Status</th>
        </tr>
      </thead>
      <tbody>
${toplist}
      </tbody>
    </table>
  </div>
</div>
</t:layout>