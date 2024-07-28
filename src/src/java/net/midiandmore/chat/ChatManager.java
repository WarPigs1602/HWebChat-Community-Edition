package net.midiandmore.chat;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.gc;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;
import static jakarta.json.Json.createObjectBuilder;
import java.util.logging.Level;
import java.util.logging.Logger;
import static net.midiandmore.chat.ChatLog.LOGGING;
import static net.midiandmore.chat.ErrorLog.LOG;

/**
 * Der Chatmanager: Hier sind die h&auml;ufigsten notwendigen Funktionen des
 * Chats abgelegt. Also Informationen, Nachrichten, Fehlerbehandlung, Benutzer-
 * und Raumverwaltung.
 *
 * @author Andreas Pschorn
 */
public final class ChatManager {

    /**
     * @return the usersPrivchat
     */
    public TreeMap<String, UsersPrivchat> getUsersPrivchat() {
        return usersPrivchat;
    }

    /**
     * @param usersPrivchat the usersPrivchat to set
     */
    public void setUsersPrivchat(TreeMap<String, UsersPrivchat> usersPrivchat) {
        this.usersPrivchat = usersPrivchat;
    }

    /**
     * Initialisiert den Chatmanager
     *
     * @param b Bootstrap-Klasse
     */
    public ChatManager(Bootstrap b) {
        setMaster(b);
        setUsers(new TreeMap<>());
        setUsersPrivchat(new TreeMap<>());
        setUsersCommunity(new TreeMap<>());
        setRooms(new TreeMap<>());
        setConnectionIds(new TreeMap<>());
        String[] value = {"", "", "", "", ""};
        setLoginCache(value);
        setPingTimer(currentTimeMillis());
        setCaptcha(new ArrayList<>());
    }

