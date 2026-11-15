<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" import="java.util.HashMap, java.util.Map, jakarta.servlet.http.HttpSession, net.midiandmore.chat.Web" %><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }

    Map<String, String> values = Web.requestValues(request);
    String nick = values.getOrDefault("nick", "");
    String password = values.getOrDefault("pwd", "");
    String skin = values.getOrDefault("skin", "");
    if (Web.isRegistered(nick)) {
        String registeredNick = Web.userData(nick, "nick2");
        if (!nick.equalsIgnoreCase(registeredNick)) {
            nick = registeredNick;
            if (values.containsKey("nick")) {
                values.put("nick", nick);
            }
        }
    }

    HttpSession session = request.getSession();
    String sid = values.getOrDefault("sid", "");
    sid = sid.isBlank() ? Web.newCaptchaId() : sid;
    skin = Web.skin(skin, request);
    Web.LoginAddresses addresses = Web.loginAddresses(request);
    String address = addresses.address();
    String resolvedAddress = addresses.resolvedAddress();
    String resolvedForwardedAddress = addresses.resolvedForwardedAddress();
    String forwardedAddress = addresses.forwardedAddress();

    if (Web.isRegistered(nick) && !Web.userData(nick, "pwd2").isEmpty()
            && Web.checkTemporaryPassword(nick, password)) {
        Web.clearTemporaryPassword(nick);
        Web.updateLoginPassword(nick, password);
    }

    boolean online = false;
    if (!session.isNew()) {
        nick = !nick.isBlank() ? nick : (String) session.getAttribute("nick");
        online = true;
        password = !password.isBlank() ? password : (String) session.getAttribute("pwd");
        skin = !skin.isBlank() ? skin : (String) session.getAttribute("skin");
    }

    boolean registered = Web.isRegistered(nick);
    String template = "";
    String reason = "";
    if (session == null) {
        template = "timeout";
    } else if (nick == null || !nick.matches(Web.configValue("allowed_chars"))) {
        template = "chars";
    } else if (!online && nick.toLowerCase().startsWith(Web.configValue("guest_prefix").toLowerCase())) {
        template = "guest";
    } else if (Web.configInt("guest") == 1 && !nick.isEmpty()
            && nick.length() < Web.configInt("min_nick_length")) {
        template = "nick";
    } else if (Web.isCommandNick(nick)) {
        template = "command";
    } else if (registered && !Web.checkPassword(nick, password)) {
        template = "pwd";
    } else {
        boolean ignoredBans = !registered
                || Integer.valueOf(Web.userData(nick, "status")) < Web.configInt("ignore_ban_status");
        if (Web.isBanned(nick) && ignoredBans) {
            template = "banned";
            reason = Web.banReason(nick);
        } else if (Web.isBanned(address) && ignoredBans) {
            template = "banned";
            reason = Web.banReason(address);
        } else if (Web.isBanned(resolvedAddress) && ignoredBans) {
            template = "banned";
            reason = Web.banReason(resolvedAddress);
        } else if (Web.isBanned(resolvedForwardedAddress) && ignoredBans) {
            template = "banned";
            reason = Web.banReason(resolvedForwardedAddress);
        } else if (Web.isBanned(forwardedAddress) && ignoredBans) {
            template = "banned";
            reason = Web.banReason(forwardedAddress);
        } else if (Web.isTimedBanned(nick) && ignoredBans) {
            template = "banned_temp";
            reason = Web.timedBanReason(nick);
        } else if (Web.isTimedBanned(address) && ignoredBans) {
            template = "banned_temp";
            reason = Web.timedBanReason(address);
        } else if (Web.isTimedBanned(resolvedAddress) && ignoredBans) {
            template = "banned_temp";
            reason = Web.timedBanReason(resolvedAddress);
        } else if (Web.isTimedBanned(resolvedForwardedAddress) && ignoredBans) {
            template = "banned_temp";
            reason = Web.timedBanReason(resolvedForwardedAddress);
        } else if (Web.isTimedBanned(forwardedAddress) && ignoredBans) {
            template = "banned_temp";
            reason = Web.timedBanReason(forwardedAddress);
        } else if (!registered && Web.configInt("guest") == 1 && !nick.isEmpty()
                && Web.configInt("only_registered_users") == 1) {
            template = "reg";
        }
    }

    if (!template.isEmpty()) {
        if (!reason.isEmpty()) {
            request.setAttribute("reason", reason);
        }
        Map<String, Object> pageValues = new HashMap<>();
        if (values.get("nick") != null && !values.get("nick").isBlank()) {
            pageValues.put("nick", values.get("nick"));
        }
        Web.view("page", request, pageValues);
        request.setAttribute("templatePage", "/WEB-INF/jsp/pages/" + template + ".jsp");
        request.setAttribute("loginTemplate", template);
    } else {
        String sessionNick = (String) session.getAttribute("nick");
        if (session.isNew() || sessionNick == null || !Web.hasCommunityUser(sessionNick)) {
            if (Web.configInt("guest") == 1 && nick.isEmpty()) {
                String guestPrefix = Web.configValue("guest_prefix");
                int number = 1;
                while (Web.communityNickExists(guestPrefix + number)) {
                    number++;
                    nick = guestPrefix + number;
                }
                if (!Web.communityNickExists(guestPrefix + "1")) {
                    nick = guestPrefix + "1";
                }
            }
            session.setMaxInactiveInterval(-1);
            session.setAttribute("nick", nick);
            session.setAttribute("pwd", password);
            session.setAttribute("skin", skin);
            session.setAttribute("ip", address);
            session.setAttribute("user-agent", request.getHeader("user-agent"));
            session.setAttribute("referer", request.getHeader("referer") != null ? request.getHeader("referer") : "");
            session.setAttribute("os-name", request.getHeader("sec-ch-ua-platform") != null
                    ? request.getHeader("sec-ch-ua-platform") : "unknown");
            session.setAttribute("os-version", request.getHeader("sec-ch-ua-platform-version") != null
                    ? request.getHeader("sec-ch-ua-platform-version") : "");
            session.setAttribute("os-mobile", request.getHeader("sec-ch-ua-mobile") != null
                    ? request.getHeader("sec-ch-ua-mobile") : "?0");
            session.setAttribute("browser", request.getHeader("sec-ch-ua") != null
                    ? request.getHeader("sec-ch-ua") : "");
            session.setAttribute("server", application.getServerInfo());
            session.setAttribute("community", "true");
            Web.addCommunityUser(nick, session);
        } else {
            Web.touchCommunityUser(sessionNick);
        }
        values.put("nick", nick);
        Web.startCommunityPage(registered ? "frameset_community_reg" : "frameset_community",
                request, response, values);
        return;
    }
