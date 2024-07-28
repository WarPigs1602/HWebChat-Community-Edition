/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.midiandmore.chat;

import java.io.IOException;
import java.io.StringReader;
import java.net.UnknownHostException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.PongMessage;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.net.InetAddress.getByName;
import java.util.Enumeration;
import static jakarta.json.Json.createReader;
import java.util.List;
import java.util.Map;
import static net.midiandmore.chat.Bootstrap.boot;
import static java.util.logging.Level.SEVERE;

/**
 *
 * @author windo
 */
@ServerEndpoint(value = "/Chat", configurator = Chat.ChatHandshake.class)
public class Chat {
    
    private HttpSession httpSession;
    private Session session;
    private String cat;
    private Map<String, List<String>> map;

    /**
     *
     * @param session
     * @param config
     * @throws java.io.IOException
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        var conf = boot.getConfig();
        var db = conf.getDb();
        var ut = boot.getUtil();
        var cm = boot.getChatManager();
        try {
            setSession(session);
            setMap(session.getRequestParameterMap());
            getSession().setMaxIdleTimeout(conf.getLong("socket_timeout"));
            var hs = (HttpSession) config.getUserProperties()
                    .get(HttpSession.class.getName());
            setHttpSession(hs);
            var target = getMap().containsKey("target") ? getMap().get("target").get(0) : "";
            if (!getMap().containsKey("target")) {
                var pwd = getMap().containsKey("pwd") ? getMap().get("pwd").get(0) : (String) getHttpSession().getAttribute("pwd");
                var room = getMap().containsKey("room") ? getMap().get("room").get(0) : (String) getHttpSession().getAttribute("room");
                var owner = getMap().containsKey("owner") ? getMap().get("owner").get(0) : (String) getHttpSession().getAttribute("owner");
                var nick = getMap().containsKey("nick") ? getMap().get("nick").get(0) : (String) getHttpSession().getAttribute("nick");
                var sid = getMap().containsKey("sid") ? getMap().get("sid").get(0) : (String) getHttpSession().getAttribute("sid");
                var skin = getMap().containsKey("skin") ? getMap().get("skin").get(0) : (String) getHttpSession().getAttribute("skin");
                boolean chatOnly = getMap().containsKey("chat_only");
                if (getHttpSession() != null && !getHttpSession().isNew()) {
                    getSession().getUserProperties().put("nick", nick);
                    getSession().getUserProperties().put("pwd", pwd);
                    getSession().getUserProperties().put("skin", skin);
                    getSession().getUserProperties().put("room", room);
                }
                config.getUserProperties().replace("user-agent", getHttpSession().getAttribute("user-agent"));
                nick = nick != null ? nick : "";
                pwd = pwd != null ? pwd : "";
                skin = skin != null ? skin : conf.getString("default_skin");
                room = room != null ? room : "";
                sid = sid != null ? sid : "";
                if (room.isBlank()) {
                    room = conf.getString("default_room");
                }
                var host = (String) config.getUserProperties().get("host");
                var reg = false;
                var sv = false;
                var ip = (String) config.getUserProperties().get("ip");
                String proxyIp = null;
                var realIp = (String) config.getUserProperties().get("x-forwarded-for");
                realIp = realIp != null ? realIp : "";
                ip = ip != null ? ip : "";
                if (realIp.contains(", ")) {
                    realIp = realIp.substring(0, realIp.indexOf(", "));
                }
                if (conf.getString("cloudflare").equals("1")) {
                    ip = realIp;
                    realIp = "";
                }
                if (conf.getString("use_proxy").equals("1")) {
                    proxyIp = (String) config.getUserProperties().get(conf.getString("real_ip"));
                }
                if (proxyIp != null && !proxyIp.equals("")) {
                    ip = proxyIp;
                }
                String ipResolved = null;
                String ipResolved2 = null;
                try {
                    ipResolved = conf.getString("resolve_ip").equals("1") ? getByName(ip).getCanonicalHostName() : ip;
                } catch (UnknownHostException uhe) {
                    ipResolved = ip;
                }
                try {
                    ipResolved2 = conf.getString("resolve_ip").equals("1") ? getByName(realIp).getCanonicalHostName() : realIp;
                } catch (UnknownHostException uhe) {
                    ipResolved2 = realIp;
                }
                db.eraseExpiredBans();
                if (db.isRegistered(nick) && !db.getData(nick, "pwd2").isEmpty() && db.checkPassword2(nick, pwd)) {
                    db.updateNick(nick, "pwd2", "");
                    db.updatePassword(nick, pwd);
                }
                if (!nick.matches(conf.getString("allowed_chars"))) {
                    ut.sendText(getTemplate("chars_chat"), getSession(), "error", "");
                    disconnect();
                } else if (nick.length() < conf.getInt("min_nick_length")) {
                    ut.sendText(getTemplate("nick"), getSession(), "error", "");
                    disconnect();
                } else if (nickIsCommand(nick)) {
                    ut.sendText(getTemplate("command_chat"), getSession(), "error", "");
                    disconnect();
                } else if ((reg = db.isRegistered(nick)) && !db.checkPassword(nick, pwd)) {
                    ut.sendText(getTemplate("pwd_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isBanned(nick) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(nick));
                    ut.sendText(getTemplate("banned_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isBanned(ip) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(ip));
                    ut.sendText(getTemplate("banned_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isBanned(realIp) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(realIp));
                    ut.sendText(getTemplate("banned_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isBanned(ipResolved) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(ipResolved));
                    ut.sendText(getTemplate("banned_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isBanned(ipResolved2) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(ipResolved2));
                    ut.sendText(getTemplate("banned_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isBanned(realIp) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(realIp));
                    ut.sendText(getTemplate("banned_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isTimedBanned(nick) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(nick));
                    ut.sendText(getTemplate("banned_temp_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isTimedBanned(ip) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(nick));
                    ut.sendText(getTemplate("banned_temp_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isTimedBanned(realIp) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(realIp));
                    ut.sendText(getTemplate("banned_temp_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(ipResolved));
                    ut.sendText(getTemplate("banned_temp_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isTimedBanned(ipResolved2) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(ipResolved2));
                    ut.sendText(getTemplate("banned_temp_chat"), getSession(), "error", "");
                    disconnect();
                } else if (db.isTimedBanned(realIp) && (!reg || (reg && parseInt(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                    getSession().getUserProperties().put("reason", db.getBanReason(realIp));
                    ut.sendText(getTemplate("banned_temp_chat"), getSession(), "error", "");
                    disconnect();
                } else if (!reg && (conf.getInt("guest") == 1 && !nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase())) && conf.getInt("only_registered_users") == 1) {
                    ut.sendText(getTemplate("reg_chat"), getSession(), "error", "");
                    disconnect();
                } else if (!reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1")) {
                    ut.sendText(getTemplate("locked_room_chat"), getSession(), "error", "");
                    disconnect();
                } else if (reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && parseInt(db.getData(nick, "status")) < conf.getInt("lock_status")) {
                    ut.sendText(getTemplate("locked_room_chat"), getSession(), "error", "");
                    disconnect();
                } else if (cm.isOnline(nick)) {
                    ut.sendText(getTemplate("online_chat"), getSession(), "error", "");
                    disconnect();
                } else {
                    ut.sendText(getTemplate("chat_header_new"), getSession(), "chat", "");
                    String color = null;
                    String oldNick = nick;
                    if (reg) {
                        color = db.getData(oldNick, "color");
                    } else if (conf.getInt("random_color") == 1) {
                        color = ut.createRandomColor();
                    } else {
                        color = conf.getString("default_color");
                    }
                    var status = 0;
                    if (reg) {
                        status = parseInt(db.getData(nick, "status"));
                        if (!db.getData(oldNick, "nick2").equalsIgnoreCase(db.getData(oldNick, "nick"))) {
                            oldNick = db.getData(oldNick, "nick");
                            nick = db.getData(oldNick, "nick2");
                        }
                        var ts = ut.getTime(db.getLongData(nick, "timestamp_login"));
                        db.updateLoginTime(oldNick);
                        var wr = getTemplate("welcome_reg");
                        wr = wr.replace("%color%", color);
                        wr = wr.replace("%nick%", oldNick);
                        wr = wr.replace("%status%", Integer.toString(status));
                        wr = wr.replace("%news_count%", valueOf(db.countMessage(oldNick)));
                        wr = wr.replace("%last_login%", ts);
                        wr = wr.replace("%skin%", skin);
                        wr = wr.replace("%owner%", owner);
                        ut.sendText(wr, getSession(), "chat", "");
                    }
                    if (!realIp.equals("")) {
                        ut.sendText(getTemplate("proxy_warn"), getSession(), "chat", "");
                    }
                    if (status >= conf.getInt("status_admin")) {
                        ut.sendText(getTemplate("welcome_admin"), getSession(), "chat", "");
                        sv = db.getData(oldNick, "sv").equals("1");
                    } else if (status >= conf.getInt("status_staff")) {
                        ut.sendText(getTemplate("welcome_staff"), getSession(), "chat", "");
                    }
                    if (reg && ut.hasBirthday(db.getData(oldNick, "bday_day"), db.getData(oldNick, "bday_month"), db.getData(oldNick, "bday_year"))) {
                        var wr = getTemplate("bday_greeting");
                        wr = wr.replace("%age%", Integer.toString(ut.getAge(db.getData(oldNick, "bday_day"), db.getData(oldNick, "bday_month"), db.getData(oldNick, "bday_year"))));
                        ut.sendText(wr, getSession(), "chat", "");
                    }
                    getHttpSession().setMaxInactiveInterval(-1);
                    var referer = (String) config.getUserProperties().get("referer");
                    var userAgent = (String) config.getUserProperties().get("user-agent");
                    var uaName = (String) getHttpSession().getAttribute("os-name");
                    var uaVersion = (String) getHttpSession().getAttribute("os-version");
                    var browser = (String) getHttpSession().getAttribute("browser");
                    uaName = uaName != null ? uaName : "unknown";
                    uaVersion = uaVersion != null ? uaVersion : "0";
                    referer = referer != null ? referer : "";
                    userAgent = userAgent != null ? userAgent : "";
                    ipResolved = ipResolved != null ? ipResolved : "";
                    ip = ip != null ? ip : "";
                    color = color != null ? color : "000000";
                    status = status != -1 ? status : 1;
                    host = host != null ? host : "";
                    skin = skin != null ? skin : conf.getString("default_skin");
                    realIp = realIp != null ? realIp : "";
                    ipResolved2 = ipResolved2 != null ? ipResolved2 : "";
                    var text = cm.addUsersInUserlist(room);
                    cm.addUser(oldNick, sid, room, referer,
                            userAgent, ipResolved, ip, color, status, getSession(), getHttpSession(), sv, host, skin, realIp, ipResolved2, uaName, uaVersion, browser);
                    if (!oldNick.equalsIgnoreCase(nick)) {
                        cm.getUser(oldNick).setNewName(nick);
                    } else {
                        cm.getUser(oldNick).setNewName(oldNick);
                    }
                    cm.getUser(oldNick).setJsid(getHttpSession().getId());
                    cm.getUser(oldNick).setFriends(db.getFriendList(nick));
                    cm.getUser(oldNick).setServerInfo((String) getHttpSession().getAttribute("server"));
                    cm.getUser(oldNick).setChatOnly((String) getHttpSession().getAttribute("chat_only") != null);
                    cm.getRoom(room).optimizeUserList();
                    if (reg) {
                        status = parseInt(db.getData(oldNick, "status"));
                        db.updateLoginTime(oldNick);
                    }
                    /*
            try {
            Map<String, Object> globalPropertiesMap = config.getUserProperties();         
            for(Map.Entry<String, Object> entry : globalPropertiesMap.entrySet()) {
                String key = entry.getKey();
               cm.sendToOneWithNoSmilies("key: " + key + ", value: " + globalPropertiesMap.get(key)+"<br>", nick); 
            }
            } catch (Exception e) {
                cm.postStackTrace(e, nick);
            } */
                    cm.sendToOneWithNoSmilies(text, nick);
                    text = cm.getUserScriptInfo(cm.getUser(oldNick));
                    cm.sendToAllUsersInRoomWithNoSmilies(text, oldNick);
                    if (reg) {
                        cm.sendOnlineFriendsList(oldNick);
                    }
                    text = db.getCommand("join");
                    text = text.replace("%color%", ut.preReplace(color));
                    text = text.replace("%nick%", ut.preReplace(nick));
                    text = text.replace("%skin%", ut.preReplace(skin));
                    cm.sendTimedMsgToAllUsersInRoom(text, room);
                    text = db.getCommand("script_join");
                    text = text.replace("%color%", ut.preReplace(color));
                    text = text.replace("%nick%", ut.preReplace(nick));
                    text = text.replace("%status%", ut.preReplace(Integer.toString(status)));
                    text = text.replace("%skin%", ut.preReplace(skin));
                    cm.sendToAllUsersInRoomWithNoSmilies(text, nick);
                    text = db.getCommand("join_supervisor");
                    text = text.replace("%color%", ut.preReplace(color));
                    text = text.replace("%nick%", ut.preReplace(nick));
                    text = text.replace("%skin%", ut.preReplace(skin));
                    text = text.replace("%ip%", ut.preReplace(cm.getUser(oldNick).getRealIp().equals("") ? cm.getUser(oldNick).getIp() : cm.getUser(oldNick).getRealIp() + "@" + cm.getUser(oldNick).getIp()));
                    text = text.replace("%host%", ut.preReplace(cm.getUser(oldNick).getRealIp().equals("") ? cm.getUser(oldNick).getHost() : cm.getUser(oldNick).getRealHost() + "@" + cm.getUser(oldNick).getHost()));
                    cm.sendSystemToSupervisor(text);
                    text = db.getCommand("join_friends");
                    text = text.replace("%color%", ut.preReplace(color));
                    text = text.replace("%nick%", ut.preReplace(nick));
                    text = text.replace("%skin%", ut.preReplace(skin));
                    cm.sendToAllFriendsInChat(text, oldNick);
                    var t = cm.getRoom(room).getTopic();
                    cm.getRoom(room).setOwner(owner);
                    if (t != null) {
                        text = db.getCommand("topic_room");
                        text = text.replace("%room%", ut.preReplace(room));
                        text = text.replace("%topic%", ut.preReplace(t));
                        text = text.replace("%skin%", ut.preReplace(skin));
                        cm.sendSystemToOne(text, nick);
                    }
                    cm.sendBirtdayScript(cm.getUser(oldNick).getName(), cm.getUser(oldNick).getRoom());
                }
                
            } else {
                var nick = getMap().containsKey("nick") ? getMap().get("nick").get(0) : "";
                var sid = getMap().containsKey("sid") ? getMap().get("sid").get(0) : "";
                var skin = getMap().containsKey("skin") ? getMap().get("skin").get(0) : "";
                var jsid = getHttpSession().getId();
                if (Bootstrap.boot.getConfig().getDb().isRegistered(nick)) {
                    nick = Bootstrap.boot.getConfig().getDb().getData(nick, "nick");
                }
                ut.sendText(getTemplate("chat_header_new"), getSession(), "privchat_user_" + nick + "|" + target, "");
                ut.sendText(getTemplate("chat_header_privchat"), getSession(), "privchat_user_" + nick + "|" + target, "");
                if (!cm.isOnline(target)) {
                    ut.sendText(getTemplate("privchat_not_online"), getSession(), "privchat_user_" + nick + "|" + target, "");
                    disconnect();
                } else if (!cm.isOnline(nick)) {
                    ut.sendText(getTemplate("privchat_not_online"), getSession(), "privchat_user_" + nick + "|" + target, "");
                    disconnect();
                } else if (nick.equalsIgnoreCase(target)) {
                    ut.sendText(getTemplate("privchat_same_nick"), getSession(), "privchat_user_" + nick + "|" + target, "");
                    disconnect();
                } else if (!cm.getUser(nick).getJsid().equals(jsid)) {
                    ut.sendText(getTemplate("privchat_session"), getSession(), "privchat_user_" + nick + "|" + target, "");
                    disconnect();
                } else {
                    Users u = cm.getUser(nick);
                    String color = u.getColor();
                    String newName = u.getNewName();
                    if (cm.isOnlinePrivchat(nick, target)) {
                        cm.quitPrivchat(nick, target);
                    }
                    cm.addUserPrivchat(nick, sid, color, getSession(), skin, target);
                    UsersPrivchat u1 = cm.getUserPrivchat(nick, target);
                    u1.setNewName(newName);
                    var text = db.getCommand("join");
                    text = text.replace("%color%", ut.preReplace(color));
                    text = text.replace("%nick%", ut.preReplace(newName));
                    text = text.replace("%skin%", ut.preReplace(skin));
                    cm.sendTimedMsgToUser(text, nick, target);
                }
                
            }
        } catch (NullPointerException e) {
            ut.sendText("Exception: " + e.getLocalizedMessage(), getSession(), "error", "");
            ErrorLog.LOG.log(SEVERE, "Exception: ", e);
        } catch (Exception e) {
            ut.sendText("Exception: " + e.getLocalizedMessage(), getSession(), "error", "");
            ErrorLog.LOG.log(SEVERE, "Exception: ", e);;
        }
    }

    /**
     *
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        var com = boot.getCommands();
        var json = createReader(new StringReader(message)).readObject();
        var category = json.getString("category");
        var target = json.getString("target");
        var text = json.getString("message");
        var sid = getMap().containsKey("sid") ? getMap().get("sid").get(0) : (String) getHttpSession().getAttribute("sid");
        if (category.equals("chat")) {
            com.parseCommand(text, sid);
        } else if (category.startsWith("privchat_user_")) {
            var target2 = getMap().containsKey("target") ? getMap().get("target").get(0) : (String) getSession().getUserProperties().get("target");
            
            com.parseCommandPrivchat(text, sid, target2);
            
        }
    }

    /**
     *
     * @param message
     */
    @OnMessage
    public synchronized void onPong(PongMessage message) {
        var conf = boot.getConfig();
        var db = conf.getDb();
        var cm = boot.getChatManager();
        var name = getMap().containsKey("nick") ? getMap().get("nick").get(0) : (String) getSession().getUserProperties().getOrDefault("nick", "");
        if (!getMap().containsKey("target")) {
            var u = cm.getUser(name);
            if (u != null) {
                cm.sendToOneWithNoScroll(db.getCommand("pong"), name);
                u.setTimeoutTimer(0);
            }
        } else {
            var target = getMap().get("target").get(0);
            var u = cm.getUserPrivchat(name, target);
            if (u != null) {
                cm.sendTextDirectPrivchat(db.getCommand("pong"), name, target);
                u.setTimeoutTimer(0);
            }
        }
    }

    /**
     *
     * @param session
     * @throws IOException
     */
    @OnClose
    public synchronized void onClose(Session session) throws IOException {
        var cm = boot.getChatManager();
        try {
            if (session.isOpen()) {
                var name = getMap().containsKey("nick") ? getMap().get("nick").get(0) : (String) getSession().getUserProperties().getOrDefault("nick", "");
                var target = getMap().containsKey("target") ? getMap().get("target").get(0) : (String) getSession().getUserProperties().getOrDefault("target", "");
                if (target.equals("")) {
                    if (cm.isOnline(name)) {
                        var u = cm.getUser(name);
                        if (u != null) {
                            cm.quit(u.getName());
                        }
                    }
                } else {
                    if (cm.isOnlinePrivchat(name, target)) {
                        var u = cm.getUserPrivchat(name, target);
                        if (u != null) {
                            cm.quitPrivchat(u.getName(), target);
                        }
                    }
                }
            }
        } catch (IllegalStateException | NullPointerException e) {
            
        }

        // WebSocket connection closes
    }

    /**
     *
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable
    ) {
        var cm = boot.getChatManager();
        try {
            if (session.isOpen()) {
                var name = getMap().containsKey("nick") ? getMap().get("nick").get(0) : (String) getSession().getUserProperties().getOrDefault("nick", "");
                var target = getMap().containsKey("target") ? getMap().get("target").get(0) : (String) getSession().getUserProperties().getOrDefault("target", "");
                if (target.equals("")) {
                    
                    if (cm.isOnline(name)) {
                        var u = cm.getUser(name);
                        if (u != null) {
                            u.setQuitReason(throwable.getLocalizedMessage());
                            cm.quit(u.getName());
                        }
                    }
                } else {
                    if (cm.isOnlinePrivchat(name, target)) {
                        var u = cm.getUserPrivchat(name, target);
                        if (u != null) {
                            u.setQuitReason(throwable.getLocalizedMessage());
                            cm.quitPrivchat(u.getName(), target);
                        }
                    }
                }
            }
        } catch (IllegalStateException | NullPointerException e) {
            
        }
        // Do error handling here
    }
    
    private void disconnect() {
        if (getHttpSession() != null && !getMap().containsKey("target") && getHttpSession().getAttribute("chat_only") != null) {
            getHttpSession().invalidate();
        }
        if (getSession().isOpen()) {
            try {
                getSession().close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Liefert ein Template als String aus
     *
     * @param template Der Templatename
     * @return
     */
    protected String getTemplate(String template) {
        var conf = boot.getConfig();
        var db = conf.getDb();
        var ut = boot.getUtil();
        String sid = null;
        String target = getMap().containsKey("target") ? getMap().get("target").get(0) : (String) getSession().getUserProperties().getOrDefault("target", "");
        if (target.equals("") && getHttpSession() != null) {
            sid = (String) getHttpSession().getAttribute("sid");
        } else {
            sid = (String) getSession().getUserProperties().get("sid");
        }
        String nick = null;
        String pwd = null;
        String skin = null;
        String room = null;
        var reason = getSession().getUserProperties().get("reason");
        if (sid == null) {
            sid = getMap().containsKey("sid") ? getMap().get("sid").get(0) : (String) getSession().getUserProperties().get("sid");
        }
        if (target.equals("") && getHttpSession() != null) {
            nick = getMap().containsKey("nick") ? getMap().get("nick").get(0) : (String) getHttpSession().getAttribute("nick");
            pwd = getMap().containsKey("pwd") ? getMap().get("pwd").get(0) : (String) getHttpSession().getAttribute("pwd");
            skin = getMap().containsKey("skin") ? getMap().get("skin").get(0) : (String) getHttpSession().getAttribute("skin");
            room = getMap().containsKey("room") ? getMap().get("room").get(0) : (String) getHttpSession().getAttribute("room");
        } else {
            nick = getMap().containsKey("nick") ? getMap().get("nick").get(0) : (String) getSession().getUserProperties().get("nick");
            skin = getMap().containsKey("skin") ? getMap().get("skin").get(0) : (String) getSession().getUserProperties().get("skin");
            room = getMap().containsKey("room") ? getMap().get("room").get(0) : (String) getSession().getUserProperties().get("room");
            pwd = getMap().containsKey("pwd") ? getMap().get("pwd").get(0) : (String) getSession().getUserProperties().get("pwd");
        }
        nick = nick != null ? nick : "";
        skin = skin != null ? skin : conf.getString("default_skin");
        room = room != null ? room : "";
        pwd = pwd != null ? pwd : "";
        sid = sid != null ? sid : "";
        var data = db.getTemplate(template, skin);
        if (data.toLowerCase().startsWith("error")) {
            var temp = data;
            data = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p><b>%missing_template%</b></p><hr><address>%SERVER_SOFTWARE% %SERVER_VERSION%-%SERVER_STATUS% Build: #%SERVER_BUILD_NUMBER%</address></body><html>";
            data = data.replace("%missing_template%", temp);
        }
        data = ut.replacePaths(data);
        data = ut.replaceFilePaths(data);
        data = ut.replaceDefaultReplacements(data, ut.preReplace(nick), sid, ut.preReplace(skin), ut.preReplace(room), "", "");
        data = ut.replaceServerInfo(data);
        return data;
    }

    /**
     * &Uuml;berpr&uuml;ft ob ein Nickname einem Befehl entspricht
     *
     * @param nick Der Nickname
     */
    private boolean nickIsCommand(String nick) {
        var conf = boot.getConfig();
        var db = conf.getDb();
        if (nick.equalsIgnoreCase("nick")) {
            return true;
        } else  if (nick.equalsIgnoreCase("moderator")) {
            return true;
        } else if (nick.equalsIgnoreCase("query")) {
            return true;
        } else if (nick.equalsIgnoreCase("mail")) {
            return true;
        } else if (nick.equalsIgnoreCase("dice")) {
            return true;
        } else if (nick.equalsIgnoreCase("fcol")) {
            return true;
        } else if (nick.equalsIgnoreCase("sf")) {
            return true;
        } else if (nick.equalsIgnoreCase("dp")) {
            return true;
        } else if (nick.equalsIgnoreCase("rf")) {
            return true;
        } else if (nick.equalsIgnoreCase("af")) {
            return true;
        } else if (nick.equalsIgnoreCase("wc")) {
            return true;
        } else if (nick.equalsIgnoreCase("me")) {
            return true;
        } else if (nick.equalsIgnoreCase("s")) {
            return true;
        } else if (nick.equalsIgnoreCase("j")) {
            return true;
        } else if (nick.equalsIgnoreCase("sepa")) {
            return true;
        } else if (nick.equalsIgnoreCase("c")) {
            return true;
        } else if (nick.equalsIgnoreCase("q")) {
            return true;
        } else if (nick.equalsIgnoreCase("hk")) {
            return true;
        } else if (nick.equalsIgnoreCase("t")) {
            return true;
        } else if (nick.equalsIgnoreCase("col")) {
            return true;
        } else if (nick.equalsIgnoreCase("away")) {
            return true;
        } else if (nick.equalsIgnoreCase("su")) {
            return true;
        } else if (nick.equalsIgnoreCase("beam")) {
            return true;
        } else if (nick.equalsIgnoreCase("sys")) {
            return true;
        } else if (nick.equalsIgnoreCase("rsu")) {
            return true;
        } else if (nick.equalsIgnoreCase("k")) {
            return true;
        } else if (nick.equalsIgnoreCase("gag")) {
            return true;
        } else if (nick.equalsIgnoreCase("mod")) {
            return true;
        } else if (nick.equalsIgnoreCase("rv")) {
            return true;
        } else if (nick.equalsIgnoreCase("gv")) {
            return true;
        } else if (nick.equalsIgnoreCase("ban")) {
            return true;
        } else if (nick.equalsIgnoreCase("uban")) {
            return true;
        } else if (nick.equalsIgnoreCase("w")) {
            return true;
        } else if (nick.equalsIgnoreCase("chgrights")) {
            return true;
        } else if (nick.equalsIgnoreCase("ig")) {
            return true;
        } else if (nick.equalsIgnoreCase("rehash")) {
            return true;
        } else if (nick.equalsIgnoreCase("q")) {
            return true;
        } else if (nick.equalsIgnoreCase("l")) {
            return true;
        } else if (nick.equalsIgnoreCase("t")) {
            return true;
        } else if (nick.equalsIgnoreCase("sv")) {
            return true;
        } else if (nick.equalsIgnoreCase("shutdown")) {
            return true;
        } else if (nick.equalsIgnoreCase("wipe")) {
            return true;
        } else if (nick.equalsIgnoreCase("sbans")) {
            return true;
        } else if (nick.equalsIgnoreCase("list")) {
            return true;
        } else if (nick.equalsIgnoreCase("info")) {
            return true;
        } else if (nick.equalsIgnoreCase("myip")) {
            return true;
        } else if (nick.equalsIgnoreCase("")) {
            return true;
        } else if (nick.equalsIgnoreCase("a")) {
            return true;
        } else if (nick.equalsIgnoreCase("i")) {
            return true;
        } else if (nick.equalsIgnoreCase("catch")) {
            return true;
        } else {
            return db.getFun().get(nick.toLowerCase()) != null;
        }
    }

    /**
     * @return the httpSession
     */
    protected HttpSession getHttpSession() {
        return httpSession;
    }

    /**
     * @param httpSession the httpSession to set
     */
    protected void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    /**
     * @return the session
     */
    protected Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    protected void setSession(Session session) {
        this.session = session;
    }

    /**
     * Chat
     */
    public static class ChatHandshake extends ServerEndpointConfig.Configurator {

        /**
         *
         * @param sec
         * @param req
         * @param response
         */
        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest req, HandshakeResponse response) {
            var request = getField(req, HttpServletRequest.class);
            Enumeration paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                var key = (String) paramNames.nextElement();
                var value = request.getParameter(key);
                sec.getUserProperties().put(key.toLowerCase(), value);
            }
            
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                var key = (String) headerNames.nextElement();
                var value = request.getHeader(key);
                sec.getUserProperties().put(key.toLowerCase(), value);
            }
            sec.getUserProperties().put("ip", request.getRemoteAddr()); // lower-case!
            var httpSession = (HttpSession) request.getSession(false);
            sec.getUserProperties().put(HttpSession.class.getName(), httpSession);
        }

        //hacking reflector to expose fields...
        @SuppressWarnings("unchecked")
        private static < I, F> F getField(I instance, Class< F> fieldType) {
            try {
                for (var type = instance.getClass(); type != Object.class; type = type.getSuperclass()) {
                    for (var field : type.getDeclaredFields()) {
                        if (fieldType.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            return (F) field.get(instance);
                        }
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | SecurityException e) {
                // Handle?
            }
            return null;
        }
        
    }

    /**
     * @return the cat
     */
    public String getCat() {
        return cat;
    }

    /**
     * @param cat the cat to set
     */
    public void setCat(String cat) {
        this.cat = cat;
    }

    /**
     * @return the map
     */
    public Map<String, List<String>> getMap() {
        return map;
    }

    /**
     * @param map the map to set
     */
    public void setMap(Map<String, List<String>> map) {
        this.map = map;
    }
}
