<%@ tag description="Style sheet: the body is prepared for the browser (paths of delivered files, dark theme part)" body-content="scriptless" trimDirectiveWhitespaces="false" import="net.midiandmore.chat.Web" %><jsp:doBody var="css"/><%
    var page = (jakarta.servlet.jsp.PageContext) jspContext;
    out.print(Web.css((String) jspContext.getAttribute("css"), (jakarta.servlet.http.HttpServletRequest) page.getRequest(),
            (jakarta.servlet.http.HttpServletResponse) page.getResponse()));
%>
