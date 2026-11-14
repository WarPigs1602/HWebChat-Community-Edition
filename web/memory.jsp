<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.Web" %><%
    Web.start(application);
    if (!Web.begin("plain", request, response)) {
        return;
    }
    Runtime runtime = Runtime.getRuntime();
    long total = runtime.totalMemory();
%>Chatserver: <%= (total - runtime.freeMemory()) >> 20 %> MB used (<%= total >> 20 %> MB reserved)<br>
