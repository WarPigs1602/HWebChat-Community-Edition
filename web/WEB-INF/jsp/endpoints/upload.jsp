<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" import="java.io.ByteArrayInputStream, java.io.ByteArrayOutputStream, java.io.IOException, java.util.HashMap, java.util.Map, jakarta.servlet.http.Part, net.midiandmore.chat.Web" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
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
    String state;
    try {
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            state = "not_picture";
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
            state = "success";
        } else {
            state = "not_registered";
        }
    } catch (IOException ex) {
        state = "error";
        request.setAttribute("message", ex.getLocalizedMessage());
    }
    Web.view("page", request);
    request.setAttribute("uploadState", state);
    request.setAttribute("uploadDesign", community ? "frameset_community_reg" : "account");
%><t:layout design="${uploadDesign}"><c:choose>
<c:when test="${uploadState eq 'not_picture'}"><h1><fmt:message key="common.image_upload"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


  <p class="form-section-title">
    <fmt:message key="common.image_could_not_be_uploaded"/>
  </p>
  <p>
    <fmt:message key="common.your_profile_picture_could_not_2"/> <span style="font-weight: bold;"><fmt:message key="common.file_is_not_an_image"/></span>!
  </p>
  <p class="form-section-title">
    &nbsp;
  </p>
</c:when>
<c:when test="${uploadState eq 'success'}"><h1><fmt:message key="common.profile_picture_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


  <p class="form-section-title">
    <fmt:message key="common.image_successfully_uploaded"/>
  </p>
  <p>
    <fmt:message key="common.your_profile_picture_has_been_2"/>
  </p>
  <p class="form-section-title">
    &nbsp;
  </p>
</c:when>
<c:when test="${uploadState eq 'not_registered'}"><h1><fmt:message key="common.profile_picture_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


  <p class="form-section-title">
    <fmt:message key="common.image_could_not_be_uploaded"/>
  </p>
  <p>
    <fmt:message key="common.your_profile_picture_could_not_2"/> <span style="font-weight: bold;"><fmt:message key="common.user_is_not_registered"/></span>!
  </p>
  <p class="form-section-title">
    &nbsp;
  </p>
</c:when>
<c:otherwise><h1><fmt:message key="common.profile_picture_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


  <p class="form-section-title">
    <fmt:message key="common.image_could_not_be_uploaded"/>
  </p>
  <p>
    <fmt:message key="common.your_profile_picture_could_not_2"/> <span style="font-weight: bold;">${message}</span>!
  </p>
  <p class="form-section-title">
    &nbsp;
  </p>
</c:otherwise>
</c:choose></t:layout>
