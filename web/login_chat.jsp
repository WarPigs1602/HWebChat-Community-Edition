<%@ page session="false" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.*, java.io.*, java.net.*, java.nio.file.*, java.sql.*, java.util.*, java.util.logging.*, jakarta.servlet.*, jakarta.servlet.http.*, jakarta.mail.*" %>
<%@ page import="static net.midiandmore.chat.Web.*" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%!
    // The nick the templates of this request are rendered with (see ChatServices.getTemplate), the nick of the
    // request values may have been replaced by the registered one; null: the default of the page is the same
    private static String templateNick(HttpServletRequest request, Map<String, String> map) {
        var mapNick = map.getOrDefault("nick", "");
        var session = request.getSession(false);
        if (session != null && session.isNew()) {
            var nick = (String) session.getAttribute("nick");
            return nick != null ? nick : mapNick;
        }
        String nick = null;
        if (session != null) {
            nick = mapNick.isBlank() ? (String) session.getAttribute("nick") : mapNick;
        }
        if (nick == null && mapNick.isEmpty()) {
            return null;
        }
        return nick != null ? nick : mapNick;
    }
%><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }
    if (!Web.isModuleEnabled("chat")) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    Map<String, String> map = Web.requestValues(request);
    // Page that is shown by the markup below and the values it differs in from the default ones
    String tpl = null;
    Map<String, Object> values = new HashMap<>();
        var nick = map.getOrDefault("nick", "");
        var conf = Bootstrap.getBoot().getConfig();
        var cm = Bootstrap.getBoot().getChatManager();
        var ut = Bootstrap.getBoot().getUtil();
        var db = conf.getDb();
        var target = map.getOrDefault("target", "");
        var skin = map.getOrDefault("skin", "");
        var session = request.getSession();
        if (target.isBlank() && (session.isNew() || session.getAttribute("nick") == null)) {
            if (db.isRegistered(nick)) {
                if (!nick.equalsIgnoreCase(db.getData(nick, "nick2"))) {
                    nick = db.getData(nick, "nick2");
                }
                map.replace("nick", nick);
            }
            var pwd = map.getOrDefault("pwd", "");
            var sid = map.getOrDefault("sid", "");
            sid = !sid.isBlank() ? sid : newCaptchaId();
            var room = map.getOrDefault("room", "");
            var owner = map.getOrDefault("owner", "");
            skin = ut.parseHost(skin, request)[1];
            var reg = false;
            var ip = request.getLocalAddr();
            String proxyIp = null;
            var realIp = request.getHeader("x-forwarded-for");
            realIp = realIp != null ? realIp : "";
            if (realIp.contains(", ")) {
                realIp = realIp.substring(0, realIp.indexOf(", "));
            }
            if (conf.getString("cloudflare").equals("1")) {
                ip = realIp;
                realIp = "";
            }
            try {
                realIp = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
            } catch (UnknownHostException e) {
            }
            if (conf.getString("use_proxy").equals("1")) {
                proxyIp = request.getHeader(conf.getString("real_ip"));
            }
            if (proxyIp != null && !proxyIp.equals("")) {
                ip = proxyIp;
            }
            String ipResolved = null;
            String ipResolved2 = null;
            try {
                ipResolved = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(ip).getCanonicalHostName() : ip;
            } catch (UnknownHostException uhe) {
                ipResolved = ip;
            }
            try {
                ipResolved2 = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
            } catch (UnknownHostException uhe) {
                ipResolved2 = realIp;
            }
            if (db.isRegistered(nick) && !db.getData(nick, "pwd2").isEmpty() && db.checkPassword2(nick, pwd)) {
                db.updateNick(nick, "pwd2", "");
                db.updatePassword(nick, pwd);
            }
            String roomName = "";
            if (!owner.isBlank()) {
                roomName = db.getRoomNameByOwner(owner.toLowerCase());
            }
            if (!owner.equals("") && roomName.isBlank()) {
                tpl = "room";
            } else if (!nick.matches(conf.getString("allowed_chars"))) {
                if (roomName.isBlank()) {
                    tpl = "chars";
                } else {
                    tpl = "chars_napping";
                }
            } else if (nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase())) {
                if (roomName.isBlank()) {
                    tpl = "guest";
                } else {
                    tpl = "guest_napping";
                }
            } else if ((conf.getInt("guest") == 1 && !nick.equals("")) && nick.length() < conf.getInt("min_nick_length")) {
                if (roomName.isBlank()) {
                    tpl = "nick";
                } else {
                    tpl = "nick_napping";
                }
            } else if (isCommandNick(nick)) {
                if (roomName.isBlank()) {
                    tpl = "command";
                } else {
                    tpl = "command_napping";
                }
            } else if ((reg = db.isRegistered(nick)) && !db.checkPassword(nick, pwd)) {
                if (roomName.isBlank()) {
                    tpl = "pwd";
                } else {
                    tpl = "pwd_napping";
                }
            } else if (db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(nick));
                if (roomName.isBlank()) {
                    tpl = "banned";
                } else {
                    tpl = "banned_napping";
                }
            } else if (db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ip));
                if (roomName.isBlank()) {
                    tpl = "banned";
                } else {
                    tpl = "banned_napping";
                }
            } else if (db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved));
                if (roomName.isBlank()) {
                    tpl = "banned";
                } else {
                    tpl = "banned_napping";
                }
            } else if (db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved2));
                if (roomName.isBlank()) {
                    tpl = "banned";
                } else {
                    tpl = "banned_napping";
                }
            } else if (db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(realIp));
                if (roomName.isBlank()) {
                    tpl = "banned";
                } else {
                    tpl = "banned_napping";
                }
            } else if (db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(nick));
                if (roomName.isBlank()) {
                    tpl = "banned_temp";
                } else {
                    tpl = "banned_temp_napping";
                }
            } else if (db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ip));
                if (roomName.isBlank()) {
                    tpl = "banned_temp";
                } else {
                    tpl = "banned_temp_napping";
                }
            } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved));
                if (roomName.isBlank()) {
                    tpl = "banned_temp";
                } else {
                    tpl = "banned_temp_napping";
                }
            } else if (db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved2));
                if (roomName.isBlank()) {
                    tpl = "banned_temp";
                } else {
                    tpl = "banned_temp_napping";
                }
            } else if (db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(realIp));
                if (roomName.isBlank()) {
                    tpl = "banned_temp";
                } else {
                    tpl = "banned_temp_napping";
                }
            } else if (!reg && (conf.getInt("guest") == 1 && !nick.equals("") && conf.getInt("only_registered_users") == 1)) {

                if (roomName.isBlank()) {
                    tpl = "reg";
                } else {
                    tpl = "reg_napping";
                }
            } else if (!reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));

                tpl = roomName.isBlank() ? "locked_room" : "locked_room_napping";
            } else if (reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("lock_status")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));

                tpl = roomName.isBlank() ? "locked_room" : "locked_room_napping";
            } else if (cm.isOnline(nick)) {

                tpl = roomName.isBlank() ? "online" : "online_napping";
            } else {
                var context = request.getServletContext();
                if (conf.getInt("guest") == 1 && nick.equals("")) {
                    var i = 1;
                    while (cm.isOnline(conf.getString("guest_prefix") + Integer.toString(i))) {
                        i++;
                        nick = conf.getString("guest_prefix") + Integer.toString(i);
                    }
                    if (!cm.isOnline(conf.getString("guest_prefix") + Integer.toString(1))) {
                        nick = conf.getString("guest_prefix") + Integer.toString(1);
                    }
                }
                session.setMaxInactiveInterval(-1);

                session.setAttribute("sid", sid);
                session.setAttribute("nick", nick);
                session.setAttribute("pwd", pwd);
                session.setAttribute("skin", skin);
                session.setAttribute("room", room);
                session.setAttribute("ip", ip);
                session.setAttribute("owner", owner);
                session.setAttribute("user-agent", request.getHeader("user-agent") != null ? request.getHeader("user-agent") : "");
                session.setAttribute("os-name", request.getHeader("sec-ch-ua-platform") != null ? request.getHeader("sec-ch-ua-platform") : "unknown");
                session.setAttribute("os-version", request.getHeader("sec-ch-ua-platform-version") != null ? request.getHeader("sec-ch-ua-platform-version") : "");
                session.setAttribute("os-mobile", request.getHeader("sec-ch-ua-mobile") != null ? request.getHeader("sec-ch-ua-mobile") : "?0");
                session.setAttribute("browser", request.getHeader("sec-ch-ua") != null ? request.getHeader("sec-ch-ua") : "");
                session.setAttribute("referer", request.getHeader("referer") != null ? request.getHeader("referer") : "");
                session.setAttribute("server", context.getServerInfo());
                session.setAttribute("chat_only", "true");
                cm.getUsersCommunity().put(nick, new UsersCommunity(Bootstrap.getBoot(), session));
                if (owner.equals("")) {
                    var room2 = map.getOrDefault("room", "");
                    if (!room2.isBlank() && db.roomExists(room2) && db.getRoomData(room2, "chat_napping").equals("1")) {
                        tpl = reg ? "frameset_reg_napping" : "frameset_napping";
                        values.put("owner", db.getRoomData(room2, "owner"));
                    } else {
                        tpl = reg ? "frameset_reg" : "frameset";
                    }
                } else {
                    tpl = reg ? "frameset_reg_napping" : "frameset_napping";

                    var room2 = db.getRoomNameByOwner(owner.toLowerCase());
                    var title = db.getRoomData(room2, "page_title");
                    var bg_color_1 = db.getRoomData(room2, "first_bgcolor");
                    var bg_color_2 = db.getRoomData(room2, "second_bgcolor");
                    var color = db.getRoomData(room2, "textcolor");
                    var link_color = db.getRoomData(room2, "linkcolor");
                    var border_color = db.getRoomData(room2, "bordercolor");
                    values.put("title", title);
                    values.put("bg_color_1", bg_color_1);
                    values.put("bg_color_2", bg_color_2);
                    values.put("color", color);
                    values.put("link_color", link_color);
                    values.put("border_color", border_color);
                    values.put("owner", owner);

                }
            }
        } else if (target.isBlank()) {
            if (db.isRegistered(nick)) {
                if (!nick.equalsIgnoreCase(db.getData(nick, "nick2"))) {
                    nick = db.getData(nick, "nick2");
                }
                map.replace("nick", nick);
            }
            var pwd = (String) session.getAttribute("pwd") != null ? (String) session.getAttribute("pwd") : "";
            nick = (String) session.getAttribute("nick");
            var sid = (String) session.getAttribute("sid");
            sid = sid != null ? sid : newCaptchaId();
            var room = map.getOrDefault("room", "");
            var owner = map.getOrDefault("owner", "");
            session.setAttribute("sid", sid);
            session.setAttribute("room", room);
            session.setAttribute("owner", owner);
            skin = ut.parseHost(skin, request)[1];
            var reg = false;
            var ip = request.getLocalAddr();
            String proxyIp = null;
            var realIp = request.getHeader("x-forwarded-for");
            realIp = realIp != null ? realIp : "";
            if (realIp.contains(", ")) {
                realIp = realIp.substring(0, realIp.indexOf(", "));
            }
            if (conf.getString("cloudflare").equals("1")) {
                ip = realIp;
                realIp = "";
            }
            try {
                realIp = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
            } catch (UnknownHostException e) {
            }
            if (conf.getString("use_proxy").equals("1")) {
                proxyIp = request.getHeader(conf.getString("real_ip"));
            }
            if (proxyIp != null && !proxyIp.equals("")) {
                ip = proxyIp;
            }
            String ipResolved = null;
            String ipResolved2 = null;
            try {
                ipResolved = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(ip).getCanonicalHostName() : ip;
            } catch (UnknownHostException uhe) {
                ipResolved = ip;
            }
            try {
                ipResolved2 = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
            } catch (UnknownHostException uhe) {
                ipResolved2 = realIp;
            }
            if (db.isRegistered(nick) && !db.getData(nick, "pwd2").isEmpty() && db.checkPassword2(nick, pwd)) {
                db.updateNick(nick, "pwd2", "");
                db.updatePassword(nick, pwd);
            }
            String roomName = "";
            if (!owner.isBlank()) {
                roomName = db.getRoomNameByOwner(owner.toLowerCase());
            }
            reg = db.isRegistered(nick);
            if (!owner.equals("") && roomName.isBlank()) {
                tpl = "room";
            } else if (!roomName.isBlank() && !nick.matches(conf.getString("allowed_chars"))) {
                tpl = "chars_napping";
            } else if (!roomName.isBlank() && nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase())) {
                tpl = "guest_napping";
            } else if (!roomName.isBlank() && (conf.getInt("guest") == 1 && !nick.equals("")) && nick.length() < conf.getInt("min_nick_length")) {
                tpl = "nick_napping";
            } else if (!roomName.isBlank() && isCommandNick(nick)) {
                tpl = "command_napping";
            } else if (!roomName.isBlank() && reg && !db.checkPassword(nick, pwd)) {
                tpl = "pwd_napping";
            } else if (!roomName.isBlank() && db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(nick));
                tpl = "banned_napping";
            } else if (!roomName.isBlank() && db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ip));
                tpl = "banned_napping";
            } else if (!roomName.isBlank() && db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved));
                tpl = "banned_napping";
            } else if (!roomName.isBlank() && db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved2));
                tpl = "banned_napping";
            } else if (!roomName.isBlank() && db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(realIp));
                tpl = "banned_napping";
            } else if (!roomName.isBlank() && db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(nick));
                tpl = "banned_temp_napping";
            } else if (!roomName.isBlank() && db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ip));
                tpl = "banned_temp_napping";
            } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved));
                tpl = "banned_temp_napping";
            } else if (!roomName.isBlank() && db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                tpl = "banned_temp_napping";
            } else if (!roomName.isBlank() && db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(realIp));
                tpl = "banned_temp_napping";
            } else if (!roomName.isBlank() && !reg && (conf.getInt("guest") == 1 && !nick.equals("") && conf.getInt("only_registered_users") == 1)) {
                tpl = "reg_napping";
            } else if (!roomName.isBlank() && !reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));
                tpl = "locked_room_napping";
            } else if (!roomName.isBlank() && reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("lock_status")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));
                tpl = "locked_room_napping";
            } else if (cm.isOnline(nick)) {
                tpl = roomName.isBlank() ? "online" : "online_napping";
            } else {
                var context = request.getServletContext();
                if (conf.getInt("guest") == 1 && nick.equals("")) {
                    var i = 1;
                    while (cm.isOnline(conf.getString("guest_prefix") + Integer.toString(i))) {
                        i++;
                        nick = conf.getString("guest_prefix") + Integer.toString(i);
                    }
                    if (!cm.isOnline(conf.getString("guest_prefix") + Integer.toString(1))) {
                        nick = conf.getString("guest_prefix") + Integer.toString(1);
                    }
                }
                if (owner.equals("")) {
                    var room2 = map.getOrDefault("room", "");
                    if (!room2.isBlank() && db.roomExists(room2) && db.getRoomData(room2, "chat_napping").equals("1")) {
                        tpl = reg ? "frameset_reg_com_napping" : "frameset_com_napping";
                        values.put("owner", db.getRoomData(room2, "owner"));
                    } else {
                        tpl = reg ? "frameset_reg_com" : "frameset_com";
                    }
                } else {
                    tpl = reg ? "frameset_reg_com_napping" : "frameset_com_napping";

                    var room2 = db.getRoomNameByOwner(owner.toLowerCase());
                    var title = db.getRoomData(room2, "page_title");
                    var bg_color_1 = db.getRoomData(room2, "first_bgcolor");
                    var bg_color_2 = db.getRoomData(room2, "second_bgcolor");
                    var color = db.getRoomData(room2, "textcolor");
                    var link_color = db.getRoomData(room2, "linkcolor");
                    var border_color = db.getRoomData(room2, "bordercolor");
                    values.put("title", title);
                    values.put("bg_color_1", bg_color_1);
                    values.put("bg_color_2", bg_color_2);
                    values.put("color", color);
                    values.put("link_color", link_color);
                    values.put("border_color", border_color);
                    values.put("owner", owner);

                }
            }
        } else {
            var oldnick = nick;
            var sid = map.getOrDefault("sid", "");
            if (cm.isOnline(nick)) {
                nick = cm.getUser(nick).getNewName();
                map.replace("nick", nick);
            }
            skin = ut.parseHost(skin, request)[1];
            var owner = map.getOrDefault("owner", "");
            request.setAttribute("sid", sid);
            request.setAttribute("target", target);
            request.setAttribute("nick", nick);
            values.put("oldnick", oldnick);
            if (owner.isBlank()) {
                tpl = "frameset_privchat";

            } else {

                tpl = "frameset_privchat_napping";
                var room2 = db.getRoomNameByOwner(owner.toLowerCase());
                var title = db.getRoomData(room2, "page_title");
                var bg_color_1 = db.getRoomData(room2, "first_bgcolor");
                var bg_color_2 = db.getRoomData(room2, "second_bgcolor");
                var color = db.getRoomData(room2, "textcolor");
                var link_color = db.getRoomData(room2, "linkcolor");
                var border_color = db.getRoomData(room2, "bordercolor");
                values.put("title", title);
                values.put("bg_color_1", bg_color_1);
                values.put("bg_color_2", bg_color_2);
                values.put("color", color);
                values.put("link_color", link_color);
                values.put("border_color", border_color);
                values.put("owner", owner);

            }
        }

        if (tpl != null) {
            var viewNick = templateNick(request, map);
            if (viewNick != null) {
                values.put("nick", viewNick);
            }
            Web.view("page", request, values);
            var napping = tpl.endsWith("_napping");
            var base = napping ? tpl.substring(0, tpl.length() - "_napping".length()) : tpl;
            var frameset = base.startsWith("frameset");
            request.setAttribute("loginPage", base.startsWith("frameset_privchat") ? "frameset_privchat" : frameset ? "frameset" : base);
            request.setAttribute("loginDesign", napping ? "napping" : "start");
            request.setAttribute("loginNapping", napping);
            request.setAttribute("loginReg", frameset && base.contains("_reg"));
            request.setAttribute("loginCom", frameset && base.contains("_com"));
        }
