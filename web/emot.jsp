<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }
    // Rooms with an owner have their own colors
    String owner = request.getParameter("owner");
    String roomName = owner != null && !owner.isBlank() ? Web.roomNameByOwner(owner) : null;
    int perRow = Web.smiliesPerRow();
    java.util.Map<String, String> smilies = Web.smilies();
    if (smilies == null) {
        smilies = new java.util.TreeMap<>();
    }
    pageContext.setAttribute("design", roomName == null || roomName.isBlank() ? "info" : "napping_info");
    pageContext.setAttribute("smilies", smilies);
    pageContext.setAttribute("perRow", perRow > 0 ? perRow : Math.max(1, smilies.size()));
%><t:layout design="${design}"><h1><fmt:message key="common.list_of_emoticons"/></h1>
<table width="100%">
<c:forEach var="smiley" items="${smilies}" varStatus="status"><c:if test="${status.index % perRow == 0}"><tr></c:if><td>${smiley.key}: ${smiley.value}</td><c:if test="${status.last and status.count % perRow != 0}"><c:forEach begin="${status.count % perRow + 1}" end="${perRow}"><td></td></c:forEach></c:if><c:if test="${status.count % perRow == 0 or status.last}"></tr>
</c:if></c:forEach>
</table>

</t:layout>