%><c:choose>
<c:when test="${loginTemplate eq 'timeout'}"><t:layout design="start"><h1>${SERVER_SOFTWARE}-Timeout</h1>


<p>
<fmt:message key="page.timeout.your_session_expired_because_of"/>...
</p>
</t:layout></c:when>
<c:when test="${loginTemplate eq 'chars'}"><t:layout design="start"><h1>
<fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p>
<fmt:message key="common.please_for_your_nickname"/> <span style="font-weight: bold;">${nick}</span> <fmt:message key="common.only_use_alphanumeric_characters"/>
</p>
</t:layout></c:when>
<c:when test="${loginTemplate eq 'guest'}"><t:layout design="start"><h1>
<fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p>
<fmt:message key="common.the_nickname_is_only_registered"/>!
</p></t:layout></c:when>
<c:when test="${loginTemplate eq 'nick'}"><t:layout design="start"><h1><fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


<p>
<fmt:message key="common.your_nickname_must_be_at"/>
</p>
</t:layout></c:when>
<c:when test="${loginTemplate eq 'command'}"><t:layout design="start"><h1>
<fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p>
<fmt:message key="common.your_nickname"/> <span style="font-weight: bold;">${nick}</span> <fmt:message key="common.corresponds_to_a_chat_command"/>
</p>
</t:layout></c:when>
<c:when test="${loginTemplate eq 'pwd'}"><t:layout design="start"><h1><fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


<p>
<fmt:message key="common.the_login_for_the_name"/> <span style="font-weight: bold;">${nick}</span> <fmt:message key="common.failed_because_of_a_wrong"/>
</p>
</t:layout></c:when>
<c:when test="${loginTemplate eq 'banned'}"><t:layout design="start"><h1>
<fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p>
<fmt:message key="common.your_nickname_or_your_ip_3"/>
</p>
<p>
<fmt:message key="common.the_reason_given_to_you"/> <span style="font-weight: bold">${reason}</span> 
</p>
<p>
<table>
<tr>
<td style="text-align: left; font-weight: bold;">
<fmt:message key="common.this_can_have_several_causes"/>
</td>
</tr>
<tr>
<td style="text-align: left;">
<fmt:message key="common.you_have_disturbed_the_chat"/>!<br>
<fmt:message key="common.you_have_violated_applicable_law"/>!<br>
<fmt:message key="common.you_have_done_unwanted_advertising"/>!<br>
<fmt:message key="common.you_have_repeatedly_flooded_the"/>!<br>
<fmt:message key="common.you_have_become_a_victim"/>)
</td>
</tr>
</table>
</p>
<p>
<fmt:message key="common.please_try_again_later"/>!
</p></t:layout></c:when>
<c:when test="${loginTemplate eq 'banned_temp'}"><t:layout design="start"><h1>
<fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p>
<fmt:message key="common.your_nickname_or_your_ip_4"/>!
</p>
<p>
<table>
<tr>
<td style="text-align: left; font-weight: bold;">
<fmt:message key="common.this_can_have_several_causes"/>
</td>
</tr>
<tr>
<td style="text-align: left;">
<fmt:message key="common.you_have_disturbed_the_chat"/>!<br>
<fmt:message key="common.you_have_violated_applicable_law"/>!<br>
<fmt:message key="common.you_have_done_unwanted_advertising"/>!<br>
<fmt:message key="common.you_have_not_followed_the"/>!<br>
<fmt:message key="common.you_have_built_too_many"/>)!<br>
</td>
</tr>
</table>
</p>
<p>
<fmt:message key="common.please_try_again_later"/>!
</p></t:layout></c:when>
<c:when test="${loginTemplate eq 'reg'}"><t:layout design="start"><h1><fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>


<p style="font-weight: bold;">
<fmt:message key="common.in_order_to_enter_this"/>
</p>
<p>
<fmt:message key="common.choose_to_log_in_as"/>!
</p>
</t:layout></c:when>
</c:choose>