%><c:choose>
<c:when test="${loginPage eq 'frameset'}"><t:layout design="${loginNapping ? 'frameset_napping' : 'frameset'}"><script type="text/javascript">
<!--
  $(document).ready(function() {
    $('#colorpicker').farbtastic('#msg');
  });
-->
</script>
<jsp:include page="/WEB-INF/jsp/includes/navbar-chat.jsp">
  <jsp:param name="registered" value="${loginReg}"/>
  <jsp:param name="napping" value="${loginNapping}"/>
</jsp:include>
<script>
 function submitMessage(text)
 {
     text = escapeHtml(text);
     var msg = {
        category: "chat",
        message: text,
        target: ""
     };
     socket.send(JSON.stringify(msg));
 }
</script>

<div class="chat-shell container-fluid p-0 d-flex flex-column"<c:if test="${not loginReg and not loginNapping and not loginCom}"> style="height: calc(100vh - 56px); min-height: 300px;"</c:if>>
  <div class="chat-main row no-gutters flex-grow-1"<c:if test="${not loginReg and not loginNapping and not loginCom}"> style="min-height: 0; flex-direction: row; flex-wrap: nowrap;"</c:if>>
    <div class="chat-frame-container" id="chat_frame_container">
      <div class="chat-tabs" id="chat_tabs"></div>
      <div class="chat_frame" id="chat_window">
        <script type="text/javascript" src="${paths.script_nav}"></script>
      </div>
    </div>
    <div class="right_frame" id="right"<c:if test="${not loginReg and not loginNapping and not loginCom}"> style="min-height: 0;"</c:if>>
