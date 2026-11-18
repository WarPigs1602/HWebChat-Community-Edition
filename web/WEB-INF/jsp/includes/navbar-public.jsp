<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.NavigationMenu" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="fn" uri="jakarta.tags.functions" %><%
    request.setAttribute("navigation_items", NavigationMenu.load());
%><c:set var="community" value="${param.variant ne 'guest'}"/><c:set var="registered" value="${param.variant eq 'community_reg'}"/><nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
  <div class="container-fluid">
    <a class="navbar-brand" href="${community ? paths.index_com : paths.index}">
      <img src="${not empty theme_logo ? theme_logo : ''}${empty theme_logo ? path_file : ''}${empty theme_logo ? 'gfx/logo.svg' : ''}" alt="HWebChat" style="height: 40px;">
    </a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="${com.design_toggle_navigation}"><span class="navbar-toggler-icon"></span></button>
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav w-100">
        <c:set var="utilityStarted" value="false"/>
        <c:forEach items="${navigation_items}" var="item">
          <c:set var="id" value="${item.id}"/>
          <c:set var="visible" value="${item.enabled
            and id ne 'register' and id ne 'password' and id ne 'language-de' and id ne 'language-en'
            and (id ne 'news' or modules.news ne '0')
            and (id ne 'board' or modules.board ne '0')
            and (id ne 'toplist' or modules.toplist ne '0')
            and (id ne 'stats' or modules.stats ne '0')
            and (id ne 'logout' or community)
            and (id ne 'chat' or (community and modules.chat ne '0'))
            and (id ne 'account' or registered)
            and ((id ne 'guest' and id ne 'login') or (not community and modules.chat ne '0' and not directChatLogin))}"/>
          <c:if test="${visible}">
            <c:set var="utility" value="${id eq 'chat' or id eq 'account' or id eq 'guest' or id eq 'login' or id eq 'language'}"/>
            <c:set var="alignment" value="${utility and not utilityStarted ? 'ml-auto' : ''}"/>
            <c:if test="${utility}"><c:set var="utilityStarted" value="true"/></c:if>
            <c:set var="defaultLabel" value=""/>
            <c:set var="defaultUrl" value=""/>
            <c:set var="icon" value=""/>
            <c:choose>
              <c:when test="${id eq 'start'}"><c:set var="defaultLabel" value="${com.design_nav_start}"/><c:set var="defaultUrl" value="${community ? paths.index_com : paths.index}"/><c:set var="icon" value="home"/></c:when>
              <c:when test="${id eq 'news'}"><c:set var="defaultLabel" value="${com.design_nav_news}"/><c:set var="defaultUrl" value="${paths.news}"/><c:set var="icon" value="bullhorn"/></c:when>
              <c:when test="${id eq 'board'}"><c:set var="defaultLabel" value="${com.design_nav_forum}"/><c:set var="defaultUrl" value="${paths.board}"/><c:set var="icon" value="comments"/></c:when>
              <c:when test="${id eq 'rules'}"><c:set var="defaultLabel" value="${com.design_nav_chattiquette}"/><c:set var="defaultUrl" value="${registered ? paths.rules_com_reg : community ? paths.rules_com : paths.rules}"/><c:set var="icon" value="info-circle"/></c:when>
              <c:when test="${id eq 'toplist'}"><c:set var="defaultLabel" value="${com.design_nav_toplist}"/><c:set var="defaultUrl" value="${paths.toplist}"/><c:set var="icon" value="trophy"/></c:when>
              <c:when test="${id eq 'stats'}"><fmt:message key="common.statistics" var="defaultLabel"/><c:set var="defaultUrl" value="${paths.stats}"/><c:set var="icon" value="bar-chart"/></c:when>
              <c:when test="${id eq 'logout'}"><c:set var="defaultLabel" value="${com.design_nav_logout}"/><c:set var="defaultUrl" value="${paths.logout}"/><c:set var="icon" value="sign-out"/></c:when>
              <c:when test="${id eq 'chat'}"><c:set var="defaultLabel" value="${com.design_nav_chat}"/><c:set var="icon" value="comments"/></c:when>
              <c:when test="${id eq 'account'}"><c:set var="defaultLabel" value="${com.design_nav_account}"/><c:set var="defaultUrl" value="${paths.account_com}"/><c:set var="icon" value="user"/></c:when>
              <c:when test="${id eq 'guest'}"><c:set var="defaultLabel" value="${com.design_nav_guest}"/><c:set var="defaultUrl" value="${paths.login}?skin=${skin}"/><c:set var="icon" value="sign-in"/></c:when>
              <c:when test="${id eq 'login'}"><c:set var="defaultLabel" value="${com.design_nav_login}"/><c:set var="icon" value="user"/></c:when>
              <c:when test="${id eq 'language'}"><c:set var="defaultLabel" value="${com.design_nav_language}"/><c:set var="icon" value="language"/></c:when>
            </c:choose>
            <c:set var="label" value="${empty item.label ? defaultLabel : item.label}"/>
            <c:choose>
              <c:when test="${not empty item.url or (id ne 'chat' and id ne 'login' and id ne 'language')}">
                <c:set var="href" value="${empty item.url ? defaultUrl : fn:startsWith(item.url, '/') ? pageContext.request.contextPath += item.url : item.url}"/>
                <li class="nav-item ${alignment} ${id eq 'start' ? 'active' : ''}"><a class="nav-link" href="<c:out value='${href}'/>"><c:if test="${not empty icon}"><i class="fa fa-${icon}" aria-hidden="true"></i> </c:if><c:out value="${label}"/></a></li>
              </c:when>
              <c:when test="${id eq 'chat'}">
                <li class="nav-item dropdown ${alignment}">
                  <a class="nav-link dropdown-toggle" href="#" id="chatDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fa fa-comments"></i> <c:out value="${label}"/></a>
                  <div class="dropdown-menu dropdown-menu-right p-2" aria-labelledby="chatDropdown">
                    <form method="POST" name="login" action="${paths.login_chat}" target="chat" accept-charset="utf-8" class="p-1" style="min-width:180px">
                      <input name="sid" value="${sid}" type="hidden"><input name="skin" value="${skin}" type="hidden">
                      <select class="form-control form-control-sm mb-2" id="nav-room-select" name="room"><c:forEach items="${chat_rooms}" var="r"><option value="<c:out value='${r}'/>"><c:out value="${r}"/></option></c:forEach><option value="__custom__"><fmt:message key="common.room_select_manual"/></option></select>
                      <fmt:message key="common.room_enter_name" var="roomEnterName"/>
                      <input class="form-control form-control-sm mb-2" id="nav-room-custom" type="text" maxlength="64" placeholder="${roomEnterName}" style="display:none" disabled>
                      <input class="btn btn-sm btn-primary btn-block" value="${com.design_btn_chat_start}" type="submit">
                      <script>(function(){var s=document.getElementById('nav-room-select'),c=document.getElementById('nav-room-custom');if(!s||!c)return;function t(){var m=s.value==='__custom__';c.style.display=m?'block':'none';c.disabled=!m;if(m){s.removeAttribute('name');c.name='room';c.focus();}else{c.removeAttribute('name');s.name='room';}}s.addEventListener('change',t);t();})();</script>
                    </form>
                  </div>
                </li>
              </c:when>
              <c:when test="${id eq 'login'}">
                <li class="nav-item dropdown ${alignment}">
                  <a class="nav-link dropdown-toggle" href="#" id="loginDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fa fa-user"></i> <c:out value="${label}"/></a>
                  <div class="dropdown-menu dropdown-menu-right p-3 login-menu" aria-labelledby="loginDropdown">
                    <h2 class="h6 login-menu-title"><i class="fa fa-sign-in" aria-hidden="true"></i> <c:out value="${label}"/></h2>
                    <p class="login-menu-hint"><fmt:message key="layout.start.log_in_to_your_account"/></p>
                    <form method="POST" name="login" action="${paths.login}" target="_top" accept-charset="utf-8" class="login-menu-form">
                      <input name="skin" value="${skin}" type="hidden">
                      <div class="form-group"><label for="navbar-login-nick">${com.design_placeholder_nick}</label><input class="form-control form-control-sm" id="navbar-login-nick" maxlength="20" name="nick" type="text" autocomplete="username" required></div>
                      <div class="form-group"><label for="navbar-login-pwd">${com.design_placeholder_pwd}</label><input class="form-control form-control-sm" id="navbar-login-pwd" maxlength="20" name="pwd" type="password" autocomplete="current-password" required></div>
                      <button class="btn btn-primary btn-block" type="submit">${com.design_btn_login_submit}</button>
                    </form>
                    <div class="login-menu-links">
                      <c:forEach items="${navigation_items}" var="child"><c:if test="${child.enabled and (child.id eq 'register' or child.id eq 'password')}">
                        <c:set var="childDefaultUrl" value="${child.id eq 'register' ? paths.register : paths.password}"/>
                        <c:set var="childUrl" value="${empty child.url ? childDefaultUrl : fn:startsWith(child.url, '/') ? pageContext.request.contextPath += child.url : child.url}"/>
                        <c:set var="childLabel" value="${empty child.label ? (child.id eq 'register' ? com.design_link_register : com.design_link_password_forgot) : child.label}"/>
                        <a class="dropdown-item" href="<c:out value='${childUrl}'/>"><i class="fa ${child.id eq 'register' ? 'fa-user-plus' : 'fa-key'}" aria-hidden="true"></i> <c:out value="${childLabel}"/></a>
                      </c:if></c:forEach>
                    </div>
                  </div>
                </li>
              </c:when>
              <c:otherwise>
                <li class="nav-item dropdown ${alignment}"><a class="nav-link dropdown-toggle" href="#" id="langDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fa fa-language"></i> <c:out value="${label}"/></a>
                  <div class="dropdown-menu dropdown-menu-right" aria-labelledby="langDropdown">
                    <c:forEach items="${navigation_items}" var="child"><c:if test="${child.enabled and (child.id eq 'language-de' or child.id eq 'language-en')}">
                      <c:set var="childLabel" value="${empty child.label ? (child.id eq 'language-de' ? com.design_lang_de : com.design_lang_en) : child.label}"/>
                      <c:choose>
                        <c:when test="${not empty child.url}"><c:set var="childUrl" value="${fn:startsWith(child.url, '/') ? pageContext.request.contextPath += child.url : child.url}"/><a class="dropdown-item" href="<c:out value='${childUrl}'/>"><c:out value="${childLabel}"/></a></c:when>
                        <c:when test="${child.id eq 'language-de'}"><a class="dropdown-item" href="#" onclick="setLang('de', this); return false;"><c:out value="${childLabel}"/></a></c:when>
                        <c:otherwise><a class="dropdown-item" href="#" onclick="setLang('en', this); return false;"><c:out value="${childLabel}"/></a></c:otherwise>
                      </c:choose>
                    </c:if></c:forEach>
                  </div>
                </li>
              </c:otherwise>
            </c:choose>
          </c:if>
        </c:forEach>
      </ul>
    </div>
  </div>
</nav>
