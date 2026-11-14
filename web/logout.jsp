<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web,jakarta.servlet.http.HttpSession" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
    Web.start(application);
    if (!Web.begin("plain", request, response)) {
        return;
    }
    // done, failed (online as guest), failed_reg (online as registered user) or timeout (no session)
    String state = "timeout";
    HttpSession httpSession = request.getSession(false);
    if (httpSession != null) {
        String nick = (String) httpSession.getAttribute("nick");
        if (nick != null && Web.isOnline(nick.toLowerCase())) {
            state = Web.isRegistered(nick) ? "failed_reg" : "failed";
        } else {
            if (nick != null) {
                Web.removeCommunityUser(nick);
            }
            httpSession.invalidate();
            state = "done";
        }
    }
    Web.view("page", request);
    pageContext.setAttribute("state", state);
%><c:choose>
<c:when test="${state eq 'failed'}"><c:set var="design" value="frameset_community"/></c:when>
<c:when test="${state eq 'failed_reg'}"><c:set var="design" value="frameset_community_reg"/></c:when>
<c:otherwise><c:set var="design" value="start"/></c:otherwise>
</c:choose><t:layout design="${design}"><c:choose>
<c:when test="${state eq 'timeout'}"><h1>${SERVER_SOFTWARE}-Timeout</h1>


<p>
<fmt:message key="page.timeout.your_session_expired_because_of"/>...
</p></c:when>
<c:when test="${state eq 'done'}"><h1>${SERVER_SOFTWARE}</h1>
<p>
<fmt:message key="page.logout.you_have_successfully_logged_out"/>
</p>
</c:when>
<c:otherwise><h1>${SERVER_SOFTWARE}</h1>
<p>
<fmt:message key="common.you_have_not_been_logged"/>
</p>
</c:otherwise>
</c:choose></t:layout>
