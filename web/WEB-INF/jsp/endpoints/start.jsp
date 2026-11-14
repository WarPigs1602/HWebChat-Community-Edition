<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.Web" %><%
    // The chat writes the page itself (sometimes binary data), so nothing else may be written here.
    if (request.getParameter("page") != null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    Web.start(application);
    Web.page("start", request, response);
%>
