<%@ page contentType="text/javascript; charset=UTF-8" session="false" isELIgnored="true" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("script", request, response)) { return; } %>if(top.frames.length > 0)
{
    top.location.href = self.location;
}

