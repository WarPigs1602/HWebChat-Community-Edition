<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web,jakarta.servlet.http.HttpSession,java.util.HashMap,java.util.Map" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
    Web.start(application);
    if (!Web.begin("plain", request, response)) {
        return;
    }
    // The messages that were left in the chat: invalid (session), unregistered, new or none
    HttpSession httpSession = request.getSession(false);
    String sid = httpSession != null ? (String) httpSession.getAttribute("sid") : null;
    sid = sid != null ? sid : request.getParameter("sid");
    String owner = request.getParameter("owner");
    String roomName = owner != null && !owner.isBlank() ? Web.roomNameByOwner(owner) : null;
    boolean napping = roomName != null && !roomName.isBlank();
    String state;
    Map<String, Object> values = new HashMap<>();
    if (!Web.isValidConnectionId(sid)) {
        state = "invalid";
    } else {
        String nick = Web.nickOfConnection(sid);
        if (!Web.isRegistered(nick)) {
            state = "unregistered";
        } else {
            values.put("color", Web.userData(nick, "color"));
            if (Web.countMessages(nick) != 0) {
                state = "new";
                values.put("count", Web.countMessages(nick));
                pageContext.setAttribute("messages", Web.messages(nick, request));
                Web.deleteMessages(nick);
            } else {
                state = "none";
            }
        }
    }
    Web.view("page", request, values);
    pageContext.setAttribute("state", state);
    pageContext.setAttribute("error", state.equals("invalid") || state.equals("unregistered"));
    pageContext.setAttribute("napping", napping);
%><c:choose>
<c:when test="${error and napping}"><c:set var="design" value="napping_message_error"/></c:when>
<c:when test="${error}"><c:set var="design" value="message_error"/></c:when>
<c:when test="${napping}"><c:set var="design" value="napping_message"/></c:when>
<c:otherwise><c:set var="design" value="message"/></c:otherwise>
</c:choose><t:layout design="${design}"><c:choose>
<c:when test="${state eq 'invalid'}"><h1><fmt:message key="common.error_3"/></h1>

<fmt:message key="common.your_sessionid_is_invalid"/>!
</c:when>
<c:when test="${state eq 'unregistered'}"><h1><fmt:message key="common.error_3"/></h1>

<fmt:message key="common.you_are_not_registered"/>
</c:when>
<c:when test="${state eq 'new'}"><h1><fmt:message key="common.news"/></h1>

<p style="font-weight: bold">${com.message_new_header}</p>
${messages}
</c:when>
<c:otherwise><h1><fmt:message key="common.new_from"/> <span style="color: #${color};">${nick}</span></h1>

<fmt:message key="common.you_don_t_have_any"/>
</c:otherwise>
</c:choose></t:layout>
