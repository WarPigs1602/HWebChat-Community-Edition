package net.midiandmore.chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.lang.System.out;
import static java.sql.DriverManager.getConnection;
import java.util.Map;
import java.util.TreeMap;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import static net.midiandmore.chat.Bootstrap.fatalError;
import static net.midiandmore.chat.Bootstrap.logError;

/**
 * Die Datenbankverwaltungsklasse
 *
 * @author Andreas Pschorn
 */
public class Database {

    /**
     * Ruft den JDBC-Treiber ab
     *
     * @param master Die Bootstrap-Klasse
     */
    public Database(Bootstrap master) {
        setMaster(master);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException cnfe) {
            fatalError(cnfe);
        }
    }

    /**
     * Verbinde Dich mit der Datenbank
     */
    private synchronized void connectDatabase() {
        try {
            if (getCon() != null) {
                if (getCon().isValid(1000)) {
                    return;
                }
                getCon().close();
            }
            var drcon = getConnection("jdbc:mysql://" + getHost() + "/" + getDb() + "?user=" + getUser() + "&password=" + getPw() + "&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=" + getTimezone());
            setCon(drcon);
            try ( // Sollte Probleme mit Emojis und anderen Sonderzeichen beheben!
                    var statement = getCon().prepareStatement("SET CHARACTER SET utf8mb4;")) {
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            fatalError(se);
        }
    }

    /**
     * Verbinde Dich mit der Datenbank neu falls Fehler Vorhanden
     *
     * @param se SQLException
     */
    private synchronized void connectDatabase(SQLException se) {
        logError(se);
        out.printf("\r\n* Trying to reconnect the Database...");
        connectDatabase();
        out.printf("\r\n* Database successfully reconneted!\r\n");
    }

    /**
     * Ladet die SQL-Konfiguration
     *
     * @param p Properties
     */
    protected void loadConfig(Properties p) {
        setDb(p.getProperty("sql.db"));
        setPw(p.getProperty("sql.pw"));
        setUser(p.getProperty("sql.user"));
        setHost(p.getProperty("sql.host"));
        setPrefix(p.getProperty("sql.prefix"));
        setTimezone(p.getProperty("time_zone"));
        connectDatabase();
    }

    /**
     * L&auml;dt die Bans aus der Datenbank und speichert Sie in Vektoren ab.
     *
     * @param name Nicknamen
     * @param reason Gr&uuml;nde
     * @param banner Derjenige der den Ban vergeben hat
     * @param time Uhrzeit des Banns
     */
    protected void loadBans(ArrayList<String> name, ArrayList<String> reason, ArrayList<String> banner, ArrayList<Long> time) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT name,reason,banner,time FROM `" + getPrefix() + "banlist` ORDER BY `time` ASC"); var resultset = statement.executeQuery()) {
                while (resultset.next()) {
                    name.add(resultset.getString("name"));
                    reason.add(resultset.getString("reason"));
                    banner.add(resultset.getString("banner"));
                    time.add(resultset.getLong("time"));
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        setBans(name, reason, banner, time);
    }

    /**
     * Ruft Templates ab (urspr&uuml;nglich in SQL-Datenbank abgespeichert
     * deswegen nicht in der Config-Klasse)
     *
     * @param template Das Template
     * @param skin Der Skin
     * @return Der Inhalt
     */
    protected String getTemplate(String template, String skin) {
        return readFile(skin, "template", template, "html");
    }

    /**
     * Ruft Templates ab (urspr&uuml;nglich in SQL-Datenbank abgespeichert
     * deswegen nicht in der Config-Klasse)
     *
     * @param template Das Template
     * @param skin Der Skin
     * @return Der Inhalt
     */
    protected String getConsole(String template, String skin) {
        return readFile(skin, "console", template, "html");
    }

    /**
     * Ruft Seiten ab (urspr&uuml;nglich in SQL-Datenbank abgespeichert deswegen
     * nicht in der Config-Klasse)
     *
     * @param template Das Template
     * @param skin Der Skin
     * @return Der Inhalt
     */
    protected String getPage(String template, String skin) {
        return readFile(skin, "pages", template, "html");
    }

    /*
     * Dies ist veraltet
     private void createFile(String text, String style, String type, String file) {
     StringBuilder sb = new StringBuilder();
     String uh = System.getProperty("user.home");
     String fs = System.getProperty("file.separator");
     sb.append(uh);
     sb.append(fs);
     sb.append(".hwc");
     sb.append(fs);
     sb.append("templates");
     sb.append(fs);
     sb.append(style);
     sb.append(fs);
     sb.append(type);
     sb.append(fs);
     sb.append(file);
     sb.append(".template");
     String fn = sb.toString();
     System.out.printf("* Exporting file \"%s\": ", fn);
     File target = new File(fn);
     try {
     FileOutputStream fos = new FileOutputStream(target, false);
     BufferedWriter br = new BufferedWriter(new OutputStreamWriter(fos));
     br.write(text);
     br.flush();
     fos.flush();
     System.out.printf("Done. \r\n", fn);

     } catch (FileNotFoundException fnfe) {
     } catch (IOException ioe) {
     }
     }
     */
    /**
     * Liest Die Templates aus (urspr&uuml;nglich in SQL-Datenbank abgespeichert
     * deswegen nicht in der Config-Klasse)
     *
     * @param style Das Design
     * @param type Der Template-Typ
     * @param file Der Dateiname
     * @return Den Inhalt
     */
    private String readFile(String style, String type, String file, String extension) {
        String text = null;
        var sb = new StringBuilder();
        if (file.contains("/") || file.contains("\\") || file.contains(".") || file.contains(":") || file.contains("\"") || file.contains("'") || file.contains("%") || file.contains("$")) {
            sb.append("Error: The requested file '");
            sb.append(file);
            sb.append("' contains invalid content!\r\n");
            text = sb.toString();
        } else {
            var uh = getProperty("user.home");
            var fs = getProperty("file.separator");
            sb.append(uh);
            sb.append(fs);
            sb.append(".homewebcom");
            sb.append(fs);
            sb.append("templates");
            sb.append(fs);
            sb.append(style);
            sb.append(fs);
            sb.append(type);
            sb.append(fs);
            sb.append(file);
            sb.append(".");
            sb.append(extension);
            var fn = sb.toString();
            var target = new File(fn);
            try {
                var fis = new FileInputStream(target);
                var br = new BufferedReader(new InputStreamReader(fis, Charset.forName(getMaster().getConfig().getString("charset"))));
                sb = new StringBuilder();
                String str = null;
                while ((str = br.readLine()) != null) {
                    sb.append(str);
                    sb.append("\r\n");
                }
                text = sb.toString();
            } catch (FileNotFoundException fnfe) {
                sb = new StringBuilder();
                sb.append("Error: The requested page '");
                sb.append(type);
                sb.append("' '");
                sb.append(file);
                sb.append("' on style '");
                sb.append(style);
                sb.append("' does not exist!\r\n");
                text = sb.toString();
            } catch (IOException ioe) {
                sb = new StringBuilder();
                sb.append("Error because an IOException: ");
                sb.append(ioe.getMessage());
                text = sb.toString();
            }
        }
        return text;
    }

    /**
     * Ruft die Designs ab (urspr&uuml;nglich in SQL-Datenbank abgespeichert
     * deswegen nicht in der Config-Klasse)
     *
     * @param template Das Template
     * @param skin Der Skin
     * @return Das Template
     */
    protected String getPageDesign(String template, String skin) {
        return readFile(skin, "design", template, "html");
    }

    /**
     * Ruft die Mail-Vorlage ab
     *
     * @param template Das Template
     * @param skin Der Skin
     * @return Das Script
     */
    protected String getMail(String template, String skin) {
        return readFile(skin, "mail", template, "txt");
    }

    /**
     * Ruft die Scripte ab (urspr&uuml;nglich in SQL-Datenbank abgespeichert
     * deswegen nicht in der Config-Klasse)
     *
     * @param template Das Template
     * @param skin Der Skin
     * @return Das Script
     */
    protected String getScript(String template, String skin) {
        return readFile(skin, "script", template, "js");
    }

    /**
     * Ruft die Style-Sheets ab (urspr&uuml;nglich in SQL-Datenbank
     * abgespeichert deswegen nicht in der Config-Klasse)
     *
     * @param template Das Template
     * @param skin Der Skin
     * @return Der Style-Sheet
     */
    protected String getStyle(String template, String skin) {
        return readFile(skin, "style", template, "css");
    }

    /**
     * Ruft die Commands ab (urspr&uuml;nglich in SQL-Datenbank abgespeichert
     * deswegen nicht in der Config-Klasse)
     *
     * @param field Der abzurufende Command
     * @return Der Command
     */
    protected String getCommand(String field) {
        var ut = getMaster().getUtil();
        var value = getCom().get(field);
        if (value == null) {
            value = "<span style=\"font-weigt: bold\">(ERROR: The field " + field + " was not defined)</span><br>";
        }
        return ut.replaceFilePaths(ut.replacePaths(value));
    }

    /**
     * Entfernt abgelaufene Bans!
     */
    protected void eraseExpiredBans() {
        var ut = getMaster().getUtil();
        var v = getTimedBanName();
        String msk = null;
        for (var count = 0; count < v.size(); count++) {
            msk = v.get(count);
            if (getTimedBanCounter().get(count) < currentTimeMillis()) {
                delTimedBan(msk);
                eraseExpiredBans();
                break;
            }
        }
    }

    /**
     * Ist der Chatter vorr&uuml;bergehend gebannt?
     *
     * @param nick Der Nick-Name oder der Host
     * @return Ist der Chatter gebannt?
     */
    protected synchronized boolean isTimedBanned(String nick) {
        var ut = getMaster().getUtil();
        nick = nick.toLowerCase();
        var v = getTimedBanName();
        var value = false;
        String msk = null;
        for (var count = 0; count < v.size(); count++) {
            msk = v.get(count);
            var mask = msk.toLowerCase();
            if (!value) {
                value = ut.wildcardMatch(nick, mask);
                if (value && getTimedBanCounter().get(count) < currentTimeMillis()) {
                    value = false;
                }
            } else {
                break;
            }
        }
        return value;
    }

    /**
     * Ist der Chatter gebannt?
     *
     * @param nick Der Nick-Name oder der Host
     * @return Ist der Chatter gebannt?
     */
    protected synchronized boolean isBanned(String nick) {
        return !getBan(nick, "name").isBlank();
    }

    /**
     * Der Banngrund Timeban
     *
     * @param nick Der Nick-Name oder der Host
     * @return Der Grund
     */
    protected synchronized String getTimedBanReason(String nick) {
        var ut = getMaster().getUtil();
        nick = nick.toLowerCase();
        var v = getTimedBanName();
        var value = false;
        String reason = null;
        var count = 0;
        for (var msk : v) {
            var mask = msk.toLowerCase();
            if (!value) {
                value = ut.wildcardMatch(nick, mask);
                reason = "";
            }
            if (value) {
                reason = getTimedBanReason().get(getTimedBanName().indexOf(mask));
                break;
            }
        }
        return reason;
    }

    /**
     * Der Banngrund
     *
     * @param nick Der Nick-Name oder der Host
     * @return Der Grund
     */
    protected synchronized String getBanReason(String nick) {
        return getBan(nick, "reason");
    }

    /**
     * L&ouml;scht zuteffenden Bans
     *
     * @param ubs Ist das Entbannen erfolgreich?
     * @param nick Der Entbanner
     * @param input Das Ziel
     * @return ISt das Bannen erfolgreich?
     */
    protected synchronized boolean delTimedBans(boolean ubs, String nick, String input) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var nme = getTimedBanName();
        for (var bnr : nme) {
            if (ut.wildcardMatch(bnr.toLowerCase(), input.toLowerCase())) {
                var suc = getCommand("uban_succes");
                var nck = suc.replace("%nick%", ut.preReplace(bnr));
                cm.sendSystemToOne(nck, nick);
                delTimedBan(bnr.toLowerCase());
                ubs = delTimedBans(true, nick, input);
                break;
            }
        }
        return ubs;
    }

    /**
     * L&ouml;scht zuteffenden Bans
     *
     * @param ubs Ist das Entbannen erfolgreich?
     * @param nick Der Entbanner
     * @param input Das Ziel
     * @return ISt das Bannen erfolgreich?
     */
    protected synchronized boolean delBans(String nick, String input) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var nme = getBanList();
        StringBuilder sb = new StringBuilder();
        nme.forEach((bnr) -> {
            if (ut.wildcardMatch(bnr.toLowerCase(), input.toLowerCase())) {
                var suc = getCommand("uban_succes");
                var nck = suc.replace("%nick%", ut.preReplace(bnr));
                if (cm.isOnline(nick)) {
                    cm.sendSystemToOne(nck, nick);
                }
                delBan(bnr.toLowerCase());
                sb.append(true);
            }
        });
        return !sb.isEmpty();
    }

    
    /**
     * L&ouml;scht einen Ban nach ID
     *
     * @param id Die Ban-ID
     */
    protected synchronized void delTimedBan(String id) {
        var bid = getTimedBanName().indexOf(id);
        getTimedBanName().remove(bid);
        getTimedBanReason().remove(bid);
        getTimedBanTime().remove(bid);
        getTimedBanUser().remove(bid);
        getTimedBanCounter().remove(bid);
    }

    /**
     * L&ouml;scht einen Ban nach ID
     *
     * @param id Die Ban-ID
     */
    protected synchronized void delBan(String id) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("DELETE FROM `" + getPrefix() + "banlist` WHERE `name` = ?")) {
                statement.setString(1, id.toLowerCase());
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * F&uuml;gt einen Ban hinzu
     *
     * @param nick Das Ziel
     * @param reason Der Grund
     * @param banner Derjenige der den Ban vergeben hat
     * @param length Die Dauer in Minuten
     */
    protected synchronized void addTimedBan(String nick, String reason, String banner, long length) {
        var bt = currentTimeMillis();
        getTimedBanName().add(nick);
        getTimedBanReason().add(reason);
        getTimedBanUser().add(banner);
        getTimedBanTime().add(bt);
        getTimedBanCounter().add((length * 1000) + bt);
    }

    /**
     * F&uuml;gt einen Ban hinzu
     *
     * @param nick Das Ziel
     * @param reason Der Grund
     * @param banner Derjenige der den Ban vergeben hat
     */
    protected synchronized void addBan(String nick, String reason, String banner) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            var bt = currentTimeMillis();
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "banlist` (name, reason, banner, time) VALUES (?, ?, ?, ?);")) {
                statement.setString(1, nick.toLowerCase());
                statement.setString(2, reason);
                statement.setString(3, banner.toLowerCase());
                statement.setLong(4, bt);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Listet Die Bans auf
     *
     * @return Die augelisteten Bans
     */
    protected synchronized String listBans() {
        var ut = getMaster().getUtil();
        var bid = getBanList();
        var bid2 = getTimedBanName();
        var nicks = bid;
        var nicks2 = bid2;
        var sb = new StringBuilder();
        String banlist = null;
        sb.append(getCommand("banlist_title"));
        sb.append("\r\n");
        if (!bid.isEmpty() || !bid2.isEmpty()) {
            sb.append(getCommand("banlist_header"));
            sb.append("\r\n");
        }
        var empty = true;
        for (var count = 0; count < nicks.size(); count++) {
            var banner = getBan(nicks.get(count),"banner");
            var color = getMaster().getConfig().getString("default_color");
            var color2 = getMaster().getConfig().getString("default_color");
            banlist = getCommand("banlist");
            var name = getBan(nicks.get(count),"name");
            if (isRegistered(name)) {
                color2 = getData(name, "color");
                name = getData(name, "nick2");
            }
            banlist = banlist.replace("%name%", ut.preReplace(name));
            banlist = banlist.replace("%banned_color%", ut.preReplace(color2));
            if (isRegistered(banner)) {
                color = getData(banner, "color");
                banner = getData(banner, "nick2");
            }
            banlist = banlist.replace("%reason%", ut.preReplace(getBan(nicks.get(count),"reason")));
            banlist = banlist.replace("%banner%", ut.preReplace(banner));
            banlist = banlist.replace("%banner_color%", ut.preReplace(color));
            banlist = banlist.replace("%time%", ut.preReplace(ut.getTime(Long.valueOf(getBan(nicks.get(count),"time")))));
            banlist = banlist.replace("%duration%", ut.preReplace(getCommand("empty_duration")));
            sb.append(banlist);
            sb.append("\r\n");
            empty = false;
        }
        for (var count = 0; count < nicks2.size(); count++) {
            var banner = getTimedBanUser().get(count);
            var color = getMaster().getConfig().getString("default_color");
            var color2 = getMaster().getConfig().getString("default_color");
            banlist = getCommand("banlist");
            var name = getTimedBanName().get(count);
            if (isRegistered(name)) {
                color2 = getData(name, "color");
                name = getData(name, "nick2");
            }
            banlist = banlist.replace("%name%", ut.preReplace(name));
            banlist = banlist.replace("%banned_color%", ut.preReplace(color2));
            if (isRegistered(banner)) {
                color = getData(banner, "color");
                banner = getData(banner, "nick2");
            }
            banlist = banlist.replace("%reason%", ut.preReplace(getTimedBanReason().get(count)));
            banlist = banlist.replace("%banner%", ut.preReplace(banner));
            banlist = banlist.replace("%banner_color%", ut.preReplace(color));
            banlist = banlist.replace("%time%", ut.preReplace(ut.getTime(getTimedBanTime().get(count))));
            banlist = banlist.replace("%duration%", ut.preReplace(ut.getTime(getTimedBanCounter().get(count))));
            sb.append(banlist);
            sb.append("\r\n");
            empty = false;
        }
        if (empty) {
            sb.append(getCommand("banlist_empty"));
            sb.append("\r\n");
        } else {
            sb.append(getCommand("banlist_footer"));
            sb.append("\r\n");
        }
        return sb.toString();
    }

    /**
     * Ist der Chatter registriert?
     *
     * @param nick Der Nick-Name
     * @return Ist der Chatter registriert?
     */
    protected boolean isRegistered(String nick) {
        var flag = false;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            if (!flag) {
                try (var statement = getCon().prepareStatement("SELECT nick,nick2 FROM `" + getPrefix() + "users` WHERE nick2=? OR nick=?")) {
                    statement.setString(1, nick);
                    statement.setString(2, nick);
                    try (var resultset = statement.executeQuery()) {
                        while (resultset.next()) {
                            if (!flag && resultset.getString("nick2").toLowerCase().equals(nick.toLowerCase())) {
                                flag = true;
                            }
                            if (!flag && resultset.getString("nick").toLowerCase().equals(nick.toLowerCase())) {
                                flag = true;
                            }
                        }
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return flag;
    }

    /**
     * &Uuml;berpr&uuml;ft ob das Passwort mit der Datenbank &uuml;bereinstimmt
     *
     * @param nick Zielnick
     * @param password Das Passwort
     * @return Ist DAs Passwort g&uuml;tig
     */
    protected boolean checkPassword(String nick, String password) {
        var flag = false;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            String regTime = null;
            var statement = getCon().prepareStatement("SELECT timestamp_reg FROM `" + getPrefix() + "users` WHERE nick=? OR nick2=?");
            statement.setString(1, nick.toLowerCase());
            statement.setString(2, nick);
            var resultset = statement.executeQuery();
            while (resultset.next()) {
                regTime = resultset.getString("timestamp_reg");
            }
            resultset.close();
            statement.close();
            var salt = new StringBuilder();
            salt.append(password);
            if (getMaster().getConfig().getInt("salt") == 1) {
                salt.append("$");
                salt.append(regTime);
            }
            statement = getCon().prepareStatement("SELECT nick,nick2,pwd FROM `" + getPrefix() + "users` WHERE (nick=? or nick2=?) and pwd=" + getMaster().getConfig().getString("encrypt_pwd") + "");
            statement.setString(1, nick.toLowerCase());
            statement.setString(2, nick);
            statement.setString(3, salt.toString());
            resultset = statement.executeQuery();
            while (resultset.next()) {
                if (!flag && resultset.getString("nick").toLowerCase().equals(nick.toLowerCase())) {
                    flag = true;
                }
                if (!flag && resultset.getString("nick2").toLowerCase().equals(nick.toLowerCase())) {
                    flag = true;
                }
            }
            resultset.close();
            statement.close();
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return flag;
    }

    /**
     * &Uuml;berpr&uuml;ft ob das neue Passwort mit der Datenbank
     * &uuml;bereinstimmt
     *
     * @param nick Zielnick
     * @param password Das Passwort
     * @return Ist DAs Passwort g&uuml;tig
     */
    protected boolean checkPassword2(String nick, String password) {
        var flag = false;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            String regTime = null;
            var statement = getCon().prepareStatement("SELECT timestamp_reg FROM `" + getPrefix() + "users` WHERE nick=? OR nick2=?");
            statement.setString(1, nick.toLowerCase());
            statement.setString(2, nick.toLowerCase());
            var resultset = statement.executeQuery();
            while (resultset.next()) {
                regTime = resultset.getString("timestamp_reg");
            }
            resultset.close();
            statement.close();
            var salt = new StringBuilder();
            salt.append(password);
            if (getMaster().getConfig().getInt("salt") == 1) {
                salt.append("$");
                salt.append(regTime);
            }
            statement = getCon().prepareStatement("SELECT nick,nick2,pwd2 FROM `" + getPrefix() + "users` WHERE (nick=? or nick2=?) and pwd2=" + getMaster().getConfig().getString("encrypt_pwd") + "");
            statement.setString(1, nick.toLowerCase());
            statement.setString(2, nick);
            statement.setString(3, salt.toString());
            resultset = statement.executeQuery();
            while (resultset.next()) {
                if (!flag && resultset.getString("nick").toLowerCase().equals(nick.toLowerCase())) {
                    flag = true;
                }
                if (!flag && resultset.getString("nick2").toLowerCase().equals(nick.toLowerCase())) {
                    flag = true;
                }
            }
            resultset.close();
            statement.close();
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return flag;
    }

    /**
     * F&uuml;gt einen neuen Freund hinzu!
     *
     * @param nick1 Zielnick als Datenbank-ID
     * @param nick2 Schreiber als Datenbank-ID
     */
    protected void addFriend(String nick1, String nick2) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "friends` (`nick`, `nick2`) VALUES (?, ?)")) {
                statement.setString(1, nick1);
                statement.setString(2, nick2);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * L&ouml;scht einen Freund aus der Datenbank
     *
     * @param nick1 Der Nick-Name
     * @param nick2 Der Nick-Name
     */
    protected void delFriend(String nick1, String nick2) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("DELETE FROM `" + getPrefix() + "friends` WHERE nick=? AND nick2=?")) {
                statement.setString(1, nick1);
                statement.setString(2, nick2);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * L&ouml;scht alle FReundeseintr&auml;ge in denen der Nick vorkommt (Beim
     * L&ouml;schen des Chatters)
     *
     * @param nick1 Der Nick-Name
     */
    protected void delFriendsFromList(String nick1) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("DELETE FROM `" + getPrefix() + "friends` WHERE nick=? OR nick2=?")) {
                statement.setString(1, nick1);
                statement.setString(2, nick1);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Die Freundesliste
     *
     * @param nick Der Nick-Name
     * @return Die Freundesliste
     */
    protected ArrayList<String> getFriendList(String nick) {
        var list = new ArrayList<String>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT nick,nick2 FROM `" + getPrefix() + "friends` WHERE nick=?")) {
                statement.setString(1, nick.toLowerCase());
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        list.add(resultset.getString("nick2").toLowerCase());
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return list;
    }

    /**
     * Liefert alle Nachrichten aus
     *
     * @param nick Der Nick-Name
     * @param request
     * @return Die Nachrichten
     */
    protected String getMessages(String nick, HttpServletRequest request, Map<String, String> map) {
        var ut = getMaster().getUtil();
        var sb = new StringBuilder();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT sender,target,text,time FROM `" + getPrefix() + "messages` WHERE target=? ORDER BY time DESC")) {
                statement.setString(1, nick.toLowerCase());
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        var sender = resultset.getString("sender");
                        var target = resultset.getString("target");
                        var message = resultset.getString("text");
                        var ts = ut.getTime(resultset.getLong("time"));
                        String color = null;
                        if (isRegistered(sender)) {
                            color = getData(sender, "color");
                            sender = getData(sender, "nick2");
                        } else if (getMaster().getConfig().getInt("random_color") == 1) {
                            color = ut.createRandomColor();
                        } else {
                            color = getMaster().getConfig().getString("default_color");
                        }
                        var text = getMaster().getChatServices().getTemplate("message_view", request, map);
                        text = text.replace("%msg_nick%", sender);
                        text = text.replace("%msg_color%", color);
                        text = text.replace("%msg_time%", ts);
                        text = text.replace("%msg%", message);
                        sb.append(text);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return sb.toString();
    }

    /**
     * Topliste anzeigen
     *
     * @param request
     * @param response
     * @return Die Topliste
     */
    protected String getToplist(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var sb = new StringBuilder();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT nick2,color,timestamp_reg,timestamp_login,points FROM `" + getPrefix() + "users` ORDER BY points DESC LIMIT " + getMaster().getConfig().getString("toplist_limit")); var resultset = statement.executeQuery()) {
                var bright = true;
                var count = 0;
                while (resultset.next()) {
                    count++;
                    var nick = resultset.getString("nick2");
                    var color = resultset.getString("color");
                    var reg = resultset.getLong("timestamp_reg");
                    var login = resultset.getLong("timestamp_login");
                    var points = resultset.getInt("points");
                    String text = null;
                    if (bright) {
                        text = getMaster().getChatServices().getTemplate("toplist_list_1", request, map);
                    } else {
                        text = getMaster().getChatServices().getTemplate("toplist_list_2", request, map);
                    }
                    text = text.replace("%top_nick%", nick);
                    text = text.replace("%top_color%", color);
                    text = text.replace("%reg_time%", ut.getTime(reg));
                    text = text.replace("%last_login%", ut.getTime(login));
                    text = text.replace("%points%", valueOf(points));
                    text = text.replace("%count%", valueOf(count));
                    if (cm.isOnline(nick)) {
                        text = text.replace("%online%", getMaster().getChatServices().getTemplate("toplist_list_online", request, map));
                    } else {
                        text = text.replace("%online%", getMaster().getChatServices().getTemplate("toplist_list_offline", request, map));
                    }
                    sb.append(text);
                    bright = !bright;
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return sb.toString();
    }

    /**
     * Z&auml;hlt die Anzahl der registrierten Chatter
     *
     * @return Die Anzahl der Chatter
     */
    protected int countChatter() {
        var count = -1;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT count(*) FROM `" + getPrefix() + "users`"); var resultset = statement.executeQuery()) {
                while (resultset.next()) {
                    count = resultset.getInt(1);
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return count;
    }

    /**
     * Z&auml;hlt die Anzahl der Nchrichten!
     *
     * @param nick Der Nick
     * @return Die Anzahl der Nachrichten
     */
    protected int countMessage(String nick) {
        var count = -1;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT count(*) FROM `" + getPrefix() + "messages` WHERE target=?")) {
                statement.setString(1, nick.toLowerCase());
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        count = resultset.getInt(1);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return count;
    }

    /**
     * L&ouml;scht Nachrichten aus der Datenbank
     *
     * @param target Der Zielnick
     */
    protected void delMessages(String target) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("DELETE FROM `" + getPrefix() + "messages` WHERE target=?")) {
                statement.setString(1, target);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * F&uuml;gt Nachrichten in die Datenbank ein (Noch nicht integiert)
     *
     * @param nick1 Der Sender
     * @param nick2 Das Ziel
     * @param content Der Text-Inhalt
     */
    protected void addRoom(String room, String topic, int locked, String lockReason, int standard, int allowSmilies) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "roomcfg` (`room`, `topic`, "
                    + "`locked`, `lock_reason`, `standard`, `allow_smilies`, `page_title`) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, room);
                statement.setString(2, topic);
                statement.setInt(3, locked);
                statement.setString(4, lockReason);
                statement.setInt(5, standard);
                statement.setInt(6, allowSmilies);
                statement.setString(7, "");
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * F&uuml;gt Nachrichten in die Datenbank ein (Noch nicht integiert)
     *
     * @param nick1 Der Sender
     * @param nick2 Das Ziel
     * @param content Der Text-Inhalt
     */
    protected void addRoomData(String nick, String room, String page_title, String bg_color_1,
            String bg_color_2, String color, String border_color, String link_color) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "roomcfg` (`owner`, `room`, "
                    + "`page_title`, `first_bgcolor`, `second_bgcolor`, `bordercolor`, `textcolor`, `linkcolor`, `chat_napping`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, nick);
                statement.setString(2, room);
                statement.setString(3, page_title);
                statement.setString(4, bg_color_1);
                statement.setString(5, bg_color_2);
                statement.setString(6, border_color);
                statement.setString(7, color);
                statement.setString(8, link_color);
                statement.setString(9, "1");
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * F&uuml;gt Nachrichten in die Datenbank ein (Noch nicht integiert)
     *
     * @param nick1 Der Sender
     * @param nick2 Das Ziel
     * @param content Der Text-Inhalt
     */
    protected void addNapping(String nick, String room, String title, String bg_color_1,
            String bg_color_2, String color_1, String color_2, String link_color_1, String link_color_2) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "napping` (`nick`, `room`, `title`, `bg_color_1`, `bg_color_2`, `color_1`, `color_2`, `link_color_1`, `link_color_2`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, nick);
                statement.setString(2, room);
                statement.setString(3, title);
                statement.setString(4, bg_color_1);
                statement.setString(5, bg_color_2);
                statement.setString(6, color_1);
                statement.setString(7, color_2);
                statement.setString(8, link_color_1);
                statement.setString(9, link_color_2);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * F&uuml;gt Nachrichten in die Datenbank ein (Noch nicht integiert)
     *
     * @param nick1 Der Sender
     * @param nick2 Das Ziel
     * @param content Der Text-Inhalt
     */
    protected void addMessage(String nick1, String nick2, String content) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "messages` (`sender`, `target`, `text`, `time`) VALUES (?, ?, ?, ?)")) {
                statement.setString(1, nick1);
                statement.setString(2, nick2);
                statement.setString(3, content);
                statement.setLong(4, currentTimeMillis());
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * F&uuml;gt eine Session in die Datenbank ein
     *
     * @param nick Der Nick
     * @param sid Die Session-ID
     * @param room Der Raum
     * @param color Die Farbe
     * @param status Der Status
     */
    protected void addSession(String nick, String sid, String room, String color, int status) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "session` (`nick`, `session`, `room`, `color`, `away_status`, `away_reason`, `gag`, `status` ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, nick);
                statement.setString(2, sid);
                statement.setString(3, room);
                statement.setString(4, color);
                statement.setString(5, "0");
                statement.setString(6, "");
                statement.setString(7, "0");
                statement.setString(8, valueOf(status));
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Entfernt alle Sessions aus der Datenbank
     */
    protected void delAllSessions() {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("DELETE FROM `" + getPrefix() + "session` WHERE ?")) {
                statement.setString(1, "1");
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Aktualisiert alle Sessions in der Datenbank
     *
     * @param nick Der Nick
     * @param field Das Feld zum Updaten
     * @param value Der Text zum ersetzen
     */
    protected void updateSession(String nick, String field, String value) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "session` SET " + field + "=? WHERE nick=?")) {
                statement.setString(1, value);
                statement.setString(2, nick);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Registriert einen Nick in der Datenbank
     *
     * @param gender Das Geschlecht
     * @param nick Der Nick
     * @param mail Die Mail-Adresse
     * @param color Die Farbe
     * @param pwd Das Passwort
     * @param question Die Frage
     * @param answer Die Antwort
     * @param day Der Tag
     * @param month Der Monat
     * @param year Das Jahr
     */
    protected void addNick(String gender, String nick, String mail, String color, String pwd, String question, String answer, String day, String month, String year) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "users` (`sex`, `nick`, `nick2`, `mail`, `color`, `pwd`, `reminder`, `answer`, `timestamp_reg`, `timestamp_login`, `bday_day`, `bday_month`, `bday_year` ) VALUES (?, ?, ?, ?, ?, " + getMaster().getConfig().getString("encrypt_pwd") + ", ?, ?, ?, ?, ?, ?,?)")) {
                var regTime = currentTimeMillis();
                var salt = new StringBuilder();
                salt.append(pwd);
                if (getMaster().getConfig().getInt("salt") == 1) {
                    salt.append("$");
                    salt.append(regTime);
                }
                statement.setString(1, gender);
                statement.setString(2, nick.toLowerCase());
                statement.setString(3, nick);
                statement.setString(4, mail);
                statement.setString(5, color);
                statement.setString(6, salt.toString());
                statement.setString(7, question);
                statement.setString(8, answer);
                statement.setLong(9, regTime);
                statement.setLong(10, regTime);
                statement.setString(11, day);
                statement.setString(12, month);
                statement.setString(13, year);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Startet einen Thread im Forum
     *
     */
    protected void addThread(String topic, String content, long ref, long user, long board, String ip, long cat) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "board` (`topic`, `content`, `ref`, `user`, `board`, `posted`, `ip`, `cat`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                var posted = currentTimeMillis();
                statement.setString(1, topic);
                statement.setString(2, content);
                statement.setLong(3, ref);
                statement.setLong(4, user);
                statement.setLong(5, board);
                statement.setLong(6, posted);
                statement.setString(7, ip);
                statement.setLong(8, cat);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Startet einen Thread im Forum
     *
     */
    protected void addBoards(String topic, String description, Long cat) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "board_boards` (`topic`, `description`, `cat`) VALUES (?, ?, ?)")) {
                var posted = currentTimeMillis();
                statement.setString(1, topic);
                statement.setString(2, description);
                statement.setLong(3, cat);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Startet einen Thread im Forum
     *
     */
    protected void addBoardCat(String topic, String description) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("INSERT INTO `" + getPrefix() + "board_cat` (`topic`, `description`) VALUES (?, ?)")) {
                var posted = currentTimeMillis();
                statement.setString(1, topic);
                statement.setString(2, description);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Aktualisert einen Nick in der Datenbank
     *
     * @param nick Der Nick
     * @param field Das Feld zum Updaten
     * @param value Der Text zum ersetzen
     */
    protected void updateNapping(String nick, String field, String value) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "napping` SET " + field + "=? WHERE nick=?")) {
                statement.setString(1, value);
                statement.setString(2, nick.toLowerCase());
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Aktualisert einen Nick in der Datenbank
     *
     * @param nick Der Nick
     * @param field Das Feld zum Updaten
     * @param value Der Text zum ersetzen
     */
    protected void updateFriends(String nick, String nick2) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "friends` SET nick=? WHERE nick=?")) {
                statement.setString(1, nick2.toLowerCase());
                statement.setString(2, nick.toLowerCase());
                statement.executeUpdate();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "friends` SET nick2=? WHERE nick2=?")) {
                statement.setString(1, nick2.toLowerCase());
                statement.setString(2, nick.toLowerCase());
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Aktualisert einen Nick in der Datenbank
     *
     * @param nick Der Nick
     * @param field Das Feld zum Updaten
     * @param value Der Text zum ersetzen
     */
    protected void updateNick(String nick, String field, String value) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "users` SET " + field + "=? WHERE nick2=? OR nick=?")) {
                statement.setString(1, value);
                statement.setString(2, nick.toLowerCase());
                statement.setString(3, nick);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Aktualisert einen Foreneintrag
     *
     * @param nick Der Nick
     * @param field Das Feld zum Updaten
     * @param value Der Text zum ersetzen
     */
    protected void updateBoards(long cat, String field, String value) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "board_boards` SET " + field + "=? WHERE id=?")) {
                statement.setString(1, value);
                statement.setLong(2, cat);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Aktualisert einen Foreneintrag
     *
     * @param nick Der Nick
     * @param field Das Feld zum Updaten
     * @param value Der Text zum ersetzen
     */
    protected void updateBoardCats(long cat, String field, String value) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "board_cat` SET " + field + "=? WHERE id=?")) {
                statement.setString(1, value);
                statement.setLong(2, cat);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Aktualisert einen Foreneintrag
     *
     * @param nick Der Nick
     * @param field Das Feld zum Updaten
     * @param value Der Text zum ersetzen
     */
    protected void updateThread(long cat, String field, String value) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "board` SET " + field + "=? WHERE id=?")) {
                statement.setString(1, value);
                statement.setLong(2, cat);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Aktualisert einen Nick in der Datenbank
     *
     * @param nick Der Nick
     * @param field Das Feld zum Updaten
     * @param value Der Text zum ersetzen
     */
    protected void updatePicture(String nick, InputStream is, String contentType) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "users` SET `image_upload`=?,`image_url`=? WHERE nick=? OR nick2=?")) {
                statement.setBlob(1, is);
                statement.setString(2, contentType);
                statement.setString(3, nick.toLowerCase());
                statement.setString(4, nick.toLowerCase());
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Setzt das Chatter-Passwort neu
     *
     * @param nick Der Nick
     * @param pwd Das Passwort
     */
    protected void updatePassword(String nick, String pwd) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            String regTime = null;
            var statement = getCon().prepareStatement("SELECT timestamp_reg FROM `" + getPrefix() + "users` WHERE nick=? OR nick2=?");
            statement.setString(1, nick.toLowerCase());
            statement.setString(2, nick);
            var resultset = statement.executeQuery();
            while (resultset.next()) {
                regTime = resultset.getString("timestamp_reg");
            }
            resultset.close();
            statement.close();
            var salt = new StringBuilder();
            salt.append(pwd);
            if (getMaster().getConfig().getInt("salt") == 1) {
                salt.append("$");
                salt.append(regTime);
            }
            statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "users` SET pwd=" + getMaster().getConfig().getString("encrypt_pwd") + " WHERE nick=? or nick2=?");
            statement.setString(1, salt.toString());
            statement.setString(2, nick.toLowerCase());
            statement.setString(3, nick);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Setzt das neue Chatter-Passwort neu
     *
     * @param nick Der Nick
     * @param pwd Das Passwort
     */
    protected void updatePassword2(String nick, String pwd) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            String regTime = null;
            var statement = getCon().prepareStatement("SELECT timestamp_reg FROM `" + getPrefix() + "users` WHERE nick=? OR nick2=?");
            statement.setString(1, nick.toLowerCase());
            statement.setString(2, nick);
            var resultset = statement.executeQuery();
            while (resultset.next()) {
                regTime = resultset.getString("timestamp_reg");
            }
            resultset.close();
            statement.close();
            var salt = new StringBuilder();
            salt.append(pwd);
            if (getMaster().getConfig().getInt("salt") == 1) {
                salt.append("$");
                salt.append(regTime);
            }
            statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "users` SET pwd2=" + getMaster().getConfig().getString("encrypt_pwd") + " WHERE nick=? or nick2=?");
            statement.setString(1, salt.toString());
            statement.setString(2, nick.toLowerCase());
            statement.setString(3, nick);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Aktualisiert die Login-Zeit des Chatters
     *
     * @param nick Der Nick
     */
    protected void updateLoginTime(String nick) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "users` SET timestamp_login=?  WHERE nick=? or nick2=?")) {
                statement.setLong(1, currentTimeMillis());
                statement.setString(2, nick.toLowerCase());
                statement.setString(3, nick);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Aktualisert die Raumdaten
     *
     * @param room Der Raum
     * @param field Das Feld
     * @param value Die Daten
     */
    protected void updateRoomData(String room, String field, String value) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("UPDATE `" + getPrefix() + "roomcfg` SET " + field + "=? WHERE room=?")) {
                statement.setString(1, value);
                statement.setString(2, room);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * L&ouml;scht einen Raum aus der Datenbank
     *
     * @param room
     */
    protected void delRoom(String room) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("DELETE FROM `" + getPrefix() + "roomcfg` WHERE room=?")) {
                statement.setString(1, room);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * L&ouml;scht einen Nicknamen aus der Datenbank
     *
     * @param nick Der Nick
     */
    protected void delNick(String nick) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("DELETE FROM `" + getPrefix() + "users` WHERE nick=?")) {
                statement.setString(1, nick);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * L&ouml;scht eine Session aus der Datenbank
     *
     * @param nick Der Nick
     */
    protected void delSession(String nick) {
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("DELETE FROM `" + getPrefix() + "session` WHERE nick=?")) {
                statement.setString(1, nick);
                statement.executeUpdate();
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
    }

    /**
     * Fr&auml;gt Daten aus einem Benutzeraccount ab
     *
     * @param nick Der Nick
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected String getData(long id, String field) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT nick," + field + " FROM `" + getPrefix() + "users` WHERE id=?")) {
                statement.setLong(1, id);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getString(field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat != null ? dat : "";
    }

    /**
     * Fr&auml;gt Daten aus einem Benutzeraccount ab
     *
     * @param nick Der Nick
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected String getData(String nick, String field) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT nick," + field + " FROM `" + getPrefix() + "users` WHERE nick2=? OR nick=?")) {
                statement.setString(1, nick);
                statement.setString(2, nick.toLowerCase());
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getString(field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat != null ? dat : "";
    }

    /**
     * Fr&auml;gt Daten aus einem Benutzeraccount ab
     *
     * @param nick Der Nick
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected String getBan(String nick, String field) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT name," + field + " FROM `" + getPrefix() + "banlist` WHERE name=?")) {
                statement.setString(1, nick.toLowerCase());
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getString(field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat != null ? dat : "";
    }
    /**
     * Fr&auml;gt Daten aus einem Benutzeraccount ab
     *
     * @param nick Der Nick
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected ArrayList<String> getBanList() {
        ArrayList<String> dat = new ArrayList<>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT name FROM `" + getPrefix() + "banlist`")) {
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat.add(resultset.getString("name"));
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat;
    }
    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @param id Die ID
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected long countBoardTreads2(long id) {
        long dat = 0;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT COUNT(*) AS board FROM `" + getPrefix() + "board` WHERE id=ref AND cat=?")) {
                statement.setLong(1, id);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getLong(1);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @param id Die ID
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected long countBoardTreads3(long id) {
        long dat = 0;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT COUNT(*) AS board FROM `" + getPrefix() + "board` WHERE ref=?")) {
                statement.setLong(1, id);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getLong(1);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @param id Die ID
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected long countBoardTreads3(String search) {
        long dat = 0;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT COUNT(*) AS board FROM `" + getPrefix() + "board` WHERE content LIKE ? OR topic LIKE ? ")) {
                statement.setString(1, "%" + search.replace(" ", "%") + "%");
                statement.setString(2, "%" + search.replace(" ", "%") + "%");
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getLong(1);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @param id Die ID
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected long countBoardTreads(long id) {
        long dat = 0;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT COUNT(*) AS board FROM `" + getPrefix() + "board` WHERE cat=?")) {
                statement.setLong(1, id);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getLong(1);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @param id Die ID
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected String getBoardRef(long cat, String field) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id," + field + " FROM `" + getPrefix() + "board` WHERE ref=? ORDER BY id ASC")) {
                statement.setLong(1, cat);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getString(field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat != null ? dat : "";
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @param id Die ID
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected String getBoard(long cat, String field) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id," + field + " FROM `" + getPrefix() + "board` WHERE id=? ORDER BY id ASC")) {
                statement.setLong(1, cat);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getString(field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat != null ? dat : "";
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @param id Die ID
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected long getBoardId() {
        long dat = 0;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT LAST_INSERT_ID();")) {
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getLong(1);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @param id Die ID
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected String getBoards(long id, String field) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id," + field + " FROM `" + getPrefix() + "board_boards` WHERE id=?")) {
                statement.setLong(1, id);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getString(field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat != null ? dat : "";
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @return Die Daten
     */
    protected TreeMap<Long, Object[]> getBoardCat() {
        TreeMap<Long, Object[]> map = new TreeMap<>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id,topic,description,deleted FROM `" + getPrefix() + "board_cat`")) {
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        long id = resultset.getLong("id");
                        Object[] list = new Object[4];
                        list[0] = id;
                        list[1] = resultset.getString("topic");
                        list[2] = resultset.getString("description");
                        list[3] = resultset.getInt("deleted");
                        map.put(id, list);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return map;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @return Die Daten
     */
    protected ArrayList<Object[]> getBoard(long cat) {
        ArrayList<Object[]> map = new ArrayList<>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id,topic,content,ref,user,board,posted,ip,cat FROM `" + getPrefix() + "board` WHERE cat=? OR ref=? ORDER BY id DESC")) {
                statement.setLong(1, cat);
                statement.setLong(2, cat);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        Object[] list = new Object[9];
                        list[0] = resultset.getString("topic");
                        list[1] = resultset.getString("content");
                        list[2] = resultset.getLong("ref");
                        list[3] = resultset.getLong("user");
                        list[4] = resultset.getLong("board");
                        list[5] = resultset.getLong("posted");
                        list[6] = resultset.getString("ip");
                        list[7] = resultset.getLong("cat");
                        long id = resultset.getLong("id");
                        list[8] = id;
                        map.add(list);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return map;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @return Die Daten
     */
    protected ArrayList<Object[]> getBoardFromIdStripped(long ref, long page) {
        long start = 0;
        long end = 0;
        if (page != 1) {
            start = (page * getMaster().getConfig().getLong("board_pages")) - getMaster().getConfig().getLong("board_pages");
        }
        end = getMaster().getConfig().getLong("board_pages");
        ArrayList<Object[]> map = new ArrayList<>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id,topic,content,ref,user,board,posted,ip,cat,deleted,closed FROM `" + getPrefix() + "board` WHERE ref=? ORDER BY id ASC LIMIT ?, ?")) {
                statement.setLong(1, ref);
                statement.setLong(2, start);
                statement.setLong(3, end);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        Object[] list = new Object[11];
                        list[0] = resultset.getString("topic");
                        list[1] = resultset.getString("content");
                        list[2] = resultset.getLong("ref");
                        list[3] = resultset.getLong("user");
                        list[4] = resultset.getLong("board");
                        list[5] = resultset.getLong("posted");
                        list[6] = resultset.getString("ip");
                        list[7] = resultset.getLong("cat");
                        long id = resultset.getLong("id");
                        list[8] = id;
                        list[9] = resultset.getLong("closed");
                        list[10] = resultset.getLong("deleted");
                        map.add(list);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return map;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @return Die Daten
     */
    protected ArrayList<Object[]> getBoardFromIdStripped2(long ref, long page) {
        long start = 0;
        long end = 0;
        if (page != 1) {
            start = (page * getMaster().getConfig().getLong("board_pages")) - getMaster().getConfig().getLong("board_pages");
        }
        end = getMaster().getConfig().getLong("board_pages");
        ArrayList<Object[]> map = new ArrayList<>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id,topic,content,ref,user,board,posted,ip,cat,deleted,closed FROM `" + getPrefix() + "board` WHERE ref=id AND cat=? ORDER BY id DESC LIMIT ?, ?")) {
                statement.setLong(1, ref);
                statement.setLong(2, start);
                statement.setLong(3, end);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        Object[] list = new Object[11];
                        list[0] = resultset.getString("topic");
                        list[1] = resultset.getString("content");
                        list[2] = resultset.getLong("ref");
                        list[3] = resultset.getLong("user");
                        list[4] = resultset.getLong("board");
                        list[5] = resultset.getLong("posted");
                        list[6] = resultset.getString("ip");
                        list[7] = resultset.getLong("cat");
                        long id = resultset.getLong("id");
                        list[8] = id;
                        list[9] = resultset.getLong("closed");
                        list[10] = resultset.getLong("deleted");
                        map.add(list);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return map;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @return Die Daten
     */
    protected ArrayList<Object[]> searchBoard(long ref, long page, String search) {
        long start = 0;
        long end = 0;
        if (page != 1) {
            start = (page * getMaster().getConfig().getLong("board_pages")) - getMaster().getConfig().getLong("board_pages");
        }
        end = getMaster().getConfig().getLong("board_pages");
        ArrayList<Object[]> map = new ArrayList<>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id,topic,content,ref,user,board,posted,ip,cat,deleted,closed FROM `" + getPrefix() + "board` WHERE topic LIKE ? OR content LIKE ? ORDER BY id DESC LIMIT ?, ?")) {
                statement.setString(1, "%" + search.replace(" ", "%") + "%");
                statement.setString(2, "%" + search.replace(" ", "%") + "%");
                statement.setLong(3, start);
                statement.setLong(4, end);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        Object[] list = new Object[11];
                        list[0] = resultset.getString("topic");
                        list[1] = resultset.getString("content");
                        list[2] = resultset.getLong("ref");
                        list[3] = resultset.getLong("user");
                        list[4] = resultset.getLong("board");
                        list[5] = resultset.getLong("posted");
                        list[6] = resultset.getString("ip");
                        list[7] = resultset.getLong("cat");
                        long id = resultset.getLong("id");
                        list[8] = id;
                        list[9] = resultset.getLong("closed");
                        list[10] = resultset.getLong("deleted");
                        map.add(list);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return map;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @return Die Daten
     */
    protected ArrayList<Object[]> getBoardFromId2(long cat) {
        ArrayList<Object[]> map = new ArrayList<>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id,topic,content,ref,user,board,posted,ip,cat,closed,deleted FROM `" + getPrefix() + "board` WHERE id=ref AND cat=? ORDER BY id DESC")) {
                statement.setLong(1, cat);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        Object[] list = new Object[11];
                        list[0] = resultset.getString("topic");
                        list[1] = resultset.getString("content");
                        list[2] = resultset.getLong("ref");
                        list[3] = resultset.getLong("user");
                        list[4] = resultset.getLong("board");
                        list[5] = resultset.getLong("posted");
                        list[6] = resultset.getString("ip");
                        list[7] = resultset.getLong("cat");
                        long id = resultset.getLong("id");
                        list[8] = id;
                        list[9] = resultset.getLong("closed");
                        list[10] = resultset.getLong("deleted");
                        map.add(list);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return map;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @return Die Daten
     */
    protected ArrayList<Object[]> getBoardFromId(long cat, long ref) {
        ArrayList<Object[]> map = new ArrayList<>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id,topic,content,ref,user,board,posted,ip,cat,closed,deleted FROM `" + getPrefix() + "board` WHERE ref=? OR id=? ORDER BY id DESC")) {
                statement.setLong(1, ref);
                statement.setLong(2, cat);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        Object[] list = new Object[11];
                        list[0] = resultset.getString("topic");
                        list[1] = resultset.getString("content");
                        list[2] = resultset.getLong("ref");
                        list[3] = resultset.getLong("user");
                        list[4] = resultset.getLong("board");
                        list[5] = resultset.getLong("posted");
                        list[6] = resultset.getString("ip");
                        list[7] = resultset.getLong("cat");
                        long id = resultset.getLong("id");
                        list[8] = id;
                        list[9] = resultset.getLong("closed");
                        list[10] = resultset.getLong("deleted");
                        map.add(list);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return map;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @return Die Daten
     */
    protected TreeMap<Long, Object[]> getBoards(long cat) {
        TreeMap<Long, Object[]> map = new TreeMap<>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id,cat,topic,readonly,description,guests,deleted FROM `" + getPrefix() + "board_boards` WHERE cat=?")) {
                statement.setLong(1, cat);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        Object[] list = new Object[7];
                        list[0] = resultset.getLong("cat");
                        list[1] = resultset.getString("topic");
                        list[2] = resultset.getInt("readonly");
                        list[3] = resultset.getString("description");
                        list[4] = resultset.getInt("guests");
                        long id = resultset.getLong("id");
                        list[5] = id;
                        list[6] = resultset.getInt("deleted");
                        map.put(id, list);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return map;
    }

    /**
     * Fr&auml;gt Daten aus den Forum ab
     *
     * @param id Die ID
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected String getBoardCat(long id, String field) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT id," + field + " FROM `" + getPrefix() + "board_cat` WHERE id=?")) {
                statement.setLong(1, id);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getString(field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat != null ? dat : "";
    }

    /**
     * Fr&auml;gt Daten aus einem Benutzeraccount ab
     *
     * @param nick Der Nick
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected TreeMap<String, String> getData(String field) {
        String dat = null;
        TreeMap<String, String> map = new TreeMap<>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT nick," + field + " FROM `" + getPrefix() + "users`")) {
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        map.put(resultset.getString("nick"), resultset.getString(field));
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return map;
    }

    protected String fromJSON(ResultSet rs, String fieldName) {
        InputStream is = null;
        try {
            is = rs.getBinaryStream(fieldName);
            var br = new BufferedReader(new InputStreamReader(is));
            var content = br.lines().reduce("", String::concat);
            try {
                br.close();
            } catch (IOException ex) {
                getLogger(Database.class.getName()).log(SEVERE, null, ex);
            }
            return content;
        } catch (SQLException ex) {
        } finally {
            try {
                is.close();
            } catch (IOException | NullPointerException ex) {
            }
        }
        return null;
    }

    /**
     * Fr&auml;gt Daten aus einem Benutzeraccount ab
     *
     * @param nick Der Nick
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected String getNapping(String nick, String field) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT nick," + field + " FROM `" + getPrefix() + "napping` WHERE nick=?")) {
                statement.setString(1, nick.toLowerCase());
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getString(field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat != null ? dat : "";
    }

    /**
     * Fr&auml;gt Daten aus einem Benutzeraccount ab
     *
     * @param nick Der Nick
     * @param field Das Feld zum abfragen
     * @return Die Daten
     */
    protected String getNappingJSON(String nick, String field) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT nick," + field + " FROM `" + getPrefix() + "napping` WHERE nick=?")) {
                statement.setString(1, nick.toLowerCase());
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = fromJSON(resultset, field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat != null ? dat : "";
    }

    /**
     * Ruft das Profilbild ab!
     *
     * @param nick Der Nick
     * @return Die Daten
     */
    protected byte[] getPicture(String nick) {
        Blob image = null;
        byte[] dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT image_upload FROM `" + getPrefix() + "users` WHERE nick2=?")) {
                statement.setString(1, nick.toLowerCase());
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        image = resultset.getBlob("image_upload");
                        if (image != null) {
                            dat = image.getBytes(1, (int) image.length());
                        }
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        //System.out.printf("%s\r\n", dat);
        return dat;
    }

    /**
     * Fr&auml;gt Daten aus einem Benutzeraccount ab (Als Long)
     *
     * @param nick Der Nick
     * @param field Das Feld zum abfragen
     * @return Daten als Long
     */
    protected Long getLongData(String nick, String field) {
        long dat = -1;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT nick," + field + " FROM `" + getPrefix() + "users` WHERE nick=?")) {
                statement.setString(1, nick.toLowerCase());
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getLong(field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return dat;
    }

    /**
     * Ruft das kleine Benutzerprofil ab
     *
     * @param sid Deine Sessionid
     * @param nick Das Ziel
     * @return Das kleine Benutzerprofil
     */
    protected String whois(String sid, String nick) {
        var cm = getMaster().getChatManager();
        var ut = getMaster().getUtil();
        var dat = "";
        if (isRegistered(nick)) {
            try {
                if (!getCon().isValid(1000)) {
                    connectDatabase();
                }
                try (var statement = getCon().prepareStatement("SELECT * FROM `" + getPrefix() + "users` WHERE nick=?")) {
                    statement.setString(1, nick.toLowerCase());
                    try (var resultset = statement.executeQuery()) {
                        while (resultset.next()) {
                            dat = getCommand("profil_registered").replace("%nick%", resultset.getString("nick2"));
                            dat = dat.replace("%color%", resultset.getString("color"));
                            dat = dat.replace("%sid%", sid);
                            dat = dat.replace("%online%", cm.isOnline(nick) ? getCommand("profil_registered_online") : getCommand("profil_registered_offline"));
                            dat = dat.replace("%room%", cm.isOnline(nick) ? cm.getUser(nick).getRoom() : getCommand("profil_registered_no_room"));
                        }
                    }
                }
            } catch (SQLException se) {
                connectDatabase(se);
            }
        } else {
            dat = getCommand("profil_unregistered").replace("%nick%", cm.isOnline(nick) ? cm.getUser(nick).getName() : nick);
            dat = dat.replace("%color%", cm.isOnline(nick) ? cm.getUser(nick).getColor() : getCommand("profil_unregistered_no_color"));
            dat = dat.replace("%sid%", sid);
            dat = dat.replace("%online%", cm.isOnline(nick) ? getCommand("profil_unregistered_online") : getCommand("profil_unregistered_offline"));
            dat = dat.replace("%room%", cm.isOnline(nick) ? cm.getUser(nick).getRoom() : getCommand("profil_unregistered_no_room"));
        }
        dat = dat.replace("%skin%", cm.getUser(cm.getNameFromId(sid)).getSkin());
        dat = ut.replaceNoCookies(dat);
        return dat != null ? dat : "";
    }

    /**
     * Existiert der Raum in der Datenbank?
     *
     * @param room Der Raum
     * @return Exisitiert der Raum?
     */
    protected boolean ownerExists(String name) {
        // 06.08.10 Hier: Fortsetzung des SQL-Injection-Schutzes durchf&uuml;hren...
        // 07.08.10 Erledigt

        var flag = false;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT owner FROM `" + getPrefix() + "roomcfg` WHERE owner=?")) {
                statement.setString(1, name);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        if (!flag && resultset.getString("owner").equalsIgnoreCase(name)) {
                            flag = true;
                        }
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return flag;
    }

    /**
     * Existiert der Raum in der Datenbank?
     *
     * @param room Der Raum
     * @return Exisitiert der Raum?
     */
    protected boolean roomExists(String room) {
        // 06.08.10 Hier: Fortsetzung des SQL-Injection-Schutzes durchf&uuml;hren...
        // 07.08.10 Erledigt

        var flag = false;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT room FROM `" + getPrefix() + "roomcfg` WHERE room=?")) {
                statement.setString(1, room);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        if (!flag && resultset.getString("room").equals(room)) {
                            flag = true;
                        }
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return flag;
    }

    /**
     * Raumdaten aus Datenbank anrufen
     *
     * @param room Der Raum
     * @param field Das Feld
     * @return Die Raumdaten
     */
    protected String getRoomData(String room, String field) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT room," + field + " FROM `" + getPrefix() + "roomcfg` WHERE room=?")) {
                statement.setString(1, room);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getString(field);
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return dat != null ? dat : "";
    }

    /**
     * Raumdaten aus Datenbank anrufen
     *
     * @param room Der Raum
     * @param field Das Feld
     * @return Die Raumdaten
     */
    protected String getRoomNameByOwner(String nick) {
        String dat = null;
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT room, owner FROM `" + getPrefix() + "roomcfg` WHERE owner=?")) {
                statement.setString(1, nick);
                try (var resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        dat = resultset.getString("room");
                    }
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return dat != null ? dat : "";
    }

    /**
     * Ruft die Staffliste ab
     *
     * @return Die Staffliste
     */
    protected TreeMap<String, Integer> getStaffList() {
        var ht = new TreeMap<String, Integer>();
        try {
            if (!getCon().isValid(1000)) {
                connectDatabase();
            }
            try (var statement = getCon().prepareStatement("SELECT nick2,status FROM `" + getPrefix() + "users` WHERE status >= 4 ORDER BY status DESC"); var resultset = statement.executeQuery()) {
                while (resultset.next()) {
                    ht.put(resultset.getString("nick2"), resultset.getInt("status"));
                }
            }
        } catch (SQLException se) {
            connectDatabase(se);
        }
        return ht;
    }
    private Bootstrap master;
    private String db;
    private String pw;
    private String user;
    private String host;
    private String prefix;

    private Bootstrap getMaster() {
        return master;
    }

    private void setMaster(Bootstrap master) {
        this.master = master;
    }

    private String getDb() {
        return db;
    }

    private void setDb(String db) {
        this.db = db;
    }

    private String getPw() {
        return pw;
    }

    private void setPw(String pw) {
        this.pw = pw;
    }

    private String getUser() {
        return user;
    }

    private void setUser(String user) {
        this.user = user;
    }

    private String getHost() {
        return host;
    }

    private void setHost(String host) {
        this.host = host;
    }

    private String getPrefix() {
        return prefix;
    }

    private void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private TreeMap<String, String> com;

    /**
     * Speichert die Bans in ArrayListen
     *
     * @param name Der Name
     * @param reason Der Grund
     * @param banner Der bannt
     * @param time Die Zeit
     */
    private void setBans(ArrayList<String> name, ArrayList<String> reason, ArrayList<String> banner, ArrayList<Long> time) {
        setBanName(name);
        setBanReason(reason);
        setBanUser(banner);
        setBanTime(time);
    }

    /**
     * Speichert die Zeitlich begrenzenten Bans in ArrayListen
     *
     * @param name Der Name
     * @param reason Der Grund
     * @param banner Der bannt
     * @param time Die Zeit
     * @param counter Die Zeit des Banendes
     */
    protected void setTimedBans(ArrayList<String> name, ArrayList<String> reason, ArrayList<String> banner, ArrayList<Long> time, ArrayList<Long> counter) {
        setTimedBanName(name);
        setTimedBanReason(reason);
        setTimedBanUser(banner);
        setTimedBanTime(time);
        setTimedBanCounter(counter);
    }

    private void setBanName(ArrayList<String> banName) {
        this.banName = banName;
    }

    private void setBanReason(ArrayList<String> banReason) {
        this.banReason = banReason;
    }

    private void setBanTime(ArrayList<Long> banTime) {
        this.banTime = banTime;
    }

    private void setBanUser(ArrayList<String> banUser) {
        this.banUser = banUser;
    }

    private ArrayList<String> banName;
    private ArrayList<String> banReason;
    private ArrayList<String> banUser;
    private ArrayList<Long> banTime;
    private ArrayList<String> timedBanName;

    private synchronized ArrayList<String> getTimedBanName() {
        return timedBanName;
    }

    private void setTimedBanName(ArrayList<String> timedBanName) {
        this.timedBanName = timedBanName;
    }

    private synchronized ArrayList<String> getTimedBanReason() {
        return timedBanReason;
    }

    private void setTimedBanReason(ArrayList<String> timedBanReason) {
        this.timedBanReason = timedBanReason;
    }

    private synchronized ArrayList<String> getTimedBanUser() {
        return timedBanUser;
    }

    private void setTimedBanUser(ArrayList<String> timedBanUser) {
        this.timedBanUser = timedBanUser;
    }

    private synchronized ArrayList<Long> getTimedBanTime() {
        return timedBanTime;
    }

    private void setTimedBanTime(ArrayList<Long> timedBanTime) {
        this.timedBanTime = timedBanTime;
    }

    private synchronized ArrayList<Long> getTimedBanCounter() {
        return timedBanCounter;
    }

    private void setTimedBanCounter(ArrayList<Long> timedBanCounter) {
        this.timedBanCounter = timedBanCounter;
    }
    private String timezone;
    private ArrayList<String> timedBanReason;
    private ArrayList<String> timedBanUser;
    private ArrayList<Long> timedBanTime;
    private ArrayList<Long> timedBanCounter;
    private TreeMap<String, String> smilies;
    private TreeMap<String, String> paths;
    private TreeMap<String, String> cmd;
    private TreeMap<String, String> hosts;
    private TreeMap<String, String> mimeTypes;
    private TreeMap<String, String> profile;
    private TreeMap<String, String> help;
    private Properties mail;
    private TreeMap<String, String[]> fun;

    /**
     * Die E-Mail-Einstellungen abfragen
     *
     * @return
     */
    protected Properties getMail() {
        return mail;
    }

    /**
     * Die E-Mail-Einstellungen setzen
     *
     * @param mail E-Mail-Einstellungen
     */
    protected void setMail(Properties mail) {
        this.mail = mail;
    }

    private Connection con;

    /**
     * Die MIME-Typen abfragen
     *
     * @return Die MIME-Typen
     */
    protected TreeMap<String, String> getMimeTypes() {
        return mimeTypes;
    }

    /**
     * Die MIME-Typen setzen
     *
     * @param mimeTypes Die MIME-Typen
     */
    protected void setMimeTypes(TreeMap<String, String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    /**
     * Die HOSTS-Parameter abfragen
     *
     * @return Die HOSTS-Parameter
     */
    protected TreeMap<String, String> getHosts() {
        return hosts;
    }

    /**
     * Die HOSTS-Parameter setzen
     *
     * @param hosts Die HOSTS-Parameter
     */
    protected void setHosts(TreeMap<String, String> hosts) {
        this.hosts = hosts;
    }

    /**
     * Die Pfad-Parameter avfragen
     *
     * @return Die Pfad-Parameter
     */
    protected TreeMap<String, String> getPaths() {
        return paths;
    }

    /**
     * Die Pfad-Parameter setzen
     *
     * @param paths Die Pfad-Parameter
     */
    protected void setPaths(TreeMap<String, String> paths) {
        this.paths = paths;
    }

    /**
     * Die Help-Parameter abfragen
     *
     * @return Die Help-Parameter
     */
    protected TreeMap<String, String> getHelp() {
        return help;
    }

    /**
     * Die Help-Parameter setzen
     *
     * @param help Die Help-Parameter
     */
    protected void setHelp(TreeMap<String, String> help) {
        this.help = help;
    }

    /**
     * Die Command-Parameter abfragen
     *
     * @return Die Command-Parameter
     */
    protected TreeMap<String, String> getCmd() {
        return cmd;
    }

    /**
     * Die Command-Parameter setzen
     *
     * @param cmd Die Command-Parameter
     */
    protected void setCmd(TreeMap<String, String> cmd) {
        this.cmd = cmd;
    }

    /**
     * Die Com-Parameter abfragen
     *
     * @return Die Com-Parameter
     */
    protected TreeMap<String, String> getCom() {
        return com;
    }

    /**
     * Die Com-Parameter setzen
     *
     * @param com Die Com-Parameter
     */
    protected void setCom(TreeMap<String, String> com) {
        this.com = com;
    }

    /**
     * Die Profil-Parameter abfragen
     *
     * @return Die Profil-Parameter
     */
    protected TreeMap<String, String> getProfile() {
        return profile;
    }

    /**
     * Setzt die Profil-Parameter
     *
     * @param profile Die Profil-Parameter
     */
    protected void setProfile(TreeMap<String, String> profile) {
        this.profile = profile;
    }

    /**
     * Die Smilies-Properties abfragen
     *
     * @return Die Smilies-Properties
     */
    protected TreeMap<String, String> getSmilies() {
        return smilies;
    }

    /**
     * Setzt die Smilies-Properties
     *
     * @param smilies Die Smilies-Properties
     */
    protected void setSmilies(TreeMap<String, String> smilies) {
        this.smilies = smilies;
    }

    /**
     * Gibt die MySQL-Connection aus
     *
     * @return MySQL-Connection
     */
    public Connection getCon() {
        return con;
    }

    /**
     * Setzt die MySQL-Connection
     *
     * @param con MySQL-Connection
     */
    public void setCon(Connection con) {
        this.con = con;
    }

    /**
     * @return the emot
     */
    protected TreeMap<String, String[]> getFun() {
        return fun;
    }

    /**
     * @param fun
     */
    protected void setFun(TreeMap<String, String[]> fun) {
        this.fun = fun;
    }

    /**
     * @return the timezone
     */
    protected String getTimezone() {
        return timezone;
    }

    /**
     * @param timezone the timezone to set
     */
    protected void setTimezone(String timezone) {
        this.timezone = timezone;
    }

}