<c:choose>
<c:when test="${not loginNapping}">      <script type="text/javascript" src="${paths.script_userlist}"></script>
</c:when>
<c:when test="${not loginReg and not loginCom}">      <script type="text/javascript" src="${paths.script_userlist_napping}&owner=${owner}"></script>
</c:when>
<c:otherwise>      <script type="text/javascript" src="${paths.script_userlist}&owner=${owner}"></script>
</c:otherwise>
</c:choose>
    </div>
  </div>
  <div class="post_frame post_field input-group"<c:if test="${not loginReg and not loginNapping and not loginCom}"> style="min-height: 64px; flex: 0 0 auto;"</c:if>>
      <input type="text" class="form-control form-control-sm" autocomplete="off" id="message" maxlength="1024" value="" onkeydown="return submitChatInput(event);"></input>
      <div class="input-group-append">
        <input type="button" value="<fmt:message key="common.submission"/>" data-i18n="msg:common.submission" data-i18n-attr="value" class="btn btn-primary btn-sm" onclick="sendText();"></input>
      </div>
      <script type="text/javascript">
    App = {};
    let textarea = document.getElementById("message");
    if (!(App.emojiSelector instanceof Emojilist)) {
        App.emojiSelector = new <c:choose><c:when test="${not loginCom and (loginReg or loginNapping)}"><fmt:message key="common.emojilist"/></c:when><c:otherwise>Emojilist</c:otherwise></c:choose>();
    }
    App.emojiSelector.attach({
        node: textarea,
        insert: function(emoji, event) {
            let startBefore = textarea.selectionStart,
            before = textarea.value.substr(0, startBefore),
            after = textarea.value.substr(textarea.selectionEnd);
            textarea.value = before + emoji + after;
            textarea.setSelectionRange(before.length+emoji.length, before.length+emoji.length);
            textarea.focus();
            /*event.preventDefault();
            event.stopPropagation();*/
        },
        categoryclick: function(event) {
            textarea.focus();
            /*event.preventDefault();
            event.stopPropagation();*/
        },
        offset: '15%',
        prepend: true,
        template: '<div class="popover" style="width: 50vw!important; max-width: 50vw!important; min-width: 50vw; height: 50vh!important; max-height: 50vh!important; min-height: 50vh!important;" role="tooltip"><div class="arrow"></div><div class="popover-body" style="position: relative; width: 100%!important; max-width: 100%!important; min-width: 100%; height: 100%!important; max-height: 100%!important; min-height: 100%!important; overflow-y: scroll;"></div></div>',
        pageTitle: function(title) {
            return {
                'Recent':'😁',
                'Smileys & People':'😀',
                'Animals & Nature':'🐱',
                'Food & Drink':'🍟',
                'Travel & Places':'🚄',
                'Activities':'⚽',
                'Objects':'💿',
                'Symbols':'❓',
                'Flags':'🏁'
            }[title];
        },
        container: 'body'
    });
    </script>
  </div>
