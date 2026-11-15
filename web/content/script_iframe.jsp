<%@ page contentType="text/javascript; charset=UTF-8" session="false" isELIgnored="true" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("script", request, response)) { return; } %>//By jQuery scrollTop

$("#chat").scrollTop($("#chat")[0].scrollHeight);