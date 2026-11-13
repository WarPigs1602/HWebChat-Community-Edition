<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%--
  Navigation bar of the chat window.
  Parameters:
    registered  "true" for registered users (adds profile and account)
    napping     "true" for chat rooms of a room owner (links keep the owner)
  Elements with data-i18n="<command key>" are translated in real time when the language
  is changed (data-i18n-attr names the attribute to translate instead of the content).
--%><c:set var="registered" value="${param.registered eq 'true'}"/><c:set var="napping" value="${param.napping eq 'true'}"/><c:set var="suffix" value="${napping ? '&owner='.concat(owner) : ''}"/><nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
  <div class="container-fluid">
    <span class="navbar-brand mb-0"><img src="file/gfx/logo.svg" alt="HWebChat" style="height: 40px;"></span>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="${com.design_toggle_navigation}" data-i18n="design_toggle_navigation" data-i18n-attr="aria-label">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav">
        <li class="nav-item dropdown" id="commands">
      <a class="nav-link dropdown-toggle" href="#" id="commandsDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fa fa-terminal"></i> <span data-i18n="design_nav_commands">${com.design_nav_commands}</span></a>
      <div class="dropdown-menu" aria-labelledby="commandsDropdown">
        <a class="dropdown-item" href="#" onclick="submitMessage('/wc'); return false;" data-i18n="design_cmd_who">${com.design_cmd_who}</a>
        <a class="dropdown-item" href="#" onclick="submitMessage('/w'); return false;" data-i18n="design_cmd_here">${com.design_cmd_here}</a>
        <a class="dropdown-item" href="#" onclick="submitMessage('/myip'); return false;" data-i18n="design_cmd_myip">${com.design_cmd_myip}</a>
        <a class="dropdown-item" href="#" onclick="submitMessage('/info'); return false;" data-i18n="design_cmd_info">${com.design_cmd_info}</a>
        <a class="dropdown-item" href="#" onclick="submitMessage('/c'); return false;" data-i18n="design_cmd_clear">${com.design_cmd_clear}</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="#" onclick="submitMessage('/q Ausgeloggt per Mausklick!'); return false;"><i class="fa fa-sign-out"></i> <span data-i18n="design_cmd_logout">${com.design_cmd_logout}</span></a>
      </div>
    </li>
<c:if test="${registered}">    <li class="nav-item" id="profile">
      <a class="nav-link" href="javascript:showChatPanel('${paths.profile}${suffix}','${com.design_nav_profile}: ${nick}');"><i class="fa fa-user"></i> <span data-i18n="design_nav_profile">${com.design_nav_profile}</span></a>
    </li>
    <li class="nav-item" id="account">
      <a class="nav-link" href="javascript:showChatPanel('${paths.account}${suffix}','${com.design_nav_account}');"><i class="fa fa-cog"></i> <span data-i18n="design_nav_account">${com.design_nav_account}</span></a>
    </li>
</c:if>    <li class="nav-item dropdown" id="help">
      <a class="nav-link dropdown-toggle" href="#" id="helpDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fa fa-question-circle"></i> <span data-i18n="design_nav_help">${com.design_nav_help}</span></a>
      <div class="dropdown-menu" aria-labelledby="helpDropdown">
        <a class="dropdown-item" href="javascript:showChatPanel('${paths.help}${suffix}','${com.design_help_commands}');" data-i18n="design_help_commands">${com.design_help_commands}</a>
        <a class="dropdown-item" href="javascript:showChatPanel('${paths.emot}${suffix}','${com.design_help_emoticons}');" data-i18n="design_help_emoticons">${com.design_help_emoticons}</a>
      </div>
    </li>
    <li class="nav-item dropdown" id="color">
      <a class="nav-link dropdown-toggle" href="#" id="colorDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fa fa-paint-brush"></i> <span data-i18n="design_nav_color">${com.design_nav_color}</span></a>
      <div class="dropdown-menu p-2" aria-labelledby="colorDropdown" onclick="event.stopPropagation();">
        <div id="colorpicker"></div>
        <div class="input-group input-group-sm mt-2">
          <input type="text" class="color_field form-control form-control-sm" id="msg" value="#123456" onkeydown="return submitChatInputCol(event);"></input>
          <div class="input-group-append">
            <input type="button" value="OK" class="btn btn-primary btn-sm" onclick="sendTextCol();"></input>
          </div>
        </div>
      </div>
    </li><li class="nav-item dropdown" id="theme">
      <a class="nav-link dropdown-toggle" href="#" id="themeDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fa fa-adjust"></i> <span data-i18n="design_nav_theme">${com.design_nav_theme}</span></a>
      <div class="dropdown-menu" aria-labelledby="themeDropdown">
        <a class="dropdown-item" href="#" data-theme-option="dark" onclick="setTheme('dark'); return false;" data-i18n="design_theme_dark">${com.design_theme_dark}</a>
        <a class="dropdown-item" href="#" data-theme-option="light" onclick="setTheme('light'); return false;" data-i18n="design_theme_light">${com.design_theme_light}</a>
      </div>
    </li><li class="nav-item dropdown" id="language">
      <a class="nav-link dropdown-toggle" href="#" id="languageDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fa fa-language"></i> <span data-i18n="design_nav_language">${com.design_nav_language}</span></a>
      <div class="dropdown-menu" aria-labelledby="languageDropdown">
        <a class="dropdown-item" href="#" data-lang-option="de" onclick="setChatLang('de'); return false;" data-i18n="design_lang_de">${com.design_lang_de}</a>
        <a class="dropdown-item" href="#" data-lang-option="en" onclick="setChatLang('en'); return false;" data-i18n="design_lang_en">${com.design_lang_en}</a>
      </div>
    </li>
</ul>
    </div>
  </div>
</nav>