</div>
  <script type="text/javascript" src="${paths.script_chat}"></script>
  <script type="text/javascript" src="${paths.script_post}"></script>
  <script type="text/javascript" src="${paths.script_color}"></script>
    
<script type="text/javascript">
<!--
  setTimeout(function () {
    $.ajax({
        type: 'GET',
        url: '${paths.webchat_json}',
        dataType: 'json',
        success: function (data) {
         for (var i=0;i<data.length;++i)
         {
            if(data[i].name == "${nick}") {
                $.farbtastic('#colorpicker').setColor("#"+data[i].color);
            }
         }
        }
    });
  },5000);
-->
</script></t:layout></c:when>
<c:when test="${loginPage eq 'frameset_privchat'}"><t:layout design="${loginNapping ? 'frameset_privchat_napping' : 'frameset_privchat'}"><script>
function submitMessage(text)
{
    text = escapeHtml(text);
    var msg = {
       <fmt:message key="common.category_privchat_user"><fmt:param value="${oldnick}"/><fmt:param value="${target}"/></fmt:message>
       <fmt:message key="common.message_text"><fmt:param value="${oldnick}"/></fmt:message>
       <fmt:message key="common.targ_et"><fmt:param value="${target}"/></fmt:message>
    };
    socket.send(JSON.stringify(msg));
}
</script>

