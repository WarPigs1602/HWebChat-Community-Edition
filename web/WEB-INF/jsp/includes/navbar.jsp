<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%--
  Navigation bar of the layouts.
  Parameters:
    variant  "guest", "community", "community_reg" (main navigation), "console" (administration) or "brand" (logo only)
    home     brand variant: key of the link target in paths.json, empty for "#"
    navclass brand variant: additional CSS classes of the <nav> element
--%><c:choose>
<c:when test="${param.variant eq 'brand'}"><nav class="navbar navbar-expand-lg navbar-dark ${param.navclass}">
  <div class="container-fluid">
    <a class="navbar-brand" href="${empty param.home ? '#' : paths[param.home]}">
      <img src="file/gfx/logo.svg" alt="HWebChat" style="height: 40px;">
    </a>
  </div>
</nav>
</c:when>
<c:when test="${param.variant eq 'console'}"><nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
  <div class="container-fluid">
    <a class="navbar-brand" href="${paths.config_index}">
      <img src="file/gfx/logo.svg" alt="HWebChat" style="height: 40px;">
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
            <a class="dropdown-item" href="${paths.config_mail}">${com.design_admin_mail}</a>
            <a class="dropdown-item" href="${paths.config_help}">${com.design_admin_help}</a>
            <a class="dropdown-item" href="${paths.config_fun}">${com.design_admin_fun}</a>
            <a class="dropdown-item" href="${paths.config_emot}">${com.design_admin_emoticons}</a>
            <a class="dropdown-item" href="${paths.config_path}">${com.design_admin_paths}</a>
            <a class="dropdown-item" href="${paths.config_mime}">${com.design_admin_mime}</a>
            <a class="dropdown-item" href="${paths.config_users}">${com.design_admin_users}</a>
            <a class="dropdown-item" href="${paths.config_rooms}">${com.design_admin_rooms}</a>
          </div>
        </li>
        <li class="nav-item"><a class="nav-link" href="<c:out value="${path_hwebchat}?page=${path_console_index}&service=news"/>"><i class="fa fa-newspaper-o" aria-hidden="true"></i> <fmt:message key="news.manage"/></a></li>
      </ul>
    </div>
  </div>
</nav>
</c:when>
<c:otherwise><c:set var="community" value="${param.variant ne 'guest'}"/><c:set var="registered" value="${param.variant eq 'community_reg'}"/><nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
  <div class="container-fluid">
    <a class="navbar-brand" href="${community ? paths.index_com : paths.index}">
      <img src="file/gfx/logo.svg" alt="HWebChat" style="height: 40px;">
    </a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="${com.design_toggle_navigation}">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav mr-auto">
        <li class="nav-item active">
          <a class="nav-link" href="${community ? paths.index_com : paths.index}"><i class="fa fa-home"></i> ${com.design_nav_start}</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="${paths.news}"><i class="fa fa-bullhorn" aria-hidden="true"></i> ${com.design_nav_news}</a>
        </li>
<c:if test="${registered}">        <li class="nav-item">
          <a class="nav-link" href="${paths.board}"><i class="fa fa-comments"></i> ${com.design_nav_forum}</a>
        </li>
</c:if>        <li class="nav-item">
          <a class="nav-link" href="${registered ? paths.rules_com_reg : community ? paths.rules_com : paths.rules}"><i class="fa fa-info-circle"></i> ${com.design_nav_chattiquette}</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="${paths.toplist}"><i class="fa fa-trophy"></i> ${com.design_nav_toplist}</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="${paths.stats}"><i class="fa fa-bar-chart"></i> <fmt:message key="common.statistics"/></a>
        </li>
<c:if test="${community}">        <li class="nav-item">
          <a class="nav-link" href="${paths.logout}"><i class="fa fa-sign-out"></i> ${com.design_nav_logout}</a>
        </li>
</c:if>      </ul>
      <ul class="navbar-nav">
