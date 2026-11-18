<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%--
  Navigation variants: brand (logo only), console (administration), guest,
  community and community_reg (public navigation).
--%><c:choose>
<c:when test="${param.variant eq 'brand'}"><nav class="navbar navbar-expand-lg navbar-dark ${param.navclass}">
  <div class="container-fluid">
    <a class="navbar-brand" href="${empty param.home ? '#' : paths[param.home]}">
      <img src="${not empty theme_logo ? theme_logo : ''}${empty theme_logo ? path_file : ''}${empty theme_logo ? 'gfx/logo.svg' : ''}" alt="HWebChat" style="height: 40px;">
    </a>
  </div>
</nav>
</c:when>
<c:when test="${param.variant eq 'console'}"><nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
  <div class="container-fluid">
    <a class="navbar-brand" href="${paths.config_index}">
      <img src="${not empty theme_logo ? theme_logo : ''}${empty theme_logo ? path_file : ''}${empty theme_logo ? 'gfx/logo.svg' : ''}" alt="HWebChat" style="height: 40px;">
    </a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="${com.design_toggle_navigation}">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav">
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle" href="#" id="adminDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            <i class="fa fa-cog"></i> ${com.design_admin_menu}
          </a>
          <div class="dropdown-menu">
            <a class="dropdown-item" href="${paths.config_chat}">${com.design_admin_chat_settings}</a>
            <a class="dropdown-item" href="${paths.config_cmdrights}">${com.design_admin_cmd_rights}</a>
            <a class="dropdown-item" href="${paths.config_cmd}">${com.design_admin_cmd_edit}</a>
            <a class="dropdown-item" href="${paths.config_host}">${com.design_admin_host}</a>
            <a class="dropdown-item" href="${paths.config_theme}">${com.design_admin_theme}</a>
            <a class="dropdown-item" href="${paths.config_index}&amp;service=pages"><fmt:message key="console.pages.menu"/></a>
            <a class="dropdown-item" href="${paths.config_index}&amp;service=navigation"><fmt:message key="console.navigation.menu"/></a>
            <a class="dropdown-item" href="${paths.config_mail}">${com.design_admin_mail}</a>
            <a class="dropdown-item" href="${paths.config_help}">${com.design_admin_help}</a>
            <a class="dropdown-item" href="${paths.config_fun}">${com.design_admin_fun}</a>
            <a class="dropdown-item" href="${paths.config_emot}">${com.design_admin_emoticons}</a>
            <a class="dropdown-item" href="${paths.config_mime}">${com.design_admin_mime}</a>
            <a class="dropdown-item" href="${paths.config_users}">${com.design_admin_users}</a>
            <a class="dropdown-item" href="${paths.config_rooms}">${com.design_admin_rooms}</a>
            <a class="dropdown-item" href="${paths.config_index}&amp;service=modules"><fmt:message key="console.modules.menu"/></a>
          </div>
        </li>
        <li class="nav-item"><a class="nav-link" href="<c:out value="${paths.config_news}"/>"><i class="fa fa-newspaper-o" aria-hidden="true"></i> <fmt:message key="news.manage"/></a></li>
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle" href="#" id="consoleLangDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fa fa-language" aria-hidden="true"></i> ${com.design_nav_language}</a>
          <div class="dropdown-menu" aria-labelledby="consoleLangDropdown">
            <a class="dropdown-item" href="${paths.config_index}&amp;lang=de" onclick="document.cookie='lang=de;path=/;max-age=31536000'">${com.design_lang_de}</a>
            <a class="dropdown-item" href="${paths.config_index}&amp;lang=en" onclick="document.cookie='lang=en;path=/;max-age=31536000'">${com.design_lang_en}</a>
          </div>
        </li>
      </ul>
    </div>
  </div>
</nav>
</c:when>
<c:otherwise><jsp:include page="/WEB-INF/jsp/includes/navbar-public.jsp"><jsp:param name="variant" value="${param.variant}"/></jsp:include></c:otherwise>
</c:choose>
