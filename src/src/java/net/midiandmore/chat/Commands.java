package net.midiandmore.chat;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.exit;
import static java.lang.System.gc;
import static java.lang.System.getProperty;
import static java.lang.System.out;
import java.util.ArrayList;
import static java.util.logging.Level.FINE;
import jakarta.mail.MessagingException;
import static net.midiandmore.chat.Bootstrap.boot;
import static net.midiandmore.chat.ChatLog.LOGGING;

/**
 * Verwaltung der Chatbefehle
 *
 * @author Andreas Pschorn
 */
public class Commands implements Software {

    private Bootstrap master;

    /**
     * Initialisert Die Commands
     *
     * @param master Die Bootstrap-Klasse
     */
    public Commands(Bootstrap master) {
        setMaster(master);
    }

    /**
     * Verabeitet die Eingaben des Chatters
     *
     * @param input Die Eingabe des Chatters
     * @param connectionId Die Sessionid des Chatters
     */
    protected void parseCommand(String input, String connectionId) {
        String nick = null;
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        try {
            if (input.length() != 0 && connectionId.length() != 0) {
                input = input.trim();
                input = input.replace('\r', '\0');
                input = input.replace('\n', '\0');
                input = input.replace('\t', '\0');
                if (input.length() > getMaster().getConfig().getInt("max_text_length")) {
                    input = input.substring(0, getMaster().getConfig().getInt("max_text_length"));
                }
                if (cm.isValidConnectionId(connectionId)) {
                    nick = cm.getNameFromId(connectionId);
                    var u = cm.getUser(nick);
                    u.setTimeoutTimer(0);
                    var count = u.getRepeatCount();
                    var floodTime = u.getFloodTime();
                    var currTime = currentTimeMillis() / 1000;
                    var diffTime = currTime - floodTime;
                    var floodCount = u.getFloodCount();
                    u.setFloodTime(currTime);
                    if (diffTime < getMaster().getConfig().getInt("flood_timer_interval")) {
                        floodCount++;
                    } else {
                        floodCount = 0;
                    }
                    u.setFloodCount(floodCount);
                    if (floodCount < getMaster().getConfig().getInt("flood_max_lines_per_interval")) {
                        var repLine = u.getRepeatLine();
                        if (repLine.equals(input)) {
                            count++;
                        } else {
                            count = 0;
                        }
                        u.setRepeatCount(count);
                        u.setRepeatLine(input);
                        if (count < getMaster().getConfig().getInt("flood_max_repeat")) {
                            if (input.startsWith("/")) {
                                parseCommands(input.substring(1, input.length()), nick);
                            } else {
                                postMsg(input, nick);
                            }
                        } else if (count == getMaster().getConfig().getInt("flood_max_repeat")) {
                            var text = db.getCommand("flood_repeat_msg");
                            cm.sendSystemToOne(text, nick);
                        }
                    } else {
                        u.setFlood(true);
                        cm.quit(nick);
                    }
                }
            }
        } catch (NullPointerException npe) {
            if (nick != null) {
                cm.postStackTrace(npe, nick);
            }
        } catch (Exception e) {
            if (nick != null) {
                cm.postStackTrace(e, nick);
            }
        }
        gc();
    }

    /**
     * Verabeitet die Eingaben des Chatters
     *
     * @param input Die Eingabe des Chatters
     * @param connectionId Die Sessionid des Chatters
     */
    protected void parseCommandPrivchat(String input, String connectionId, String target) {
        String nick = null;
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        try {
            if (input.length() != 0 && connectionId.length() != 0) {
                input = input.trim();
                input = input.replace('\r', '\0');
                input = input.replace('\n', '\0');
                input = input.replace('\t', '\0');
                if (input.length() > getMaster().getConfig().getInt("max_text_length")) {
                    input = input.substring(0, getMaster().getConfig().getInt("max_text_length"));
                }
                if (cm.isValidConnectionIdPrivchat(connectionId, target)) {
                    nick = cm.getNameFromIdPrivchat(connectionId, target);
                    var u = cm.getUserPrivchat(nick, target);
                    u.setTimeoutTimer(0);
                    var count = u.getRepeatCount();
                    var floodTime = u.getFloodTime();
                    var currTime = currentTimeMillis() / 1000;
                    var diffTime = currTime - floodTime;
                    var floodCount = u.getFloodCount();
                    u.setFloodTime(currTime);
                    if (diffTime < getMaster().getConfig().getInt("flood_timer_interval")) {
                        floodCount++;
                    } else {
                        floodCount = 0;
                    }
                    u.setFloodCount(floodCount);
                    if (floodCount < getMaster().getConfig().getInt("flood_max_lines_per_interval")) {
                        var repLine = u.getRepeatLine();
                        if (repLine.equals(input)) {
                            count++;
                        } else {
                            count = 0;
                        }
                        u.setRepeatCount(count);
                        u.setRepeatLine(input);
                        if (count < getMaster().getConfig().getInt("flood_max_repeat")) {
                            var text = db.getCommand("chat_msg");
                            text = text.replace("%color%", ut.preReplace(u.getColor()));
                            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                            input = ut.parseHtml(input);
                            input = ut.replaceLinks(input);
                            input = ut.parseBb(input);
                            input = ut.replaceSmilies(input);
                            text = text.replace("%content%", ut.preReplace(input));
                            cm.sendToUser(text, nick, target);
                        } else if (count == getMaster().getConfig().getInt("flood_max_repeat")) {
                            var text = db.getCommand("flood_repeat_msg");
                            cm.sendToOneDirect(text + "<br>", nick);
                        }
                    } else {
                        u.setFlood(true);
                        cm.quit(nick);
                    }
                }
            }
        } catch (NullPointerException npe) {
            if (nick != null) {
                cm.postStackTracePrivchat(npe, nick, target);
            }
        } catch (Exception e) {
            if (nick != null) {
                cm.postStackTracePrivchat(e, nick, target);
            }
        }
        gc();
    }

    /**
     * Schreibt einen normalen Text &ouml;ffentlich
     *
     * @param input Der Text
     * @param nick Der Chatternick
     */
    private void postMsg(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (u.isGagged()) {
            gagged(nick, u.getRoom());
        } else {
            var text = db.getCommand("chat_msg");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%content%", ut.preReplace(input));
            cm.sendToAllUsersInRoom(text, nick, true);
        }
    }

