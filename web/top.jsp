<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web,jakarta.servlet.http.HttpSession" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }
    // The top list looks like the rest of the community for users that are logged in
    String design = "start";
    HttpSession httpSession = request.getSession(false);
    if (httpSession != null && !httpSession.isNew() && "true".equals(httpSession.getAttribute("community"))) {
        String nick = (String) httpSession.getAttribute("nick");
        design = nick != null && Web.isRegistered(nick) ? "frameset_community_reg" : "frameset_community";
    }
    pageContext.setAttribute("design", design);
    pageContext.setAttribute("toplist", Web.toplist(request, response));
%><t:layout design="${design}"><div class="toplist">
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
