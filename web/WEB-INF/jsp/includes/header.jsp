<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%--
  Content of <head> shared by all layouts.
  Parameters:
    title       key of the page title in commands.json (com.*)
    charset     "true" to start with <meta charset>
    software    "coolchat" or "version" for <meta name="software">, empty for none
    contenttype "true" for the http-equiv content-type <meta>
    fonts       "true" to preload the emoji fonts
    styles      comma separated style sheets: "@bootstrap", "@fa4", "@fa6", "@prism" or a fixed path key
    scripts     comma separated scripts: "@popper", "@bootstrap", "@prism" or a fixed path key
    embedded    "true" to hide the navigation bar when the page is shown inside the chat
--%><script>if(window.frameElement && window.frameElement.classList.contains('chat-panel-iframe')){document.documentElement.classList.add('chat-panel-embedded');}</script><style>html.chat-panel-embedded body > nav.navbar{display:none!important;}html.chat-panel-embedded body{padding-top:0!important;}</style>
<c:if test="${param.charset eq 'true'}">  <meta charset="UTF-8">
</c:if>  <title>${com[param.title]}</title>
  <script src="${path_file}theme.js"></script>
  <meta content="${SERVER_VENDOR}" name="author">
  <meta content="&copy;${SERVER_YEAR} by ${SERVER_VENDOR}" name="description">
<c:choose>
<c:when test="${param.software eq 'coolchat'}">  <meta content="CoolChat" name="software">
</c:when>
<c:when test="${param.software eq 'version'}">  <meta content="${SERVER_SOFTWARE} ${SERVER_VERSION}-${SERVER_STATUS}" name="software">
</c:when>
</c:choose>
<c:if test="${param.contenttype eq 'true'}">  <meta content="text/html; charset=UTF-8" http-equiv="content-type">
</c:if>  <meta name="viewport" content="width=device-width, initial-scale=1">
<c:if test="${param.fonts eq 'true'}">  <link rel="preload" href="${path_file}fonts/TwitterColorEmoji-SVGinOT.ttf" as="font">
  <link rel="preload" href="${path_file}fonts/NotoColorEmoji.ttf" as="font">
</c:if>
<c:forTokens items="${param.styles}" delims="," var="style">
<c:choose>
<c:when test="${style eq '@bootstrap'}">  <link rel="stylesheet" href="${path_file}bootstrap/css/bootstrap.min.css" type="text/css">
</c:when>
<c:when test="${style eq '@fa4'}">  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
</c:when>
<c:when test="${style eq '@fa6'}">  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" type="text/css">
</c:when>
<c:when test="${style eq '@prism'}">  <link rel="stylesheet" href="https://prismjs.com/themes/prism.css">
  <link rel="stylesheet" href="https://prismjs.com/plugins/line-numbers/prism-line-numbers.css">
  <link rel="stylesheet" media="screen" href="${path_file}prism-live.css" type="text/css">
</c:when>
<c:otherwise>  <link rel="stylesheet" href="${paths[style]}" type="text/css" data-hwc-theme>
</c:otherwise>
</c:choose>
</c:forTokens>
<c:forTokens items="${param.scripts}" delims="," var="script">
<c:choose>
<c:when test="${script eq '@popper'}">  <script src="${path_file}popper/js/popper.min.js"></script>
</c:when>
<c:when test="${script eq '@bootstrap'}">  <script src="${path_file}bootstrap/js/bootstrap.min.js"></script>
</c:when>
<c:when test="${script eq '@prism'}">  <script src="https://blissfuljs.com/bliss.shy.min.js"></script>
  <script src="https://prismjs.com/prism.js"></script>
  <script src="https://prismjs.com/plugins/line-numbers/prism-line-numbers.js"></script>
  <script src="${path_file}prism-live.js?load=css,html,javascript"></script>
</c:when>
<c:otherwise>  <script src="${paths[script]}"></script>
</c:otherwise>
</c:choose>
</c:forTokens>
<c:if test="${param.embedded eq 'true'}">  <style>
  body.embedded nav.navbar { display: none !important; }
  body.embedded { padding-top: 0 !important; }
  </style>
</c:if>
