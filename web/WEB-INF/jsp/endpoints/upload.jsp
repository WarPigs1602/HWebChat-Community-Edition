<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" import="java.io.ByteArrayInputStream, java.io.ByteArrayOutputStream, java.io.IOException, java.util.HashMap, java.util.Map, jakarta.servlet.http.Part, net.midiandmore.chat.Web" %><%
    if (!"POST".equals(request.getMethod())) {
        response.setHeader("Allow", "POST");
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST method required.");
        return;
    }
    Web.start(application);
    if (!Web.isReady()) {
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Setup required");
        return;
    }
    Part filePart = request.getPart("file");
    if (filePart == null) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file uploaded");
        return;
    }
    String contentType = filePart.getContentType();
    response.setContentType("text/html; charset=" + Web.charset());
    Map<String, String> values = new HashMap<>();
    for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
        values.put(entry.getKey(), entry.getValue()[0]);
    }
    String nick = (String) request.getSession().getAttribute("nick");
    boolean community = Web.useCommunityTemplate(request.getSession().getAttribute("community") != null,
            values.get("upload_context"));
    String suffix = community ? "_com" : "";
    try {
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            Web.print("picture_not_picture" + suffix, request, response, values);
        } else if (nick != null && Web.isRegistered(nick)) {
            byte[] imageBytes;
            try (java.io.InputStream in = filePart.getInputStream(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                in.transferTo(bytes);
                imageBytes = bytes.toByteArray();
            }
            if (imageBytes.length == 0) {
                throw new IOException("Upload ist leer");
            }
            Web.storePicture(nick, new ByteArrayInputStream(imageBytes), contentType);
            Web.print("picture_success" + suffix, request, response, values);
        } else {
            Web.print("picture_not_registered" + suffix, request, response, values);
        }
    } catch (IOException ex) {
        Web.printError("picture_error" + suffix, request, response, values, ex.getLocalizedMessage());
    }
%>
