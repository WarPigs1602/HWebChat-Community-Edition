<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true" import="java.net.URLEncoder, java.nio.charset.StandardCharsets, java.nio.file.Files, java.nio.file.NoSuchFileException, java.nio.file.Path, java.util.*, jakarta.servlet.http.HttpServletResponse, net.midiandmore.chat.*" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%
    Web.start(application);
    if (!Web.isReady()) {
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        return;
    }
    if (!Web.isModuleEnabled("download_archive") || !Web.isModuleEnabled("file_manager")) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    if (!"GET".equalsIgnoreCase(request.getMethod())) {
        response.setHeader("Allow", "GET");
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    if (!Web.begin("page", request, response)) {
        return;
    }
    response.setHeader("Cache-Control", "no-store");
    var store = new ManagedFileStore();
    var filename = request.getParameter("file");
    if (filename != null) {
        Path file;
        try {
            if (!store.isPublished(filename)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            file = store.file(filename);
        } catch (IllegalArgumentException | NoSuchFileException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (java.io.IOException ex) {
            Bootstrap.logError(ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        var encodedName = URLEncoder.encode(file.getFileName().toString(), StandardCharsets.UTF_8)
                .replace("+", "%20").replace("*", "%2A");
        response.setContentType("application/octet-stream");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"download\"; filename*=UTF-8''" + encodedName);
        try {
            response.setContentLengthLong(Files.size(file));
            try (var input = Files.newInputStream(file); var output = response.getOutputStream()) {
                input.transferTo(output);
            }
        } catch (java.io.IOException ex) {
            Bootstrap.logError(ex);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        return;
    }
    List<String> categories;
    List<ManagedFileStore.Entry> allFiles;
    try {
        categories = store.categories();
        allFiles = store.publishedFiles();
    } catch (java.io.IOException ex) {
        Bootstrap.logError(ex);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
    }
    var selectedCategory = request.getParameter("category");
    if (selectedCategory == null) {
        selectedCategory = "";
    } else if (!selectedCategory.isEmpty()
            && !ManagedFileStore.UNCATEGORIZED_FILTER.equals(selectedCategory)
            && !categories.contains(selectedCategory)) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
    }
    var categoryGroups = new TreeMap<String, List<ManagedFileStore.Entry>>((left, right) -> {
        if (left.isEmpty()) return right.isEmpty() ? 0 : 1;
        if (right.isEmpty()) return -1;
        return String.CASE_INSENSITIVE_ORDER.compare(left, right);
    });
    for (var entry : allFiles) {
        var matchesFilter = selectedCategory.isEmpty()
                || (ManagedFileStore.UNCATEGORIZED_FILTER.equals(selectedCategory) && entry.getCategory().isEmpty())
                || selectedCategory.equals(entry.getCategory());
        if (matchesFilter) {
            categoryGroups.computeIfAbsent(entry.getCategory(), ignored -> new ArrayList<>()).add(entry);
        }
    }
    categoryGroups.values().forEach(entries -> entries.sort(
            java.util.Comparator.comparing(ManagedFileStore.Entry::getTitle, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ManagedFileStore.Entry::getName, String.CASE_INSENSITIVE_ORDER)));
    Web.view("page", request);
    request.setAttribute("download_archive_categories", categories);
    request.setAttribute("download_archive_groups", categoryGroups.entrySet());
    request.setAttribute("download_archive_selected_category", selectedCategory);
%>
<t:layout design="start">
  <h1><fmt:message key="page.download_archive.title"/></h1>
  <p><fmt:message key="page.download_archive.intro"/></p>
  <form action="${paths.downloads}" method="GET" class="form-inline mb-3">
    <label for="download-category" class="mr-2"><fmt:message key="page.download_archive.category"/></label>
    <select id="download-category" class="form-control form-control-sm mr-2" name="category">
      <option value="" ${empty download_archive_selected_category ? 'selected' : ''}><fmt:message key="page.download_archive.all_categories"/></option>
      <option value="__uncategorized__" ${download_archive_selected_category eq '__uncategorized__' ? 'selected' : ''}><fmt:message key="page.download_archive.uncategorized"/></option>
      <c:forEach var="category" items="${download_archive_categories}">
        <option value="<c:out value='${category}'/>" ${download_archive_selected_category eq category ? 'selected' : ''}><c:out value="${category}"/></option>
      </c:forEach>
    </select>
    <button class="btn btn-outline-primary btn-sm" type="submit"><fmt:message key="page.download_archive.filter"/></button>
  </form>
  <c:choose>
    <c:when test="${not empty download_archive_groups}">
      <c:forEach var="group" items="${download_archive_groups}">
        <section class="mb-4">
          <h2><c:choose><c:when test="${empty group.key}"><fmt:message key="page.download_archive.uncategorized"/></c:when><c:otherwise><c:out value="${group.key}"/></c:otherwise></c:choose></h2>
          <div class="table-responsive">
            <table class="table table-sm">
              <thead><tr>
                <th><fmt:message key="page.download_archive.name"/></th>
                <th><fmt:message key="page.download_archive.filename"/></th>
                <th><fmt:message key="page.download_archive.size"/></th>
                <th><fmt:message key="page.download_archive.modified"/></th>
                <th></th>
              </tr></thead>
              <tbody>
              <c:forEach var="entry" items="${group.value}">
                <c:url var="download_url" value="/downloads.jsp"><c:param name="file" value="${entry.name}"/></c:url>
                <tr>
                  <td><c:out value="${entry.title}"/></td>
                  <td><c:out value="${entry.name}"/></td>
                  <td><c:out value="${entry.size}"/></td>
                  <td><c:out value="${entry.modified}"/></td>
                  <td><a class="btn btn-primary btn-sm" href="<c:out value='${download_url}'/>"><fmt:message key="page.download_archive.download"/></a></td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </div>
        </section>
      </c:forEach>
    </c:when>
    <c:otherwise><p><fmt:message key="page.download_archive.empty"/></p></c:otherwise>
  </c:choose>
</t:layout>
