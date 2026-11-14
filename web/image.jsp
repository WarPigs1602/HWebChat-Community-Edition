<%@ page contentType="image/png" session="false" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.Web" %><%
    Web.start(application);
    if (!Web.isReady()) {
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Setup required");
        return;
    }
    Web.profilePicture(request.getParameter("nick"), response);
%>