    /**
     * Schreibt eine Handlung
     *
     * @param input Der Text
     * @param nick Der Chatternick
     */
    private void postMe(String input, String nick) {
        resetIdleTime(nick);
        var ut = getMaster().getUtil();
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        input = ut.replaceLinks(input);
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("me", u.getStatus())) {
            low("me", nick);
            return;
        }
        if (u.isGagged()) {
            gagged(nick, u.getRoom());
        } else {
            var text = db.getCommand("chat_me");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%content%", ut.preReplace(input));
            cm.sendToAllUsersInRoom(text, nick, true);

        }
    }

    /**
     * Wechselt die Chatterfarbe
     *
     * @param input Die Farbe im Format "RRGGBB"
     * @param nick Der Chatternick
     */
    private void changeColor(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        input = input.toUpperCase();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("color", u.getStatus())) {
            low("col", nick);
            return;
        }
        if (input.length() != 6 || !input.matches("[A-F0-9]*")) {
            invalidColor(input, nick);
        } else if (u.getColor().equals(input)) {
            sameColor(input, nick);
        } else if (isInvalidColor(input.toCharArray())) {
            brightColor(input, nick);
        } else {
            var text = db.getCommand("fade");
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%old_color%", ut.preReplace(u.getColor()));
            text = text.replace("%new_color%", ut.preReplace(input));
            ArrayList<UsersPrivchat> target = cm.getTarget(u.getName());
            for (UsersPrivchat u1 : cm.getUsersPrivchat().values()) {
                if (u1.getName().equalsIgnoreCase(u.getName())) {
                    u1.setColor(input);
                }
            }
            for (UsersPrivchat u1 : target) {
                cm.sendToUser(text, u.getName(), u1.getTarget());

            }
            cm.sendToAllUsersInRoom(text, nick, false);
            u.setColor(input);
            text = db.getCommand("script_color");
            text = text.replace("%color%", input);
            text = text.replace("%nick%", u.getNewName());
            cm.sendToAllUsersInRoom(text, nick, false);

            db.updateSession(nick, "color", input);
            if (db.isRegistered(nick)) {
                db.updateNick(nick, "color", input);
            }
        }
    }

    /**
     * Pr&uuml;ft ob Die Farbe ung&uuml;ltig ist
     *
     * @param col Die Farbe im Format "RRGGBB"
     */
    private boolean isInvalidColor(char[] col) {
        var valid = true;
        var c = getMaster().getConfig();
        if (c.getString("bright_0").indexOf(col[0]) != -1) {
            valid = false;
        }
        if (c.getString("bright_1").indexOf(col[1]) != -1) {
            valid = false;
        }
        if (c.getString("bright_2").indexOf(col[2]) != -1) {
            valid = false;
        }
        if (c.getString("bright_3").indexOf(col[3]) != -1) {
            valid = false;
        }
        if (c.getString("bright_4").indexOf(col[4]) != -1) {
            valid = false;
        }
        if (c.getString("bright_5").indexOf(col[5]) != -1) {
            valid = false;
        }
        return !valid;
    }

    /**
     * Loggt den Chatter aus
     *
     * @param input Der Grund
     * @param nick Der Chatternick
     */
    private void quit(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("quit", u.getStatus())) {
            low("q", nick);
            return;
        }
        if (input != null) {
            u.setQuitReason(input);
        }
        cm.quit(nick);
    }

    /**
     * Chatdistributionsinformationen
     *
     * @param nick Der Chatternick
     */
    private void info(String nick) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("info", u.getStatus())) {
            low("info", nick);
            return;
        }
        var line = line();
        cm.sendToOne(line, nick);
        cm.sendToOne(SERVER_SOFTWARE + " " + SERVER_VERSION + "-" + SERVER_STATUS + "<br>", nick);
        cm.sendToOne("&copy; " + SERVER_YEAR + " by <a href=\"mailto:" + SERVER_MAIL + "\">" + SERVER_VENDOR + "</a><br>", nick);
        cm.sendToOne("All rights reserved.<br>", nick);
        cm.sendToOne("<a href=\"" + getMaster().getConfig().getString("path_hwebchat") + "?page=" + getMaster().getConfig().getString("path_link") + "&url=http://" + SERVER_HOMEPAGE + "\" target=\"_blank\">" + SERVER_HOMEPAGE + "</a><br>", nick);
        cm.sendToOne("<br>", nick);
        cm.sendToOne("<span style=\"font-weight: bold\">Backend information:</span><br>", nick);
        cm.sendToOne("Running on system: <span style=\"font-weight: bold\">" + getProperty("os.name") + " (" + getProperty("os.version") + ", " + getProperty("os.arch") + ")</span><br>", nick);
        cm.sendToOne("Java version: <span style=\"font-weight: bold\">" + getProperty("java.version") + " (" + getProperty("java.vendor") + ")</span><br>", nick);
        cm.sendToOne("Running on container: <span style=\"font-weight: bold\">" + u.getServerInfo() + "</span><br>", nick);
        cm.sendToOne("<br>", nick);
        cm.sendToOne("<span style=\"font-weight: bold\">Frontend information:</span><br>", nick);
        cm.sendToOne("Browser: <span style=\"font-weight: bold\">" + ut.getBrowser(ut.parseHtml(u.getUserAgent()), ut.parseHtml(u.getBrowser())) + "</span><br>", nick);
        cm.sendToOne("Operating system: <span style=\"font-weight: bold\">" + ut.getOs(ut.parseHtml(u.getUserAgent()), u.getOsName(), u.getOsVersion()) + "</span><br>", nick);
        cm.sendToOne("<br>", nick);
        cm.sendToOne("<span style=\"font-weight: bold\">Time information:</span><br>", nick);
        cm.sendToOne("The chat is running since: <span style=\"font-weight: bold\">" + ut.getTime(getMaster().getStartTime()) + " " + ut.getCurrentUsTimeZone() + "</span><br>", nick);
        cm.sendToOne("The current time is: <span style=\"font-weight: bold\">" + ut.getTime(currentTimeMillis()) + " " + ut.getCurrentUsTimeZone() + "</span><br>", nick);
        cm.sendToOne("The current runtime is: <span style=\"font-weight: bold\">" + ut.getCalculatedTime(currentTimeMillis(), getMaster().getStartTime()) + "</span><br>", nick);
        cm.sendToOne("<br>", nick);
        var ht = db.getStaffList();
        var adminTable = new ArrayList<String>();
        var staffTable = new ArrayList<String>();
        var admins = new StringBuilder();
        var staff = new StringBuilder();
        ht.keySet().forEach((elemnt) -> {
            if (ht.get(elemnt) >= 9) {
                adminTable.add(elemnt);
            }
            if (ht.get(elemnt) >= 4 && ht.get(elemnt) <= 8) {
                staffTable.add(elemnt);
            }
        });
        if (!adminTable.isEmpty()) {
            admins.append("<span style=\"font-weight: bold\">Admin:</span><br>");
        } else {
            admins.append("<span style=\"font-weight: bold\">Admin:</span> This chat has no administrative users (Please add an administrator in the admin console)...<br><br>");
        }
        var i = 0;
        for (var element : adminTable) {
            admins.append("<span style=\"color: #");
            admins.append(db.getData(element, "color"));
            admins.append("\">");
            admins.append(element);
            admins.append("</span>");
            if (adminTable.size() - 1 != i) {
                admins.append(", ");
            } else {
                admins.append("<br>");
                admins.append("<br>");
            }
            i++;
        }
        if (!staffTable.isEmpty()) {
            staff.append("<span style=\"font-weight: bold\">VIP:</span><br>");
        } else {
            staff.append("<span style=\"font-weight: bold\">VIP:</span> This chat has no VIP's...<br><br>");
        }
        i = 0;
        for (var element : staffTable) {
            staff.append("<span style=\"color: #");
            staff.append(db.getData(element, "color"));
            staff.append("\">");
            staff.append(element);
            staff.append("</span>");
            if (staffTable.size() - 1 != i) {
                staff.append(", ");
            } else {
                staff.append("<br>");
                staff.append("<br>");
            }
            i++;
        }
        cm.sendToOne(admins.toString(), nick);
        cm.sendToOne(staff.toString(), nick);
        cm.sendToOne(line, nick);
    }

    /**
     * L&auml;dt die Konfiguration neu
     *
     * @param nick Der Chatternick
     */
    private void rehash(String nick) {
        var cm = getMaster().getChatManager();
        var u = cm.getUser(nick);
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        if (!cm.isPrivileged("rehash", u.getStatus())) {
            low("rehash", nick);
        } else {
            var txt = db.getCommand("rehash");
            cm.sendSystemToOne(txt, nick);
            try {
                getMaster().getConfig().hash();
                txt = db.getCommand("rehash_success");
                cm.sendSystemToOne(txt, nick);
            } catch (Exception e) {
                txt = db.getCommand("rehash_fail");
                cm.sendSystemToOne(txt, nick);
                cm.postStackTrace(e, nick);
            }
        }
    }

    /**
     * Vergibt ein neues Raumthema
     *
     * @param topic Das Thema
     * @param nick Der Chatternick
     */
    private void topicRoom(String topic, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        var room = u.getRoom();
        String text = null;

        if (!cm.isPrivileged("topic_private", u.getStatus())) {
            low("t", nick);
        } else {
            if (cm.getRoom(room).isStandard() && !cm.isPrivileged("topic_standard", u.getStatus())) {
                low("t", nick);
            } else {
                if (topic == null) {
                    if (db.roomExists(room)) {
                        db.updateRoomData(room, "topic", "");
                    }
                    cm.getRoom(room).setTopic(null);
                    text = db.getCommand("script_topic_del");
                    cm.sendToAllUsersInRoomWithNoSmilies(text, u.getNewName());
                    text = db.getCommand("del_topic");
                } else {
                    text = db.getCommand("script_topic_add");
                    text = text.replace("%topic%", ut.preReplace(topic));
                    cm.sendToAllUsersInRoomWithNoSmilies(text, u.getNewName());
                    if (db.roomExists(room)) {
                        db.updateRoomData(room, "topic", topic);
                    }
                    cm.getRoom(room).setTopic(topic);
                    text = db.getCommand("set_topic");
                    text = text.replace("%topic%", ut.preReplace(topic));
                }
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%room%", ut.preReplace(room));
                cm.sendTimedMsgToAllUsersInRoom(text, room);

            }
        }
    }

    /**
     * &Auml;ndert Den Supervisor Status f&uuml;r Operatoren
     *
     * @param nick Der Chatternick
     */
    private void supervisor(String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        var room = u.getRoom();
        String text = null;
        if (!cm.isPrivileged("supervisor", u.getStatus())) {
            low("sv", nick);
        } else {
            if (u.isSupervisor()) {
                text = db.getCommand("del_supervisor");
                u.setSupervisor(false);
                db.updateNick(nick, "sv", "0");
            } else {
                text = db.getCommand("set_supervisor");
                u.setSupervisor(true);
                db.updateNick(nick, "sv", "1");
            }
            cm.sendSystemToOne(text, nick);
        }
    }

    /**
     * Ruft eigene Hostinfos (wie ip, host oder os) ab
     *
     * @param nick Der Chatternick
     */
    private void myIp(String nick) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("ip", u.getStatus())) {
            low("myip", nick);
            return;
        }
        var text = db.getCommand("my_ip");
        text = text.replace("%host%", ut.preReplace(u.getRealIp().equals("") ? u.getHost() : u.getRealHost() + "@" + u.getHost()));
        text = text.replace("%ip%", ut.preReplace(u.getRealIp().equals("") ? u.getIp() : u.getRealIp() + "@" + u.getIp()));
        text = text.replace("%ip_type%", u.isIPv6() ? "IPv6" : "IPv4");
        text = text.replace("%user_agent%", ut.preReplace(ut.parseHtml(u.getUserAgent())));
        text = text.replace("%os%", ut.preReplace(ut.getOs(ut.parseHtml(u.getUserAgent()), u.getOsName(), u.getOsVersion())));
        text = text.replace("%browser%", ut.preReplace(ut.getBrowser(ut.parseHtml(u.getUserAgent()), ut.parseHtml(u.getBrowser()))));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Sperrt oder entsperrt den Raum
     *
     * @param nick Der Chatternick
     */
    private void lockRoom(String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        var room = u.getRoom();
        String text = null;
        if (!cm.isPrivileged("lock", u.getStatus())) {
            low("l", nick);
        } else {
            if (cm.getRoom(room).isStandard()) {
                text = db.getCommand("no_lock_standard");
                text = text.replace("%room%", ut.preReplace(room));
                cm.sendSystemToOne(text, nick);
            } else {
                if (cm.getRoom(room).isOpen()) {
                    text = db.getCommand("script_room_close");
                    cm.sendToAllUsersInRoom(text, u.getNewName(), false);
                    text = db.getCommand("close_room");
                    cm.getRoom(room).setOpen(false);
                } else {
                    text = db.getCommand("script_room_open");
                    cm.sendToAllUsersInRoom(text, u.getNewName(), false);
                    text = db.getCommand("open_room");
                    cm.getRoom(room).setOpen(true);
                }
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%room%", ut.preReplace(room));
                cm.sendTimedMsgToAllUsersInRoom(text, room);
            }
        }
    }

    /**
     * Schmei&szlig;t einen Chatter aus dem Chat
     *
     * @param to Infos wie Zielnick (Mit Wildcadrs) und den Grund
     * @param nick Der Chatternick
     */
    private void sendMail(String to, String nick) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var mailer = getMaster().getSendMail();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        String content = null;
        var index = to.indexOf(" ");
        if (!cm.isPrivileged("mail", u.getStatus())) {
            low("mail", nick);
        } else if (index != -1) {
            content = to.substring(index + 1, to.length());
            to = to.substring(0, index);
            if (!db.isRegistered(to)) {
                isNotRegistered(to, nick);
            } else {
                try {
                    var text = db.getCommand("send_mail");
                    text = text.replace("%color%", ut.preReplace(db.getData(to, "color")));
                    text = text.replace("%nick%", ut.preReplace(db.getData(to, "nick2")));
                    text = text.replace("%text%", ut.preReplace(content));
                    var mail = db.getData(to, "mail");
                    mailer.sendEmail(content, db.getCommand("send_mail_subject"), mail);
                    cm.sendSystemToOne(text, nick);
                } catch (MessagingException me) {
                    var text = db.getCommand("send_mail_error");
                    text = text.replace("%error%", ut.preReplace(me.getLocalizedMessage()));
                    cm.sendSystemToOne(text, nick);
                }
            }
        } else {
            invalid("mail", nick);
        }
    }

    /**
     * Schmei&szlig;t einen Chatter aus dem Chat
     *
     * @param input Infos wie Zielnick (Mit Wildcadrs) und den Grund
     * @param nick Der Chatternick
     */
    private void hardKick(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        String reason = null;
        var index = input.indexOf(" ");
        if (index != -1) {
            reason = input.substring(index + 1, input.length());
            input = input.substring(0, index);
        } else {
            reason = db.getCommand("hardkick_reason_text");
        }
        if (!cm.isPrivileged("hardkick", u.getStatus())) {
            low("hk", nick);
        } else if (!cm.isOnline(input)) {
            offline(input, nick);
        } else {
            var u1 = cm.getUser(input);
            var text = db.getCommand("hardkick_reason");
            text = text.replace("%color%", u.getColor());
            text = text.replace("%nick%", u.getNewName());
            text = text.replace("%reason%", reason);
            u1.setQuitReason(text);
            u1.setKickReason(reason);
            cm.quit(input);
        }
    }

    /**
     * Hebt einen Ban auf
     *
     * @param input Das Ziel (Unterst&uuml;tzt Wildcards)
     * @param nick Der Chatternick
     */
    private void unBan(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("unban", u.getStatus())) {
            low("uban", nick);
        } else {
            var ubs = db.delBans(nick, input);
            ubs = db.delTimedBans(ubs, nick, input);
            if (!ubs) {
                var err = db.getCommand("uban_error");
                var txt = err.replace("%nick%", ut.preReplace(input));
                cm.sendSystemToOne(txt, nick);
            }
        }
    }

    /**
     * Vergibt einen Bann
     *
     * @param input Das Ziel und der Grund (Mit Wildcards)
     * @param nick Der Chatternick
     */
    protected void ban(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        String reason = null;
        var index = input.indexOf(" ");
        String flags = null;
        var timed = false;
        var ip = false;
        long dur = 0;
        if (input.startsWith("-") && index != -1) {
            flags = input.substring(1, index).toLowerCase();
            input = input.substring(index + 1, input.length());
            timed = flags.contains("t");
            ip = flags.contains("i");
            index = input.indexOf(" ");
        }
        if (timed) {
            if (!cm.isPrivileged("ban_timed", u.getStatus())) {
                low("ban -t", nick);
                return;
            }
            db.eraseExpiredBans();
            String text = null;
            if (index != -1) {
                try {
                    dur = Integer.valueOf(input.substring(0, index));
                } catch (NumberFormatException nfe) {
                    text = db.getCommand("ban_error");
                    cm.sendSystemToOne(text, nick);
                    return;
                }
                if (dur > getMaster().getConfig().getLong("max_ban_duration")) {
                    text = db.getCommand("ban_duration");
                    text = text.replace("%duration%", getMaster().getConfig().getString("max_ban_duration"));
                    cm.sendSystemToOne(text, nick);
                    return;
                }
                if (dur <= 0) {
                    text = db.getCommand("ban_min");
                    cm.sendSystemToOne(text, nick);
                    return;
                }
                input = input.substring(index + 1, input.length());
                index = input.indexOf(" ");
            } else {
                text = db.getCommand("ban_no_nick");
                cm.sendSystemToOne(text, nick);
                return;
            }
            if (index != -1) {
                reason = input.substring(index + 1, input.length());
                input = input.substring(0, index).toLowerCase();
            } else {
                reason = db.getCommand("ban_reason_text");
            }
            text = db.getCommand("ban_reason");
            text = text.replace("%color%", u.getColor());
            text = text.replace("%nick%", u.getNewName());
            text = text.replace("%reason%", reason);
            if (db.isBanned(input) || db.isTimedBanned(input)) {
                text = db.getCommand("banned");
                text = text.replace("%nick%", ut.preReplace(input));
                cm.sendSystemToOne(text, nick);
            } else {
                if (ip) {
                    if (!cm.isOnline(input)) {
                        offline(input, nick);
                        return;
                    } else {
                        var u1 = cm.getUser(input);
                        input = getMaster().getConfig().getString("resolve_ip").equals("1") ? u1.getHost() : u1.getIp();
                    }
                }
                db.delTimedBans(false, nick, input);
                var be = false;
                db.addTimedBan(input.toLowerCase(), reason, nick, dur);
                for (var us : cm.getUsers().keySet()) {
                    Users ue = cm.getUsers().get(us);
                    var un = ue.getName();
                    var ui = ue.getIp();
                    var ri = ue.getRealIp();
                    var rh = ue.getRealHost();
                    var uh = ue.getHost();
                    if (db.isTimedBanned(un) && ue.getStatus() < 4) {
                        cm.getUser(un).setQuitReason(text);
                        cm.getUser(un).setTimedBanned(true);
                        cm.getUser(un).setTimedBanReason(reason);
                        cm.quit(un);
                        be = true;
                    } else if (db.isTimedBanned(ui) && ue.getStatus() < 4) {
                        cm.getUser(un).setQuitReason(text);
                        cm.getUser(un).setTimedBanned(true);
                        cm.getUser(un).setTimedBanReason(reason);
                        cm.quit(un);
                        be = true;
                    } else if (db.isTimedBanned(uh) && ue.getStatus() < 4) {
                        cm.getUser(un).setQuitReason(text);
                        cm.getUser(un).setTimedBanned(true);
                        cm.getUser(un).setTimedBanReason(reason);
                        cm.quit(un);
                        be = true;
                    } else if (db.isTimedBanned(ri) && ue.getStatus() < 4) {
                        cm.getUser(un).setQuitReason(text);
                        cm.getUser(un).setTimedBanned(true);
                        cm.getUser(un).setTimedBanReason(reason);
                        cm.quit(un);
                        be = true;
                    } else if (db.isTimedBanned(rh) && ue.getStatus() < 4) {
                        cm.getUser(un).setQuitReason(text);
                        cm.getUser(un).setTimedBanned(true);
                        cm.getUser(un).setTimedBanReason(reason);
                        cm.quit(un);
                        be = true;
                    }
                }
                if (!be) {
                    text = db.getCommand("ban_text").replace("%reason%", ut.preReplace(text));
                    text = text.replace("%nick%", ut.preReplace(input));
                    cm.sendSystemToOne(text, nick);
                }
            }
        } else {
            if (index != -1) {
                reason = input.substring(index + 1, input.length());
                input = input.substring(0, index).toLowerCase();
            } else {
                reason = db.getCommand("ban_reason_text");
            }
            var text = db.getCommand("ban_reason");
            text = text.replace("%color%", u.getColor());
            text = text.replace("%nick%", u.getNewName());
            text = text.replace("%reason%", reason);
            if (!cm.isPrivileged("ban", u.getStatus())) {
                low("ban", nick);
            } else if (db.isBanned(input) || db.isTimedBanned(input)) {
                text = db.getCommand("banned");
                text = text.replace("%nick%", ut.preReplace(input));
                cm.sendSystemToOne(text, nick);
            } else {
                if (ip) {
                    if (!cm.isOnline(input)) {
                        offline(input, nick);
                        return;
                    } else {
                        var u1 = cm.getUser(input);
                        input = getMaster().getConfig().getString("resolve_ip").equals("1") ? u1.getHost() : u1.getIp();
                    }
                }
                db.delBans(nick, input);
                var be = false;
                db.addBan(input.toLowerCase(), reason, nick);
                for (var us : cm.getUsers().keySet()) {
                    Users ue = cm.getUsers().get(us);
                    var un = ue.getName();
                    var ui = ue.getIp();
                    var ri = ue.getRealIp();
                    var rh = ue.getRealHost();
                    var uh = ue.getHost();
                    if (db.isBanned(un) && ue.getStatus() < 4) {
                        cm.getUser(un).setQuitReason(text);
                        cm.getUser(un).setBanned(true);
                        cm.getUser(un).setBanReason(reason);
                        cm.quit(un);
                        be = true;
                    } else if (db.isBanned(ui) && ue.getStatus() < 4) {
                        cm.getUser(un).setQuitReason(text);
                        cm.getUser(un).setBanned(true);
                        cm.getUser(un).setBanReason(reason);
                        cm.quit(un);
                        be = true;
                    } else if (db.isBanned(uh) && ue.getStatus() < 4) {
                        cm.getUser(un).setQuitReason(text);
                        cm.getUser(un).setBanned(true);
                        cm.getUser(un).setBanReason(reason);
                        cm.quit(un);
                        be = true;
                    } else if (db.isBanned(ri) && ue.getStatus() < 4) {
                        cm.getUser(un).setQuitReason(text);
                        cm.getUser(un).setBanned(true);
                        cm.getUser(un).setBanReason(reason);
                        cm.quit(un);
                        be = true;
                    } else if (db.isBanned(rh) && ue.getStatus() < 4) {
                        cm.getUser(un).setQuitReason(text);
                        cm.getUser(un).setBanned(true);
                        cm.getUser(un).setBanReason(reason);
                        cm.quit(un);
                        be = true;
                    }
                }
                if (!be) {
                    text = db.getCommand("ban_text").replace("%reason%", ut.preReplace(text));
                    text = text.replace("%nick%", ut.preReplace(input));
                    cm.sendSystemToOne(text, nick);
                }
            }
        }
    }

    /**
     * Vergibts Superuser-Rechte
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void setSu(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        var u1 = cm.getUser(input);
        if (!cm.isPrivileged("superuser_set", u.getStatus())) {
            low("su", nick);
        } else if (!cm.isOnline(input)) {
            offline(input, nick);
        } else if (!u.getRoom().equals(u1.getRoom())) {
            notInSameRoom(input, nick);
        } else if (u1.isSuperuser()) {
            isSuperuser(input, nick);
        } else {
            var text = db.getCommand("set_su");
            var room = u.getRoom();
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%su_color%", ut.preReplace(u1.getColor()));
            text = text.replace("%su_nick%", ut.preReplace(u1.getNewName()));
            cm.sendTimedMsgToAllUsersInRoom(text, room);
            u1.setStatus(3);
            text = db.getCommand("script_su_give");
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%status%", "3");
            cm.sendToAllUsersInRoom(text, room);
            db.updateSession(u1.getName(), "status", Integer.toString(u1.getStatus()));
        }
    }

    /**
     * Damit wird ein Nickwechsel ausgelößt
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void nick(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("nick", u.getStatus())) {
            low("nick", nick);
        } else if (!input.matches(Bootstrap.boot.getConfig().getString("allowed_chars"))) {
            var text = getMaster().getConfig().getDb().getCommand("user_chars");
            cm.sendSystemToOne(text, nick);
        } else if (input.length() < getMaster().getConfig().getInt("min_nick_length")) {
            var text = getMaster().getConfig().getDb().getCommand("user_nick_length");
            cm.sendSystemToOne(text, nick);
        } else if (getMaster().getConfig().getDb().isBanned(input)) {
            var text = getMaster().getConfig().getDb().getCommand("user_nick_banned");
            text = text.replace("%reason%", getMaster().getConfig().getDb().getBanReason(input));
            cm.sendSystemToOne(text, nick);
        } else if (getMaster().getConfig().getDb().isTimedBanned(input)) {
            var text = getMaster().getConfig().getDb().getCommand("user_nick_banned");
            text = text.replace("reason", getMaster().getConfig().getDb().getBanReason(input));
            cm.sendSystemToOne(text, nick);
        } else if (nickIsCommand(input)) {
            var text = getMaster().getConfig().getDb().getCommand("user_command");
            cm.sendSystemToOne(text, nick);
        } else if (cm.isOnline(input) && !nick.equalsIgnoreCase(input)) {
            var text = getMaster().getConfig().getDb().getCommand("user_online");
            var u1 = cm.getUser(input);
            text = text.replace("%color%", ut.preReplace(u1.getColor()));
            text = text.replace("%nick%", ut.preReplace(u1.getNewName().equalsIgnoreCase(input) ? u1.getNewName() : u1.getName()));
            cm.sendSystemToOne(text, nick);
        } else if (getMaster().getConfig().getDb().isRegistered(input) && !getMaster().getConfig().getDb().getData(input, "nick").equalsIgnoreCase(nick.toLowerCase())) {
            var text = getMaster().getConfig().getDb().getCommand("user_registered");
            text = text.replace("%color%", ut.preReplace(getMaster().getConfig().getDb().getData(input, "color")));
            text = text.replace("%nick%", ut.preReplace(getMaster().getConfig().getDb().getData(input, "nick2")));
            cm.sendSystemToOne(text, nick);
        } else if (!getMaster().getConfig().getDb().isRegistered(nick) && getMaster().getConfig().getInt("only_registered_users") == 1) {
            var text = getMaster().getConfig().getDb().getCommand("user_not_registered");
            cm.sendSystemToOne(text, nick);
        } else if (getMaster().getConfig().getInt("guest") == 1 && input.toLowerCase().startsWith(getMaster().getConfig().getString("guest_prefix").toLowerCase())) {
            var text = getMaster().getConfig().getDb().getCommand("user_guest");
            cm.sendSystemToOne(text, nick);
        } else {
            var text = getMaster().getConfig().getDb().getCommand("user_nick");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%newnick%", ut.preReplace(input));
            cm.sendTimedMsgToAllUsersInRoom(text, u.getRoom());
            ArrayList<UsersPrivchat> target = cm.getTarget(u.getName());
            for (UsersPrivchat u1 : cm.getUsersPrivchat().values()) {
                if (u1.getName().equalsIgnoreCase(u.getName())) {
                    u1.setNewName(input);
                }

            }
            for (UsersPrivchat u1 : target) {
                cm.sendSystemToUser(text, u.getName(), u1.getTarget());

            }
            text = getMaster().getConfig().getDb().getCommand("script_nick_replace");
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%newnick%", ut.preReplace(input));
            cm.sendToAllUsersInRoomWithNoSmilies(text, nick);
            getMaster().getConfig().getDb().updateSession(u.getName(), "nick", input);
            if (getMaster().getConfig().getDb().isRegistered(nick)) {
                text = getMaster().getConfig().getDb().getCommand("user_nick_friends");
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%newnick%", ut.preReplace(input));
                cm.sendToAllFriendsInChat(text, nick);
            }
            if (getMaster().getConfig().getDb().isRegistered(nick)) {
                getMaster().getConfig().getDb().updateNick(nick, "nick2", input);
                getMaster().getConfig().getDb().updateFriends(u.getNewName(), input);
                for (var us : cm.getUsers().keySet()) {
                    Users u1 = cm.getUsers().get(us);
                    for (var friend : u1.getFriends()) {
                        if (friend.equalsIgnoreCase(u.getNewName())) {
                            u1.getFriends().remove(u.getNewName().toLowerCase());
                            u1.getFriends().add(input.toLowerCase());
                        }
                    }
                }
            }
            u.setNewName(input);
        }
    }

    /**
     * Vergibts Superuser-Rechte
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void setRoomSu(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        var u1 = cm.getUser(input);
        var r = cm.getRoom(u.getRoom());
        var list = r.getSus();
        var db = getMaster().getConfig().getDb();
        var hasSu = list.contains(input.toLowerCase());
        if (!nick.equalsIgnoreCase(r.getOwner()) && u.getStatus() < 4) {
            low("asu", nick);
        } else if (input.equalsIgnoreCase(nick)) {
            isYou("asu", nick);
        } else if (cm.isOnline(input) && !u.getRoom().equals(u1.getRoom())) {
            notInSameRoom(input, nick);
        } else if (cm.isOnline(input) && u1.getStatus() > u.getStatus()) {
            morePower(input, nick);
        } else if (hasSu) {
            isSuperuser(input, nick);
        } else if (!db.isRegistered(input)) {
            isNotRegistered(input, nick);
        } else {
            if (cm.isOnline(input)) {
                var text = db.getCommand("set_room_su");
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%su_color%", ut.preReplace(u1.getColor()));
                text = text.replace("%su_nick%", ut.preReplace(u1.getNewName()));
                cm.sendTimedMsgToAllUsersInRoom(text, r.getName());
                u1.setStatus(3);
                list.add(u1.getNewName().toLowerCase());
                var users = list.toString();
                users = users.replace("[", "");
                users = users.replace("]", "");
                users = users.replace(",", "");
                getMaster().getConfig().getDb().updateRoomData(r.getName(), "su", users.trim().toLowerCase());
                r.setSus(list);
                text = db.getCommand("script_su_give");
                text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
                text = text.replace("%status%", "3");
                cm.sendToAllUsersInRoom(text, r.getName());
                db.updateSession(u1.getName(), "status", Integer.toString(u1.getStatus()));
            } else {
                var text = db.getCommand("set_room_su");
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%su_color%", ut.preReplace(db.getData(input.toLowerCase(), "color")));
                text = text.replace("%su_nick%", ut.preReplace(db.getData(input.toLowerCase(), "nick2")));
                cm.sendSystemToOne(text, u.getName());
                list.add(input.toLowerCase());
                var users = list.toString();
                users = users.replace("[", "");
                users = users.replace("]", "");
                users = users.replace(",", "");
                getMaster().getConfig().getDb().updateRoomData(r.getName(), "su", users.trim().toLowerCase());
                r.setSus(list);
            }
        }
    }

    /**
     * Vergibts Superuser-Rechte
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void removeRoomSu(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        var u1 = cm.getUser(input);
        var r = cm.getRoom(u.getRoom());
        var list = r.getSus();
        var db = getMaster().getConfig().getDb();
        var hasSu = list.contains(input.toLowerCase());
        if (!nick.equalsIgnoreCase(r.getOwner()) && u.getStatus() < 4) {
            low("dsu", nick);
        } else if (input.equalsIgnoreCase(nick)) {
            isYou("dsu", nick);
        } else if (cm.isOnline(input) && !u.getRoom().equals(u1.getRoom())) {
            notInSameRoom(input, nick);
        } else if (cm.isOnline(input) && u1.getStatus() > u.getStatus()) {
            morePower(input, nick);
        } else if (!hasSu) {
            noSuperuser(input, nick);
        } else if (!db.isRegistered(input)) {
            isNotRegistered(input, nick);
        } else {
            if (cm.isOnline(input)) {
                var text = db.getCommand("del_room_su");
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%su_color%", ut.preReplace(u1.getColor()));
                text = text.replace("%su_nick%", ut.preReplace(u1.getNewName()));
                cm.sendTimedMsgToAllUsersInRoom(text, u.getRoom());
                u1.setStatus(1);
                list.remove(u1.getNewName().toLowerCase());
                var users = list.toString();
                users = users.replace("[", "");
                users = users.replace("]", "");
                users = users.replace(",", "");
                getMaster().getConfig().getDb().updateRoomData(r.getName(), "su", users.trim().toLowerCase());
                r.setSus(list);
                text = db.getCommand("script_su_remove");
                text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
                text = text.replace("%status%", "1");
                cm.sendToAllUsersInRoom(text, u1.getName(), false);
                db.updateSession(u1.getName(), "status", Integer.toString(u1.getStatus()));
            } else {
                var text = db.getCommand("del_room_su");
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%su_color%", ut.preReplace(db.getData(input.toLowerCase(), "color")));
                text = text.replace("%su_nick%", ut.preReplace(db.getData(input.toLowerCase(), "nick2")));
                cm.sendSystemToOne(text, u.getName());
                list.remove(input.toLowerCase());
                var users = list.toString();
                users = users.replace("[", "");
                users = users.replace("]", "");
                users = users.replace(",", "");
                getMaster().getConfig().getDb().updateRoomData(r.getName(), "su", users.trim().toLowerCase());
                r.setSus(list);
            }
        }
    }

    private void gagged(String nick, String room) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var text = getMaster().getConfig().getDb().getCommand("gagged");
        text = text.replace("%room%", ut.preReplace(room));
        text = ut.replaceSmilies(text);
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Postet einen Funbefehl im Chat
     *
     * @param input Der Befehl
     * @param user Das Ziel
     * @param nick Der Chatternick
     */
    private void fun(String input, String user, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("fun", u.getStatus())) {
            low(input, nick);
            return;
        }
        if (u.isGagged()) {
            gagged(nick, u.getRoom());
        } else {
            var u1 = cm.getUser(user);
            String text = null;
            if (!cm.isOnline(user)) {
                offline(user, nick);
            } else if (!u.getRoom().equals(u1.getRoom())) {
                notInSameRoom(user, nick);
            } else if (u.isGagged()) {
                gagged(nick, u.getRoom());
            } else {
                text = db.getFun().get(input.toLowerCase())[0];
                text = text.replace("%color%", u.getColor());
                text = text.replace("%nick%", u.getNewName());
                text = text.replace("%fun_color%", u1.getColor());
                text = text.replace("%fun_nick%", u1.getNewName());
                cm.sendTimedMsgToAllUsersInRoom(text, u.getRoom());
            }
        }
    }

    /**
     * Entfernt de Voice-Status
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void removeVoice(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        var u1 = cm.getUser(input);
        if (!cm.isPrivileged("moderator_remove", u.getStatus())) {
            low("rv", nick);
        } else if (!cm.isOnline(input)) {
            offline(input, nick);
        } else if (!u.getRoom().equals(u1.getRoom())) {
            notInSameRoom(input, nick);
        } else if (u1.getStatus() >= 3) {
            notEnoughPower(input, nick);
        } else if (!u1.isVoice()) {
            noVoice(input, nick);
        } else {
            var text = db.getCommand("moderator_remove");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%moderator_color%", ut.preReplace(u1.getColor()));
            text = text.replace("%moderator_nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%room%", ut.preReplace(u.getRoom()));
            cm.sendTimedMsgToAllUsersInRoom(text, u.getRoom());
            u1.setStatus(1);
            text = db.getCommand("script_moderator_remove");
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%status%", "1");
            cm.sendToAllUsersInRoom(text, u1.getName(), false);
            db.updateSession(u1.getName(), "status", Integer.toString(u1.getStatus()));
        }
    }

    /**
     * Vergibt den Voice-Status wenn m&ouml;glich
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void setVoice(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        var u1 = cm.getUser(input);
        if (!cm.isPrivileged("moderator_set", u.getStatus())) {
            low("gv", nick);
        } else if (!cm.isOnline(input)) {
            offline(input, nick);
        } else if (!u.getRoom().equals(u1.getRoom())) {
            notInSameRoom(input, nick);
        } else if (u1.isVoice()) {
            isVoice(input, nick);
        } else {
            var text = db.getCommand("set_moderator");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%moderator_color%", ut.preReplace(u1.getColor()));
            text = text.replace("%moderator_nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%room%", ut.preReplace(u.getRoom()));
            cm.sendTimedMsgToAllUsersInRoom(text, u.getRoom());
            u1.setStatus(2);
            text = db.getCommand("script_moderator_give");
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%status%", "2");
            cm.sendToAllUsersInRoom(text, u1.getName(), false);
            db.updateSession(u1.getName(), "status", Integer.toString(u1.getStatus()));
        }
    }

    /**
     * Entfernt den Superuser-Status
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void removeSu(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        var u1 = cm.getUser(input);
        if (!cm.isPrivileged("superuser_remove", u.getStatus())) {
            low("rsu", nick);
        } else if (!cm.isOnline(input)) {
            offline(input, nick);
        } else if (!u.getRoom().equals(u1.getRoom())) {
            notInSameRoom(input, nick);
        } else if (u1.getStatus() >= 4) {
            notEnoughPower(input, nick);
        } else if (!u1.isSuperuser()) {
            noSuperuser(input, nick);
        } else {
            var text = db.getCommand("del_su");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%su_color%", ut.preReplace(u1.getColor()));
            text = text.replace("%su_nick%", ut.preReplace(u1.getNewName()));
            cm.sendTimedMsgToAllUsersInRoom(text, u.getRoom());
            u1.setStatus(1);
            text = db.getCommand("script_su_remove");
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%status%", "1");
            cm.sendToAllUsersInRoom(text, u1.getName(), false);
            db.updateSession(u1.getName(), "status", Integer.toString(u1.getStatus()));
        }
    }

    /**
     * Fl&uuml;stert zu jemanden
     *
     * @param message Der Text
     * @param nick Der Chatternick
     */
    private void whisper(String message, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        var target = u.getLastWhisperedNick();
        if (target == null) {
            cm.sendSystemToOne(db.getCommand("whisper_error"), nick);
        } else if (!cm.isOnline(target)) {
            offline(target, nick);
        } else {
            whisper(target, message, nick);
        }
    }

    /**
     * Fl&uuml;stert zu jemanden
     *
     * @param target Das Ziel
     * @param message Der Text
     * @param nick Der Chatternick
     */
    private void whisper(String target, String message, String nick) {
        resetIdleTime(nick);
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        message = ut.replaceLinks(message);
        if (!cm.isOnline(target) && !db.isRegistered(nick)) {
            offline(target, nick);
        } else if (!cm.isOnline(target) && !db.isRegistered(target)) {
            offline(target, nick);
        } else if (!cm.isOnline(target)) {
            db.addMessage(nick, target, message);
            var text = db.getCommand("add_message");
            text = text.replace("%color%", ut.preReplace(db.getData(target, "color")));
            text = text.replace("%nick%", ut.preReplace(db.getData(target, "nick2")));
            text = text.replace("%message%", ut.preReplace(message));
            cm.sendSystemToOne(text, nick);
        } else {
            var u = cm.getUser(nick);
            var u1 = cm.getUser(target);
            var text = db.getCommand("whisper_to");
            text = text.replace("%color%", ut.preReplace(u1.getColor()));
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%message%", ut.preReplace(message));
            cm.sendToOne(text, nick);
            if (!u1.getIgnore().contains(u.getName().toLowerCase())) {
                text = db.getCommand("whisper_from");
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%message%", ut.preReplace(message));
                cm.sendToOne(text, target);
            }
            u.setLastWhisperedNick(target.toLowerCase());
        }
    }

    /**
     * Damit wird geschrien
     *
     * @param input Der Text
     * @param nick Der Chatternick
     */
    private void postShout(String input, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("shout", u.getStatus())) {
            low("s", nick);
        } else if (u.isGagged()) {
            gagged(nick, u.getRoom());
        } else {
            var text = db.getCommand("chat_shout");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%content%", ut.preReplace(input.toUpperCase()));
            postMe(text, nick);

        }
    }

    /**
     * Wechselt einen Chatter ein einem neuen abgeschlossenen Raum
     *
     * @param room Der Raum
     * @param nick Der Chatternick
     */
    private void sepaRoom(String room, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("sepa", u.getStatus())) {
            low("sepa", nick);
            return;
        }
        if (db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && u.getStatus() < 4) {
            var text = db.getCommand("locked_room_db");
            text = text.replace("%room%", ut.preReplace(room));
            text = text.replace("%reason%", ut.preReplace(db.getRoomData(room, "lock_reason")));
            cm.sendSystemToOne(text, nick);
        } else if (db.roomExists(room) && !db.getRoomData(room, "standard").equals("0")) {
            var text = db.getCommand("sepa_standard_room");
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendSystemToOne(text, nick);
        } else if (cm.roomExists(room)) {
            var text = db.getCommand("exists_room");
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendSystemToOne(text, nick);
        } else {
            var oldroom = u.getRoom();
            var text = db.getCommand("sepa_room");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendTimedMsgToAllUsersInRoom(text, oldroom);
            text = db.getCommand("sepa");
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendSystemToOne(text, nick);

            /**
             * Dieses Script entfernt den Chatter aus den altem Raum.
             */
            text = db.getCommand("script_change_remove");
            text = text.replace("%nick%", ut.preReplace(u.getName()));
            cm.sendToAllUsersInRoomWithNoSmilies(text, u.getName());
            cm.sendToOne(cm.clearUserlist(), u.getName());

            /**
             * Hier wird der Raum gewechselt
             */
            cm.changeRoom(u.getName(), room, true);
            cm.sendToAllUsersInRoomWithNoSmilies(cm.getUserScriptInfo(u), u.getName());

            /**
             * Hier erh&auml;lt man das Raumthema oder auch nicht!
             */
            var t = cm.getRoom(room).getTopic();
            if (t != null) {
                text = db.getCommand("topic_room");
                text = text.replace("%room%", ut.preReplace(room));
                text = text.replace("%topic%", ut.preReplace(t));
                cm.sendSystemToOne(text, ut.preReplace(u.getName()));
            }
            /**
             * Hier wird die Benutzerliste bereinigt und als Privatnachricht an
             * den Chatter &uuml;bermittelt :)
             */
            text = cm.clearUserlist();
            cm.sendToOneWithNoSmilies(text, u.getName());

            /**
             * F&uuml;gt alle Chatte im Zielraum in die Benutzerliste ein und
             * &uuml;bermittelt diese Daten als Privatenachricht in die
             * Chatterliste ein.
             */
            text = cm.addUsersInUserlist(room);
            cm.sendToOneWithNoSmilies(text, u.getName());

            /**
             * Hier werden die Sitzungsdaten bez&uuml;glich Benutzerstatus in
             * der Datenbank aktualisiert :)
             */
            db.updateSession(u.getName(), "status", Integer.toString(u.getStatus()));
            cm.sendBirtdayScript(u.getName(), u.getRoom());
        }
    }

    /**
     * Damit wird eine Einladung akzeptiert
     *
     * @param nick Der Chatternick
     */
    private void acceptInvite(String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("accept_invite", u.getStatus())) {
            low("a", nick);
            return;
        }
        var room = u.getInviteRoom();
        var oldroom = u.getRoom();
        if (room == null) {
            var text = db.getCommand("not_invited");
            cm.sendSystemToOne(text, nick);
        } else if (!cm.roomExists(room)) {
            var text = db.getCommand("room_empty");
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendSystemToOne(text, nick);
        } else if (oldroom.equals(room)) {
            var text = db.getCommand("same_room");
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendSystemToOne(text, nick);
        } else {

            /**
             * Raumwechselnachricht wird an alle Chatter im Raum
             * &uuml;bermittelt!
             */
            var text = db.getCommand("change_room");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendTimedMsgToAllUsersInRoom(text, oldroom);

            /**
             * Dieses Script entfernt den Chatter aus den altem Raum.
             */
            text = db.getCommand("script_change_remove");
            text = text.replace("%nick%", ut.preReplace(u.getName()));
            cm.sendToAllUsersInRoomWithNoSmilies(text, u.getName());
            cm.sendToOne(cm.clearUserlist(), u.getName());

            text = db.getCommand("join_room");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%room%", ut.preReplace(oldroom));
            cm.sendTimedMsgToAllUsersInRoom(text, room);
            /**
             * Hier wird der Raum gewechselt
             */
            cm.changeRoom(u.getName(), room, false);
            text = db.getCommand("script_join");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getName()));
            text = text.replace("%status%", Integer.toString(u.getStatus()));
            cm.sendToAllUsersInRoomWithNoSmilies(text, u.getName());
            cm.sendToAllUsersInRoomWithNoSmilies(cm.getUserScriptInfo(u), u.getName());
            /**
             * Hier erh&auml;lt man das Raumthema oder auch nicht!
             */
            var t = cm.getRoom(room).getTopic();
            /**
             * Wenn ein Thema f&uuml;r den Raum vorhanden ist, wird mit dieser
             * Abfrage das Raumthema als private Systemnachricht
             * &uuml;bermittelt!
             */
            if (t != null) {
                text = db.getCommand("topic_room");
                text = text.replace("%room%", ut.preReplace(room));
                text = text.replace("%topic%", ut.preReplace(t));
                cm.sendSystemToOne(text, u.getName());
            }

            /**
             * Hier wird die Benutzerliste bereinigt und als Privatnachricht an
             * den Chatter &uuml;bermittelt :)
             */
            text = cm.clearUserlist();
            cm.sendToOneWithNoSmilies(text, u.getName());

            /**
             * F&uuml;gt alle Chatte im Zielraum in die Benutzerliste ein und
             * &uuml;bermittelt diese Daten als Privatenachricht in die
             * Chatterliste ein.
             */
            text = cm.addUsersInUserlist(room);
            cm.sendToOneWithNoSmilies(text, u.getName());

            /**
             * Hier werden die Sitzungsdaten bez&uuml;glich Benutzerstatus in
             * der Datenbank aktualisiert :)
             */
            db.updateSession(u.getName(), "status", Integer.toString(u.getStatus()));
            u.setInviteRoom(null);
        }
    }

    /**
     * Damit wird ein Chatter gebeamt
     *
     * @param input Notwendige Daten wie Zielnick und Zielraum
     * @param nick Der Chatternick
     */
    private void beamRoom(String input, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        resetIdleTime(nick);
        String room = null;
        String selectedNick;
        var index = input.indexOf(" ");
        if (index == -1) {
            invalid("beam", nick);
        } else {
            room = input.substring(index + 1, input.length());
            selectedNick = input.substring(0, index);
            var u1 = cm.getUser(nick);
            if (!cm.isPrivileged("beam", u1.getStatus())) {
                low("beam", nick);
            } else if (!cm.isOnline(selectedNick)) {
                offline(selectedNick, nick);
            } else {
                var u = cm.getUser(selectedNick);
                var oldroom = u.getRoom();
                if (oldroom.equals(room)) {
                    var text = db.getCommand("same_room_beam");
                    text = text.replace("%room%", ut.preReplace(room));
                    text = text.replace("%color%", ut.preReplace(u.getColor()));
                    text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                    cm.sendSystemToOne(text, nick);
                } else {
                    var text = db.getCommand("change_room_beam");
                    text = text.replace("%beamer_color%", ut.preReplace(u1.getColor()));
                    text = text.replace("%beamer_nick%", ut.preReplace(u1.getNewName()));
                    text = text.replace("%color%", ut.preReplace(u.getColor()));
                    text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                    text = text.replace("%room%", ut.preReplace(room));
                    cm.sendTimedMsgToAllUsersInRoom(text, oldroom);
                    if (!u.getRoom().equals(u1.getRoom())) {
                        cm.sendSystemToOne(text, nick);
                    }
                    /**
                     * Dieses Script entfernt den Chatter aus den altem Raum.
                     */
                    text = db.getCommand("script_change_remove");
                    text = text.replace("%nick%", ut.preReplace(u.getName()));
                    cm.sendToAllUsersInRoomWithNoSmilies(text, u.getName());
                    cm.sendToOneWithNoSmilies(cm.clearUserlist(), u.getName());
                    if (cm.roomExists(room)) {
                        text = db.getCommand("join_room_beam");
                        text = text.replace("%beamer_color%", ut.preReplace(u1.getColor()));
                        text = text.replace("%beamer_nick%", ut.preReplace(u1.getNewName()));
                        text = text.replace("%color%", ut.preReplace(u.getColor()));
                        text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                        text = text.replace("%room%", ut.preReplace(oldroom));
                        cm.sendTimedMsgToAllUsersInRoom(text, room);
                        /**
                         * Hier wird der Raum gewechselt
                         */
                        cm.changeRoom(u.getName(), room, false);
                        text = db.getCommand("script_join");
                        text = text.replace("%color%", u.getColor());
                        text = text.replace("%nick%", ut.preReplace(u.getName()));
                        text = text.replace("%status%", Integer.toString(u.getStatus()));
                        cm.sendToAllUsersInRoomWithNoSmilies(text, u.getName());
                    } else {
                        /**
                         * Hier wird der Raum gewechselt
                         */
                        cm.changeRoom(u.getName(), room, false);
                    }
                    cm.sendToAllUsersInRoomWithNoSmilies(cm.getUserScriptInfo(u), u.getName());
                    var t = cm.getRoom(room).getTopic();
                    if (t != null) {
                        text = db.getCommand("topic_room");
                        text = text.replace("%room%", ut.preReplace(room));
                        text = text.replace("%topic%", ut.preReplace(t));
                        cm.sendSystemToOne(text, u.getName());
                    }
                    /**
                     * Hier wird die Benutzerliste bereinigt und als
                     * Privatnachricht an den Chatter &uuml;bermittelt :)
                     */
                    text = cm.clearUserlist();
                    cm.sendToOneWithNoSmilies(text, u.getName());

                    /**
                     * F&uuml;gt alle Chatte im Zielraum in die Benutzerliste
                     * ein und &uuml;bermittelt diese Daten als Privatenachricht
                     * in die Chatterliste ein.
                     */
                    text = cm.addUsersInUserlist(room);
                    cm.sendToOneWithNoSmilies(text, u.getName());

                    /**
                     * Hier werden die Sitzungsdaten bez&uuml;glich
                     * Benutzerstatus in der Datenbank aktualisiert :)
                     */
                    db.updateSession(u.getName(), "status", Integer.toString(u.getStatus()));
                    cm.sendBirtdayScript(u.getName(), u.getRoom());
                }
            }
        }
    }

    /**
     * Damit wird ein Chatter mit Dir eine einem neuen Raum gezogen
     *
     * @param input Zielinformationen wie Raum und Nick
     * @param nick Der Chatternick
     */
    private void catchUser(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        String room = null;
        String selectedNick;
        var index = input.indexOf(" ");
        if (index == -1) {
            room = getMaster().getConfig().getString("default_catch_room");
            selectedNick = input;
        } else {
            room = input.substring(index + 1, input.length());
            selectedNick = input.substring(0, index);
        }

        var u1 = cm.getUser(nick);
        if (!cm.isPrivileged("catch", u1.getStatus())) {
            low("catch", nick);
        } else if (!cm.isOnline(selectedNick)) {
            offline(selectedNick, nick);
        } else if (cm.roomExists(room)) {
            var text = db.getCommand("exists_room");
            text = text.replace("%room%", room);
            cm.sendSystemToOne(text, nick);
        } else {
            var u = cm.getUser(selectedNick);
            var oldroom = u.getRoom();
            if (oldroom.equals(room)) {
                var text = db.getCommand("same_room2");
                text = text.replace("%room%", ut.preReplace(room));
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                cm.sendSystemToOne(text, nick);
            } else {
                var text = db.getCommand("change_room_catch");
                text = text.replace("%catcher_color%", ut.preReplace(u1.getColor()));
                text = text.replace("%catcher_nick%", ut.preReplace(u1.getNewName()));
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%room%", ut.preReplace(room));
                cm.sendTimedMsgToAllUsersInRoom(text, u.getRoom());
                if (!u.getRoom().equals(u1.getRoom())) {
                    cm.sendTimedMsgToAllUsersInRoom(text, u1.getRoom());
                }

                /**
                 * Dieses Script entfernt den Chatter aus den altem Raum.
                 */
                text = db.getCommand("script_change_remove");
                text = text.replace("%nick%", ut.preReplace(u.getName()));
                cm.sendToAllUsersInRoomWithNoSmilies(text, u.getName());
                cm.sendToOne(cm.clearUserlist(), u.getName());

                if (!u.getName().equals(u1.getName())) {
                    /**
                     * Dieses Script entfernt den Chatter aus den altem Raum.
                     */
                    text = db.getCommand("script_change_remove");
                    text = text.replace("%nick%", ut.preReplace(u1.getName()));
                    cm.sendToAllUsersInRoomWithNoSmilies(text, u1.getName());
                    cm.sendToOne(cm.clearUserlist(), u1.getName());

                }
                cm.changeRoom(u.getName(), room, true);
                if (!u.getName().equals(u1.getName())) {
                    cm.changeRoom(u1.getName(), room, true);
                }
                var t = cm.getRoom(room).getTopic();
                if (t != null) {
                    text = db.getCommand("topic_room");
                    text = text.replace("%room%", ut.preReplace(room));
                    text = text.replace("%topic%", ut.preReplace(t));
                    cm.sendSystemToOne(text, u.getName());
                    if (!u.getName().equals(u1.getName())) {
                        cm.sendSystemToOne(text, u1.getName());
                    }
                }
                /**
                 * Hier wird die Benutzerliste bereinigt und als Privatnachricht
                 * an den Chatter &uuml;bermittelt :)
                 */
                text = cm.clearUserlist();
                cm.sendToOneWithNoSmilies(text, u.getName());
                if (!u.getName().equals(u1.getName())) {
                    cm.sendToOneWithNoSmilies(text, u1.getName());
                }
                /**
                 * F&uuml;gt alle Chatte im Zielraum in die Benutzerliste ein
                 * und &uuml;bermittelt diese Daten als Privatenachricht in die
                 * Chatterliste ein.
                 */
                text = cm.addUsersInUserlist(room);
                cm.sendToOneWithNoSmilies(text, u.getName());
                if (!u.getName().equals(u1.getName())) {
                    cm.sendToOneWithNoSmilies(text, u1.getName());
                }
                /**
                 * Hier werden die Sitzungsdaten bez&uuml;glich Benutzerstatus
                 * in der Datenbank aktualisiert :)
                 */
                db.updateSession(u.getName(), "status", Integer.toString(u.getStatus()));
                if (!u.getName().equals(u1.getName())) {
                    db.updateSession(u1.getName(), "status", Integer.toString(u1.getStatus()));
                }
            }
        }
    }

    /**
     * Hier ist die Raumwechselfunktion
     *
     * @param room Der Zielraum
     * @param nick Der Chatternick
     */
    private void changeRoom(String room, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();

        /**
         * Die Ruhezeit wird intern zur&uuml;ckgesetzt!
         */
        resetIdleTime(nick);

        /*
         * Es werden die Chatterdaten aus der Users-Klasse geladen!
         */
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("change_room", u.getStatus())) {
            low("j", nick);
            return;
        }

        /**
         * Ermittle den Raum von wo der Chatter gerade kommt und trage als
         * String f&uuml;r den alten Raum ein!
         */
        var oldroom = u.getRoom();

        /**
         * Abfragen ob der Raum betreten werden kann
         */
        if (db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && u.getStatus() < 4) {

            /**
             * Der Zielraum wurde von einem Administrator gesperrt und kann nur
             * von Staff-Mitgliedern betreten werden!
             */
            var text = db.getCommand("locked_room_db");
            text = text.replace("%room%", ut.preReplace(room));
            text = text.replace("%reason%", ut.preReplace(ut.preReplace(db.getRoomData(room, "lock_reason"))));
            cm.sendSystemToOne(text, nick);
        } else if (cm.roomExists(room) && !cm.getRoom(room).isOpen()) {

            /**
             * Der Zielraum ist abgeschlossen!
             */
            var text = db.getCommand("locked_room");
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendSystemToOne(text, nick);
        } else if (oldroom.equals(room)) {

            /**
             * Der Chatter befindet sich bereits im Raum
             */
            var text = db.getCommand("same_room");
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendSystemToOne(text, nick);
        } else {

            /**
             * Raumwechselnachricht wird an alle Chatter im Raum
             * &uuml;bermittelt!
             */
            var text = db.getCommand("change_room");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendTimedMsgToAllUsersInRoom(text, oldroom);

            /**
             * Dieses Script entfernt den Chatter aus den altem Raum.
             */
            text = db.getCommand("script_change_remove");
            text = text.replace("%nick%", ut.preReplace(u.getName()));
            cm.sendToAllUsersInRoomWithNoSmilies(text, u.getName());
            cm.sendToOne(cm.clearUserlist(), u.getName());

            /**
             * Wenn der Raum vorhanden ist, soll mit dieser Abfrage dann anl
             * alle im Zielraum eine Nachricht &uuml;bermittelt werden dass der
             * Chatter den Raum betritt!
             */
            if (cm.roomExists(room)) {
                text = db.getCommand("join_room");
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%room%", ut.preReplace(oldroom));
                cm.sendTimedMsgToAllUsersInRoom(text, room);
                /**
                 * Hier wird der Raum gewechselt
                 */
                cm.changeRoom(u.getName(), room, false);
                text = db.getCommand("script_join");
                text = text.replace("%color%", u.getColor());
                text = text.replace("%nick%", ut.preReplace(u.getName()));
                text = text.replace("%status%", Integer.toString(u.getStatus()));
                cm.sendToAllUsersInRoomWithNoSmilies(text, u.getName());
            } else {
                /**
                 * Hier wird der Raum gewechselt
                 */
                cm.changeRoom(u.getName(), room, false);
            }
            cm.sendToAllUsersInRoomWithNoSmilies(cm.getUserScriptInfo(u), u.getName());
            /**
             * Hier erh&auml;lt man das Raumthema oder auch nicht!
             */
            var t = cm.getRoom(room).getTopic();

            /**
             * Wenn ein Thema f&uuml;r den Raum vorhanden ist, wird mit dieser
             * Abfrage das Raumthema als private Systemnachricht
             * &uuml;bermittelt!
             */
            if (t != null) {
                text = db.getCommand("topic_room");
                text = text.replace("%room%", ut.preReplace(room));
                text = text.replace("%topic%", ut.preReplace(t));
                cm.sendSystemToOne(text, u.getName());
            }
            /**
             * Hier wird die Benutzerliste bereinigt und als Privatnachricht an
             * den Chatter &uuml;bermittelt :)
             */
            text = cm.clearUserlist();
            cm.sendToOneWithNoSmilies(text, u.getName());

            /**
             * F&uuml;gt alle Chatte im Zielraum in die Benutzerliste ein und
             * &uuml;bermittelt diese Daten als Privatenachricht in die
             * Chatterliste ein.
             */
            text = cm.addUsersInUserlist(room);
            cm.sendToOneWithNoSmilies(text, u.getName());

            /**
             * Hier werden die Sitzungsdaten bez&uuml;glich Benutzerstatus in
             * der Datenbank aktualisiert :)
             */
            db.updateSession(u.getName(), "status", Integer.toString(u.getStatus()));
            cm.sendBirtdayScript(u.getName(), u.getRoom());
        }
    }

    /**
     * Diese Methode ist daf&uuml;r gedacht einen Kick-Befehl f&uuml;r Superuser
     * in andere R&auml;ume zur Verf&uuml;gung zu stellen
     *
     * @param input Das Ziel und der Raum wohin derjenige gekickt wird
     * @param nick Der Chatternick
     */
    private void kick(String input, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        resetIdleTime(nick);
        var index = input.indexOf(" ");
        String room = null;
        if (index != -1) {
            room = input.substring(index + 1, input.length());
            input = input.substring(0, index);
        } else {
            room = getMaster().getConfig().getString("kick_room");
        }
        var u = cm.getUser(nick);
        var oldroom = u.getRoom();
        var u1 = cm.getUser(input);
        if (!cm.isPrivileged("kick", u.getStatus())) {
            low("k", nick);
        } else if (!cm.isOnline(input)) {
            offline(input, nick);
        } else if (!u.getRoom().equals(u1.getRoom())) {
            notInSameRoom(input, nick);
        } else if (db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && u.getStatus() < 4) {
            var text = db.getCommand("locked_room_db_kick");
            text = text.replace("%room%", ut.preReplace(room));
            text = text.replace("%color%", ut.preReplace(u1.getColor()));
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%reason%", ut.preReplace(db.getRoomData(room, "lock_reason")));
            cm.sendSystemToOne(text, nick);
        } else if (cm.roomExists(room) && !cm.getRoom(room).isOpen()) {
            var text = db.getCommand("locked_room");
            text = text.replace("%room%", ut.preReplace(room));
            cm.sendSystemToOne(text, nick);
        } else if (u1.getStatus() > u.getStatus()) {
            morePower(input, nick);
        } else {
            u1.setKicked(true);
            var text = db.getCommand("kick_room");
            text = text.replace("%kick_color%", ut.preReplace(u1.getColor()));
            text = text.replace("%kick_nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%kick_room%", ut.preReplace(room));
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%room%", ut.preReplace(oldroom));
            cm.sendTimedMsgToAllUsersInRoom(text, oldroom);
            /**
             * Dieses Script entfernt den Chatter aus den altem Raum.
             */
            text = db.getCommand("script_change_remove");
            text = text.replace("%nick%", u1.getNewName());
            cm.sendToAllUsersInRoomWithNoSmilies(text, u1.getName());
            cm.sendToOneWithNoSmilies(cm.clearUserlist(), u1.getName());
            if (cm.roomExists(room)) {
                text = db.getCommand("land_room");
                text = text.replace("%kick_color%", ut.preReplace(u1.getColor()));
                text = text.replace("%kick_nick%", ut.preReplace(u1.getNewName()));
                text = text.replace("%old_room%", ut.preReplace(oldroom));
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%room%", ut.preReplace(room));
                cm.sendTimedMsgToAllUsersInRoom(text, room);

                /**
                 * Hier wird der Raum gewechselt
                 */
                cm.changeRoom(u1.getName(), room, false);
                text = db.getCommand("script_join");
                text = text.replace("%color%", u1.getColor());
                text = text.replace("%nick%", ut.preReplace(u1.getName()));
                text = text.replace("%status%", Integer.toString(u1.getStatus()));
                cm.sendToAllUsersInRoomWithNoSmilies(text, u1.getName());
            } else {
                /**
                 * Hier wird der Raum gewechselt
                 */
                cm.changeRoom(u1.getName(), room, false);
            }
            cm.sendToAllUsersInRoomWithNoSmilies(cm.getUserScriptInfo(u1), u1.getName());
            var t = cm.getRoom(room).getTopic();
            if (t != null) {
                text = db.getCommand("topic_room");
                text = text.replace("%room%", ut.preReplace(room));
                text = text.replace("%topic%", ut.preReplace(t));
                cm.sendSystemToOne(text, u1.getName());
            }
            /**
             * Hier wird die Benutzerliste bereinigt und als Privatnachricht an
             * den Chatter &uuml;bermittelt :)
             */
            text = cm.clearUserlist();
            cm.sendToOneWithNoSmilies(text, u1.getName());

            /**
             * F&uuml;gt alle Chatte im Zielraum in die Benutzerliste ein und
             * &uuml;bermittelt diese Daten als Privatenachricht in die
             * Chatterliste ein.
             */
            text = cm.addUsersInUserlist(room);
            cm.sendToOneWithNoSmilies(text, u1.getName());

            /**
             * Hier werden die Sitzungsdaten bez&uuml;glich Benutzerstatus in
             * der Datenbank aktualisiert :)
             */
            db.updateSession(u1.getName(), "status", Integer.toString(u1.getStatus()));
            u1.setKicked(false);
            cm.sendBirtdayScript(u1.getName(), u1.getRoom());
        }
    }

    /**
     * Damit wird ein Nick gegnebelt oder wieder entknebelt
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void gag(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        var u1 = cm.getUser(input);
        var room = u.getRoom();
        if (!cm.isPrivileged("gag", u.getStatus())) {
            low("gag", nick);
        } else if (!cm.isOnline(input)) {
            offline(input, nick);
        } else if (!u.getRoom().equals(u1.getRoom())) {
            notInSameRoom(input, nick);
        } else if (u1.getStatus() > u.getStatus()) {
            morePower(input, nick);
        } else if (!u1.isGagged()) {
            var text = db.getCommand("user_gag");
            text = text.replace("%gag_color%", ut.preReplace(u1.getColor()));
            text = text.replace("%gag_nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            cm.sendTimedMsgToAllUsersInRoom(text, room);
            text = db.getCommand("script_gag_add");
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            cm.sendToAllUsersInRoomWithNoSmilies(text, u1.getName());
            u1.setGagged(true);
            db.updateSession(u1.getName(), "gag", "1");
        } else {
            u1.setGagged(false);
            db.updateSession(u1.getName(), "gag", "0");
            var text = db.getCommand("user_ungag");
            text = text.replace("%gag_color%", ut.preReplace(u1.getColor()));
            text = text.replace("%gag_nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            cm.sendTimedMsgToAllUsersInRoom(text, room);
            text = db.getCommand("script_gag_remove");
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            cm.sendToAllUsersInRoomWithNoSmilies(text, u1.getName());
        }
    }

    /**
     * Damit w&uuml;rfelt man!
     *
     * @param input Die Anzahl der Augen
     * @param nick Der Chatternick
     */
    private void dice(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        var eyes = 6;
        var number = 0;
        if (!cm.isPrivileged("dice", u.getStatus())) {
            low("dice", nick);
            return;
        }
        if (u.isGagged()) {
            gagged(nick, u.getRoom());
        } else {
            String text = null;
            try {
                eyes = Integer.valueOf(input);
            } catch (NumberFormatException nfe) {
                text = db.getCommand("dice_error");
                cm.sendSystemToOne(text, nick);
                return;
            }
            if (eyes > getMaster().getConfig().getLong("dice_max")) {
                text = db.getCommand("dice_max");
                text = text.replace("%eyes%", ut.preReplace(getMaster().getConfig().getString("dice_max")));
                cm.sendSystemToOne(text, nick);
            } else if (eyes <= 1) {
                text = db.getCommand("dice_min");
                cm.sendSystemToOne(text, nick);
            } else {
                number = round(ut.getRnd().nextFloat() * eyes);
                text = db.getCommand("dice");
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%eyes%", ut.preReplace(Integer.toString(eyes)));
                text = text.replace("%number%", ut.preReplace(Integer.toString(number)));
                cm.sendTimedMsgToAllUsersInRoom(text, u.getRoom());
            }
        }
    }

    /**
     * Damit wird ein Chatter eingeladen
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void invite(String input, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        var u1 = cm.getUser(input);
        if (!cm.isPrivileged("invite", u.getStatus())) {
            low("i", nick);
        } else if (!cm.isOnline(input)) {
            offline(input, nick);
        } else if (input.toLowerCase().equals(nick.toLowerCase())) {
            cm.sendSystemToOne(db.getCommand("user_invite_self"), nick);
        } else if (u1.getRoom().equals(u.getRoom())) {
            var text = db.getCommand("same_room2");
            text = text.replace("%color%", ut.preReplace(u1.getColor()));
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%room%", ut.preReplace(u.getRoom()));
            cm.sendSystemToOne(text, nick);
        } else {
            var text = db.getCommand("user_invite_source");
            text = text.replace("%color%", ut.preReplace(u1.getColor()));
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%room%", ut.preReplace(u.getRoom()));
            cm.sendSystemToOne(text, nick);
            text = db.getCommand("user_invite_target");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%room%", ut.preReplace(u.getRoom()));
            cm.sendSystemToOne(text, u1.getName());
            u1.setInviteRoom(u.getRoom());
        }
    }

    /**
     * Damit wird jemand Ignoriert oder die Ignorierung wieder aufgehoben
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void ig(String input, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("ignore", u.getStatus())) {
            low("ig", nick);
            return;
        }
        if (!cm.isOnline(input)) {
            offline(input, nick);
        } else if (input.toLowerCase().equals(nick.toLowerCase())) {
            cm.sendSystemToOne(db.getCommand("user_ig_self"), nick);
        } else if (!u.getIgnore().contains(input.toLowerCase())) {
            var u1 = cm.getUser(input);
            var text = db.getCommand("user_ig");
            text = text.replace("%color%", ut.preReplace(u1.getColor()));
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            cm.sendSystemToOne(text, nick);
            text = db.getCommand("user_ig_target");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            cm.sendSystemToOne(text, u1.getName());
            u.getIgnore().add(input.toLowerCase());
        } else {
            var u1 = cm.getUser(input);
            var text = db.getCommand("user_unig");
            text = text.replace("%color%", ut.preReplace(u1.getColor()));
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            cm.sendSystemToOne(text, nick);
            text = db.getCommand("user_unig_target");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            cm.sendSystemToOne(text, u1.getName());
            u.getIgnore().remove(u.getIgnore().indexOf(input.toLowerCase()));
        }
    }

    /**
     * Damit wird Die Idlezeit wieder zur&uuml;ckgesetzt
     *
     * @param nick Der Chatternick
     */
    private void resetIdleTime(String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        u.setIdleTime(currentTimeMillis());
        if (u.isAway()) {
            var reason = u.getAwayReason();
            reason = ut.replaceLinks(reason);
            var text = db.getCommand("away_end");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%reason%", ut.preReplace(reason));
            cm.sendTimedMsgToAllUsersInRoom(text, u.getRoom());
            text = db.getCommand("script_away_remove");
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%away_reason%", "");
            text = text.replace("%away_status%", "0");
            cm.sendToAllUsersInRoomWithNoSmilies(text, u.getName());
            u.setAway(false);
            db.updateSession(nick, "away_status", "0");
        }
    }

    /**
     * Damit meldet mas Sich ab
     *
     * @param input Der Grund
     * @param nick Der Chatternick
     */
    private void away(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("away", u.getStatus())) {
            low("away", nick);
            return;
        }
        if (u.isGagged()) {
            gagged(nick, u.getRoom());
        } else {
            var text = db.getCommand("away_start");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            u.setAwayReason(input);
            input = ut.replaceLinks(input);
            text = text.replace("%reason%", ut.preReplace(input));
            cm.sendTimedMsgToAllUsersInRoom(text, u.getRoom());
            u.setAway(true);
            input = u.getAwayReason();
            text = db.getCommand("script_away_add");
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%away_reason%", ut.preReplace(input));
            text = text.replace("%away_status%", "1");
            cm.sendToAllUsersInRoomWithNoSmilies(text, u.getName());
            db.updateSession(nick, "away_reason", input);
            db.updateSession(nick, "away_status", "1");
        }
    }

    /**
     * Damit werden Befehle geparst
     *
     * @param input Der Befehl mitsamt Inhalt
     * @param nick Der Chatternick
     */
    private void parseCommands(String input, String nick) {
        var index = input.indexOf(" ");
        if (index != -1) {
            command(input.substring(0, index), input.substring(index + 1, input.length()), nick);
        } else {
            command(input, nick);
        }
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
     * Damit werden Befehle geparst
     *
     * @param command Der Befehl
     * @param text Der Inhalt
     * @param nick Der Chatternick
     */
    private void command(String command, String text, String nick) {
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        if (command.equalsIgnoreCase("nick")) {
            nick(text, nick);
        } else if (command.equalsIgnoreCase("moderator")) {
            moderator(text, nick);
        } else if (command.equalsIgnoreCase("mail")) {
            sendMail(text, nick);
        } else if (command.equalsIgnoreCase("asu")) {
            setRoomSu(text, nick);
        } else if (command.equalsIgnoreCase("dsu")) {
            removeRoomSu(text, nick);
        } else if (command.equalsIgnoreCase("dice")) {
            dice(text, nick);
        } else if (command.equalsIgnoreCase("fcol")) {
            fade(text, nick);
        } else if (command.equalsIgnoreCase("rf")) {
            delFriend(text, nick);
        } else if (command.equalsIgnoreCase("af")) {
            addFriend(text, nick);
        } else if (command.equalsIgnoreCase("wc")) {
            printUserList(text, nick);
        } else if (command.equalsIgnoreCase("me")) {
            postMe(text, nick);
        } else if (command.equalsIgnoreCase("s")) {
            postShout(text, nick);
        } else if (command.equalsIgnoreCase("j")) {
            changeRoom(text, nick);
        } else if (command.equalsIgnoreCase("dp")) {
            deletePicture(text, nick);
        } else if (command.equalsIgnoreCase("sepa")) {
            sepaRoom(text, nick);
        } else if (command.equalsIgnoreCase("q")) {
            quit(text, nick);
        } else if (command.equalsIgnoreCase("hk")) {
            hardKick(text, nick);
        } else if (command.equalsIgnoreCase("t")) {
            topicRoom(text, nick);
        } else if (command.equalsIgnoreCase("col")) {
            changeColor(text, nick);
        } else if (command.equalsIgnoreCase("away")) {
            away(text, nick);
        } else if (command.equalsIgnoreCase("su")) {
            setSu(text, nick);
        } else if (command.equalsIgnoreCase("sys")) {
            systemMessage(text, nick);
        } else if (command.equalsIgnoreCase("rsu")) {
            removeSu(text, nick);
        } else if (command.equalsIgnoreCase("gv")) {
            setVoice(text, nick);
        } else if (command.equalsIgnoreCase("rv")) {
            removeVoice(text, nick);
        } else if (command.equalsIgnoreCase("k")) {
            kick(text, nick);
        } else if (command.equalsIgnoreCase("gag")) {
            gag(text, nick);
        } else if (command.equalsIgnoreCase("ban")) {
            ban(text, nick);
        } else if (command.equalsIgnoreCase("uban")) {
            unBan(text, nick);
        } else if (command.equalsIgnoreCase("w")) {
            whois(text, nick);
        } else if (command.equalsIgnoreCase("chgrights")) {
            chgrights(text, nick);
        } else if (command.equalsIgnoreCase("ig")) {
            ig(text, nick);
        } else if (command.equalsIgnoreCase("query")) {
            query(text, nick);
        } else if (command.equalsIgnoreCase("i")) {
            invite(text, nick);
        } else if (command.equalsIgnoreCase("beam")) {
            beamRoom(text, nick);
        } else if (command.equalsIgnoreCase("catch")) {
            catchUser(text, nick);
        } else if (command.equalsIgnoreCase("")) {
            whisper(text, nick);
        } else if (command.equalsIgnoreCase("rehash")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("q")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("l")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("t")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("mod")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("sv")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("shutdown")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("wipe")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("sbans")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("list")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("info")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("myip")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("a")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("sf")) {
            invalid2(command, nick);
        } else if (command.equalsIgnoreCase("c")) {
            invalid2(command, nick);
        } else if (db.getFun().get(command.toLowerCase()) != null) {
            fun(command, text, nick);
        } else {
            whisper(command, text, nick);
        }
    }

    /**
     * Damit werden Befehle ohne weiteren Informationen geparst
     *
     * @param command Der Befehl
     * @param nick Der Chatternick
     */
    private void command(String command, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        if (command.equalsIgnoreCase("dice")) {
            dice("6", nick);
        } else if (command.equalsIgnoreCase("sf")) {
            listFriends(nick);
        } else if (command.equalsIgnoreCase("c")) {
            clearStream(nick);
        } else if (command.equalsIgnoreCase("w")) {
            printUserList(u.getRoom(), nick);
        } else if (command.equalsIgnoreCase("wc")) {
            printUserList(nick);
        } else if (command.equalsIgnoreCase("rehash")) {
            rehash(nick);
        } else if (command.equalsIgnoreCase("a")) {
            acceptInvite(nick);
        } else if (command.equalsIgnoreCase("q")) {
            quit(null, nick);
        } else if (command.equalsIgnoreCase("l")) {
            lockRoom(nick);
        } else if (command.equalsIgnoreCase("t")) {
            topicRoom(null, nick);
        } else if (command.equalsIgnoreCase("sv")) {
            supervisor(nick);
        } else if (command.equalsIgnoreCase("shutdown")) {
            shutdown(nick);
        } else if (command.equalsIgnoreCase("wipe")) {
            wipe(nick);
        } else if (command.equalsIgnoreCase("sbans")) {
            listBans(nick);
        } else if (command.equalsIgnoreCase("list")) {
            list(nick);
        } else if (command.equalsIgnoreCase("info")) {
            info(nick);
        } else if (command.equalsIgnoreCase("myip")) {
            myIp(nick);
        } else if (command.equalsIgnoreCase("beam")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("i")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("me")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("s")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("j")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("sepa")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("query")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("hk")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("moderator")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("col")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("away")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("su")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("asu")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("dsu")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("sys")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("rsu")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("rv")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("dp")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("gv")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("k")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("gag")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("ban")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("catch")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("uban")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("about")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("chgrights")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("ig")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("mod")) {
            moderate(nick);
        } else if (command.equalsIgnoreCase("af")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("rf")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("nick")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("mail")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("fcol")) {
            invalid(command, nick);
        } else if (command.equalsIgnoreCase("")) {
            invalid(command, nick);
        } else if (db.getFun().get(command.toLowerCase()) != null) {
            invalid(command, nick);
        } else {
            unknown(command, nick);
        }
    }

    /**
     * Zeigt die aktuelle Userliste ein
     *
     * @param nick Der Chatternick
     */
    private void printUserList(String nick) {
        var cm = getMaster().getChatManager();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("who", u.getStatus())) {
            low("wc", nick);
            return;
        }
        var sid = u.getConnectionId();
        var ul = cm.getUserList(sid, u.getSkin());
        cm.sendToOne(ul, nick);
    }

    /**
     * Zeigt die aktuelle Userliste ein
     *
     * @param nick Der Chatternick
     */
    private void query(String target, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (!cm.isOnline(target)) {
            offline(target, nick);
        } else if (!cm.isPrivileged("query", u.getStatus())) {
            low("query", nick);
        } else {
            var u1 = cm.getUser(target);
            var text = db.getCommand("query");
            text = text.replace("%color%", ut.preReplace(u1.getColor()));
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            text = text.replace("%oldnick%", ut.preReplace(u1.getName()));
            text = text.replace("%sid%", ut.preReplace(getMaster().getChatServices().generateSid()));
            text = text.replace("%owner%", ut.preReplace(""));
            text = text.replace("%target%", ut.preReplace(u.getName()));
            text = ut.replaceFilePaths(text);
            text = ut.replacePaths(text);
            cm.sendSystemToOne(text, nick);
            text = db.getCommand("query_target");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%oldnick%", ut.preReplace(u.getName()));
            text = text.replace("%sid%", ut.preReplace(u1.getConnectionId()));
            text = text.replace("%owner%", ut.preReplace(""));
            text = text.replace("%target%", ut.preReplace(u1.getName()));
            text = ut.replaceFilePaths(text);
            text = ut.replacePaths(text);
            cm.sendSystemToOne(text, target);
        }
    }

    /**
     * Ruft das kleine Benutzerprofil ab
     *
     * @param target Das Ziel
     * @param nick Der Chatternick
     */
    private void whois(String target, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("whois", u.getStatus())) {
            low("w", nick);
            return;
        }
        var sid = u.getConnectionId();
        var who = db.whois(sid, target);
        var owner = cm.getRoom(u.getRoom()).getOwner();
        who = who.replace("%owner%", owner);
        cm.sendToOne(who, nick);
    }

    /**
     * Postet eine Liste der User im Zielraum ab
     *
     * @param room Der Raum
     * @param nick Der Chatternick
     */
    private void printUserList(String room, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("who_room", u.getStatus())) {
            low(u.getRoom().equals(room) ? "w" : "wc", nick);
            return;
        }
        if (cm.roomExists(room)) {
            var sid = u.getConnectionId();
            var ul = cm.getUserList(nick, room, sid, u.getSkin());
            cm.sendToOne(ul, nick);
        } else {
            var cmd = db.getCommand("room_empty");
            var txt = cmd.replace("%room%", room);
            cm.sendSystemToOne(txt, nick);
        }
    }

    /**
     * Das Ziel ist offline
     *
     * @param target Das Ziel
     * @param nick Der Chatternick
     */
    private void offline(String target, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        String text = null;
        if (db.isRegistered(target.toLowerCase())) {
            text = db.getCommand("offline_reg");
            text = text.replace("%nick%", ut.preReplace(db.getData(target, "nick2")));
            text = text.replace("%color%", ut.preReplace(db.getData(target, "color")));
        } else {
            text = db.getCommand("offline");
            text = text.replace("%nick%", ut.preReplace(target));
        }
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Zeigt Dem Chatter das ein Befehl nicht existiert
     *
     * @param command Der Befehl
     * @param nick Der Chatternick
     */
    private void unknown(String command, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var text = db.getCommand("unknown_command");
        text = text.replace("%command%", ut.preReplace(command));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Sendet eine Systemnachricht an alle
     *
     * @param input Der Text
     * @param nick Der Chatternick
     */
    private void systemMessage(String input, String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        input = ut.replaceLinks(input);
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("system", u.getStatus())) {
            low("sys", nick);
        } else {
            var line = line();
            cm.sendToAllUsersInChat(line);
            var text = db.getCommand("system_message");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            text = text.replace("%text%", ut.preReplace(input));
            cm.sendToAllUsersInChat(text);
            cm.sendToAllUsersInChat(line);
        }
    }

    /**
     * Leert den Chatsream
     *
     * @param nick Der Chatternick
     */
    private void clearStream(String nick) {
        var cm = getMaster().getChatManager();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("clear", u.getStatus())) {
            low("c", nick);
            return;
        }
        var text = getMaster().getConfig().getDb().getCommand("clear");
        cm.sendToOne(text, nick);
        text = getMaster().getConfig().getDb().getCommand("refresh");
        cm.sendSystemToOne(text, nick);
        gc();
    }

    /**
     * Zeichnet eine einfache Linie
     */
    private String line() {
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        return db.getCommand("draw_line");
    }

    /**
     * Setzt den Moderatorstatus f&uuml; einen Nick
     *
     * @param nick Der Chatternick
     */
    private void moderator(String target, String nick) {
        resetIdleTime(nick);
        ChatManager cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var u = cm.getUser(nick);
        Database db = conf.getDb();
        String text = null;
        if (!cm.isPrivileged("moderator", u.getStatus())) {
            low("moderator", nick);
        } else if (!db.isRegistered(target)) {
            isNotRegistered(target, nick);
        } else {
            var color = db.getData(target, "color");
            var name = db.getData(target, "nick2");
            var moderator = db.getData(target, "moderator").equals("1");
            if (cm.isOnline(target)) {
                if (!moderator) {
                    text = db.getCommand("moderator_add_target");
                    db.updateNick(target, "moderator", "1");
                } else {
                    text = db.getCommand("moderator_del_target");
                    db.updateNick(target, "moderator", "0");
                }
                var u1 = cm.getUser(target);
                text = text.replace("%moderator_color%", color);
                text = text.replace("%moderator_nick%", name);
                text = text.replace("%color%", u.getColor());
                text = text.replace("%nick%", u.getNewName());
                cm.sendSystemToOne(text, u1.getNewName());
                if (!moderator) {
                    text = db.getCommand("moderator_add");
                } else {
                    text = db.getCommand("moderator_del");
                }
                text = text.replace("%moderator_color%", color);
                text = text.replace("%moderator_nick%", name);
                text = text.replace("%color%", u.getColor());
                text = text.replace("%nick%", u.getNewName());
                cm.sendSystemToOne(text, u.getNewName());

            } else {
                if (!moderator) {
                    text = db.getCommand("moderator_add");
                    db.updateNick(target, "moderator", "1");
                } else {
                    text = db.getCommand("moderator_del");
                    db.updateNick(target, "moderator", "0");
                }
                text = text.replace("%moderator_color%", color);
                text = text.replace("%moderator_nick%", name);
                text = text.replace("%color%", u.getColor());
                text = text.replace("%nick%", u.getNewName());
                cm.sendSystemToOne(text, u.getNewName());
            }
        }
    }

    /**
     * Moderiert einen Raum oder hebt die Moderierung wieder auf
     *
     * @param nick Der Chatternick
     */
    private void moderate(String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        String text = null;
        var color = u.getColor();
        var name = u.getNewName();
        var room = u.getRoom();
        var r = cm.getRoom(room);
        if (!cm.isPrivileged("moderate_room", u.getStatus())) {
            low("mod", nick);
        } else if (r.isModerated()) {
            text = db.getCommand("moderate_room_remove");
            text = text.replace("%room%", ut.preReplace(room));
            text = text.replace("%color%", ut.preReplace(color));
            text = text.replace("%nick%", ut.preReplace(name));
            cm.sendSystemToAllUsersInRoom(text, room);
            r.setModerated(false);
        } else {
            text = db.getCommand("moderate_room_add");
            text = text.replace("%room%", ut.preReplace(room));
            text = text.replace("%color%", ut.preReplace(color));
            text = text.replace("%nick%", ut.preReplace(name));
            cm.sendSystemToAllUsersInRoom(text, room);
            r.setModerated(true);
        }
    }

    /**
     * Schmei&szlig;t alle Chatter im Chat wieder Raus (Bei fatalem Chatfehler
     * empfohlen)
     *
     * @param nick Der Chatternick
     */
    private void wipe(String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("wipe", u.getStatus())) {
            low("wipe", nick);
        } else {
            cm.quitAllUsers();
        }
    }

    /**
     * &Auml;ndert die Rechte eines Chatters
     *
     * @param input Das Ziel
     * @param nick Der Chatternick
     */
    private void chgrights(String input, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        var index = input.indexOf(" ");
        try {
            var status = 1;
            if (index != -1) {
                status = Integer.valueOf(input.substring(index + 1, input.length()));
                input = input.substring(0, index);
            }
            String color = null;

            if (!cm.isPrivileged("change_rights", u.getStatus())) {
                low("chgrights", nick);
            } else if (!db.isRegistered(input)) {
                isNotRegistered(input, nick);
            } else if (status <= 0) {
                chgrightsIsInvalid(nick);
            } else if (status >= 11) {
                chgrightsIsInvalid(nick);
            } else {
                var text = db.getCommand("chgrights");
                text = text.replace("%color%", ut.preReplace(db.getData(input, "color")));
                text = text.replace("%nick%", ut.preReplace(db.getData(input, "nick2")));
                text = text.replace("%old_status%", ut.preReplace(db.getData(input, "status")));
                text = text.replace("%new_status%", ut.preReplace(String.valueOf(status)));
                cm.sendSystemToOne(text, u.getName());
                if (cm.isOnline(input.toLowerCase())) {
                    var u1 = cm.getUser(input);
                    text = db.getCommand("chgrights_online");
                    text = text.replace("%color%", ut.preReplace(u.getColor()));
                    text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                    text = text.replace("%old_status%", ut.preReplace(db.getData(input, "status")));
                    text = text.replace("%new_status%", ut.preReplace(String.valueOf(status)));
                    cm.sendSystemToOne(text, u1.getName());
                    text = db.getCommand("script_chgrights");
                    text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
                    text = text.replace("%status%", ut.preReplace(String.valueOf(status)));
                    cm.sendToAllUsersInRoomWithNoSmilies(text, u1.getName());
                    u1.setStatus(status);
                    db.updateSession(u1.getName(), "status", String.valueOf(status));
                }
                db.updateNick(input, "status", String.valueOf(status));
            }
        } catch (NumberFormatException e) {
            chgrightsIsInvalid(nick);
        }
    }

    /**
     * &Auml;ndern der Rechte durch ung&uuml;ltige Daten nicht m&ouml;glich
     *
     * @param nick Der Chatternick
     */
    private void chgrightsIsInvalid(String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var text = db.getCommand("chgrights_invalid");
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Zeigt eine ausf&uuml;hliche Liste aller Chatter im Chat an
     *
     * @param nick Der Chatternick
     */
    private void list(String nick) {
        var cm = getMaster().getChatManager();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("list", u.getStatus())) {
            low("list", nick);
        } else {
            var sid = u.getConnectionId();
            var ul = cm.getVipUserList(sid);
            cm.sendToOneDirect(ul, nick);
        }
    }

    /**
     * Beendet den Chat komplett
     *
     * @param nick Der Chatternick
     */
    private void shutdown(String nick) {
        resetIdleTime(nick);
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("shutdown", u.getStatus())) {
            low("shutdown", nick);
        } else {
            var text = db.getCommand("chat_shutdown");
            cm.sendSystemToAllUsersInChat(text);
            wipe(nick);
            out.println("* Bye Bye");
            exit(0);
        }
    }

    /**
     * Zeigt alle Bans an
     *
     * @param nick Der Chatternick
     */
    private void deletePicture(String input, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("delete_picture", u.getStatus())) {
            low("dp", nick);
        } else if (!db.isRegistered(input)) {
            isNotRegistered(input, nick);
        } else {
            getMaster().getConfig().getDb().updateNick(input, "image_upload", null);
            getMaster().getConfig().getDb().updateNick(input, "image_url", null);
            var text = db.getCommand("delete_picture");
            text = text.replace("%color%", ut.preReplace(db.getData(input, "color")));
            text = text.replace("%nick%", ut.preReplace(db.getData(input, "nick2")));
            cm.sendSystemToOne(text, u.getName());
        }
    }

    /**
     * Zeigt alle Bans an
     *
     * @param nick Der Chatternick
     */
    private void listBans(String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("show_bans", u.getStatus())) {
            low("sbans", nick);
        } else {
            var db = conf.getDb();
            db.eraseExpiredBans();
            cm.sendTextDirect(db.listBans(), nick);
            cm.sendScroll(nick);
        }
    }

    /**
     * Der Chatter ist nicht berechtigt einen bestimmten Befehl zu nutzen
     *
     * @param command Der Befehl
     * @param nick Der Chatternick
     */
    private void low(String command, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var text = db.getCommand("status_low");
        text = text.replace("%command%", ut.preReplace(command));
        cm.sendSystemToOne(text, nick);
        var u = cm.getUser(nick);
        text = db.getCommand("status_low_supervisor");
        text = text.replace("%color%", ut.preReplace(u.getColor()));
        text = text.replace("%nick%", ut.preReplace(u.getNewName()));
        text = text.replace("%ip%", ut.preReplace(u.getRealIp().equals("") ? u.getIp() : u.getRealIp() + "@" + u.getIp()));
        text = text.replace("%host%", ut.preReplace(u.getRealIp().equals("") ? u.getHost() : u.getRealHost() + "@" + u.getHost()));
        text = text.replace("%command%", ut.preReplace(command));
        cm.sendSystemToSupervisor(text);
    }

    /**
     * Der Chatter hat bereits die gleiche Farbe
     *
     * @param command Die Farbe im Format "RRGGBB"
     * @param nick Der Chatternick
     */
    private void sameColor(String command, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var text = db.getCommand("same_color");
        text = text.replace("%color%", ut.preReplace(command));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Die Farbe ist zu Hell
     *
     * @param command Die Fabre im Format "RRGGBB"
     * @param nick Der Chatternick
     */
    private void brightColor(String command, String nick) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var text = db.getCommand("bright_color");
        text = text.replace("%color%", ut.preReplace(command));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Die Farbe ist ung&uuml;ltig
     *
     * @param command Die Farbe im Format "RRGGBB"
     * @param nick Der Chatternick
     */
    private void invalidColor(String command, String nick) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var text = db.getCommand("invalid_color");
        text = text.replace("%color%", ut.preReplace(command));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Ung&uuml;ltiger Befehlsparameter
     *
     * @param command Der Befehl
     * @param nick Der Chatternick
     */
    private void invalid(String command, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var text = db.getCommand("invalid");
        text = text.replace("%command%", ut.preReplace(command));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Keine weiteren Befehlsparameter
     *
     * @param command Der Befehl
     * @param nick Der Chatternick
     */
    private void invalid2(String command, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var text = db.getCommand("invalid2");
        text = text.replace("%command%", ut.preReplace(command));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Nicht implementiert
     *
     * @param command Der Befehl
     * @param nick Der Chatternick
     */
    private void notImplemented(String command, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var text = db.getCommand("not_implemented");
        text = text.replace("%command%", ut.preReplace(command));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Du bist nicht im selben Raum wie Das Ziel
     *
     * @param target Das Ziel
     * @param nick Der Chatternick
     */
    private void notInSameRoom(String target, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(target);
        var ut = getMaster().getUtil();
        var text = db.getCommand("not_in_room");
        text = text.replace("%color%", ut.preReplace(u.getColor()));
        text = text.replace("%nick%", ut.preReplace(u.getNewName()));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Der Chatter ist zu schwach um Rechte zu entziehen
     *
     * @param target Das Ziel
     * @param nick Der Chatternick
     */
    private void notEnoughPower(String target, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var u = cm.getUser(target);
        var ut = getMaster().getUtil();
        var text = db.getCommand("power_su");
        text = text.replace("%color%", ut.preReplace(u.getColor()));
        text = text.replace("%nick%", ut.preReplace(u.getNewName()));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Der Chatter ist zu schwach um den Befhl auszuf&uuml;hren
     *
     * @param target Das Ziel
     * @param nick Der Chatternick
     */
    private void morePower(String target, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(target);
        var text = db.getCommand("more_power");
        text = text.replace("%color%", ut.preReplace(u.getColor()));
        text = text.replace("%nick%", ut.preReplace(u.getNewName()));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Der Chatter hat bereits Superuser-Rechte
     *
     * @param target Das Ziel
     * @param nick Der Chatternick
     */
    private void isSuperuser(String target, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var ut = getMaster().getUtil();
        var db = getMaster().getConfig().getDb();
        if (cm.isOnline(target)) {
            var u = cm.getUser(target);
            var text = db.getCommand("has_su");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            cm.sendSystemToOne(text, nick);
        } else {
            var text = db.getCommand("has_su");
            text = text.replace("%color%", ut.preReplace(db.getData(target, "color")));
            text = text.replace("%nick%", ut.preReplace(db.getData(target, "nick2")));
            cm.sendSystemToOne(text, nick);

        }
    }

    /**
     * Der Chatter hat keine Superuser-Rechte
     *
     * @param target Das Ziel
     * @param nick Der Chatternick
     */
    private void noSuperuser(String target, String nick) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var db = getMaster().getConfig().getDb();
        if (cm.isOnline(target)) {
            var u = cm.getUser(target);
            var text = db.getCommand("no_su");
            text = text.replace("%color%", ut.preReplace(u.getColor()));
            text = text.replace("%nick%", ut.preReplace(u.getNewName()));
            cm.sendSystemToOne(text, nick);
        } else if (cm.isOnline(target)) {
            var u = cm.getUser(target);
            var text = db.getCommand("no_su");
            text = text.replace("%color%", ut.preReplace(db.getData(target, "color")));
            text = text.replace("%nick%", ut.preReplace(db.getData(target, "nick2")));
            cm.sendSystemToOne(text, nick);
        } else {
            var text = db.getCommand("no_su");
            text = text.replace("%color%", ut.preReplace(getMaster().getConfig().getString("default_color")));
            text = text.replace("%nick%", ut.preReplace(target));
            cm.sendSystemToOne(text, nick);
        }
    }

    /**
     * Der Chatter hat bereits Voice
     *
     * @param target Das Ziel
     * @param nick Der Chatternick
     */
    private void isVoice(String target, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(target);
        var text = db.getCommand("has_moderator");
        text = text.replace("%color%", ut.preReplace(u.getColor()));
        text = text.replace("%nick%", ut.preReplace(u.getNewName()));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Der Chatter hat kein Voice
     *
     * @param target Das Ziel
     * @param nick Der Chatternick
     */
    private void noVoice(String target, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(target);
        var text = db.getCommand("no_moderator");
        text = text.replace("%color%", ut.preReplace(u.getColor()));
        text = text.replace("%nick%", ut.preReplace(u.getNewName()));
        cm.sendSystemToOne(text, nick);
    }

    /**
     * Damit wird ein Freund etfernt!
     *
     * @param user Der Zielchatter
     * @param nick Der Chatternick
     */
    private void delFriend(String user, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("del_friend", u.getStatus())) {
            low("rf", nick);
        } else if (!db.isRegistered(nick)) {
            notRegistered(nick);
        } else if (!db.isRegistered(user)) {
            isNotRegistered(user, nick);
        } else if (user.equalsIgnoreCase(nick)) {
            isYou("rf", nick);
        } else if (!u.getFriends().contains(user.toLowerCase())) {
            var text = db.getCommand("no_friend");
            if (cm.isOnline(user)) {
                var u1 = cm.getUser(user);
                text = text.replace("%color%", ut.preReplace(u1.getColor()));
                text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            } else {
                text = text.replace("%color%", ut.preReplace(db.getData(user, "color")));
                text = text.replace("%nick%", ut.preReplace(db.getData(user, "nick2")));
            }
            cm.sendSystemToOne(text, nick);
        } else {
            var text = db.getCommand("del_friend");
            if (cm.isOnline(user)) {
                var u1 = cm.getUser(user);
                text = text.replace("%color%", ut.preReplace(u1.getColor()));
                text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            } else {
                text = text.replace("%color%", ut.preReplace(db.getData(user, "color")));
                text = text.replace("%nick%", ut.preReplace(db.getData(user, "nick2")));
            }
            db.delFriend(nick, user);
            u.getFriends().remove(user.toLowerCase());
            cm.sendTimedMsgToOne(text, nick);
        }
    }

    /**
     * Damit wird ein Freund hinzugef&uuml;gt
     *
     * @param user Der Zielchatter
     * @param nick Der Chatternick
     */
    private void addFriend(String user, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (!cm.isPrivileged("add_friend", u.getStatus())) {
            low("af", nick);
        } else if (!db.isRegistered(nick)) {
            notRegistered(nick);
        } else if (!db.isRegistered(user)) {
            isNotRegistered(user, nick);
        } else if (user.equalsIgnoreCase(nick)) {
            isYou("af", nick);
        } else if (u.getFriends().contains(user.toLowerCase())) {
            var text = db.getCommand("is_friend");
            if (cm.isOnline(user)) {
                var u1 = cm.getUser(user);
                text = text.replace("%color%", ut.preReplace(u1.getColor()));
                text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            } else {
                text = text.replace("%color%", ut.preReplace(db.getData(user, "color")));
                text = text.replace("%nick%", ut.preReplace(db.getData(user, "nick2")));
            }
            cm.sendSystemToOne(text, nick);
        } else {
            var text = db.getCommand("add_friend");
            if (cm.isOnline(user)) {
                var u1 = cm.getUser(user);
                text = text.replace("%color%", ut.preReplace(u1.getColor()));
                text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
            } else {
                text = text.replace("%color%", ut.preReplace(db.getData(user, "color")));
                text = text.replace("%nick%", ut.preReplace(db.getData(user, "nick2")));
            }
            db.addFriend(nick, user);
            u.getFriends().add(user.toLowerCase());
            cm.sendTimedMsgToOne(text, nick);
        }
    }

    /**
     * Der Chatter ist nicht registriert...
     *
     * @param user Das Ziel
     * @param nick Der Chatternick
     */
    private void isNotRegistered(String user, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        var ind = user.indexOf(" ");
        if (ind != -1) {
            user = user.substring(0, ind);
        }
        var text = db.getCommand("not_registered");
        if (cm.isOnline(user.toLowerCase())) {
            var u1 = cm.getUser(user);
            text = text.replace("%color%", ut.preReplace(u1.getColor()));
            text = text.replace("%nick%", ut.preReplace(u1.getNewName()));
        } else {
            text = text.replace("%color%", ut.preReplace(getMaster().getConfig().getString("default_color")));
            text = text.replace("%nick%", ut.preReplace(user));
        }
        cm.sendSystemToOne(text, u.getName());
    }

    /**
     * Ich bin nicht registriert...
     *
     * @param nick Der Chatternick
     */
    private void notRegistered(String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var text = db.getCommand("is_not_registered");
        var u = cm.getUser(nick);
        text = text.replace("%color%", ut.preReplace(u.getColor()));
        text = text.replace("%nick%", ut.preReplace(u.getNewName()));
        cm.sendSystemToOne(text, u.getName());
    }

    /**
     * Du kannste den Befehl nicht and Dich selbst ausf&uuml;hren
     *
     * @param command Das Ziel
     * @param nick Dein nick
     */
    private void isYou(String command, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        cm.sendSystemToOne(db.getCommand("is_you").replace("%command%", command), nick);
    }

    /**
     * Listet alle Freunde auf
     *
     * @param nick Der Chatternick
     */
    private void listFriends(String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        var friends = u.getFriends();
        if (!cm.isPrivileged("list_friends", u.getStatus())) {
            low("sf", nick);
        } else if (!db.isRegistered(nick)) {
            notRegistered(nick);
        } else if (friends.isEmpty()) {
            cm.sendSystemToOne(db.getCommand("friend_list_no_friends"), nick);
        } else {
            var sb = new StringBuilder();
            var text = db.getCommand("friend_list_title");
            text = text.replace("%count%", ut.preReplace(String.valueOf(friends.size())));
            sb.append(text);
            sb.append("\r\n");
            for (var user : friends) {
                if (cm.isOnline(user)) {
                    var u1 = cm.getUser(user);
                    text = db.getCommand("friend_list_online");
                    text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                    text = text.replace("%color%", ut.preReplace(u1.getColor()));
                    text = text.replace("%user%", ut.preReplace(u1.getNewName()));
                    text = text.replace("%login_time%", ut.preReplace(ut.getSimpleTime(u1.getLoginTime())));
                    text = text.replace("%idle_time%", ut.preReplace(String.valueOf((currentTimeMillis() - u1.getIdleTime()) / 1000)));
                } else {
                    text = db.getCommand("friend_list_offline");
                    text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                    text = text.replace("%color%", ut.preReplace(db.getData(user, "color")));
                    text = text.replace("%user%", ut.preReplace(db.getData(user, "nick2")));
                    text = text.replace("%login_date%", ut.preReplace(ut.getTime(db.getLongData(user, "timestamp_login"))));
                }
                sb.append(text);
                sb.append("\r\n");
            }
            cm.sendToOne(sb.toString(), nick);
        }
    }

    /**
     * Postet einen Farb&uuml;bergang
     *
     * @param input Das Ziel und der Grund (Mit Wildcards)
     * @param nick Der Chatternick
     */
    private void fade(String input, String nick) {
        var cm = getMaster().getChatManager();
        var conf = getMaster().getConfig().getMaster().getConfig();
        var db = conf.getDb();
        var ut = getMaster().getUtil();
        var u = cm.getUser(nick);
        if (u.isGagged()) {
            gagged(nick, u.getRoom());
        } else {
            String color1 = null;
            String color2 = null;
            var index = input.indexOf(" ");
            if (index != -1) {
                color1 = input.substring(0, index).toUpperCase();
                input = input.substring(index + 1, input.length());
            }
            index = input.indexOf(" ");
            if (index != -1) {
                color2 = input.substring(0, index).toUpperCase();
                input = input.substring(index + 1, input.length());
            }
            if (!cm.isPrivileged("fcol", u.getStatus())) {
                low("fcol", nick);
            } else if (color1 == null || color2 == null) {
                invalid("fcol", nick);
            } else if (color1.length() != 6 || !color1.matches("[A-F0-9]*")) {
                invalidColor(color1, nick);
            } else if (isInvalidColor(color1.toCharArray())) {
                brightColor(color1, nick);
            } else if (color2.length() != 6 || !color2.matches("[A-F0-9]*")) {
                invalidColor(color2, nick);
            } else if (isInvalidColor(color2.toCharArray())) {
                brightColor(color2, nick);
            } else {
                resetIdleTime(nick);
                var text = db.getCommand("chat_fade");
                text = text.replace("%color%", ut.preReplace(u.getColor()));
                text = text.replace("%nick%", ut.preReplace(u.getNewName()));
                text = text.replace("%content%", ut.preReplace(input));
                text = text.replace("%color1%", ut.preReplace(color1));
                text = text.replace("%color2%", ut.preReplace(color2));
                cm.sendToAllUsersInRoom(text, nick, false);

            }
        }
    }

    private Bootstrap getMaster() {
        return master;
    }

    private void setMaster(Bootstrap master) {
        this.master = master;
    }
}
