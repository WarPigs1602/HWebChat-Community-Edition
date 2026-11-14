<%@ page session="false" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.Web" %><%
    Web.start(application);
    if (!Web.begin("plain", request, response)) {
        return;
    }
    // Information for chat portals such as http://www.webchat.de
    boolean json = "1".equals(request.getParameter("use_json"));
    response.setContentType(json ? "application/json" : "text/plain");
    out.print(Web.webChat(json));
%>