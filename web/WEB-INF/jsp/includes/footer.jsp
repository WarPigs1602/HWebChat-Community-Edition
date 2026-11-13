<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%--
  Footer parts shared by the layouts.
  Parameters:
    parts  comma separated: "legal" (copyright and legal links), "lang" (language switch script),
           "cookie" (cookie notice script), "embedded" (marks the page as embedded in the chat),
           "chat" (scripts of the chat window)
--%><c:forTokens items="${param.parts}" delims="," var="part">
<c:choose>
<c:when test="${part eq 'legal'}">  <div class="container-fluid text-center page-footer">
    <small>
      <p>
        &copy;${SERVER_YEAR} by <a href="mailto:${SERVER_MAIL}">${SERVER_VENDOR}</a>
        <br>
        <a href="${paths.dataprotection}" target="_blank">${com.design_link_dataprotection}</a> | <a href="${paths.impressum}" target="_blank">${com.design_link_impressum}</a>
      </p>
    </small>
  </div>
</c:when>
<c:when test="${part eq 'lang'}"><script>
function setLang(l, el) {
  document.cookie = 'lang=' + l + ';path=/;max-age=' + 60*60*24*365;
  var url = new URL(window.location.href);
  url.searchParams.set('lang', l);
  window.location.href = url.toString();
}
function setChatLang(lang) { setLang(lang); }
</script>
</c:when>
<c:when test="${part eq 'cookie'}"><script>
function getCookie(name) {
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
  return null;
}
(function() {
  var warning = document.getElementById('cookie-warning');
  if (warning && !getCookie('cookie_accepted')) {
    warning.style.display = 'block';
  }
})();
</script>
</c:when>
<c:when test="${part eq 'embedded'}"><script>
if (window.self !== window.top) {
  document.body.classList.add('embedded');
}
</script>
</c:when>
<c:when test="${part eq 'chat'}"><script src="${path_file}typing-indicator.js"></script><script src="${path_file}nick-completion.js"></script><script src="${path_file}message-history.js"></script>
</c:when>
</c:choose>
</c:forTokens>
