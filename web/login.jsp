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
%><jsp:include page="${templatePage}"/>