<c:choose>
<c:when test="${community}">        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle" href="#" id="chatDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            <i class="fa fa-comments"></i> ${com.design_nav_chat}
          </a>
          <div class="dropdown-menu dropdown-menu-right p-2" aria-labelledby="chatDropdown">
            <form method="POST" name="login" action="${paths.login_chat}" target="chat" accept-charset="utf-8" class="form-inline">
              <input name="sid" value="${sid}" type="hidden">
              <select class="form-control form-control-sm mr-2 mb-2" name="room" style="width: 150px;">
                <option selected value="Lounge">Lounge</option>
                <option value="<fmt:message key="common.staff_lounge"/>"><fmt:message key="common.staff_lounge"/></option>
                <option value="Development">Development</option>
                <option value="<fmt:message key="common.exile"/>"><fmt:message key="common.exile"/></option>
              </select>
              <input name="skin" value="${skin}" type="hidden">
              <input class="btn btn-sm btn-primary mb-2" value="${com.design_btn_chat_start}" type="submit">
            </form>
          </div>
        </li>
<c:if test="${registered}">        <li class="nav-item">
          <a class="nav-link" href="${paths.account_com}"><i class="fa fa-user"></i> ${com.design_nav_account}</a>
        </li>
</c:if></c:when>
<c:otherwise>        <li class="nav-item">
          <a class="nav-link" href="${paths.login}&skin=${skin}"><i class="fa fa-sign-in"></i> ${com.design_nav_guest}</a>
        </li>
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle" href="#" id="loginDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            <i class="fa fa-user"></i> ${com.design_nav_login}
          </a>
          <div class="dropdown-menu dropdown-menu-right p-3 login-menu" aria-labelledby="loginDropdown">
            <h2 class="h6 login-menu-title"><i class="fa fa-sign-in" aria-hidden="true"></i> ${com.design_nav_login}</h2>
            <p class="login-menu-hint"><fmt:message key="layout.start.log_in_to_your_account"/></p>
            <form method="POST" name="login" action="${paths.login}" target="_top" accept-charset="utf-8" class="login-menu-form">
              <input name="skin" value="${skin}" type="hidden">
              <div class="form-group">
                <label for="navbar-login-nick">${com.design_placeholder_nick}</label>
                <input class="form-control form-control-sm" id="navbar-login-nick" maxlength="20" name="nick" type="text" autocomplete="username" required>
              </div>
              <div class="form-group">
                <label for="navbar-login-pwd">${com.design_placeholder_pwd}</label>
                <input class="form-control form-control-sm" id="navbar-login-pwd" maxlength="20" name="pwd" type="password" autocomplete="current-password" required>
              </div>
              <button class="btn btn-primary btn-block" type="submit">${com.design_btn_login_submit}</button>
            </form>
            <div class="login-menu-links">
              <a class="dropdown-item" href="${paths.register}"><i class="fa fa-user-plus" aria-hidden="true"></i> ${com.design_link_register}</a>
              <a class="dropdown-item" href="${paths.password}"><i class="fa fa-key" aria-hidden="true"></i> ${com.design_link_password_forgot}</a>
            </div>
          </div>
        </li>
</c:otherwise>
</c:choose>
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle" href="#" id="themeDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            <i class="fa fa-adjust"></i> ${com.design_nav_theme}
          </a>
          <div class="dropdown-menu dropdown-menu-right" aria-labelledby="themeDropdown">
            <a class="dropdown-item" href="#" data-theme-option="dark" onclick="setTheme('dark'); return false;">${com.design_theme_dark}</a>
            <a class="dropdown-item" href="#" data-theme-option="light" onclick="setTheme('light'); return false;">${com.design_theme_light}</a>
          </div>
        </li>
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle" href="#" id="langDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            <i class="fa fa-language"></i> ${com.design_nav_language}
          </a>
          <div class="dropdown-menu dropdown-menu-right" aria-labelledby="langDropdown">
            <a class="dropdown-item" href="#" onclick="setLang('de', this); return false;">${com.design_lang_de}</a>
            <a class="dropdown-item" href="#" onclick="setLang('en', this); return false;">${com.design_lang_en}</a>
          </div>
        </li>
      </ul>
    </div>
  </div>
</nav>
</c:otherwise>
</c:choose>