    /**
     * Loggt einen Chatter im Chat mittels der User-Klasse ein
     *
     * @param name Nickname
     * @param connectionId Sessionid
     * @param room Raum
     * @param referer HTTP-Referer
     * @param userAgent HTTP UserAgent
     * @param host Hostname
     * @param ip IP
     * @param color Chatterfarbe
     * @param status Chatterstatus
     * @param session
     * @param hs
     * @param sv Ist Supervisor (Nur Operatoren)
     * @param serverHost HTTP-Host
     * @param skin Verwendeter Skin
     * @param realIp
     * @param realHost
     * @param osName
     * @param osVersion
     * @param browser
     */
    protected void addUser(String name, String connectionId, String room, String referer,
            String userAgent, String host, String ip, String color, int status, Session session, HttpSession hs, boolean sv, String serverHost, String skin, String realIp, String realHost, String osName, String osVersion, String browser) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        var u = new Users(getMaster(), name, connectionId, room, referer,
                userAgent, host, ip, color, status, session, hs, currentTimeMillis(), sv, serverHost, skin, realIp, realHost, osName, osVersion, browser);
        if (db.isRegistered(name.toLowerCase())) {
            db.updateNick(name.toLowerCase(), "nick2", name);
            u.setRegistered(true);
        }
        name = name.toLowerCase();
        addUser(name, connectionId, u);
        setRoom(name, room, false);
        db.addSession(u.getName(), connectionId, room, color, status);
    }

    /**
     * Loggt einen Chatter im Chat mittels der User-Klasse ein
     *
     * @param name Nickname
     * @param connectionId Sessionid
     * @param room Raum
     * @param referer HTTP-Referer
     * @param userAgent HTTP UserAgent
     * @param host Hostname
     * @param ip IP
     * @param color Chatterfarbe
     * @param status Chatterstatus
     * @param session
     * @param hs
     * @param sv Ist Supervisor (Nur Operatoren)
     * @param serverHost HTTP-Host
     * @param skin Verwendeter Skin
     * @param realIp
     * @param realHost
     * @param osName
     * @param osVersion
     * @param browser
     */
    protected void addUserPrivchat(String name, String connectionId,
            String color, Session session, String skin, String target) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        var u = new UsersPrivchat(name, connectionId, color, session, skin, target);
        name = name.toLowerCase();
        addUserPrivchat(name, connectionId, u);
    }

    /**
     * Entfernt den Chatter wieder
     *
     * @param name Nickname
     */
    protected void removeUser(String name) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        name = name.toLowerCase();
        if (isOnline(name)) {
            var u = getUser(name);
            var room = u.getRoom();
            var connectionId = u.getConnectionId();
            removeUserFromRoom(name, room);
            getUsers().remove(name);
            getConnectionIds().remove(connectionId);
            var hs = u.getHttpSession();
            if (hs != null && u.isChatOnly()) {
                getUsersCommunity().remove(u.getName());
                hs.invalidate();
                try {
                    var s = u.getSession();
                    if (s != null && s.isOpen()) {
                        s.close();
                    }
                } catch (IllegalStateException | NullPointerException e) {

                } catch (IOException ex) {
                    Logger.getLogger(ChatManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                db.delSession(u.getName());
                u = null;
                room = null;
                connectionId = null;
                name = null;
            }
        }
    }

    /**
     * Hat der Chatter die notwendigen Rechte um eine Funktion auszuf&uuml;hren
     *
     * @param param Befehl
     * @param priv Rechte des Chatters
     * @return
     */
    protected boolean isPrivileged(String param, int priv) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        return priv >= parseInt(db.getCmd().get(param));
    }

    /**
     * Sendet einen Text zu einem Chatter (Mit gepasten Emoticons)
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    protected void sendToOneWithNoScroll(String text, String name) {
        var ut = getMaster().getUtil();
        var u = getUser(name);
        if (u != null && u.getKickReason() == null && u.getQuitReason() == null && getRoom(u.getRoom()).isAllowSmilies()) {
            text = ut.replaceSmilies(text);
        }
        sendText(text, name);
    }

    /**
     * Sendet einen Text zu einem Chatter (Mit gepasten Emoticons)
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    protected void sendToOne(String text, String name) {
        var ut = getMaster().getUtil();
        var u = getUser(name);
        if (u != null && u.getKickReason() == null && u.getQuitReason() == null && getRoom(u.getRoom()).isAllowSmilies()) {
            text = ut.replaceSmilies(text);
        }
        sendText(text, name);
        sendScroll(name);
    }

    /**
     * Sendet einen Text zu einem Chatter (Mit gepasten Emoticons)
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    protected void sendToOneDirect(String text, String name) {
        var ut = getMaster().getUtil();
        var u = getUser(name);
        if (u != null && u.getKickReason() == null && u.getQuitReason() == null && getRoom(u.getRoom()).isAllowSmilies()) {
            text = ut.replaceSmilies(text);
        }
        sendTextDirect(text, name);
        sendScroll(name);
    }

    /**
     * Sendet einen Text zu einem Chatter (Ohne gepasten Emoticons)
     *
     * @param name Der Nickname
     */
    protected void sendScroll(String name) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        sendText(db.getCommand("script_scroll"), name);
    }

    /**
     * Sendet einen Text zu einem Chatter (Ohne gepasten Emoticons)
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    protected void sendToOneWithNoSmilies(String text, String name) {
        sendText(text, name);
        sendScroll(name);
    }

    /**
     * Sendet einen Text zu einem Chatter und stellt diesen im Webserver dar
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    protected synchronized void sendTextDirect(String text, String name) {
        var u = getUser(name);
        if (u != null) {
            var s = u.getSession();
            if (s != null && s.isOpen()) {
                try {
                    u.getSession().getBasicRemote().sendText(createObjectBuilder()
                            .add("category", "chat")
                            .add("target", "")
                            .add("message", text)
                            .build().toString());
                } catch (IOException ex) {
                }
            }
        }
    }

    /**
     * Sendet einen Text zu einem Chatter und stellt diesen im Webserver dar
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    private synchronized void sendText(String text, String name) {
        var u = getUser(name);
        if (u != null) {
            var br = new BufferedReader(new StringReader(text));
            try {
                String tok = null;
                while ((tok = br.readLine()) != null) {
                    if (tok.isEmpty()) {
                        continue;
                    }
                    sendTextDirect(tok, name);

                }
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Sendet einen Text zu einem Chatter (Mit optionalen Timestamp)
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    protected void sendTimedMsgToOne(String text, String name) {
        sendToOne(loadCommand("timed_msg", text), name);
    }

    /**
     * Sendet eine Systemnachricht zu einem Chatter (Mit optionalen Timestamp)
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    protected void sendSystemToOne(String text, String name) {
        sendTimedMsgToOne(loadCommand("system_msg", text), name);
    }

    /**
     * L&auml;dt einen Command
     *
     * @param text Der Command
     * @param cmdText Der Inhalt
     */
    private String loadCommand(String text, String cmdText) {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        text = db.getCommand(text);
        text = text.replace("%time%", ut.getCurrentTime());
        text = text.replace("%content%", cmdText);
        return text;
    }

    /**
     * Feherhafter Refresh
     *
     * @param u Der Chatter
     */
    protected void refreshError(Users u) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        u.setWaitingForRefresh(false);
        u.setRefreshError(true);
        u.setQuitReason(db.getCommand("quit_refresh_error"));
        quit(u.getName());
        gc();
    }

    /**
     * Sendet an alle Chatter im Raum eine Textnachricht (Mit optionalem
     * Timestamp)
     *
     * @param text Der Text
     * @param room Der Raum
     * @param name
     */
    protected void sendTimedMsgToAllUsersInRoom(String text, String room, String name) {
        var ut = getMaster().getUtil();
        var e = getAllUserNamesInRoom(room);
        if (getRoom(room).isAllowSmilies()) {
            text = ut.replaceSmilies(text);
        }
        if (e == null) {
            return;
        }
        for (var nick : e) {
            if (!nick.equalsIgnoreCase(name)) {
                sendTimedMsgToOne(text, nick);
            }
        }
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet an alle Chatter im Raum eine Textnachricht (Ohne Smilies)
     *
     * @param text Der Text
     * @param name Der Nickname
     * @param flag
     */
    protected void sendToAllUsersInRoomWithNoSmilies(String text, String name, boolean flag) {
        var ut = getMaster().getUtil();
        var u = getUser(name);
        if (u == null) {
            return;
        }
        var room = u.getRoom();
        var e = getAllUserNamesInRoom(room);
        if (e == null) {
            return;
        }
        e.stream().map((String nick) -> {
            if (flag && !nick.equalsIgnoreCase(name)) {
                sendText(text, nick);
            }
            return nick;
        }).forEachOrdered((nick) -> {
            if (flag && !nick.equalsIgnoreCase(name)) {
                sendScroll(nick);
            }
        });
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Dies loggt den Chatter aus
     *
     * @param name
     */
    protected synchronized void quitAllUsers() {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        getUsers().forEach((name, u) -> {
            var r = getRoom(u.getRoom());
            var owner = r.getOwner();
            var text = db.getCommand("quit");
            var text1 = db.getCommand("quit_supervisor");
            var reason = "";
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%skin%", ut.preReplace(u.getSkin()));
            text1 = text1.replace("%color%", ut.preReplace(u.getColor()));
            text1 = text1.replace("%nick%", ut.preReplace(u.getNewName()));
            text1 = text1.replace("%skin%", ut.preReplace(u.getSkin()));
            text1 = text1.replace("%ip%", ut.preReplace(u.getRealIp().equals("") ? u.getIp() : u.getRealIp() + "@" + u.getIp()));
            text1 = text1.replace("%host%", ut.preReplace(u.getRealIp().equals("") ? u.getHost() : u.getRealHost() + "@" + u.getHost()));
            reason = db.getCommand("quit_reason_default");
            text = text.replace("%reason%", db.getCommand("quit_reason").replace("%content%", reason));
            text = text.replace("%skin%", ut.preReplace(u.getSkin()));
            text1 = text1.replace("%reason%", db.getCommand("quit_reason").replace("%content%", reason));
            text1 = text1.replace("%skin%", ut.preReplace(u.getSkin()));
            sendTimedMsgToAllUsersInRoom(text, u.getRoom(), name);
            sendSystemToSupervisor(text1);
            db.delSession(u.getName());
            try {
                u.getSession().close();
            } catch (IOException ex) {
            }
        });
        getUsers().clear();
        getRooms().clear();
        getUsersPrivchat().clear();
        getConnectionIds().clear();
    }

    /**
     * Dies loggt den Chatter aus
     *
     * @param name
     */
    protected synchronized void quit(String name) {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        try {
            if (name == null) {
                return;
            }
            var u = getUser(name);
            if (u == null) {
                return;
            }

            if (u.isQuitted()) {
                return;
            } else {
                u.setQuitted(true);
            }
            if (!isOnline(name)) {
                return;
            }
            name = u == null ? name : u.getName();
            boolean flood;
            flood = u == null ? false : u.getFlood();
            var r = getRoom(u.getRoom());
            var owner = r.getOwner();
            var text = db.getCommand("quit");
            var text1 = db.getCommand("quit_supervisor");
            var reason = "";
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%skin%", ut.preReplace(u.getSkin()));
            text1 = text1.replace("%color%", ut.preReplace(u.getColor()));
            text1 = text1.replace("%nick%", ut.preReplace(u.getNewName()));
            text1 = text1.replace("%skin%", ut.preReplace(u.getSkin()));
            text1 = text1.replace("%ip%", ut.preReplace(u.getRealIp().equals("") ? u.getIp() : u.getRealIp() + "@" + u.getIp()));
            text1 = text1.replace("%host%", ut.preReplace(u.getRealIp().equals("") ? u.getHost() : u.getRealHost() + "@" + u.getHost()));
            if (flood) {
                u.setQuitReason(db.getCommand("quit_reason_flood"));
            }
            if (u.getQuitReason() != null) {
                reason = u.getQuitReason();
            } else {
                reason = db.getCommand("quit_reason_default");
            }
            text = text.replace("%reason%", db.getCommand("quit_reason").replace("%content%", reason));
            text = text.replace("%skin%", ut.preReplace(u.getSkin()));
            text1 = text1.replace("%reason%", db.getCommand("quit_reason").replace("%content%", reason));
            text1 = text1.replace("%skin%", ut.preReplace(u.getSkin()));
            sendTimedMsgToAllUsersInRoom(text, u.getRoom(), name);
            sendSystemToSupervisor(text1);
            ArrayList<UsersPrivchat> target = getTarget(u.getName());
            for (UsersPrivchat u1 : getUsersPrivchat().values()) {
                if (u1.getName().equalsIgnoreCase(u.getName())) {
                    quitPrivchat(u1.getName(), u1.getTarget());
                }
            }
            text = db.getCommand("script_quit");
            text = text.replace("%nick%", ut.preReplace(name));
            text = text.replace("%skin%", ut.preReplace(u.getSkin()));

            sendToAllUsersInRoomWithNoSmilies(text, name, true);
            if (u.isRegistered()) {
                text = db.getCommand("quit_friends");
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                sendToAllFriendsInChat(text, name);
            }
            u.getSession().close();
        } catch (Exception e) {
        }
        removeUser(name);
    }

    /**
     * Sendet eine Systemnachricht an alle Freunde im Chat
     *
     * @param text Der Text
     * @param name Der Nick
     */
    protected void sendToAllFriendsInChat(String text, String name) {
        var ut = getMaster().getUtil();
        text = ut.replaceSmilies(text);
        for (var u : getUsers().values()) {
            if (u != null && name != null && u.getFriends() != null && u.getFriends().contains(name.toLowerCase())) {
                sendSystemToOne(text, u.getName());
            }
        }
    }

    /**
     * Sendet eine Nachricht an alle im Chat
     *
     * @param nick Der Nick
     */
    protected void removeFriend(String nick) {
        getUsers().values().forEach((u) -> {
            var friends = u.getFriends();
            if (friends.contains(nick.toLowerCase())) {
                friends.remove(nick.toLowerCase());
            }
        });
    }

    /**
     * Sendet eine Nachricht an alle im Chat
     *
     * @param text Der Text
     */
    protected void sendToAllUsersInChat(String text) {
        var ut = getMaster().getUtil();
        text = ut.replaceSmilies(text);
        for (var name : getUsers().keySet()) {
            sendText(text, name);
        }
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet eine Nachricht an alle im Chat (Mit optionalen Timestamp)
     *
     * @param text Der Text
     */
    protected void sendTimedMsgToAllUsersInChat(String text) {
        var ut = getMaster().getUtil();
        text = ut.replaceSmilies(text);
        for (var name : getUsers().keySet()) {
            sendTimedMsgToOne(text, name);
        }
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet eine Systemnachricht an alle im Chat (Mit optionalen Timestamp)
     *
     * @param text Der Text
     */
    protected void sendSystemToAllUsersInChat(String text) {
        var ut = getMaster().getUtil();
        text = ut.replaceSmilies(text);
        for (var name : getUsers().keySet()) {
            sendSystemToOne(text, name);
        }
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet eine Nachricht an alle Chatter mit Supervisormodus im Chat
     *
     * @param text Der Text
     */
    protected void sendToSupervisor(String text) {
        var ut = getMaster().getUtil();
        text = ut.replaceSmilies(text);
        for (var name : getUsers().keySet()) {
            if (getUser(name) != null && getUser(name).isSupervisor()) {
                sendText(text, name);
                sendScroll(name);
            }
        }
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet eine Nachricht an alle Chatter mit Supervisormodus im Chat (Mit
     * Timestamp)
     *
     * @param text Der Text
     */
    protected void sendTimedMsgToSupervisor(String text) {
        var ut = getMaster().getUtil();
        text = ut.replaceSmilies(text);
        for (var nick : getUsers().keySet()) {
            if (getUser(nick) != null && getUser(nick).isSupervisor()) {
                sendTimedMsgToOne(text, nick);
            }
        }
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet eine Systemachricht an alle Chatter mit Supervisormodus im Chat
     * (Mit Timestamp)
     *
     * @param text Der Text
     */
    protected void sendSystemToSupervisor(String text) {
        var ut = getMaster().getUtil();
        text = ut.replaceSmilies(text);
        for (var nick : getUsers().keySet()) {
            if (getUser(nick) != null && getUser(nick).isSupervisor()) {
                sendSystemToOne(text, nick);
            }
        }
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet an alle Chatter im Raum eine Textnachricht (Ohne Smilies)
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    protected void sendToAllUsersInRoomWithNoSmilies(String text, String name) {
        var ut = getMaster().getUtil();
        var u = getUser(name);
        if (u == null) {
            return;
        }
        var room = u.getRoom();
        var e = getAllUserNamesInRoom(room);
        if (e == null) {
            return;
        }
        e.stream().map((nick) -> {
            sendText(text, nick);
            return nick;
        }).forEachOrdered((nick) -> {
            sendScroll(nick);
        });
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet an alle Chatter im Raum eine Textnachricht (Mit Smilies)
     *
     * @param text Der Text
     * @param name Der Nickname
     * @param ignore
     */
    protected void sendToAllUsersInRoom(String text, String name, boolean ignore) {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        var u = getUser(name);
        if (u == null) {
            return;
        }
        var room = u.getRoom();
        if (getRoom(room).isAllowSmilies()) {
            text = ut.replaceSmilies(text);
        }
        if (!u.isVoice() && getRoom(room).isModerated()) {
            text = db.getCommand("moderated_room");
            text = text.replace("%room%", u.getRoom());
            text = ut.replaceSmilies(text);
            sendSystemToOne(text, name);
        } else {
            var e = getAllUserNamesInRoom(room);
            if (e == null) {
                return;
            }
            for (var name1 : e) {
                var u1 = getUser(name1);
                if (u1 == null) {
                    return;
                }
                if (!ignore) {
                    sendText(text, name1);
                    sendScroll(name1);
                } else if (!u1.getIgnore().contains(name.toLowerCase())) {
                    sendText(text, name1);
                    sendScroll(name1);
                }
            }
        }
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet an alle Chatter im Raum eine Textnachricht (Mit optionalem
     * Timestamp)
     *
     * @param text Der Text
     * @param room Der Raum
     */
    protected void sendTimedMsgToAllUsersInRoom(String text, String room) {
        var ut = getMaster().getUtil();
        var e = getAllUserNamesInRoom(room);
        if (getRoom(room).isAllowSmilies()) {
            text = ut.replaceSmilies(text);
        }
        if (e == null) {
            return;
        }
        for (var nick : e) {
            sendTimedMsgToOne(text, nick);
        }
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet an alle Chatter im Raum eine Systemnachricht (Mit optionalem
     * Timestamp)
     *
     * @param text Der Text
     * @param room Der Raum
     */
    protected void sendSystemToAllUsersInRoom(String text, String room) {
        var ut = getMaster().getUtil();
        var e = getAllUserNamesInRoom(room);
        if (e == null) {
            return;
        }
        if (getRoom(room).isAllowSmilies()) {
            text = ut.replaceSmilies(text);
        }
        for (var nick : e) {
            sendSystemToOne(text, nick);
        }
        LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
    }

    /**
     * Sendet an alle Chatter im Raum eine Systemnachricht (Mit optionalem
     * Timestamp)
     *
     * @param text Der Text
     * @param room Der Raum
     */
    protected void sendToAllUsersInRoom(String text, String room) {
        var ut = getMaster().getUtil();
        if (room != null) {
            var e = getAllUserNamesInRoom(room);
            if (e == null) {
                return;
            }
            if (getRoom(room).isAllowSmilies()) {
                text = ut.replaceSmilies(text);
            }
            for (var nick : e) {
                if (nick != null) {
                    sendToOne(text, nick);
                }
            }
            LOGGING.log(FINE, "{0}<br>", ut.removeHtml(text));
        }
    }

    /**
     * Ermittelt alle Chatternamen im Raum
     *
     * @param room Der Raum
     */
    private ArrayList<String> getAllUserNamesInRoom(String room) {
        return getRoom(room).getUsers();
    }

    /**
     * Ermittelt die Anzahl der Chatter im Raum
     *
     * @param room Der Raum
     */
    private String getUserSizeInRoom(String room) {
        return String.valueOf(getRoom(room).getUsers().size());
    }

    /**
     * Ermittelt die Anzahl der Chatter im Chat
     *
     * @return
     */
    protected String getUserSizeInChat() {
        return String.valueOf(getUsers().size());
    }

    /**
     * Ermittelt die Anzahl der R&auml;ume
     */
    private String getRoomsSize() {
        return String.valueOf(getRooms().size());
    }

    /**
     * Ermittelt alle Chatternamen im Raum
     *
     * @param room Der Raum
     */
    protected ArrayList<Users> getAllUsersInRoom(String room) {
        var u = new ArrayList<Users>();
        var e = getAllUserNamesInRoom(room);
        e.forEach((nick) -> {
            u.add(getUser(nick));
        });
        return u;
    }

    protected void sendBirtdayScript(String nick, String room) {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        var e = getAllUsersInRoom(room);
        e.forEach((user) -> {
            if (db.isRegistered(user.getName())) {
                if (ut.hasBirthday(db.getData(user.getName(), "bday_day"), db.getData(user.getName(), "bday_month"), db.getData(user.getName(), "bday_year"))) {
                    var text = db.getCommand("has_birthday");
                    text = text.replace("%nick%", user.getName());
                    sendToAllUsersInRoomWithNoSmilies(text, user.getName());
                }
            }
        });
    }

    /**
     * F&uuml;gt den Raum in die gescriptete Userliste
     *
     * @param room Der Raum
     * @return
     */
    protected String addRoomInUserList(String room) {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        var locked = "0";
        var standard = "0";
        var nosmilies = "0";
        var topic = "";
        if (roomExists(room)) {
            var r = getRoom(room);
            locked = r.isOpen() ? "0" : "1";
            standard = !r.isStandard() ? "0" : "1";
            nosmilies = !r.isAllowSmilies() ? "0" : "1";
            topic = r.getTopic() != null ? r.getTopic() : "";
        } else {
            if (db.roomExists(room)) {
                locked = db.getRoomData(room, "locked");
                standard = db.getRoomData(room, "standard");
                nosmilies = (db.getRoomData(room, "allow_smilies").equals("1")) ? "0" : "1";
                topic = db.getRoomData(room, "topic");
            }
        }
        var text = db.getCommand("script_add_room");
        text = text.replace("%room%", ut.preReplace(room));
        text = text.replace("%locked%", ut.preReplace(locked));
        text = text.replace("%standard%", ut.preReplace(standard));
        text = text.replace("%nosmilies%", ut.preReplace(nosmilies));
        text = text.replace("%topic%", ut.preReplace(topic));
        return text;
    }

    /**
     * Leert die gescriptete Userliste
     *
     * @return
     */
    protected String clearUserlist() {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        return db.getCommand("script_clear_userlist");
    }

    /**
     * F&uuml;llt die gescriptete Userliste
     *
     * @param room Der Raum
     * @return
     */
    protected String addUsersInUserlist(String room) {
        if (roomExists(room)) {
            var eu = getAllUsersInRoom(room);
            var sb = new StringBuilder();
            sb.append(addRoomInUserList(room));
            sb.append("\r\n");
            eu.stream().map((u) -> {
                sb.append(getUserScriptInfo(u));
                return u;
            }).forEachOrdered((_item) -> {
                sb.append("\r\n");
            });
            return sb.toString();
        } else {
            return addRoomInUserList(room) + "\r\n";
        }
    }

    /**
     * F&uuml;gt Benutzerinformationen in die gescriptete Userliste
     *
     * @param u Der User
     * @return
     */
    protected String getUserScriptInfo(Users u) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        var nickName = u.getName();
        var reg = db.isRegistered(nickName);
        var nickGender = reg ? db.getData(nickName, "sex") : "?";
        var nickRegistered = reg ? "1" : "0";
        var nickColor = u.getColor() != null ? u.getColor() : "000000";
        var nickStatus = u.getStatus() != -1 ? String.valueOf(u.getStatus()) : "1";
        var nickAwayStatus = u.isAway() ? "1" : "0";
        var nickAwayReason = u.getAwayReason() == null ? "" : u.getAwayReason();
        var nickGagged = u.isGagged() ? "1" : "0";
        var text = db.getCommand("script_add_user");
        text = text.replace("%nick%", nickName);
        text = text.replace("%gender%", nickGender);
        text = text.replace("%registered%", nickRegistered);
        text = text.replace("%color%", nickColor);
        text = text.replace("%status%", nickStatus);
        text = text.replace("%away%", nickAwayStatus);
        text = text.replace("%awayReason%", nickAwayReason);
        text = text.replace("%gag%", nickGagged);
        return text;
    }

    /**
     * Erzeugt eine Liste der Chatter mit aktiviertem Supervisormodus
     */
    private ArrayList<Users> getSupervisor() {
        var u = new ArrayList<Users>();
        getUsers().values().forEach((u1) -> {
            if (u1.isSupervisor()) {
                u.add(u1);
            }
        });
        return u;
    }

    /**
     * Raumwechsel
     *
     * @param name Der Nickname
     * @param room Der Raum
     * @param sepa Ist ein Separée
     */
    protected void changeRoom(String name, String room, boolean sepa) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        name = name.toLowerCase();
        var u = getUser(name);
        removeUserFromRoom(name, u.getRoom());
        setRoom(name, room, sepa);
        db.updateSession(u.getName(), "room", u.getRoom());
    }

    /**
     * Ist der Raum vorhanden?
     *
     * @param room Raum
     * @return
     */
    protected boolean roomExists(String room) {
        return getRoom(room) != null;
    }

    /**
     * Entfert eine Chatter aus dem Raum
     *
     * @param name Der Nickname
     * @param room Der Raum
     */
    protected void removeUserFromRoom(String name, String room) {
        var r = getRoom(room);
        if (r.isLastUser()) {
            removeRoom(room);
        } else {
            r.remove(name);
        }
        r = null;
    }

    /**
     * Versetzt einen Chatte in einem Raum
     *
     * @param name Der Nickname
     * @param room Der Raum
     * @param sepa Ist ein Separée
     */
    protected void setRoom(String name, String room, boolean sepa) {
        name = name.toLowerCase();
        var u = getUser(name);
        Rooms r = null;
        if (roomExists(room)) {
            r = getRoom(room);
            if (r.getUsers().contains(name)) {
                r.getUsers().remove(name);
                r.optimizeUserList();
            }
            var sus = r.getSus();
            if (u.getStatus() < 4) {
                if (!sus.isEmpty()) {
                    if (sus.contains(name.toLowerCase())) {
                        u.setStatus(3);
                    } else {
                        u.setStatus(1);
                    }
                }
            }
            r.getUsers().add(name);
            u.setRoom(room);
            r.getUsers().sort(null);
        } else {
            r = new Rooms(getMaster(), room, !sepa);
            if (u.getStatus() < 4) {
                if (!r.isStandard() && !u.isKicked()) {
                    var sus = r.getSus();
                    if (!sus.isEmpty()) {
                        if (sus.contains(name.toLowerCase())) {
                            u.setStatus(3);
                        } else {
                            u.setStatus(1);
                        }
                    } else {
                        getUser(name).setStatus(3);
                    }
                } else {
                    u.setStatus(1);
                }
            }
            r.getUsers().add(name);
            addRoom(room, r);
            u.setRoom(room);
            r.getUsers().sort(null);
        }
    }

    /**
     * Wenn Fehler im Chatstream dannn Poste eine Exception im Chat
     *
     * @param e Die Exception
     * @param nick Der Nickname
     */
    protected void postStackTrace(Exception e, String nick) {
        var ut = getMaster().getUtil();
        var u = getUser(nick);
        sendSystemToOne("Guru Meditation: " + e.getLocalizedMessage(), nick);
        if (!u.isSupervisor()) {
            sendSystemToSupervisor("Error from user: " + u.getName());
            sendSystemToSupervisor("Guru Meditation: " + e.getLocalizedMessage());
        }
        var st = new StringTokenizer(ut.getStackTrace(e), "\r\n");
        while (st.hasMoreTokens()) {
            var token = st.nextToken();
            sendSystemToOne("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + token, nick);
            if (!u.isSupervisor()) {
                sendSystemToSupervisor("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + token);
            }
        }
    }

    /**
     * Die Rooms-Klasse abrufen
     *
     * @param room Der Raum
     * @return
     */
    protected Rooms getRoom(String room) {
        /* 
        if (!getRooms().contains(room)) {
            return null;
        }
         */
        return getRooms().get(room);
    }

    /**
     * F&uuml;gt eine Raum hinzu
     *
     * @param room Der Raumname
     * @param r Die Rooms-Klasse
     */
    protected void addRoom(String room, Rooms r) {
        getRooms().put(room, r);
    }

    /**
     * Entfernt einen Raum
     *
     * @param room Der Raum
     */
    protected void removeRoom(String room) {
        getRooms().remove(room);
    }

    /**
     * Ermittelt einen Nickname aus eine Sessionid
     *
     * @param connectionId Die Sessionid
     * @return
     */
    protected String getNameFromId(String connectionId) {
        return getConnectionIds().get(connectionId);
    }

    /**
     * Ist die Sessionid g&uuml;ltig?
     *
     * @param connectionId Die Sessionid
     * @return
     */
    protected boolean isValidConnectionId(String connectionId) {
        return getNameFromId(connectionId) != null;
    }

    /**
     * Holt die passende Users-Klasse anhand des Benutzernamens
     *
     * @param name Der Nick-Name
     * @return
     */
    protected Users getUser(String name) {
        return getUsers().get(name.toLowerCase());
    }

    /**
     * Ist der Chatter Online?
     *
     * @param name Der Nickname
     * @return
     */
    protected boolean isOnline(String name) {
        return getUsers().containsKey(name.toLowerCase());
    }

    /**
     * Ist der Chatter Online?
     *
     * @param name Der Nickname
     * @return
     */
    protected boolean isOnlinePrivchat(String name, String target) {
        for (UsersPrivchat u : getUsersPrivchat().values()) {
            if (u.getName().equalsIgnoreCase(name) && u.getTarget().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ist die Sessionid g&uuml;ltig?
     *
     * @param connectionId Die Sessionid
     * @return
     */
    protected boolean isValidConnectionIdPrivchat(String connectionId, String target) {
        return getNameFromIdPrivchat(connectionId, target) != null;
    }

    protected String getNameFromIdPrivchat(String connectionId, String target) {
        return getUsersPrivchat().get(connectionId).getName();
    }

    /**
     * Holt die passende Users-Klasse anhand des Benutzernamens
     *
     * @param name Der Nick-Name
     * @return
     */
    protected UsersPrivchat getUserPrivchat(String name, String target) {
        for (UsersPrivchat u : getUsersPrivchat().values()) {
            if (u.getName().equalsIgnoreCase(name) && u.getTarget().equalsIgnoreCase(target)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Sendet an alle Chatter im Raum eine Textnachricht (Mit optionalem
     * Timestamp)
     *
     * @param text Der Text
     * @param target
     * @param name
     */
    protected void sendToUser(String text, String name, String target) {
        if (isOnlinePrivchat(name, target)) {
            sendTextDirectPrivchat(text, name, target);
        }
        if (isOnlinePrivchat(target, name)) {
            sendTextDirectPrivchat(text, target, name);
        }
    }

    /**
     * Wenn Fehler im Chatstream dannn Poste eine Exception im Chat
     *
     * @param e Die Exception
     * @param nick Der Nickname
     */
    protected void postStackTracePrivchat(Exception e, String nick, String target) {
        var ut = getMaster().getUtil();
        var u = getUserPrivchat(nick, target);
        sendToUser("Guru Meditation: " + e.getLocalizedMessage() + "<br>", nick, target);
        var st = new StringTokenizer(ut.getStackTrace(e), "\r\n");
        while (st.hasMoreTokens()) {
            var token = st.nextToken();
            sendToUser("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + token + "<br>", nick, target);
        }
    }

    /**
     * Ist der Chatter Online?
     *
     * @param name Der Nickname
     * @return
     */
    protected ArrayList<UsersPrivchat> getTarget(String name) {
        ArrayList<UsersPrivchat> u2 = new ArrayList<>();
        String target = null;
        for (UsersPrivchat u : getUsersPrivchat().values()) {
            if (name.equalsIgnoreCase(u.getName())) {
                u2.add(u);
            }
        }
        return u2;
    }

    /**
     * Sendet eine Systemnachricht zu einem Chatter (Mit optionalen Timestamp)
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    protected void sendSystemToUser(String text, String name, String target) {
        sendTimedMsgToUser(loadCommand("system_msg", text), name, target);
    }

    /**
     * Sendet an alle Chatter im Raum eine Textnachricht (Mit optionalem
     * Timestamp)
     *
     * @param text Der Text
     * @param target
     * @param name
     */
    protected void sendTimedMsgToUser(String text, String name, String target) {
        if (isOnlinePrivchat(name, target)) {
            sendTextDirectPrivchat(loadCommand("timed_msg", text), name, target);
        }
        if (isOnlinePrivchat(target, name)) {
            sendTextDirectPrivchat(loadCommand("timed_msg", text), target, name);
        }
    }

    /**
     * Entfernt den Chatter wieder
     *
     * @param name Nickname
     */
    protected void removeUserPrivchat(String name, String target) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        name = name.toLowerCase();
        if (isOnline(name)) {
            var u = getUserPrivchat(name, target);
            getUsersPrivchat().remove(u.getConnectionId());
            try {
                try {
                    var s = u.getSession();
                    if (s != null && s.isOpen()) {
                        s.close();
                    }
                } catch (IllegalStateException | NullPointerException e) {

                }
            } catch (IOException ex) {
            }
            db.delSession(u.getName());
            u = null;
            name = null;
        }
    }

    /**
     * Dies loggt den Chatter aus
     *
     * @param name
     */
    protected synchronized void quitPrivchat(String name, String target) {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        try {
            if (name == null) {
                return;
            }
            var u = getUserPrivchat(name, target);
            if (u == null) {
                return;
            }

            if (u.isQuitted()) {
                return;
            } else {
                u.setQuitted(true);
            }
            if (!isOnline(name)) {
                return;
            }
            name = u == null ? name : u.getName();
            boolean flood;
            flood = u == null ? false : u.getFlood();
            var text = db.getCommand("quit");
            var reason = "";
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%skin%", ut.preReplace(u.getSkin()));
            if (flood) {
                u.setQuitReason(db.getCommand("quit_reason_flood"));
            }
            reason = db.getCommand("quit_reason_default");
            text = text.replace("%reason%", db.getCommand("quit_reason").replace("%content%", reason));
            text = text.replace("%skin%", ut.preReplace(u.getSkin()));
            try {
                sendTimedMsgToUser(text, name, target);
            } catch (Exception e) {

            }
            u.getSession().close();
            u = null;

        } catch (Exception e) {

        }
        removeUserPrivchat(name, target);
    }

    /**
     * Sendet einen Text zu einem Chatter und stellt diesen im Webserver dar
     *
     * @param text Der Text
     * @param name Der Nickname
     */
    protected synchronized void sendTextDirectPrivchat(String text, String nick, String target) {
        try {
            if (isOnlinePrivchat(nick, target)) {
                var u = getUserPrivchat(nick, target);
                var s = u.getSession();
                if (s != null && s.isOpen()) {
                    try {
                        u.getSession().getBasicRemote().sendText(createObjectBuilder()
                                .add("category", "privchat_user_" + nick + "|" + target)
                                .add("target", "")
                                .add("message", text)
                                .build().toString());
                    } catch (IOException ex) {
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * F&uuml;gt einen Chatter im Chat hinzu
     *
     * @param name Der Nickname
     * @param connectionId Die Sessionid
     * @param u Die Users-Klasse
     */
    protected void addUserPrivchat(String name, String connectionId, UsersPrivchat u) {
        getUsersPrivchat().put(connectionId, u);
    }

    /**
     * F&uuml;gt einen Chatter im Chat hinzu
     *
     * @param name Der Nickname
     * @param connectionId Die Sessionid
     * @param u Die Users-Klasse
     */
    protected void addUser(String name, String connectionId, Users u) {
        name = name.toLowerCase();
        getUsers().put(name, u);
        getConnectionIds().put(connectionId, name);
    }

    /**
     * Fr&auml;gt die aktuelle Freundesliste ab.
     *
     * @param nick Die Sessionid
     */
    protected void sendOnlineFriendsList(String nick) {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        var u = getUser(nick);
        if (u == null) {
            return;
        }
        var friends = u.getFriends();
        if (friends == null) {
            return;
        }
        var friendCount = u.getFriends().size();
        if (friendCount == 0) {
            sendToOne(db.getCommand("friends_not_in_list"), nick);
            sendToOne("<br>", nick);
            return;
        }
        friendCount = 0;
        for (var name : friends) {

            if (isOnline(name)) {
                friendCount++;
            }
        }
        if (friendCount == 0) {
            sendToOne(db.getCommand("friends_empty"), nick);
            sendToOne("<br>", nick);
            return;
        }
        friends = u.getFriends();
        var text = db.getCommand("friends_title");
        text = text.replace("%count%", String.valueOf(friendCount));
        sendToOne(text, nick);
        for (var name : friends) {
            if (!isOnline(name)) {
                continue;
            }
            var u1 = getUser(name);
            text = db.getCommand("friends_text");
            text = text.replace("%nick%", u.getName());
            text = text.replace("%color%", u1.getColor());
            text = text.replace("%user%", u1.getName());
            text = text.replace("%login_time%", ut.getSimpleTime(u1.getLoginTime()));
            text = text.replace("%idle_time%", String.valueOf((currentTimeMillis() - u1.getIdleTime()) / 1000));
            sendToOne(text, nick);
        }
        sendToOne("<br>", nick);
    }

    /**
     * Ruft eine User-Liste im HTML-Format ab
     *
     * @param sid Die Sessionid
     * @param skin Der Skin
     * @return
     */
    protected String getUserList(String sid, String skin) {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        Users u = null;
        String text = null;
        String roomText = null;
        String t = null;
        String status = null;
        var sb = new StringBuilder();
        var moderated = false;
        text = db.getCommand("chat_list_title");
        text = text.replace("%count_chat%", getUserSizeInChat());
        text = text.replace("%count_room%", getRoomsSize());
        sb.append(text).append("\r\n");
        for (var room : getRooms().keySet()) {
            var names = getAllUserNamesInRoom(room);
            text = db.getCommand("chat_list_room");
            if (getRoom(room).isOpen()) {
                roomText = db.getCommand("chat_list_room_open");
                roomText = roomText.replace("%room%", room);
            } else {
                roomText = db.getCommand("chat_list_room_closed");
                roomText = roomText.replace("%room%", room);
            }
            text = text.replace("%room%", roomText);
            t = getRoom(room).getTopic();
            if (t != null) {
                roomText = db.getCommand("chat_list_room_topic");
                roomText = roomText.replace("%topic%", t);
            } else {
                roomText = "";
            }
            text = text.replace("%topic%", roomText);
            moderated = getRoom(room).isModerated();
            if (moderated) {
                roomText = db.getCommand("chat_list_room_moderated");
            } else {
                roomText = "";
            }
            text = text.replace("%moderated%", roomText);
            text = text.replace("%count_room%", getUserSizeInRoom(room));
            text = text.replace("%count_chat%", getUserSizeInChat());
            sb.append(text);
            var name = new StringBuilder();
            for (var count = 0; count < names.size(); count++) {
                var nick = names.get(count);
                u = getUser(nick);
                if (u == null) {
                    var r = getRoom(room);
                    r.optimizeUserList();
                    if (r.isLastUser()) {
                        removeRoom(room);
                    } else {
                        r.remove(names.get(count));
                    }
                    r = null;
                    LOG.log(WARNING, "Normal Userlist Error");
                    continue;
                }
                if (u.getStatus() >= 3) {
                    text = db.getCommand("chat_list_is_su_start");
                    sb.append(text);
                }
                if (u.isAway()) {
                    text = db.getCommand("chat_list_is_away_start");
                    text = text.replace("%away_reason%", u.getAwayReason());
                    sb.append(text);
                }
                if (u.isGagged()) {
                    text = db.getCommand("chat_list_is_gag_start");
                    sb.append(text);
                }
                text = db.getCommand("chat_list_names").replace("%color%", u.getColor());
                text = text.replace("%nick%", u.getName());
                text = text.replace("%sid%", sid);
                text = text.replace("%skin%", skin);
                text = ut.replaceNoCookies(text);
                sb.append(text);
                if (u.isGagged()) {
                    text = db.getCommand("chat_list_is_gag_end");
                    sb.append(text);
                }
                if (u.isAway()) {
                    text = db.getCommand("chat_list_is_away_end");
                    sb.append(text);
                }
                if (u.getStatus() >= 3) {
                    text = db.getCommand("chat_list_is_su_end");
                    sb.append(text);
                }
                if (names.size() - 1 != count) {
                    sb.append(db.getCommand("chat_list_split"));
                }
                text = null;
                status = null;
                u = null;
            }
            sb.append("<br>\r\n");
        }
        return sb.toString();
    }

    /**
     * Info f&uuml;r Chatinformationsseiten wie z. B. http://www.webchat.de Im
     * Json Format
     *
     * @return
     */
    protected String getWebChatJson() {
        String text = null;
        String room = null;
        var sb = new StringBuilder();
        sb.append("[\r\n");
        var i = 0;
        var len = getUsers().size();
        for (var us : getUsers().keySet()) {
            Users u = getUsers().get(us);
            sb.append("{ \r\n   \"name\": \"");
            sb.append(u.getName());
            sb.append("\",\r\n   \"time\": \"");
            sb.append(u.getLoginTime() / 1000);
            sb.append("\",\r\n   \"idle_time\": \"");
            sb.append((currentTimeMillis() - u.getIdleTime()) / 1000);
            sb.append("\",\r\n   \"full_idle_time\": \"");
            sb.append((currentTimeMillis() / 1000) - u.getFloodTime());
            sb.append("\",\r\n   \"room\": \"");
            sb.append(u.getRoom());
            sb.append("\",\r\n   \"color\": \"");
            sb.append(u.getColor());
            sb.append("\",\r\n   \"status\": \"");
            sb.append(u.getStatus());
            sb.append("\",\r\n   \"away\": \"");
            sb.append(u.isAway() ? u.getAwayReason() : "");
            sb.append("\",\r\n   \"gagged\": \"");
            sb.append(u.isGagged() ? "true" : "false");
            sb.append("\",\r\n   \"registered\": \"");
            sb.append(u.isRegistered() ? "true" : "false");
            sb.append("\"\r\n");
            if (len - 1 != i) {
                sb.append("},");
            } else {
                sb.append("}");
            }
            sb.append("\r\n");
            i++;
        }
        sb.append("]\r\n");
        return sb.toString();
    }

    /**
     * Info f&uuml;r Chatinformationsseiten wie z. B. http://www.webchat.de
     *
     * @return
     */
    protected String getWebChat() {
        Users u = null;
        String text = null;
        var sb = new StringBuilder();
        for (var room : getRooms().keySet()) {
            var names = getAllUserNamesInRoom(room);
            var name = new StringBuilder();
            for (var nick : names) {
                u = getUser(nick);
                sb.append("(");
                sb.append(u.getName());
                sb.append(") ");
                sb.append("(");
                sb.append(u.getLoginTime() / 1000);
                sb.append(") ");
                sb.append("(");
                sb.append(u.getRoom());
                sb.append(")\r\n");
                u = null;
            }
        }
        return sb.toString();
    }

    /**
     * Userliste f&uuml;r Staffmitglieder im HTML-Format
     *
     * @param sid Die Sessionid
     * @return
     */
    protected String getVipUserList(String sid) {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        Users u = null;
        String text = null;
        String roomText = null;
        String t = null;
        String status = null;
        var sb = new StringBuilder();
        text = db.getCommand("chat_vip_list_title");
        text = text.replace("%count_chat%", getUserSizeInChat());
        text = text.replace("%count_room%", getRoomsSize());
        sb.append(text).append("\r\n");
        for (var room : getRooms().keySet()) {
            var names = getAllUserNamesInRoom(room);
            text = db.getCommand("chat_vip_list_room");
            if (getRoom(room).isOpen()) {
                roomText = db.getCommand("chat_vip_list_room_open");
                roomText = roomText.replace("%room%", room);
            } else {
                roomText = db.getCommand("chat_vip_list_room_closed");
                roomText = roomText.replace("%room%", room);
            }
            text = text.replace("%room%", roomText);
            t = getRoom(room).getTopic();
            if (t != null) {
                roomText = db.getCommand("chat_vip_list_room_topic");
                roomText = roomText.replace("%topic%", t);
            } else {
                roomText = "";
            }
            text = text.replace("%topic%", roomText);
            text = text.replace("%count_room%", getUserSizeInRoom(room));
            text = text.replace("%count_chat%", getUserSizeInChat());
            sb.append(text);
            sb.append("\r\n");
            sb.append(db.getCommand("chat_vip_list_header"));
            var name = new StringBuilder();
            for (var nick : names) {
                u = getUser(nick);
                if (u == null) {
                    LOG.log(WARNING, "Vip-Userlist Error");
                    continue;
                }
                text = db.getCommand("chat_vip_list_names").replace("%color%", u.getColor());
                text = text.replace("%nick%", u.getName());
                text = text.replace("%sid%", sid);
                text = text.replace("%ip%", u.getRealIp().equals("") ? u.getIp() : u.getRealIp() + "@" + u.getIp());
                text = text.replace("%host%", u.getRealIp().equals("") ? u.getHost() : u.getRealHost() + "@" + u.getHost());
                text = text.replace("%server_host%", u.getServerHost());
                text = text.replace("%referer%", u.getReferer());
                text = text.replace("%ip_type%", u.isIPv6() ? "IPv6" : "IPv4");
                text = text.replace("%user_agent%", ut.parseHtml(u.getUserAgent()));
                sb.append(text);
                text = null;
                status = null;
                u = null;
            }
            sb.append(db.getCommand("chat_vip_list_footer"));
            sb.append("\r\n");
        }
        return sb.toString();
    }

    /**
     * Die Raumuserliste im HTML-Format
     *
     * @param room Der Raum
     * @param sid Die Sessionid
     * @param skin Der Skin
     * @return
     */
    protected String getUserList(String nick, String room, String sid, String skin) {
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig();
        Users u = null;
        String text = null;
        String reason = null;
        String status = null;
        String gag = null;
        String privchat = null;
        Database db = conf.getDb();
        var sb = new StringBuilder();
        var names = getAllUserNamesInRoom(room);
        if (getRoom(room).isOpen()) {
            text = db.getCommand("room_list_title_open").replace("%room%", room);
        } else {
            text = db.getCommand("room_list_title_close").replace("%room%", room);
        }
        if (getRoom(room).isModerated()) {
            text = text.replace("%moderated%", db.getCommand("room_list_title_moderated"));
        } else {
            text = text.replace("%moderated%", "");
        }
        text = text.replace("%count_room%", getUserSizeInRoom(room));
        text = text.replace("%count_chat%", getUserSizeInChat());
        sb.append(text).append("\r\n");
        for (var i = 0; i < names.size(); i++) {
            u = getUser(names.get(i));
            if (u == null) {
                var r = getRoom(room);
                r.optimizeUserList();
                if (r.isLastUser()) {
                    removeRoom(room);
                } else {
                    r.remove(names.get(i));
                }
                r = null;
                LOG.log(WARNING, "Detailed-Userlist Error");
                continue;
            }
            if (u.isAway()) {
                reason = db.getCommand("room_list_away").replace("%reason%", u.getAwayReason());
                reason = ut.replaceLinks(reason);
            } else {
                reason = "";
            }
            if (u.isGagged()) {
                gag = db.getCommand("room_list_gag");
            } else {
                gag = "";
            }
            if (getUser(nick).getName().equals(u.getName())) {
                privchat = "";
            } else {
                privchat = db.getCommand("private_chat");
                privchat = privchat.replace("%nick%", u.getName());
            }
            if (u.getStatus() >= 10) {
                status = db.getCommand("room_list_status_10");
            } else if (u.getStatus() == 9) {
                status = db.getCommand("room_list_status_9");
            } else if (u.getStatus() == 8) {
                status = db.getCommand("room_list_status_8");
            } else if (u.getStatus() == 7) {
                status = db.getCommand("room_list_status_7");
            } else if (u.getStatus() == 6) {
                status = db.getCommand("room_list_status_6");
            } else if (u.getStatus() == 5) {
                status = db.getCommand("room_list_status_5");
            } else if (u.getStatus() == 4) {
                status = db.getCommand("room_list_status_4");
            } else if (u.isSuperuser()) {
                status = db.getCommand("room_list_status_3");
            } else if (u.isVoice()) {
                status = db.getCommand("room_list_status_2");
            } else {
                status = "";
            }
            if (db.isRegistered(u.getName()) && db.getData(u.getName(), "moderator").equals("1")) {
                status = status + db.getCommand("room_list_status_moderator");
            }
            if (u.isRegistered()) {
                text = db.getCommand("room_list_names").replace("%color%", u.getColor());
            } else {
                text = db.getCommand("room_list_names_unreg").replace("%color%", u.getColor());
            }
            text = text.replace("%nick%", u.getName());
            text = text.replace("%login_time%", ut.getSimpleTime(u.getLoginTime()));
            text = text.replace("%idle_time%", String.valueOf((currentTimeMillis() - u.getIdleTime()) / 1000));
            text = text.replace("%away%", reason);
            text = text.replace("%status%", status);
            text = text.replace("%sid%", sid);
            text = text.replace("%skin%", skin);
            text = text.replace("%gag%", gag);
            text = text.replace("%privchat%", privchat);
            var owner = getRoom(room).getOwner();
            owner = owner != null ? owner : "";
            text = text.replace("%owner%", owner);
            text = ut.replaceNoCookies(text);
            sb.append(text);
            if (names.size() - 1 != i) {
                sb.append("\r\n");
            }
            text = null;
            status = null;
            reason = null;
            u = null;
        }
        return sb.toString();
    }

    /**
     * Die Chatverbindung aufrechterhalten
     */
    protected synchronized void pingPong() {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        db.eraseExpiredBans();
        for (var u : getUsers().values()) {
            try {
                var timer = u.getTimer();
                var timeoutTimer = u.getTimeoutTimer();
                if (timer == 60) {
                    if (db.isRegistered(u.getName())) {
                        db.updateNick(u.getName(), "points", String.valueOf(Integer.parseInt(db.getData(u.getName(), "points")) + 1));
                        db.updateLoginTime(u.getName());
                    }
                    timer = 0;
                } else {
                    timer = timer + 1;
                }
                u.setTimer(timer);
                if (u.getSession() != null && u.getSession().isOpen()) {
                    if (timeoutTimer == conf.getLong("timeout")) {
                        timeout(u);
                        return;
                    }
                    if (timeoutTimer == conf.getLong("ping") || timeoutTimer == conf.getLong("ping") * 2) {
                        u.getSession().getBasicRemote().sendPing(ByteBuffer.wrap("Ping? Pong!".getBytes(conf.getString("charset"))));
                        sendToOneWithNoScroll(db.getCommand("ping"), u.getName());
                    }
                    timeoutTimer = timeoutTimer + 1;
                    u.setTimeoutTimer(timeoutTimer);
                    getUsersCommunity().get(u.getNewName()).setTimeoutTimer(0);
                } else {
                    quit(u.getName());
                }
            } catch (IOException | IllegalArgumentException ex) {
            }
        }
        for (UsersPrivchat u : getUsersPrivchat().values()) {
            try {
                var timer = u.getTimer();
                var timeoutTimer = u.getTimeoutTimer();
                timer = timer + 1;
                u.setTimer(timer);
                if (u.getSession() != null && u.getSession().isOpen()) {
                    if (timeoutTimer == conf.getLong("timeout")) {
                        timeoutPrivchat(u);
                        return;
                    }
                    if (timeoutTimer == conf.getLong("ping") || timeoutTimer == conf.getLong("ping") * 2) {
                        u.getSession().getBasicRemote().sendPing(ByteBuffer.wrap("Ping? Pong!".getBytes(conf.getString("charset"))));
                        sendToOneWithNoScroll(db.getCommand("ping"), u.getName());
                    }
                    timeoutTimer = timeoutTimer + 1;
                    u.setTimeoutTimer(timeoutTimer);
                } else {
                    quitPrivchat(u.getName(), u.getTarget());
                }
            } catch (Exception ex) {
            }
        }
        for (UsersCommunity u : getUsersCommunity().values()) {
            try {
                var timeoutTimer = u.getTimeoutTimer();
                if (timeoutTimer == conf.getLong("timeout_community")) {
                    if (u.getHttpSession() != null) {
                        u.getHttpSession().invalidate();
                    }
                    getUsersCommunity().remove(u.getName());
                    return;
                }
                timeoutTimer = timeoutTimer + 1;
                u.setTimeoutTimer(timeoutTimer);
            } catch (Exception ex) {
            }
        }
        gc();
    }

    /**
     * Die Chatverbindung aufrechterhalten
     *
     * @param u Der Chatter
     */
    protected void timeoutPrivchat(UsersPrivchat u) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        u.setQuitReason(db.getCommand("quit_timeout"));
        u.setTimeout(true);
        quitPrivchat(u.getName(), u.getTarget());
        gc();
    }

    /**
     * Die Chatverbindung aufrechterhalten
     *
     * @param u Der Chatter
     */
    protected void timeout(Users u) {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        u.setQuitReason(db.getCommand("quit_timeout"));
        u.setTimeout(true);
        quit(u.getName());
        gc();
    }

    private TreeMap<String, Users> users;
    private TreeMap<String, UsersCommunity> usersCommunity;
    private TreeMap<String, UsersPrivchat> usersPrivchat;
    private TreeMap<String, Rooms> rooms;
    private TreeMap<String, String> connectionIds;
    private String[] loginCache;
    private long pingTimer = -1;
    private ArrayList<String[]> captcha;

    /**
     * &Uuml;berpr&uuml;ft die G&uuml;ltigkeit eines Captchas
     *
     * @param chars Die verwendete Zeichenkette
     * @param cid Die Captchaid
     * @return
     */
    protected boolean isCorrectCaptcha(String chars, String cid) {
        for (var num = 0; num < 15; num++) {
            if (getCaptcha().get(num)[0].equals(chars) && getCaptcha().get(num)[1].equals(cid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ruft die Captchaliste ab
     *
     * @return
     */
    protected ArrayList<String[]> getCaptcha() {
        return this.captcha;
    }

    /**
     * Vergibt eine neue Captchaliste
     *
     * @param captcha Die Captchaliste
     */
    private void setCaptcha(ArrayList<String[]> captcha) {
        this.captcha = captcha;
        for (var num = 0; num < 15; num++) {
            String[] capt = {Long.toString(currentTimeMillis()), Long.toString(currentTimeMillis())};
            getCaptcha().add(capt);
        }
    }

    /**
     * Aktualisiert die Captchaliste
     *
     * @param chars Die zu vergebende Zeichenkette
     * @param cid Die Captchaid
     */
    protected void updateCaptcha(String chars, String cid) {
        String[] elem = {chars, cid};
        getCaptcha().add(elem);
        getCaptcha().remove(0);
    }

    /**
     * Ruft die Users-TreeMap ab, wo die Chatter gespeichert sind.
     */
    protected TreeMap<String, Users> getUsers() {
        return users;
    }

    /**
     * Ruft alle Sessionids ab
     */
    protected TreeMap<String, String> getConnectionIds() {
        return connectionIds;
    }

    private Bootstrap master;

    /**
     *
     * @return
     */
    public Bootstrap getMaster() {
        return master;
    }

    /**
     *
     * @param master
     */
    public void setMaster(Bootstrap master) {
        this.master = master;
    }

    /**
     * Setzt die Users-TreeMap neu
     *
     * @param users Die neue Users-TreeMap
     */
    protected void setUsers(TreeMap<String, Users> users) {
        this.users = users;
    }

    /**
     * Setzt die Liste der R&auml;ume
     *
     * @param rooms R&auml;e
     */
    protected void setRooms(TreeMap<String, Rooms> rooms) {
        this.rooms = rooms;
    }

    /**
     * Ruft die Raumnamen ab
     *
     * @return
     */
    protected TreeMap<String, Rooms> getRooms() {

        return rooms;
    }

    /**
     * Setzt die Sessionids neu
     *
     * @param connectionIds Sessionid-Liste
     */
    protected void setConnectionIds(TreeMap<String, String> connectionIds) {
        this.connectionIds = connectionIds;
    }

    /**
     * Ruft den Login-Cache-Array ab
     *
     * @return
     */
    protected String[] getLoginCache() {
        return loginCache;
    }

    /**
     * Vergibt den Login-Cache-Array neu
     *
     * @param loginCache
     */
    protected void setLoginCache(String[] loginCache) {
        this.loginCache = loginCache;
    }

    /**
     * Setzt den Ping-Intervall neu
     *
     * @param pingTimer Neuer Ping-Intervall
     */
    protected void setPingTimer(long pingTimer) {
        this.pingTimer = pingTimer;
    }

    /**
     * Ruft den Ping-Intervall ab
     *
     * @return
     */
    protected long getPingTimer() {
        return this.pingTimer;
    }

    /**
     * @return the usersCommunity
     */
    public TreeMap<String, UsersCommunity> getUsersCommunity() {
        return usersCommunity;
    }

    /**
     * @param usersCommunity the usersCommunity to set
     */
    public void setUsersCommunity(TreeMap<String, UsersCommunity> usersCommunity) {
        this.usersCommunity = usersCommunity;
    }
}
