<%@ include file="/WEB-INF/jsp/includes/route.jspf" %><% if (!Web.begin("page", request, response)) return; %><% Web.loginChat(request, response, Web.requestValues(request)); %>
