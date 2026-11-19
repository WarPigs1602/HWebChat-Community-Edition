<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true" import="java.io.*, java.net.*, java.nio.charset.StandardCharsets, java.nio.file.*, java.util.*, jakarta.servlet.http.HttpServletResponse, jakarta.servlet.http.Part, net.midiandmore.chat.*" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%
    Web.start(application);
    if (!Web.isReady()) {
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        return;
    }
    if (!Web.isModuleEnabled("file_manager")) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    var administrator = Web.consoleAdministrator(request);
    if (administrator == null) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }
    var method = request.getMethod();
    if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
        response.setHeader("Allow", "GET, POST");
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    if (!Web.begin("page", request, response)) {
        return;
    }
    response.setHeader("Cache-Control", "no-store");
    var lang = (String) request.getAttribute("lang");
    var store = new ManagedFileStore();
    var success = "";
    var error = "";

    if ("GET".equalsIgnoreCase(method)) {
        var download = request.getParameter("download");
        if (download != null) {
            Path file;
            try {
                file = store.file(download);
            } catch (IllegalArgumentException | NoSuchFileException ex) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            var encodedName = URLEncoder.encode(file.getFileName().toString(), StandardCharsets.UTF_8)
                    .replace("+", "%20").replace("*", "%2A");
            response.setContentType("application/octet-stream");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"download\"; filename*=UTF-8''" + encodedName);
            response.setContentLengthLong(Files.size(file));
            try (var input = Files.newInputStream(file); var output = response.getOutputStream()) {
                input.transferTo(output);
            }
            return;
        }
    } else {
        var session = request.getSession(false);
        var token = session == null ? null : Web.consoleCsrf(session);
        var submitted = request.getParameter("csrf");
        if (token == null || submitted == null || !java.security.MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8), submitted.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        var action = request.getParameter("action");
        try {
            if ("upload".equals(action)) {
                Part filePart = request.getPart("file");
                if (filePart == null || filePart.getSubmittedFileName() == null) {
                    error = "console.files.error_filename";
                } else if (filePart.getSize() > ManagedFileStore.MAX_FILE_SIZE) {
                    error = "console.files.error_too_large";
                } else {
                    try (var input = filePart.getInputStream()) {
                        store.store(filePart.getSubmittedFileName(), input);
                    }
                    success = "console.files.upload_success";
                }
            } else if ("delete".equals(action)) {
                store.delete(request.getParameter("file"));
                success = "console.files.delete_success";
            } else if ("publish".equals(action)) {
                var published = request.getParameter("published");
                if (!"1".equals(published) && !"0".equals(published)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                store.setPublished(request.getParameter("file"), "1".equals(published));
                success = "1".equals(published)
                        ? "console.files.publish_success"
                        : "console.files.unpublish_success";
            } else if ("metadata".equals(action)) {
                store.updateMetadata(request.getParameter("file"), request.getParameter("title"),
                        request.getParameter("category"));
                success = "console.files.metadata_success";
            } else if ("add_category".equals(action)) {
                store.addCategory(request.getParameter("category"));
                success = "console.files.category_added";
            } else if ("delete_category".equals(action)) {
                store.removeCategory(request.getParameter("category"));
                success = "console.files.category_deleted";
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (ManagedFileStore.ValidationException ex) {
            error = ex.getMessageKey();
        } catch (IllegalArgumentException ex) {
            error = "console.files.error_filename";
        } catch (FileAlreadyExistsException ex) {
            error = "console.files.error_exists";
        } catch (ManagedFileStore.LimitExceededException ex) {
            error = "console.files.error_storage_limit";
        } catch (NoSuchFileException ex) {
            error = "console.files.error_missing";
        } catch (IOException ex) {
            Bootstrap.logError(ex);
            error = "console.files.error_generic";
        }
    }

    var consoleValues = new HashMap<String, Object>();
    consoleValues.put("nick", "");
    consoleValues.put("sid", "");
    consoleValues.put("skin", "");
    consoleValues.put("room", "");
    consoleValues.put("reason", "");
    Web.view("page", request, consoleValues);
    request.setAttribute("console_administrator", administrator);
    request.setAttribute("console_csrf", Web.consoleCsrf(request.getSession(false)));
    request.setAttribute("file_manager_success", success);
    request.setAttribute("file_manager_error", error);
    try {
        request.setAttribute("managed_categories", store.categories());
        request.setAttribute("managed_files", store.list());
    } catch (IOException ex) {
        Bootstrap.logError(ex);
        request.setAttribute("file_manager_error", "console.files.error_generic");
    }
%>
<t:layout design="console">
  <h1><fmt:message key="console.files.title"/></h1>
  <p><fmt:message key="console.files.intro"/></p>
  <c:if test="${modules.download_archive ne '0' and modules.file_manager ne '0'}">
    <p><a class="btn btn-outline-primary btn-sm" href="${paths.downloads}"><fmt:message key="console.files.open_archive"/></a></p>
  </c:if>
  <c:if test="${not empty file_manager_success}"><div class="alert alert-success" role="status"><fmt:message key="${file_manager_success}"/></div></c:if>
  <c:if test="${not empty file_manager_error}"><div class="alert alert-danger" role="alert"><fmt:message key="${file_manager_error}"/></div></c:if>
  <section class="mb-4">
    <h2><fmt:message key="console.files.categories"/></h2>
    <form action="${paths.file_manager}" method="POST" class="form-inline mb-2">
      <input type="hidden" name="csrf" value="<c:out value='${console_csrf}'/>">
      <input type="hidden" name="action" value="add_category">
      <label for="managed-category" class="mr-2"><fmt:message key="console.files.new_category"/></label>
      <input id="managed-category" class="form-control form-control-sm mr-2" type="text" name="category" maxlength="80" required>
      <button class="btn btn-outline-primary btn-sm" type="submit"><fmt:message key="console.files.add_category"/></button>
    </form>
    <ul class="list-inline">
      <c:forEach var="category" items="${managed_categories}">
        <li class="list-inline-item">
          <c:out value="${category}"/>
          <form action="${paths.file_manager}" method="POST" class="d-inline">
            <input type="hidden" name="csrf" value="<c:out value='${console_csrf}'/>">
            <input type="hidden" name="action" value="delete_category">
            <input type="hidden" name="category" value="<c:out value='${category}'/>">
            <button class="btn btn-link btn-sm text-danger" type="submit"><fmt:message key="console.files.delete_category"/></button>
          </form>
        </li>
      </c:forEach>
      <c:if test="${empty managed_categories}"><li><fmt:message key="console.files.no_categories"/></li></c:if>
    </ul>
  </section>
  <form action="${paths.file_manager}" method="POST" enctype="multipart/form-data" class="form-inline mb-3">
    <input type="hidden" name="csrf" value="<c:out value='${console_csrf}'/>">
    <input type="hidden" name="action" value="upload">
    <label for="managed-file" class="mr-2"><fmt:message key="console.files.upload"/></label>
    <input id="managed-file" class="form-control form-control-sm mr-2" type="file" name="file" required>
    <button class="btn btn-primary btn-sm" type="submit"><fmt:message key="console.files.upload"/></button>
  </form>
  <table class="table table-sm">
    <thead><tr>
      <th><fmt:message key="console.files.name"/></th>
      <th><fmt:message key="console.files.size"/></th>
      <th><fmt:message key="console.files.modified"/></th>
      <th><fmt:message key="console.files.details"/></th>
      <th><fmt:message key="console.files.publication"/></th>
      <th></th>
    </tr></thead>
    <tbody>
      <c:forEach var="entry" items="${managed_files}">
        <c:url var="download_url" value="/file-manager.jsp"><c:param name="download" value="${entry.name}"/></c:url>
        <tr>
          <td><c:out value="${entry.name}"/></td>
          <td><c:out value="${entry.size}"/></td>
          <td><c:out value="${entry.modified}"/></td>
          <td>
            <form action="${paths.file_manager}" method="POST" class="form-inline">
              <input type="hidden" name="csrf" value="<c:out value='${console_csrf}'/>">
              <input type="hidden" name="action" value="metadata">
              <input type="hidden" name="file" value="<c:out value='${entry.name}'/>">
              <input class="form-control form-control-sm mr-2" type="text" name="title" aria-label="<fmt:message key='console.files.title'/>" value="<c:out value='${entry.title}'/>" maxlength="200">
              <select class="form-control form-control-sm mr-2" name="category" aria-label="<fmt:message key='console.files.category'/>">
                <option value="" ${empty entry.category ? 'selected' : ''}><fmt:message key="console.files.uncategorized"/></option>
                <c:forEach var="category" items="${managed_categories}">
                  <option value="<c:out value='${category}'/>" ${entry.category eq category ? 'selected' : ''}><c:out value="${category}"/></option>
                </c:forEach>
              </select>
              <button class="btn btn-outline-secondary btn-sm" type="submit"><fmt:message key="console.files.save_metadata"/></button>
            </form>
          </td>
          <td><fmt:message key="${entry.published ? 'console.files.published' : 'console.files.private'}"/></td>
          <td class="text-nowrap">
            <a class="btn btn-outline-secondary btn-sm" href="<c:out value='${download_url}'/>"><fmt:message key="console.files.download"/></a>
            <form action="${paths.file_manager}" method="POST" class="d-inline">
              <input type="hidden" name="csrf" value="<c:out value='${console_csrf}'/>">
              <input type="hidden" name="action" value="publish">
              <input type="hidden" name="file" value="<c:out value='${entry.name}'/>">
              <input type="hidden" name="published" value="${entry.published ? '0' : '1'}">
              <button class="btn btn-outline-primary btn-sm" type="submit"><fmt:message key="${entry.published ? 'console.files.unpublish' : 'console.files.publish'}"/></button>
            </form>
            <form action="${paths.file_manager}" method="POST" class="d-inline">
              <input type="hidden" name="csrf" value="<c:out value='${console_csrf}'/>">
              <input type="hidden" name="action" value="delete">
              <input type="hidden" name="file" value="<c:out value='${entry.name}'/>">
              <button class="btn btn-outline-danger btn-sm" type="submit"><fmt:message key="console.files.delete"/></button>
            </form>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty managed_files}"><tr><td colspan="6"><fmt:message key="console.files.empty"/></td></tr></c:if>
    </tbody>
  </table>
</t:layout>