<div class="chat-privchat-shell container-fluid p-0 d-flex flex-column" style="height: 100%;">
  <div class="chat_frame flex-grow-1" id="chat_window" style="min-height: 0;">
    <script type="text/javascript" src="${paths.script_nav}"></script>
  </div>
  <div class="post_frame post_field input-group" style="min-height: 64px; flex: 0 0 auto;">
      <input type="text"<c:if test="${not loginNapping}"> name="msg"</c:if> class="form-control form-control-sm" autocomplete="off" id="message" maxlength="1024" value="" onkeydown="return submitChatInput(event)<c:if test="${loginNapping}">;</c:if>"></input>
      <div class="input-group-append">
        <input type="button" value="<fmt:message key="common.submission"/>" class="btn btn-primary btn-sm" onclick="sendText();"></input>
      </div>
      <script type="text/javascript">
    App = {};
    let textarea = document.getElementById("message");
    if (!(App.emojiSelector instanceof Emojilist)) {
        App.emojiSelector = new <fmt:message key="common.emojilist"/>();
    }
    App.emojiSelector.attach({
        node: textarea,
        insert: function(emoji, event) {
            let startBefore = textarea.selectionStart,
            before = textarea.value.substr(0, startBefore),
            after = textarea.value.substr(textarea.selectionEnd);
            textarea.value = before + emoji + after;
            textarea.setSelectionRange(before.length+emoji.length, before.length+emoji.length);
            textarea.focus();
            /*event.preventDefault();
            event.stopPropagation();*/
        },
        categoryclick: function(event) {
            textarea.focus();
            /*event.preventDefault();
            event.stopPropagation();*/
        },
        offset: '15%',
        prepend: true,
        template: '<div class="popover" style="width: 50vw!important; max-width: 50vw!important; min-width: 50vw; height: 50vh!important; max-height: 50vh!important; min-height: 50vh!important;" role="tooltip"><div class="arrow"></div><div class="popover-body" style="position: relative; width: 100%!important; max-width: 100%!important; min-width: 100%; height: 100%!important; max-height: 100%!important; min-height: 100%!important; overflow-y: scroll;"></div></div>',
        pageTitle: function(title) {
            return {
                'Recent':'😁',
                'Smileys & People':'😀',
                'Animals & Nature':'🐱',
                'Food & Drink':'🍟',
                'Travel & Places':'🚄',
                'Activities':'⚽',
                'Objects':'💿',
                'Symbols':'❓',
                'Flags':'🏁'
            }[title];
        },
        container: 'body'
    });
    </script>
  </div>
</div>
  <script type="text/javascript" src="${paths.script_chat_privchat}"></script>
  <script type="text/javascript" src="${paths.script_post_privchat}"></script>
  <script type="text/javascript" src="${paths.script_color}"></script>
</t:layout></c:when>
<c:when test="${not empty loginPage}"><t:layout design="${loginDesign}"><h1><fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<c:choose>
<c:when test="${loginPage eq 'room'}"><p>
<fmt:message key="page.room.this_room_is_not_registered"/>
</p></c:when>
<c:when test="${loginPage eq 'chars'}"><p>
<fmt:message key="common.please_for_your_nickname"/> <span style="font-weight: bold;">${nick}</span> <fmt:message key="common.only_use_alphanumeric_characters"/>
</p></c:when>
<c:when test="${loginPage eq 'guest'}"><p>
<fmt:message key="common.the_nickname_is_only_registered"/>!
</p></c:when>
<c:when test="${loginPage eq 'nick'}"><p>
<fmt:message key="common.your_nickname_must_be_at"/>
</p></c:when>
<c:when test="${loginPage eq 'command'}"><p>
<fmt:message key="common.your_nickname"/> <span style="font-weight: bold;">${nick}</span> <fmt:message key="common.corresponds_to_a_chat_command"/>
</p></c:when>
<c:when test="${loginPage eq 'pwd'}"><p>
<fmt:message key="common.the_login_for_the_name"/> <span style="font-weight: bold;">${nick}</span> <fmt:message key="common.failed_because_of_a_wrong"/>
</p></c:when>
<c:when test="${loginPage eq 'banned'}"><p>
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
</p></c:when>
<c:when test="${loginPage eq 'banned_temp'}"><p>
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
</p></c:when>
<c:when test="${loginPage eq 'reg'}"><p style="font-weight: bold;">
<fmt:message key="common.in_order_to_enter_this"/>
</p>
<p>
<fmt:message key="common.choose_to_log_in_as"/>!
</p></c:when>
<c:when test="${loginPage eq 'locked_room'}"><p>
<fmt:message key="common.the_room"/> <span style="font-weight: bold;">${room}</span> <fmt:message key="common.could_not_be_entered_reason"/>: <span style="font-weight: bold;">${reason}</span>)
</p>
<p>
<fmt:message key="common.please_choose_another_room"/>!
</p></c:when>
<c:when test="${loginPage eq 'online'}"><p>
<span style="font-weight: bold;">${nick}</span> <fmt:message key="common.is_already_logged_in"/>
</p></c:when>
</c:choose>
</t:layout></c:when>
</c:choose>
