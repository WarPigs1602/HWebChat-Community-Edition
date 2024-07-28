/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.midiandmore.chat;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Random;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.net.InetAddress.getByName;
import static java.net.URLConnection.guessContentTypeFromStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author windo
 */
public class ChatServices {

    /**
     *
     * @param master
     */
    public ChatServices(Bootstrap master) {

    }

    /**
     * Zeigt Inhalte auf so dem Webserver an so das Diese im Browser verarbeitet
     * werden kann.
     *
     * @param request
     * @param response
     */
    protected void parsePage(HttpServletRequest request, HttpServletResponse response) {
        var map = request.getParameterMap();
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var cm = Bootstrap.boot.getChatManager();
        Map<String, String> map2 = new HashMap<>();
        for (var key : map.keySet()) {
            String[] arr = {""};
            var value = map.getOrDefault(key, arr);
            map2.put(key, value[0]);
        }
        if (map2.getOrDefault("page", "").isBlank()) {
            // Die Startseite
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            printTemplate("start", request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_output"))) {
            // Chat-Services und Seiten abrufen
            out(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_login"))) {
            // Der Login-Frame
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            loginFrame(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_logout"))) {
            // Der Logout
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            logout(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_login_chat"))) {
            // Der Login-Frame
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            chatFrame(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_memory"))) {
            // Speicherinfo (Debug)
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            meminfo(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_toplist"))) {
            // Speicherinfo (Debug)
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            try {
                request.setCharacterEncoding(conf.getString("charset"));
                response.setCharacterEncoding(conf.getString("charset"));
            } catch (UnsupportedEncodingException ex) {
            }
            toplist(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_captcha"))) {
            // Captcha zur G&uuml;ltigkeitspr&uuml;fung
            Bootstrap.boot.getCaptcha().drawCaptcha(request, response);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_reg_form"))) {
            // Registrierungsformular
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            regForm(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_image"))) {
            profilePicture(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_webchat"))) {
            // Info f&uuml;r Chatinformationsseiten wie z. B. http://www.webchat.de
            var json = map2.getOrDefault("use_json", "");
            if (json.equalsIgnoreCase("1")) {
                response.setContentType("application/json");
                var webChat = cm.getWebChatJson();
                ut.submitContent(webChat, response);
            } else {
                response.setContentType("text/plain");
                var webChat = cm.getWebChat();
                ut.submitContent(webChat, response);
            }
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_password"))) {
            // Passwortwiederherstellungsformular
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            password(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_console"))) {
            // Passwortwiederherstellungsformular
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            console(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_link"))) {
            // Chatweiterleitung
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            redirect(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_message"))) {
            // Im Chat hinterlassene Nachrichten abrufen
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            readMessage(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_account"))) {
            // Account bearbeiten!
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            accountForm(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_account_com"))) {
            // Account bearbeiten!
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            accountForm2(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_board"))) {
            // Account bearbeiten!
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            board(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_profile"))) {
            // Benutzerprofil abrufen
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            userProfile(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_emot"))) {
            // Emoticonshilfe anzeigen lassen
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            emot(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_help"))) {
            // Chathilfe (Provisorium)
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            help(request, response, map2);
        } else {
            // Fehlermeldung 404 Datei nicht gefunden!
            response.setStatus(404);
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            printTemplate("error", request, response, map2);
        }
    }

    private void console(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter();
        var ut = Bootstrap.boot.getUtil();
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        try {
            filter.init(Bootstrap.boot);
        } catch (ServletException ex) {
            Logger.getLogger(ChatServices.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            filter.doFilter(request, response);
        } catch (IOException ex) {
            Logger.getLogger(ChatServices.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServletException ex) {
            Logger.getLogger(ChatServices.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!filter.error) {
            var service = map.getOrDefault("service", "");
            var change = map.getOrDefault("change", "");
            var text = "";
            if (service.isBlank()) {
                text = getConsole("index", request, map);
                ut.submitContent(text, response);
            } else if (service.equals("room")) {
                Database db = conf.getDb();
                var room = map.getOrDefault("room", "");
                var topic = map.getOrDefault("topic", "");
                var lock = map.getOrDefault("lock", "");
                var lockReason = map.getOrDefault("lock_reason", "");
                var standard = map.getOrDefault("standard", "");
                var allowSmilies = map.getOrDefault("allow_smilies", "");
                var exists = db.roomExists(room);
                if (change.isBlank()) {
                    text = getConsole("rooms", request, map);
                } else if (change.equals("search")) {
                    if (!exists) {
                        text = getConsole("rooms_fail", request, map);
                        text = text.replace("%room%", room);
                    } else {
                        text = getConsole("rooms_config", request, map);
                        topic = db.getRoomData(room, "topic");
                        lock = db.getRoomData(room, "locked");
                        lockReason = db.getRoomData(room, "lock_reason");
                        standard = db.getRoomData(room, "standard");
                        allowSmilies = db.getRoomData(room, "allow_smilies");
                        text = text.replace("%room%", room);
                        text = text.replace("%topic%", topic);
                        text = text.replace("%lock%", lock);
                        text = text.replace("%lock_reason%", lockReason);
                        text = text.replace("%standard%", standard);
                        text = text.replace("%allow_smilies%", allowSmilies);
                    }
                } else if (change.equals("modify")) {
                    if (!exists) {
                        text = getConsole("rooms_fail", request, map);
                        text = text.replace("%room%", room);
                    } else {
                        text = getConsole("rooms_modify", request, map);
                        db.updateRoomData(room, "topic", topic);
                        db.updateRoomData(room, "locked", lock);
                        db.updateRoomData(room, "lock_reason", lockReason);
                        db.updateRoomData(room, "standard", standard);
                        db.updateRoomData(room, "allow_smilies", allowSmilies);
                        text = text.replace("%room%", room);
                        text = text.replace("%topic%", topic);
                        text = text.replace("%lock%", lock);
                        text = text.replace("%lock_reason%", lockReason);
                        text = text.replace("%standard%", standard);
                        text = text.replace("%allow_smilies%", allowSmilies);
                    }
                } else if (change.equals("add_room")) {
                    if (exists) {
                        text = getConsole("rooms_exist", request, map);
                        text = text.replace("%room%", room);
                    } else {
                        text = getConsole("rooms_add", request, map);
                        text = text.replace("%room%", room);
                        text = text.replace("%topic%", topic);
                        text = text.replace("%lock%", "0");
                        text = text.replace("%lock_reason%", lockReason);
                        text = text.replace("%standard%", "0");
                        text = text.replace("%allow_smilies%", "1");
                    }
                } else if (change.equals("set_room")) {
                    if (exists) {
                        text = getConsole("rooms_exist", request, map);
                        text = text.replace("%room%", room);
                    } else {
                        text = getConsole("rooms_set", request, map);
                        text = text.replace("%room%", room);
                        text = text.replace("%topic%", topic);
                        text = text.replace("%lock%", lock);
                        text = text.replace("%lock_reason%", lockReason);
                        text = text.replace("%standard%", standard);
                        text = text.replace("%allow_smilies%", allowSmilies);
                        db.addRoom(room, topic, Integer.valueOf(lock), lockReason, Integer.valueOf(standard), Integer.valueOf(allowSmilies));
                    }
                } else if (change.equals("delete")) {
                    if (!exists) {
                        text = getConsole("rooms_fail", request, map);
                        text = text.replace("%room%", room);
                    } else {
                        text = getConsole("rooms_delete", request, map);
                        text = text.replace("%room%", room);
                        db.delRoom(room);
                    }
                } else {
                    text = getConsole("rooms_404", request, map);
                    text = text.replace("%room%", room);
                }
                ut.submitContent(text, response);
            } else if (service.equals("chat")) {
                text = getConsole("chat", request, map);
                TreeMap<String, String[]> p = conf.loadDataFromJSON("config.json", "name", "value", "description");
                TreeMap<String, String[]> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
                var update = map.getOrDefault("update", "");

                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page")) {
                            continue;
                        } else {
                            String[] arr2 = {"", ""};
                            var arr = p.getOrDefault(key1, arr2);
                            var description = arr[1];
                            String[] arr1 = {map.getOrDefault(key1, ""), description};
                            p1.put(key1, arr1);
                        }
                    }
                    conf.saveDataToJSON("config.json", p1, "name", "value", "description");
                    conf.hash();
                    text = getConsole("chat", request, map);
                    text = text.replace("%success%", getConsole("success", request, map));

                } else {
                    text = text.replace("%success%", "");
                }
                p = conf.loadDataFromJSON("config.json", "name", "value", "description");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr[0];
                    var description = arr[1];
                    var table = getConsole("chat_table", request, map);
                    table = table.replace("%description%", description);
                    table = table.replace("%name%", ut.parseHtml(key));
                    table = table.replace("%value%", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.replace("%config%", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("fun")) {
                var update = map.getOrDefault("update", "");

                text = getConsole("fun", request, map);
                TreeMap<String, String[]> p = conf.loadDataFromJSON("fun.json", "command", "content", "description");
                TreeMap<String, String[]> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page")) {
                            continue;
                        } else {
                            String[] arr2 = {"", ""};
                            var arr = p.getOrDefault(key1, arr2);
                            var description = arr[1];
                            String[] arr1 = {ut.parseHtmlReverse(map.getOrDefault(key1, "")), description};
                            if (!map.getOrDefault(key1, "").isBlank()) {
                                p1.put(key1, arr1);
                            }
                        }
                    }
                    if (!map.getOrDefault("command", "").isBlank() && !map.getOrDefault("description", "").isBlank() && !map.getOrDefault("content", "").isBlank()) {
                        var name = map.getOrDefault("command", "");
                        var value = map.getOrDefault("content", "");
                        var desc = map.getOrDefault("description", "");
                        p1.remove("command");
                        p1.remove("content");
                        p1.remove("description");
                        String[] arr = {value, desc};
                        p1.put(name, arr);
                    } else {
                        p1.remove("command");
                        p1.remove("content");
                        p1.remove("description");
                    }
                    conf.saveDataToJSON("fun.json", p1, "command", "content", "description");
                    text = text.replace("%success%", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.replace("%success%", "");
                }
                p = conf.loadDataFromJSON("fun.json", "command", "content", "description");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr[0];
                    var description = arr[1];
                    var table = getConsole("fun_table", request, map);
                    table = table.replace("%description%", description);
                    table = table.replace("%name%", ut.parseHtml(key));
                    table = table.replace("%value%", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.replace("%config%", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("cmdrights")) {
                var update = map.getOrDefault("update", "");

                text = getConsole("cmdcfg", request, map);
                TreeMap<String, String> p = conf.loadDataFromJSON("cmdcfg.json", "name", "status");
                TreeMap<String, String> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page")) {
                            continue;
                        } else {
                            var arr = map.getOrDefault(key1, "");
                            p1.put(key1, arr);
                        }
                    }
                    conf.saveDataToJSON("cmdcfg.json", p1, "name", "status");
                    text = text.replace("%success%", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.replace("%success%", "");
                }
                p = conf.loadDataFromJSON("cmdcfg.json", "name", "status");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("cmdcfg_table", request, map);
                    table = table.replace("%name%", ut.parseHtml(key));
                    table = table.replace("%value%", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.replace("%config%", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("cmd")) {
                var update = map.getOrDefault("update", "");

                text = getConsole("commands", request, map);
                TreeMap<String, String> p = conf.loadDataFromJSON("commands.json", "name", "command");
                TreeMap<String, String> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page")) {
                            continue;
                        } else {
                            var arr = map.getOrDefault(key1, "");
                            p1.put(key1, arr);
                        }
                    }
                    conf.saveDataToJSON("commands.json", p1, "name", "command");
                    text = text.replace("%success%", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.replace("%success%", "");
                }
                p = conf.loadDataFromJSON("commands.json", "name", "command");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("commands_table", request, map);
                    table = table.replace("%name%", ut.parseHtml(key));
                    table = table.replace("%value%", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.replace("%config%", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("host")) {
                var update = map.getOrDefault("update", "");

                text = getConsole("host", request, map);
                TreeMap<String, String> p = conf.loadDataFromJSON("hosts.json", "host", "skin");
                TreeMap<String, String> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page")) {
                            continue;
                        } else {
                            var arr = map.getOrDefault(key1, "");
                            if (!arr.isBlank()) {
                                p1.put(key1, arr);
                            }
                        }
                    }
                    if (!p1.getOrDefault("name", "").isBlank() && !p1.getOrDefault("value", "").isBlank()) {
                        var name = p1.getOrDefault("name", "");
                        var value = p1.getOrDefault("value", "");
                        p1.remove("name");
                        p1.remove("value");
                        p1.put(name, value);
                    } else {
                        p1.remove("name");
                        p1.remove("value");
                    }
                    conf.saveDataToJSON("hosts.json", p1, "host", "skin");
                    text = text.replace("%success%", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.replace("%success%", "");
                }
                p = conf.loadDataFromJSON("hosts.json", "host", "skin");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("host_table", request, map);
                    table = table.replace("%name%", ut.parseHtml(key));
                    table = table.replace("%value%", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.replace("%config%", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("emot")) {
                var update = map.getOrDefault("update", "");

                text = getConsole("emot", request, map);
                TreeMap<String, String> p = conf.loadDataFromJSON("emot.json", "name", "emot");
                TreeMap<String, String> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page")) {
                            continue;
                        } else {
                            var arr = map.getOrDefault(key1, "");
                            if (!arr.isBlank()) {
                                p1.put(key1, arr);
                            }
                        }
                    }
                    if (!p1.getOrDefault("name", "").isBlank() && !p1.getOrDefault("value", "").isBlank()) {
                        var name = p1.getOrDefault("name", "");
                        var value = p1.getOrDefault("value", "");
                        p1.remove("name");
                        p1.remove("value");
                        p1.put(name, value);
                    } else {
                        p1.remove("name");
                        p1.remove("value");
                    }
                    conf.saveDataToJSON("emot.json", p1, "name", "emot");
                    text = text.replace("%success%", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.replace("%success%", "");
                }
                p = conf.loadDataFromJSON("emot.json", "name", "emot");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("emot_table", request, map);
                    table = table.replace("%name%", ut.parseHtml(key));
                    table = table.replace("%value%", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.replace("%config%", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("mime")) {
                var update = map.getOrDefault("update", "");

                text = getConsole("mime", request, map);
                TreeMap<String, String> p = conf.loadDataFromJSON("mime-types.json", "suffix", "type");
                TreeMap<String, String> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page")) {
                            continue;
                        } else {
                            var arr = map.getOrDefault(key1, "");
                            if (!arr.isBlank()) {
                                p1.put(key1, arr);
                            }
                        }
                    }
                    if (!p1.getOrDefault("name", "").isBlank() && !p1.getOrDefault("value", "").isBlank()) {
                        var name = p1.getOrDefault("name", "");
                        var value = p1.getOrDefault("value", "");
                        p1.remove("name");
                        p1.remove("value");
                        p1.put(name, value);
                    } else {
                        p1.remove("name");
                        p1.remove("value");
                    }
                    conf.saveDataToJSON("mime-types.json", p1, "suffix", "type");
                    text = text.replace("%success%", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.replace("%success%", "");
                }
                p = conf.loadDataFromJSON("mime-types.json", "suffix", "type");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("mime_table", request, map);
                    table = table.replace("%name%", ut.parseHtml(key));
                    table = table.replace("%value%", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.replace("%config%", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("mail")) {
                var update = map.getOrDefault("update", "");

                text = getConsole("mail", request, map);
                TreeMap<String, String> p = conf.loadDataFromJSON("mail.json", "name", "value");
                TreeMap<String, String> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page")) {
                            continue;
                        } else {
                            var arr = map.getOrDefault(key1, "");
                            p1.put(key1, arr);
                        }
                    }
                    conf.saveDataToJSON("mail.json", p1, "name", "value");
                    text = text.replace("%success%", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.replace("%success%", "");
                }
                p = conf.loadDataFromJSON("mail.json", "name", "value");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("mail_table", request, map);
                    table = table.replace("%name%", ut.parseHtml(key));
                    table = table.replace("%value%", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.replace("%config%", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("path")) {
                var update = map.getOrDefault("update", "");

                text = getConsole("path", request, map);
                TreeMap<String, String> p = conf.loadDataFromJSON("paths.json", "name", "path");
                TreeMap<String, String> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page")) {
                            continue;
                        } else {
                            var arr = map.getOrDefault(key1, "");
                            p1.put(key1, arr);
                        }
                    }
                    conf.saveDataToJSON("paths.json", p1, "name", "path");
                    text = text.replace("%success%", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.replace("%success%", "");
                }
                p = conf.loadDataFromJSON("paths.json", "name", "path");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("path_table", request, map);
                    table = table.replace("%name%", ut.parseHtml(key));
                    table = table.replace("%value%", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.replace("%config%", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("help")) {
                var update = map.getOrDefault("update", "");

                text = getConsole("help", request, map);
                TreeMap<String, String> p = conf.loadDataFromJSON("help.json", "name", "html");
                TreeMap<String, String> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page")) {
                            continue;
                        } else {
                            var arr = map.getOrDefault(key1, "");
                            p1.put(key1, arr);
                        }
                    }
                    conf.saveDataToJSON("help.json", p1, "name", "html");
                    text = text.replace("%success%", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.replace("%success%", "");
                }
                p = conf.loadDataFromJSON("help.json", "name", "html");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("help_table", request, map);
                    table = table.replace("%name%", ut.parseHtml(key));
                    table = table.replace("%value%", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.replace("%config%", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("users")) {
                var update = map.getOrDefault("update", "");
                StringBuilder sb = new StringBuilder();
                if (!update.isBlank()) {
                    var nick = map.getOrDefault("user", "");
                    Database db = conf.getDb();
                    if (db.isRegistered(nick)) {
                        if (change.isBlank()) {
                            text = getConsole("users_page", request, map);
                            text = text.replace("%nick%", nick);

                        } else if (change.equals("password")) {
                            var newPwd = map.getOrDefault("new_pwd", "");
                            var newPwd2 = map.getOrDefault("new_pwd2", "");
                            String errorMessage = null;
                            if (!newPwd.isBlank()) {
                                if (newPwd.length() < conf.getInt("min_pwd_length")) {
                                    errorMessage = getTemplate("account_error_message_pass_short", request, map);
                                } else if (newPwd.length() > conf.getInt("max_pwd_length")) {
                                    errorMessage = getTemplate("account_error_message_pass_long", request, map);
                                } else if (!newPwd.equals(newPwd2)) {
                                    errorMessage = getTemplate("account_error_message_pass_same", request, map);
                                }
                                if (errorMessage == null) {
                                    text = getConsole("users_pwd_success", request, map);
                                    text = text.replace("%nick%", nick);
                                    db.updatePassword(nick, newPwd);
                                } else {
                                    text = getConsole("users_pwd", request, map);
                                    text = text.replace("%account_error_if_one%", getTemplate("account_failed", request, map));
                                    text = text.replace("%error_message%", errorMessage);
                                    text = text.replace("%nick%", nick);
                                }
                            } else {
                                text = getConsole("users_pwd", request, map);
                                text = text.replace("%account_error_if_one%", "");
                                text = text.replace("%error_message%", "");
                                text = text.replace("%nick%", nick);
                            }
                        } else if (change.equals("delete")) {
                            text = getConsole("users_delete", request, map);
                            text = text.replace("%nick%", nick);
                            if (cm.isOnline(nick)) {
                                cm.quit(nick);
                                cm.removeFriend(nick);
                            }
                            db.delFriendsFromList(nick);
                            db.delNick(nick);
                        } else if (change.equals("data")) {
                            var mail = map.getOrDefault("mail", "");
                            var gender = map.getOrDefault("gender", "");
                            var day = map.getOrDefault("day", "");
                            var month = map.getOrDefault("month", "");
                            var year = map.getOrDefault("year", "");
                            var status = map.getOrDefault("status", "");
                            String errorMessage = null;
                            if (!gender.equals("-") && !gender.equals("m") && !gender.equals("f")) {
                                errorMessage = getTemplate("account_error_message_gender_invalid", request, map);
                            } else if (!ut.isValidDate(day, month, year)) {
                                errorMessage = getTemplate("account_error_message_date", request, map);
                            } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                                errorMessage = getTemplate("account_error_message_age_old", request, map);
                            } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                                errorMessage = getTemplate("account_error_message_age_young", request, map);
                            } else if (!mail.contains("@")) {
                                errorMessage = getTemplate("account_error_message_mail_invalid", request, map);
                            } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                                errorMessage = getTemplate("account_error_message_mail_invalid", request, map);
                            }
                            if (errorMessage == null) {
                                db.updateNick(nick, "sex", gender);
                                db.updateNick(nick, "bday_day", day);
                                db.updateNick(nick, "bday_month", month);
                                db.updateNick(nick, "bday_year", year);
                                db.updateNick(nick, "mail", mail);
                                db.updateNick(nick, "status", status);
                            }
                            mail = db.getData(nick, "mail");
                            gender = db.getData(nick, "sex");
                            day = db.getData(nick, "bday_day");
                            month = db.getData(nick, "bday_month");
                            year = db.getData(nick, "bday_year");
                            status = db.getData(nick, "status");
                            mail = mail == null ? "" : mail;
                            gender = gender == null ? "" : gender;
                            day = day == null ? "" : day;
                            month = month == null ? "" : month;
                            year = year == null ? "" : year;
                            text = getConsole("users_account", request, map);
                            text = ut.parseGender(text, gender);
                            text = text.replace("%mail%", mail);
                            text = text.replace("%day%", day);
                            text = text.replace("%month%", month);
                            text = text.replace("%year%", year);
                            text = text.replace("%status%", status);
                            text = text.replace("%nick%", nick);
                            if (errorMessage == null) {
                                text = text.replace("%account_error_if_one%", "");
                            } else {
                                text = text.replace("%account_error_if_one%", errorMessage);
                            }
                        } else if (change.equals("profile")) {
                            var homepage = db.getData(nick, "homepage");
                            var city = db.getData(nick, "city");
                            var hobby = db.getData(nick, "hobby");
                            var description = db.getData(nick, "description");
                            var slogan = db.getData(nick, "slogan");
                            var signature = db.getData(nick, "signature");
                            var icq = db.getData(nick, "icq");
                            var live = db.getData(nick, "live");
                            var yahoo = db.getData(nick, "yahoo");
                            var facebook = db.getData(nick, "facebook");
                            var twitter = db.getData(nick, "twitter");
                            var irc = db.getData(nick, "irc");
                            var youtube = db.getData(nick, "youtube");
                            var fam = db.getData(nick, "fam_status");
                            text = getConsole("users_reg", request, map);
                            text = text.replace("%nick%", db.getData(nick, "nick2"));
                            text = text.replace("%homepage%", homepage);
                            text = text.replace("%city%", city);
                            text = text.replace("%hobby%", hobby);
                            text = text.replace("%description%", description);
                            text = text.replace("%slogan%", slogan);
                            text = text.replace("%signature%", signature);
                            text = text.replace("%icq%", icq);
                            text = text.replace("%live%", live);
                            text = text.replace("%yahoo%", yahoo);
                            text = text.replace("%facebook%", facebook);
                            text = text.replace("%twitter%", twitter);
                            text = text.replace("%irc%", irc);
                            text = text.replace("%youtube%", youtube);
                            text = text.replace("%fam_status%", fam);
                            text = text.replace("%success%", "");
                        } else if (change.equals("profile_change")) {
                            text = getConsole("users_reg", request, map);
                            text = text.replace("%success%", getConsole("success", request, map));
                            var homepage = map.getOrDefault("homepage", "");
                            var city = map.getOrDefault("city", "");
                            var hobby = map.getOrDefault("hobby", "");
                            var description = map.getOrDefault("description", "");
                            var slogan = map.getOrDefault("slogan", "");
                            var signature = map.getOrDefault("signature", "");
                            var icq = map.getOrDefault("icq", "");
                            var live = map.getOrDefault("live", "");
                            var yahoo = map.getOrDefault("yahoo", "");
                            var facebook = map.getOrDefault("facebook", "");
                            var twitter = map.getOrDefault("twitter", "");
                            var irc = map.getOrDefault("irc", "");
                            var youtube = map.getOrDefault("youtube", "");
                            var fam = map.getOrDefault("fam_status", "");
                            text = text.replace("%nick%", db.getData(nick, "nick2"));
                            text = text.replace("%homepage%", homepage);
                            text = text.replace("%city%", city);
                            text = text.replace("%hobby%", hobby);
                            text = text.replace("%description%", description);
                            text = text.replace("%slogan%", slogan);
                            text = text.replace("%signature%", signature);
                            text = text.replace("%icq%", icq);
                            text = text.replace("%live%", live);
                            text = text.replace("%yahoo%", yahoo);
                            text = text.replace("%facebook%", facebook);
                            text = text.replace("%twitter%", twitter);
                            text = text.replace("%irc%", irc);
                            text = text.replace("%youtube%", youtube);
                            text = text.replace("%fam_status%", fam);
                            db.updateNick(nick, "homepage", homepage);
                            db.updateNick(nick, "city", city);
                            db.updateNick(nick, "hobby", hobby);
                            db.updateNick(nick, "description", description);
                            db.updateNick(nick, "slogan", slogan);
                            db.updateNick(nick, "signature", signature);
                            db.updateNick(nick, "icq", icq);
                            db.updateNick(nick, "live", live);
                            db.updateNick(nick, "yahoo", yahoo);
                            db.updateNick(nick, "facebook", facebook);
                            db.updateNick(nick, "twitter", twitter);
                            db.updateNick(nick, "irc", irc);
                            db.updateNick(nick, "youtube", youtube);
                            db.updateNick(nick, "fam_status", fam);
                        }
                    } else if (change.equals("reg")) {
                        var gender = map.getOrDefault("reg_gender", "");
                        nick = map.getOrDefault("reg_nick", "");
                        var mail = map.getOrDefault("reg_mail", "");
                        var mail2 = map.getOrDefault("reg_mail2", "");
                        var question = map.getOrDefault("reg_reminder_question", "");
                        var answer = map.getOrDefault("reg_reminder_answer", "");
                        var captcha = map.getOrDefault("reg_captcha", "");
                        var cid = map.getOrDefault("reg_cid", "");
                        var day = map.getOrDefault("reg_day", "");
                        var month = map.getOrDefault("reg_month", "");
                        var year = map.getOrDefault("reg_year", "");
                        var owner = map.getOrDefault("owner", "");
                        var pwd = map.getOrDefault("pwd", "");
                        var pwd2 = map.getOrDefault("pwd2", "");
                        var errorMessage = "";
                        var regSuccess = false;
                        var skin = map.getOrDefault("skin", "");
                        var ip = request.getLocalAddr();
                        String proxyIp = null;
                        if (conf.getString("use_proxy").equals("1")) {
                            proxyIp = request.getHeader(conf.getString("real_ip"));
                        }
                        if (proxyIp != null && !proxyIp.equals("")) {
                            ip = proxyIp;
                        }
                        String ipResolved = null;
                        try {
                            ipResolved = conf.getString("resolve_ip").equals("1") ? getByName(ip).getCanonicalHostName() : ip;
                        } catch (UnknownHostException uhe) {
                            ipResolved = ip;
                        }
                        if (!pwd.equals(pwd2)) {
                            errorMessage = getTemplate("reg_error_message_password_invalid", request, map);
                        } else if (!gender.equals("-") && !gender.equals("m") && !gender.equals("f")) {
                            errorMessage = getTemplate("reg_error_message_gender_invalid", request, map);
                        } else if (!nick.matches(conf.getString("allowed_chars"))) {
                            errorMessage = getTemplate("reg_error_message_nick_chars", request, map);
                        } else if ((conf.getInt("guest") == 1 && nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase()))) {
                            errorMessage = getTemplate("reg_error_message_nick_guest", request, map);
                        } else if (nick.length() < conf.getInt("min_nick_length")) {
                            errorMessage = getTemplate("reg_error_message_nick_short", request, map);
                        } else if (nick.length() > conf.getInt("max_nick_length")) {
                            errorMessage = getTemplate("reg_error_message_nick_long", request, map);
                        } else if (nickIsCommand(nick)) {
                            errorMessage = getTemplate("reg_error_message_nick_command", request, map);
                        } else if (!ut.isValidDate(day, month, year)) {
                            errorMessage = getTemplate("reg_error_message_date", request, map);
                        } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                            errorMessage = getTemplate("reg_error_message_age_old", request, map);
                        } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                            errorMessage = getTemplate("reg_error_message_age_young", request, map);
                        } else if (db.isRegistered(nick)) {
                            errorMessage = getTemplate("reg_error_message_nick_registered", request, map);
                        } else if (!mail.equals(mail2)) {
                            errorMessage = getTemplate("reg_error_message_mail_same", request, map);
                        } else if (!mail.contains("@")) {
                            errorMessage = getTemplate("reg_error_message_mail_invalid", request, map);
                        } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                            errorMessage = getTemplate("reg_error_message_mail_invalid", request, map);
                        } else {
                            regSuccess = true;
                        }
                        String rf = null;

                        if (!regSuccess) {
                            rf = getConsole("reg_form", request, map);
                            if (service.equals("reg_done")) {
                                rf = rf.replace("%reg_error_if_one%", getTemplate("reg_failed", request, map));
                                rf = rf.replace("%error_message%", errorMessage);
                            } else {
                                rf = rf.replace("%reg_error_if_one%", "");
                            }
                            cid = generateSid();
                        } else {
                            String color = null;
                            if (conf.getInt("random_color") == 1) {
                                color = ut.createRandomColor();
                            } else {
                                color = conf.getString("default_color");
                            }
                            db.addNick(gender, nick, mail, color, pwd, question, answer, day, month, year);
                            rf = getConsole("reg_form_success", request, map);
                        }
                        rf = rf.replace("%reg_nick%", nick);
                        rf = rf.replace("%reg_mail%", mail);
                        rf = rf.replace("%reg_cid%", cid);
                        rf = rf.replace("%reg_captcha%", captcha);
                        rf = rf.replace("%reg_reminder_question%", question);
                        rf = rf.replace("%reg_reminder_answer%", answer);
                        rf = rf.replace("%reg_day%", day);
                        rf = rf.replace("%reg_month%", month);
                        rf = rf.replace("%reg_year%", year);
                        rf = rf.replace("%skin%", skin);
                        rf = ut.parseGender(rf, gender);
                        ut.submitContent(rf, response);
                    } else {
                        text = getConsole("users_fail", request, map);
                        text = text.replace("%nick%", nick);
                    }
                } else {
                    text = getConsole("users", request, map);
                }
                ut.submitContent(text, response);
            } else {
                text = getConsole("404", request, map);
                ut.submitContent(text, response);
            }
        } else {
            response.setStatus(401);
            var text = "";
            text = getConsole("401", request, map);
            text = text.replace("%message%", filter.message);
            ut.submitContent(text, response);
        }
    }

    /**
     * Passwort&uuml;berpr&uuml;fungsformular
     *
     * @param out Die Output-Klasse
     */
    private void password(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var service = map.getOrDefault("service", "");
        var nick = map.getOrDefault("reg_nick", "");
        var mail = map.getOrDefault("reg_mail", "");
        var captcha = map.getOrDefault("reg_captcha", "");
        var cid = map.getOrDefault("reg_cid", "");
        var day = map.getOrDefault("reg_day", "");
        var month = map.getOrDefault("reg_month", "");
        var year = map.getOrDefault("reg_year", "");
        var owner = map.getOrDefault("owner", "");
        var skin = map.getOrDefault("skin", "");
        String roomName = "";
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var cm = Bootstrap.boot.getChatManager();
        var mailer = Bootstrap.boot.getSendMail();
        var db = conf.getDb();
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        var errorMessage = "";
        var regSuccess = false;
        if (!db.isRegistered(nick)) {
            errorMessage = getTemplate("reg_error_message_nick_not_registered", request, map);
        } else if (!db.getData(nick, "mail").equalsIgnoreCase(mail)) {
            errorMessage = getTemplate("reg_error_message_wrong_mail", request, map);
        } else if (!ut.isValidDate(day, month, year)) {
            errorMessage = getTemplate("reg_error_message_date", request, map);
        } else if (!db.getData(nick, "bday_day").equalsIgnoreCase(day) && !db.getData(nick, "bday_month").equalsIgnoreCase(month) && !db.getData(nick, "bday_year").equalsIgnoreCase(year)) {
            errorMessage = getTemplate("reg_error_message_date_wrong", request, map);
        } else if (!cm.isCorrectCaptcha(captcha.toLowerCase(), cid)) {
            errorMessage = getTemplate("reg_error_message_captcha", request, map);
        } else {
            regSuccess = true;
        }
        String rf = null;
        var pwd = ut.createRandomPassword();
        if (regSuccess) {
            try {
                var text = getMail("password", request, map);
                text = text.replace("%pwd%", pwd);
                text = text.replace("%nick%", nick);
                mailer.sendEmail(text, conf.getString("password_subject_" + skin), mail);
            } catch (MessagingException me) {
                errorMessage = me.getLocalizedMessage();
                regSuccess = false;
            }
        }
        if (!regSuccess) {
            if (roomName.isBlank()) {
                rf = getTemplate("pass_form", request, map);
            } else {
                rf = getTemplate("pass_form_napping", request, map);
            }
            if (service.equals("pass_done")) {
                rf = rf.replace("%reg_error_if_one%", getTemplate("pass_failed", request, map));
                rf = rf.replace("%error_message%", errorMessage);
            } else {
                rf = rf.replace("%reg_error_if_one%", "");
            }
            cid = generateSid();
        } else {
            if (roomName.isBlank()) {
                rf = getTemplate("pass_form_success", request, map);
            } else {
                rf = getTemplate("pass_form_success_napping", request, map);
            }
            rf = rf.replace("%reg_error_if_one%", "");
            db.updatePassword2(nick, pwd);
        }
        rf = rf.replace("%reg_nick%", nick);
        rf = rf.replace("%reg_mail%", mail);
        rf = rf.replace("%reg_cid%", cid);
        rf = rf.replace("%reg_captcha%", captcha);
        rf = rf.replace("%reg_day%", day);
        rf = rf.replace("%reg_month%", month);
        rf = rf.replace("%reg_year%", year);
        ut.submitContent(rf, response);
    }

    /**
     * Die Dereferer-Funktion
     *
     * @param out Die Output-Klasse
     */
    private void redirect(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var url = map.getOrDefault("url", "");
        if(!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("ftp://") && !url.startsWith("irc://") && !url.startsWith("mailto:")) {
            url = "about:blank";
        }
        var skin = map.getOrDefault("skin", "");
        var ut = Bootstrap.boot.getUtil();
        url = ut.decodeHtml(url);
        skin = ut.parseHost(skin, request)[1];
        var text = getTemplate("link", request, map);
        text = text.replace("%link%", ut.parseHtml(url));
        ut.submitContent(text, response);
    }

    /**
     * Zeigt die Topliste an!
     *
     * @param out Die Output-Klasse
     */
    private void profilePicture(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        OutputStream out = null;
        try {
            var nick = map.getOrDefault("nick", "");
            var co = Bootstrap.boot.getConfig();
            var db = co.getDb();
            var contentType = "image/gif";
            byte[] picture = null;
            if (!nick.equals("") && db.isRegistered(nick)) {
                picture = db.getPicture(nick);
                contentType = db.getData(nick, "image_url");
            }
            if (picture == null) {
                InputStream in = null;
                try {
                    var sb = new StringBuilder();
                    sb.append(co.getUh());
                    sb.append(co.getFs());
                    sb.append(".homeweb");
                    sb.append(co.getFs());
                    sb.append("pictures");
                    sb.append(co.getFs());
                    sb.append("notfound.gif");
                    var filename = sb.toString();
                    in = new BufferedInputStream(
                            new FileInputStream(filename));
                    var s = guessContentTypeFromStream(in);
                    response.setContentType(s);
                    picture = new byte[in.available()];
                    in.read(picture);
                } catch (FileNotFoundException ex) {
                } catch (IOException ex) {
                }
            }
            out = response.getOutputStream();
            out.write(picture);
        } catch (IOException ex) {
        }
    }

    /**
     * Zeigt die Topliste an!
     *
     * @param out Die Output-Klasse
     */
    private void toplist(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var skin = map.getOrDefault("skin", "");
        var text = getTemplate("toplist", request, map);
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        text = text.replace("%toplist%", db.getToplist(request, response, map));
        ut.submitContent(text, response);
    }

    /**
     * Zeigt die Topliste an!
     *
     * @param out Die Output-Klasse
     */
    private void logout(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var session = request.getSession(false);
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var db = conf.getDb();
        if (session != null) {
            String nick = (String) session.getAttribute("nick");
            if (cm.isOnline(nick.toLowerCase())) {
                if (db.isRegistered(nick)) {
                    printTemplate("logout_fail_reg", request, response, map);
                } else {
                    printTemplate("logout_fail", request, response, map);
                }
            } else {
                cm.getUsersCommunity().remove(nick);
                session.invalidate();
                printTemplate("logout", request, response, map);
            }
        } else {
            printTemplate("timeout", request, response, map);
        }
    }

    /**
     * Zeigt die Hilfe an!
     *
     * @param out Die Output-Klasse
     */
    private void help(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var skin = map.getOrDefault("skin", "");
        String nick = null;
        var sid = map.getOrDefault("sid", "");
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var db = conf.getDb();
        var ut = Bootstrap.boot.getUtil();
        var session = request.getSession(false);
        sid = (String) session.getAttribute("sid");
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        int status;
        if (cm.isValidConnectionId(sid)) {
            nick = cm.getNameFromId(sid);
            var u = cm.getUser(nick);
            status = u.getStatus();
        } else {
            status = 3;
        }
        if (status < 3) {
            status = 3;
        }
        var owner = map.getOrDefault("owner", "");
        String roomName = "";
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        String text = null;
        if (roomName.isBlank()) {
            text = getTemplate("help", request, map);
        } else {
            text = getTemplate("help_napping", request, map);
        }
        text = ut.parseHelp(text, status);
        ut.submitContent(text, response);
    }

    /**
     * Zeigt die Emoticons an
     *
     * @param out Die Output-Klasse
     */
    private void emot(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var skin = map.getOrDefault("skin", "");
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        skin = ut.parseHost(skin, request)[1];
        var db = conf.getDb();
        String text = null;
        try {
            var row = conf.getInt("emots_per_row");
            var cnt = 0;
            var owner = map.getOrDefault("owner", "");
            String roomName = "";
            if (!owner.isBlank()) {
                roomName = db.getRoomNameByOwner(owner.toLowerCase());
            }
            if (roomName.isBlank()) {
                text = getTemplate("emoticons", request, map);
            } else {
                text = getTemplate("emoticons_napping", request, map);
            }
            var p = db.getSmilies();
            var sb = new StringBuilder();
            sb.append("<table width=\"100%\">\r\n<tr>\r\n<td>\r\n");
            for (var key : p.keySet()) {
                sb.append(key);
                sb.append(": ");
                sb.append(p.getOrDefault(key, ""));
                cnt++;
                if (row == cnt) {
                    sb.append("\r\n</td>\r\n</tr>\r\n<tr>\r\n<td>\r\n");
                    cnt = 0;
                } else {
                    sb.append("\r\n</td>\r\n<td>\r\n");
                }
            }
            while (cnt != 0) {
                cnt++;
                if (row == cnt) {
                    sb.append("\r\n</td>\r\n</tr>\r\n");
                    break;
                } else {
                    sb.append("\r\n</td>\r\n<td>\r\n");
                }
            }
            sb.append("\r\n</table>\r\n");
            text = text.replace("%emot%", sb.toString());

        } catch (NullPointerException npe) {
            text = ut.getStackTrace(npe);
        }

        text = text.replace("%path_file%", conf.getString("path_file"));
        ut.submitContent(text, response);
    }

    /**
     * Zeigt das Profil an
     *
     * @param out Die Output-Klasse
     */
    private void userProfile(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var user = map.getOrDefault("user", "");
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        var sid = map.getOrDefault("sid", "");
        var skin = map.getOrDefault("skin", "");
        var owner = map.getOrDefault("owner", "");
        skin = ut.parseHost(skin, request)[1];
        var db = conf.getDb();
        var reg = db.isRegistered(user);
        String text = null;
        var session = request.getSession(false);
        sid = (String) session.getAttribute("sid");
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        var nick = (String) session.getAttribute("nick");
        nick = nick != null ? nick : map.getOrDefault("nick", "");
        var roomName = "";
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        if (!cm.isValidConnectionId(sid)) {
            if (!roomName.isBlank()) {
                text = getTemplate("profile_invalid_session_id", request, map);
            } else {
                text = getTemplate("profile_invalid_session_id_napping", request, map);
            }

            text = text.replace("%user%", user);
        } else if (!reg) {
            if (roomName.isBlank()) {
                text = getTemplate("profile_nick_not_registered", request, map);
            } else {
                text = getTemplate("profile_nick_not_registered_napping", request, map);
            }

            text = text.replace("%user%", user);
        } else {
            try {
                if (roomName.isBlank()) {
                    text = getTemplate("profile_user", request, map);
                } else {
                    text = getTemplate("profile_user_napping", request, map);
                }
                var buf = db.getData(user, "homepage");
                var prof = db.getProfile().getOrDefault("homepage", "");
                var replacement = "%PROFILE_[homepage]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%homepage%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "city");
                replacement = "%city%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, buf);
                } else {
                    text = text.replace(replacement, db.getProfile().getOrDefault("unknown", ""));
                }
                buf = db.getData(user, "hobby");
                prof = db.getProfile().getOrDefault("hobby", "");
                replacement = "%PROFILE_[hobby]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%hobby%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "status");
                prof = db.getProfile().getOrDefault("status", "");
                replacement = "%PROFILE_[status]%";
                var stat = db.getLongData(user, "status").intValue();
                if (stat >= 10) {
                    buf = db.getCommand("status_10");
                } else if (stat == 9) {
                    buf = db.getCommand("status_9");
                } else if (stat == 8) {
                    buf = db.getCommand("status_8");
                } else if (stat == 7) {
                    buf = db.getCommand("status_7");
                } else if (stat == 6) {
                    buf = db.getCommand("status_6");
                } else if (stat == 5) {
                    buf = db.getCommand("status_5");
                } else if (stat == 4) {
                    buf = db.getCommand("status_4");
                } else if (stat == 3) {
                    buf = db.getCommand("status_3");
                } else if (stat == 2) {
                    buf = db.getCommand("status_2");
                } else {
                    buf = db.getCommand("status_1");
                }
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%status%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "description");
                prof = db.getProfile().getOrDefault("description", "");
                replacement = "%PROFILE_[description]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%description%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "slogan");
                prof = db.getProfile().getOrDefault("slogan", "");
                replacement = "%PROFILE_[slogan]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%slogan%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "signature");
                prof = db.getProfile().getOrDefault("signature", "");
                replacement = "%PROFILE_[signature]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%signature%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getLongData(user, "icq") != 0 ? db.getData(user, "icq") : null;
                prof = db.getProfile().getOrDefault("icq", "");
                replacement = "%PROFILE_[icq]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%icq%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "live");
                prof = db.getProfile().getOrDefault("skype", "");
                replacement = "%PROFILE_[skype]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%live%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "yahoo");
                prof = db.getProfile().getOrDefault("yahoo", "");
                replacement = "%PROFILE_[yahoo]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%yahoo%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "facebook");
                prof = db.getProfile().getOrDefault("facebook", "");
                replacement = "%PROFILE_[facebook]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%facebook%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "twitter");
                prof = db.getProfile().getOrDefault("twitter", "");
                replacement = "%PROFILE_[twitter]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%twitter%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "irc");
                prof = db.getProfile().getOrDefault("irc", "");
                replacement = "%PROFILE_[irc]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%irc%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "youtube");
                prof = db.getProfile().getOrDefault("youtube", "");
                replacement = "%PROFILE_[youtube]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%youtube%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "visitors");
                prof = db.getProfile().getOrDefault("last_visitor", "");
                replacement = "%PROFILE_[last_visitor]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%visitors%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "fam_status");
                prof = db.getProfile().getOrDefault("fam_status", "");
                replacement = "%PROFILE_[fam_status]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%fam_status%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "image_url");
                prof = db.getProfile().getOrDefault("image", "");
                replacement = "%PROFILE_[image]%";
                if (buf != null && !buf.equals("")) {
                    text = text.replace(replacement, prof);
                    text = text.replace("%image_url%", buf);
                } else {
                    text = text.replace(replacement, "");
                }
                buf = db.getData(user, "sex");
                buf = switch (buf) {
                    case "m" ->
                        db.getProfile().getOrDefault("gender_m", "");
                    case "f" ->
                        db.getProfile().getOrDefault("gender_f", "");
                    default ->
                        db.getProfile().getOrDefault("gender_n", "");
                };
                text = text.replace("%sex%", buf);
                text = text.replace("%user%", db.getData(user, "nick2"));
                text = text.replace("%homepage%", db.getData(user, "homepage") == null ? "" : db.getData(user, "homepage"));
                text = text.replace("%points%", db.getData(user, "points"));
                text = text.replace("%timestamp_login%", ut.getTime(db.getLongData(user, "timestamp_login")));
                text = text.replace("%timestamp_reg%", ut.getTime(db.getLongData(user, "timestamp_reg")));
                text = text.replace("%bday_day%", db.getData(user, "bday_day") == null ? "" : db.getData(user, "bday_day"));
                text = text.replace("%bday_month%", db.getData(user, "bday_month") == null ? "" : db.getData(user, "bday_month"));
                text = text.replace("%bday_year%", db.getData(user, "bday_year") == null ? "" : db.getData(user, "bday_year"));
                text = text.replace("%login_room%", db.getData(user, "login_room") == null ? "" : db.getData(user, "login_room"));
                text = text.replace("%color%", db.getData(user, "color") == null ? "" : db.getData(user, "color"));
            } catch (NullPointerException npe) {
                text = ut.getStackTrace(npe);
            }
        }
        text = ut.replacePaths(text);
        text = ut.replaceFilePaths(text);
        text = ut.replaceServerInfo(text);
        text = text.replace("%host_ws%", "ws://" + request.getHeader("host"));
        text = text.replace("%host_http%", "http://" + request.getHeader("host"));
        text = text.replace("%owner%", owner);
        db.updateNick(user, "visitors", nick);
        ut.submitContent(text, response);
    }

    /**
     * Deinen Account verwalten
     *
     * @param out Die Output-Klasse
     */
    private void accountForm(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        String text = null;
        var service = map.getOrDefault("service", "");
        var data = map.getOrDefault("data", "");
        var sid = map.getOrDefault("sid", "");
        var skin = map.getOrDefault("skin", "");
        var owner = map.getOrDefault("owner", "");
        var roomName = "";
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }

        skin = ut.parseHost(skin, request)[1];
        var session = request.getSession(false);
        sid = (String) session.getAttribute("sid");
        skin = (String) session.getAttribute("skin");
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        skin = skin != null ? skin : map.getOrDefault("skin", "");
        var sidv = sid != null ? cm.isValidConnectionId(sid) : false;
        var reg = false;
        String nick = null;
        if (sidv) {
            nick = cm.getNameFromId(sid);
            reg = db.isRegistered(nick);
        }
        if (!sidv) {
            if (!roomName.isBlank()) {
                text = getTemplate("invalid_session_id2", request, map);
            } else {
                text = getTemplate("invalid_session_id2_napping", request, map);
            }
        } else if (!reg) {
            if (!roomName.isBlank()) {
                text = getTemplate("nick_not_registered", request, map);
            } else {
                text = getTemplate("nick_not_registered_napping", request, map);

            }
        } else if (service.equals("account")) {
            String homepage = null;
            String city = null;
            String hobby = null;
            String description = null;
            String slogan = null;
            String signature = null;
            String icq = null;
            String live = null;
            String yahoo = null;
            String facebook = null;
            String twitter = null;
            String irc = null;
            String youtube = null;
            String fam = null;
            switch (data) {
                case "" -> {
                    homepage = db.getData(nick, "homepage");
                    city = db.getData(nick, "city");
                    hobby = db.getData(nick, "hobby");
                    description = db.getData(nick, "description");
                    slogan = db.getData(nick, "slogan");
                    signature = db.getData(nick, "signature");
                    icq = db.getData(nick, "icq");
                    live = db.getData(nick, "live");
                    yahoo = db.getData(nick, "yahoo");
                    facebook = db.getData(nick, "facebook");
                    twitter = db.getData(nick, "twitter");
                    irc = db.getData(nick, "irc");
                    youtube = db.getData(nick, "youtube");
                    fam = db.getData(nick, "fam_status");
                    if (!roomName.isBlank()) {
                        text = getTemplate("account_form_profile", request, map);
                    } else {
                        text = getTemplate("account_form_profile_napping", request, map);
                    }
                    text = text.replace("%homepage%", homepage);
                    text = text.replace("%city%", city);
                    text = text.replace("%hobby%", hobby);
                    text = text.replace("%description%", description);
                    text = text.replace("%slogan%", slogan);
                    text = text.replace("%signature%", signature);
                    text = text.replace("%icq%", icq);
                    text = text.replace("%live%", live);
                    text = text.replace("%yahoo%", yahoo);
                    text = text.replace("%facebook%", facebook);
                    text = text.replace("%twitter%", twitter);
                    text = text.replace("%irc%", irc);
                    text = text.replace("%youtube%", youtube);
                    text = text.replace("%fam_status%", fam);
                }
                case "change" -> {
                    homepage = map.getOrDefault("homepage", "");
                    city = map.getOrDefault("city", "");
                    hobby = map.getOrDefault("hobby", "");
                    description = map.getOrDefault("description", "");
                    slogan = map.getOrDefault("slogan", "");
                    signature = map.getOrDefault("signature", "");
                    icq = map.getOrDefault("icq", "");
                    live = map.getOrDefault("live", "");
                    yahoo = map.getOrDefault("yahoo", "");
                    facebook = map.getOrDefault("facebook", "");
                    twitter = map.getOrDefault("twitter", "");
                    irc = map.getOrDefault("irc", "");
                    youtube = map.getOrDefault("youtube", "");
                    fam = map.getOrDefault("fam_status", "");
                    if (!roomName.isBlank()) {
                        text = getTemplate("account_form_profile_success", request, map);
                    } else {
                        text = getTemplate("account_form_profile_success_napping", request, map);
                    }
                    text = text.replace("%homepage%", homepage);
                    text = text.replace("%city%", city);
                    text = text.replace("%hobby%", hobby);
                    text = text.replace("%description%", description);
                    text = text.replace("%slogan%", slogan);
                    text = text.replace("%signature%", signature);
                    text = text.replace("%icq%", icq);
                    text = text.replace("%live%", live);
                    text = text.replace("%yahoo%", yahoo);
                    text = text.replace("%facebook%", facebook);
                    text = text.replace("%twitter%", twitter);
                    text = text.replace("%irc%", irc);
                    text = text.replace("%youtube%", youtube);
                    text = text.replace("%fam_status%", fam);
                    db.updateNick(nick, "homepage", homepage);
                    db.updateNick(nick, "city", city);
                    db.updateNick(nick, "hobby", hobby);
                    db.updateNick(nick, "description", description);
                    db.updateNick(nick, "slogan", slogan);
                    db.updateNick(nick, "signature", signature);
                    db.updateNick(nick, "icq", icq);
                    db.updateNick(nick, "live", live);
                    db.updateNick(nick, "yahoo", yahoo);
                    db.updateNick(nick, "facebook", facebook);
                    db.updateNick(nick, "twitter", twitter);
                    db.updateNick(nick, "irc", irc);
                    db.updateNick(nick, "youtube", youtube);
                    db.updateNick(nick, "fam_status", fam);
                }
                default -> {
                    if (roomName.isBlank()) {
                        text = getTemplate("account_form_unknown", request, map);
                    } else {
                        text = getTemplate("account_form_unknown_napping", request, map);
                    }
                }
            }
        } else if (service.equals("password")) {
            switch (data) {
                case "change" -> {
                    var oldPwd = map.getOrDefault("old_pwd", "");
                    var newPwd = map.getOrDefault("new_pwd", "");
                    var newPwd2 = map.getOrDefault("new_pwd2", "");
                    String errorMessage = null;
                    if (!db.checkPassword(nick, oldPwd)) {
                        errorMessage = getTemplate("account_error_message_pass_wrong", request, map);
                    } else if (newPwd.length() < conf.getInt("min_pwd_length")) {
                        errorMessage = getTemplate("account_error_message_pass_short", request, map);
                    } else if (newPwd.length() > conf.getInt("max_pwd_length")) {
                        errorMessage = getTemplate("account_error_message_pass_long", request, map);
                    } else if (!newPwd.equals(newPwd2)) {
                        errorMessage = getTemplate("account_error_message_pass_same", request, map);
                    }
                    if (errorMessage == null) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_password_success", request, map);
                        } else {
                            text = getTemplate("account_form_password_success_napping", request, map);
                        }

                        db.updatePassword(nick, newPwd);
                    } else {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_password", request, map);
                        } else {
                            text = getTemplate("account_form_password_napping", request, map);
                        }
                        text = text.replace("%account_error_if_one%", getTemplate("account_failed", request, map));
                        text = text.replace("%error_message%", errorMessage);
                    }
                }
                case "" -> {
                    if (roomName.isBlank()) {
                        text = getTemplate("account_form_password", request, map);
                    } else {
                        text = getTemplate("account_form_password_napping", request, map);
                    }
                }
                default -> {
                    if (roomName.isBlank()) {
                        text = getTemplate("account_form_unknown", request, map);
                    } else {
                        text = getTemplate("account_form_unknown_napping", request, map);
                    }
                }
            }
        } else if (service.equals("delete")) {
            switch (data) {
                case "delete" -> {
                    var pwd = map.getOrDefault("pwd", "");
                    var pwd2 = map.getOrDefault("pwd2", "");
                    String errorMessage = null;
                    if (!pwd.equals(pwd2)) {
                        errorMessage = getTemplate("account_error_message_pass_same", request, map);
                    } else if (!db.checkPassword(nick, pwd)) {
                        errorMessage = getTemplate("account_error_message_pass_wrong", request, map);
                    }
                    if (errorMessage == null) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_delete_success", request, map);
                        } else {
                            text = getTemplate("account_form_delete_success_napping", request, map);
                        }
                        cm.quit(nick);
                        cm.removeFriend(nick);
                        db.delFriendsFromList(nick);
                        db.delNick(nick);
                    } else {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_delete", request, map);
                        } else {
                            text = getTemplate("account_form_delete_napping", request, map);
                        }

                        text = text.replace("%account_error_if_one%", getTemplate("account_failed", request, map));
                        text = text.replace("%error_message%", errorMessage);
                    }
                }
                case "" -> {
                    if (roomName.isBlank()) {
                        text = getTemplate("account_form_delete", request, map);
                    } else {
                        text = getTemplate("account_form_delete_napping", request, map);
                    }
                }
                default -> {
                    if (roomName.isBlank()) {
                        text = getTemplate("account_form_unknown", request, map);
                    } else {
                        text = getTemplate("account_form_unknown_napping", request, map);
                    }
                }
            }
        } else if (service.equals("napping")) {
            String title = "";
            String bg_color_1 = "";
            String bg_color_2 = "";
            String color = "";
            String border_color = "";
            String link_color = "";
            String room = "";
            switch (data) {
                case "change" -> {
                    title = map.getOrDefault("title", "");
                    bg_color_1 = map.getOrDefault("bg_color_1", "");
                    bg_color_2 = map.getOrDefault("bg_color_2", "");
                    color = map.getOrDefault("color", "");
                    link_color = map.getOrDefault("link_color", "");
                    border_color = map.getOrDefault("border_color", "");
                    room = map.getOrDefault("room2", "");
                    if (bg_color_1.length() != 6 || !bg_color_1.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = getTemplate("account_form_napping_color_napping", request, map);
                        }
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (bg_color_2.length() != 6 || !bg_color_2.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = getTemplate("account_form_napping_color_napping", request, map);
                        }
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (border_color.length() != 6 || !border_color.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = getTemplate("account_form_napping_color_napping", request, map);
                        }
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (color.length() != 6 || !color.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = getTemplate("account_form_napping_color_napping", request, map);
                        }
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (link_color.length() != 6 || !link_color.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = getTemplate("account_form_napping_color_napping", request, map);
                        }
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (db.ownerExists(nick) && !db.getRoomNameByOwner(nick).equals(room)) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_used", request, map);
                        } else {
                            text = getTemplate("account_form_napping_used_napping", request, map);
                        }
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (db.roomExists(room) && db.getRoomData(room, "owner").equalsIgnoreCase(nick)) {
                        db.updateRoomData(room, "owner", nick);
                        db.updateRoomData(room, "first_bgcolor", bg_color_1);
                        db.updateRoomData(room, "second_bgcolor", bg_color_2);
                        db.updateRoomData(room, "bordercolor", border_color);
                        db.updateRoomData(room, "linkcolor", link_color);
                        db.updateRoomData(room, "textcolor", color);
                        db.updateRoomData(room, "page_title", title);
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_change", request, map);
                        } else {
                            text = getTemplate("account_form_napping_change_napping", request, map);
                        }
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (!db.roomExists(room)) {
                        db.addRoomData(nick, room, title, bg_color_1, bg_color_2, color, border_color, link_color);
                        db.updateRoomData(room, "su", nick);
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_add", request, map);
                        } else {
                            text = getTemplate("account_form_napping_add_napping", request, map);
                        }
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else {
                        text = getTemplate("account_form_napping_error", request, map);
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    }
                }
                case "" -> {
                    room = db.getRoomNameByOwner(nick.toLowerCase());
                    if (!room.isBlank()) {
                        title = db.getRoomData(room, "page_title");
                        bg_color_1 = db.getRoomData(room, "first_bgcolor");
                        bg_color_2 = db.getRoomData(room, "second_bgcolor");
                        color = db.getRoomData(room, "textcolor");
                        border_color = db.getRoomData(room, "bordercolor");
                        link_color = db.getRoomData(room, "linkcolor");
                        room = db.getRoomData(room, "room");
                    }
                    if (!map.getOrDefault("room", "").isBlank()) {
                        map.replace("room", map.getOrDefault("room", ""), room);
                    }
                    if (roomName.isBlank()) {
                        text = getTemplate("account_form_napping", request, map);
                    } else {
                        text = getTemplate("account_form_napping_napping", request, map);
                    }
                    text = text.replace("%title%", title);
                    text = text.replace("%bg_color_1%", bg_color_1);
                    text = text.replace("%bg_color_2%", bg_color_2);
                    text = text.replace("%color%", color);
                    text = text.replace("%link_color%", link_color);
                    text = text.replace("%border_color%", border_color);
                    text = text.replace("%room2%", room);
                    text = text.replace("%nick%", nick);
                }
                default -> {
                    if (roomName.isBlank()) {
                        text = getTemplate("account_form_unknown", request, map);
                    } else {
                        text = getTemplate("account_form_unknown_napping", request, map);
                    }
                }
            }
        } else if (service.equals("data")) {
            String errorMessage = null;
            String mail = null;
            String gender = null;
            String day = null;
            String month = null;
            String year = null;
            switch (data) {
                case "change" -> {
                    mail = map.getOrDefault("mail", "");
                    gender = map.getOrDefault("gender", "");
                    day = map.getOrDefault("day", "");
                    month = map.getOrDefault("month", "");
                    year = map.getOrDefault("year", "");
                    if (!gender.equals("-") && !gender.equals("m") && !gender.equals("f")) {
                        errorMessage = getTemplate("account_error_message_gender_invalid", request, map);
                    } else if (!ut.isValidDate(day, month, year)) {
                        errorMessage = getTemplate("account_error_message_date", request, map);
                    } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                        errorMessage = getTemplate("account_error_message_age_old", request, map);
                    } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                        errorMessage = getTemplate("account_error_message_age_young", request, map);
                    } else if (!mail.contains("@")) {
                        errorMessage = getTemplate("account_error_message_mail_invalid", request, map);
                    } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                        errorMessage = getTemplate("account_error_message_mail_invalid", request, map);
                    }
                    if (errorMessage == null) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_default_success", request, map);
                        } else {
                            text = getTemplate("account_form_default_success_napping", request, map);
                        }
                        db.updateNick(nick, "sex", gender);
                        db.updateNick(nick, "bday_day", day);
                        db.updateNick(nick, "bday_month", month);
                        db.updateNick(nick, "bday_year", year);
                        db.updateNick(nick, "mail", mail);
                    } else {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_default", request, map);
                        } else {
                            text = getTemplate("account_form_default_napping", request, map);
                        }
                        text = ut.parseGender(text, gender);
                        text = text.replace("%account_error_if_one%", getTemplate("account_failed", request, map));
                        text = text.replace("%error_message%", errorMessage);
                        text = text.replace("%mail%", mail);
                        text = text.replace("%day%", day);
                        text = text.replace("%month%", month);
                        text = text.replace("%year%", year);
                    }
                }
                case "" -> {
                    mail = db.getData(nick, "mail");
                    gender = db.getData(nick, "sex");
                    day = db.getData(nick, "bday_day");
                    month = db.getData(nick, "bday_month");
                    year = db.getData(nick, "bday_year");
                    mail = mail == null ? "" : mail;
                    gender = gender == null ? "" : gender;
                    day = day == null ? "" : day;
                    month = month == null ? "" : month;
                    year = year == null ? "" : year;
                    if (roomName.isBlank()) {
                        text = getTemplate("account_form_default", request, map);
                    } else {
                        text = getTemplate("account_form_default_napping", request, map);
                    }
                    text = ut.parseGender(text, gender);
                    text = text.replace("%mail%", mail);
                    text = text.replace("%day%", day);
                    text = text.replace("%month%", month);
                    text = text.replace("%year%", year);
                }
                default -> {
                    if (roomName.isBlank()) {
                        text = getTemplate("account_form_unknown", request, map);
                    } else {
                        text = getTemplate("account_form_unknown_napping", request, map);
                    }
                }
            }
        } else if (service.equals("picture")) {
            if (roomName.isBlank()) {
                text = getTemplate("picture", request, map);
            } else {
                text = getTemplate("picture_napping", request, map);
            }
        } else if (service.equals("picture_del")) {
            switch (data) {
                case "delete":
                    db.updateNick(nick, "image_upload", null);
                    db.updateNick(nick, "image_url", null);
                    if (roomName.isBlank()) {
                        text = getTemplate("picture_del_success", request, map);
                    } else {
                        text = getTemplate("picture_del_success_napping", request, map);
                    }
                    break;
                case "":
                    if (roomName.isBlank()) {
                        text = getTemplate("picture_del", request, map);
                    } else {
                        text = getTemplate("picture_del_napping", request, map);
                    }
                    break;
                default:
                    if (roomName.isBlank()) {
                        text = getTemplate("picture_del_unkown", request, map);
                    } else {
                        text = getTemplate("picture_del_unkown_napping", request, map);
                    }
                    break;
            }

        } else if (!service.equals("")) {
            if (roomName.isBlank()) {
                text = getTemplate("account_form_unknown", request, map);
            } else {
                text = getTemplate("account_form_unknown_napping", request, map);
            }
        } else {
            if (roomName.isBlank()) {
                text = getTemplate("account_form", request, map);
            } else {
                text = getTemplate("account_form_napping2", request, map);
            }
        }
        text = text.replace("%account_error_if_one%", "");
        ut.submitContent(text, response);
    }

    /*
         * Deinen Account verwalten
     *
     * @param out Die Output-Klasse
     */
    private void board(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var session = request.getSession(false);
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        TreeMap<String, UsersCommunity> com = cm.getUsersCommunity();
        if (session == null || com.get((String) session.getAttribute("nick")) == null) {
            printTemplate("timeout", request, response, map);
            return;
        }
        var db = conf.getDb();
        var nick = (String) session.getAttribute("nick");
        var moderator = db.getData(nick, "moderator").equals("1");
        com.get((String) session.getAttribute("nick")).setTimeoutTimer(0);
        String text = null;
        var service = map.getOrDefault("service", "");
        var data = map.getOrDefault("data", "");
        var skin = (String) session.getAttribute("skin");

        var sid = session.getId();
        skin = skin != null ? skin : map.getOrDefault("skin", "");
        skin = ut.parseHost(skin, request)[1];
        var reg = false;
        reg = db.isRegistered(nick);
        var bid = map.getOrDefault("id", "0");
        if (!bid.matches("\\d+")) {
            bid = "0";
        }
        var pid = map.getOrDefault("pid", "0");
        if (!pid.matches("\\d+")) {
            pid = "0";
        }
        var ref = map.getOrDefault("ref", "0");
        if (!ref.matches("\\d+")) {
            ref = "0";
        }
        var start = map.getOrDefault("new", "");
        var addBoard = map.getOrDefault("add_board", "");
        var addCat = map.getOrDefault("add_cat", "");
        var delCat = map.getOrDefault("del_cat", "");
        var editCat = map.getOrDefault("edit_cat", "");
        var delBoards = map.getOrDefault("delete_boards", "");
        var closeBoards = map.getOrDefault("close_boards", "");
        var editBoards = map.getOrDefault("edit_boards", "");
        var close = map.getOrDefault("close", "");
        var open = map.getOrDefault("open", "");
        var del = map.getOrDefault("del", "");
        var undel = map.getOrDefault("undel", "");
        var ban = map.getOrDefault("ban", "");
        var answer = map.getOrDefault("answer", "");
        var page = map.getOrDefault("p", "1");
        if (!page.matches("\\d+")) {
            page = "1";
        }
        var search = map.getOrDefault("search", "");
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
            realIp = conf.getString("resolve_ip").equals("1") ? getByName(realIp).getCanonicalHostName() : realIp;
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
            ipResolved = conf.getString("resolve_ip").equals("1") ? getByName(ip).getCanonicalHostName() : ip;
        } catch (UnknownHostException uhe) {
            ipResolved = ip;
        }
        try {
            ipResolved2 = conf.getString("resolve_ip").equals("1") ? getByName(realIp).getCanonicalHostName() : realIp;
        } catch (UnknownHostException uhe) {
            ipResolved2 = realIp;
        }
        if (db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(nick));
            printTemplate("banned", request, response, map);
        } else if (db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ip));
            printTemplate("banned", request, response, map);
        } else if (db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ipResolved));
            printTemplate("banned", request, response, map);
        } else if (db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ipResolved2));
            printTemplate("banned", request, response, map);
        } else if (db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(realIp));
            printTemplate("banned", request, response, map);
        } else if (db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(nick));
            printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(ip));
            printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(ipResolved));
            printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(ipResolved2));
            printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(realIp));
            printTemplate("banned_temp", request, response, map);
        } else if (!reg) {
            text = getTemplate("nick_not_registered_com", request, map);
        } else if (!service.isBlank()) {
            text = getTemplate("board_unknown", request, map);
        } else if (!ban.isBlank()) {
            var uid = map.getOrDefault("uid", "0");
            if (!uid.matches("\\d+")) {
                uid = "0";
            }
            var target = db.getData(Long.valueOf(uid), "nick");
            var target2 = db.getData(Long.valueOf(uid), "nick2");
            var color = db.getData(target, "color");
            var banned = db.isBanned(target);
            if (!moderator) {
                text = getTemplate("boards_moderated", request, map);
            } else if (target.isBlank()) {
                text = getTemplate("boards_no_user", request, map);
            } else if (banned) {
                db.delBans(nick, target2);
                text = getTemplate("boards_unban", request, map);
            } else {
                var cmd = Bootstrap.boot.getCommands();
                if (cm.isOnline(nick)) {
                    cmd.ban(target + " " + conf.getString("default_ban_reason_board"), nick);
                } else {
                    db.addBan(target2, conf.getString("default_ban_reason_board"), nick);
                }
                text = getTemplate("boards_ban", request, map);
            }
            text = text.replace("%color%", color);
            text = text.replace("%nick%", nick);
            text = text.replace("%user%", db.getData(target, "nick2"));
        } else if (!undel.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = getTemplate("boards_locked", request, map);
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "deleted", "0");
                text = getTemplate("boards_thread_undel", request, map);
            } else {
                text = getTemplate("boards_thread_moderator", request, map);
            }
        } else if (!del.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = getTemplate("boards_locked", request, map);
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "deleted", "1");
                text = getTemplate("boards_thread_del", request, map);
            } else {
                text = getTemplate("boards_thread_moderator", request, map);
            }
        } else if (!open.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = getTemplate("boards_locked", request, map);
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "closed", "0");
                text = getTemplate("boards_thread_opened", request, map);
            } else {
                text = getTemplate("boards_thread_moderator", request, map);
            }
        } else if (!close.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = getTemplate("boards_locked", request, map);
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "closed", "1");
                text = getTemplate("boards_thread_closed", request, map);
            } else {
                text = getTemplate("boards_thread_moderator", request, map);
            }
        } else if (!answer.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = getTemplate("boards_locked", request, map);
            } else {
                ref = db.getBoard(Long.valueOf(pid), "ref");
                db.addThread(map.getOrDefault("topic", ""), map.getOrDefault("thread", ""), Long.valueOf(ref), Long.valueOf(db.getData(nick, "id")), 0, ip, Long.valueOf(bid));
                text = getTemplate("boards_added", request, map);
                text = text.replace("%id%", bid);
                pid = String.valueOf(db.getBoardId());
                text = text.replace("%pid%", pid);
            }
        } else if (!editBoards.isBlank()) {
            var edited = map.getOrDefault("edited", "").equalsIgnoreCase("true");
            if (!moderator) {
                text = getTemplate("boards_locked", request, map);
            } else {
                if (edited) {
                    var topic = map.getOrDefault("topic", "");
                    var description = map.getOrDefault("description", "");
                    db.updateBoards(Long.valueOf(bid), "topic", topic);
                    db.updateBoards(Long.valueOf(bid), "description", description);
                    text = getTemplate("boards_edit", request, map);
                    text = getTemplate("boards_edited", request, map);
                } else {
                    var topic = db.getBoards(Long.valueOf(bid), "topic");
                    var description = db.getBoards(Long.valueOf(bid), "description");
                    text = getTemplate("boards_edit", request, map);
                    text = text.replace("%id%", bid);
                    text = text.replace("%topic%", ut.parseHtml(topic));
                    text = text.replace("%description%", ut.parseHtml(description));
                }
            }
        } else if (!delBoards.isBlank()) {
            var deleted = db.getBoards(Long.valueOf(bid), "deleted").equals("1");
            if (!moderator) {
                text = getTemplate("boards_locked", request, map);
            } else {
                if (deleted) {
                    db.updateBoards(Long.valueOf(bid), "deleted", "0");
                    text = getTemplate("boards_undel", request, map);
                    text = text.replace("%id%", bid);
                    text = text.replace("%pid%", pid);
                } else {
                    db.updateBoards(Long.valueOf(bid), "deleted", "1");
                    text = getTemplate("boards_deleted", request, map);
                    text = text.replace("%id%", bid);;
                    text = text.replace("%pid%", pid);
                }
            }
        } else if (!closeBoards.isBlank()) {
            var closed = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (!moderator) {
                text = getTemplate("boards_locked", request, map);
            } else {
                if (closed) {
                    db.updateBoards(Long.valueOf(bid), "readonly", "0");
                    text = getTemplate("boards_open", request, map);
                    text = text.replace("%id%", bid);
                    text = text.replace("%pid%", pid);
                } else {
                    db.updateBoards(Long.valueOf(bid), "readonly", "1");
                    text = getTemplate("boards_close", request, map);
                    text = text.replace("%id%", bid);;
                    text = text.replace("%pid%", pid);
                }
            }
        } else if (!pid.equals("0")) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            ref = db.getBoard(Long.valueOf(pid), "ref");
            if (!ref.matches("\\d+")) {
                ref = "0";
            }
            if (ref.equals("0")) {
                text = getTemplate("boards_not_exists", request, map);
            } else {
                if (db.getBoard(Long.valueOf(ref), "closed").equals("1")) {
                    text = getTemplate("boards3", request, map);
                } else if (locked && !moderator) {
                    text = getTemplate("boards3", request, map);
                } else {
                    text = getTemplate("boards2", request, map);
                }
                var count = db.countBoardTreads3(Long.valueOf(ref));
                ArrayList<Object[]> board = db.getBoardFromIdStripped(Long.valueOf(ref), Long.valueOf(page));
                StringBuilder sb = new StringBuilder();
                String[] topic = new String[1];
                long[] id = new long[1];
                id[0] = 0;
                long pages = (long) Math.ceil((float) count / conf.getLong("board_pages"));
                StringBuilder pageContent = new StringBuilder();
                for (long l = 1; l <= pages; l++) {
                    if (l != Long.valueOf(page)) {
                        var content = getTemplate("boards_page", request, map);
                        content = content.replace("%page%", String.valueOf(l));
                        content = content.replace("%pid%", pid);
                        content = content.replace("%id%", bid);
                        pageContent.append(content);
                        pageContent.append(" ");
                    } else {
                        var content = getTemplate("boards_page_active", request, map);
                        content = content.replace("%page%", String.valueOf(l));
                        content = content.replace("%pid%", pid);
                        content = content.replace("%id%", bid);
                        pageContent.append(content);
                        pageContent.append(" ");
                    }
                }
                if (page.equals("1")) {
                    text = text.replace("%prev%", getTemplate("boards_page_prev_disabled", request, map));
                } else {
                    var content = getTemplate("boards_page_prev_enabled", request, map);
                    content = content.replace("%page%", String.valueOf(Long.valueOf(page) - 1));
                    content = content.replace("%pid%", pid);
                    content = content.replace("%id%", bid);
                    text = text.replace("%prev%", content);
                }
                if (Long.valueOf(page) == pages) {
                    text = text.replace("%next%", getTemplate("boards_page_next_disabled", request, map));
                } else {
                    var content = getTemplate("boards_page_next_enabled", request, map);
                    content = content.replace("%page%", String.valueOf(Long.valueOf(page) + 1));
                    content = content.replace("%pid%", pid);
                    content = content.replace("%id%", bid);
                    text = text.replace("%next%", content);
                }
                text = text.replace("%pages%", pageContent.toString());
                text = text.replace("%count%", String.valueOf(count));
                topic[0] = "";
                board.forEach((boards) -> {
                    if ((Long) boards[10] == 0) {
                        var brd = getTemplate("boards_thread", request, map);
                        topic[0] = String.valueOf(boards[0]);
                        brd = brd.replace("%topic%", ut.parseHtml(String.valueOf(boards[0])));
                        var content = String.valueOf(boards[1]);
                        brd = brd.replace("%content%", ut.parseBb(ut.parseHtml(content)));
                        brd = brd.replace("%ref%", String.valueOf(boards[2]));
                        var user = db.getData((Long) boards[3], "nick2");
                        var signature = ut.parseBb(ut.parseHtml(db.getData((Long) boards[3], "signature")));
                        if (moderator) {
                            brd = brd.replace("%delete%", getTemplate("boards_thread_delete", request, map));
                            if (boards[2].equals(boards[8]) && String.valueOf(boards[9]).equals("0")) {
                                brd = brd.replace("%close%", getTemplate("boards_thread_close", request, map));
                            } else if (String.valueOf(boards[9]).equals("1")) {
                                brd = brd.replace("%close%", getTemplate("boards_thread_open", request, map));
                            } else {
                                brd = brd.replace("%close%", "");
                            }
                            if (db.isBanned(user)) {
                                brd = brd.replace("%ban%", getTemplate("boards_thread_unban", request, map));
                            } else {
                                brd = brd.replace("%ban%", getTemplate("boards_thread_ban", request, map));
                            }
                        } else {
                            brd = brd.replace("%delete%", "");
                            brd = brd.replace("%close%", "");
                            brd = brd.replace("%ban%", "");
                        }
                        if (user.equalsIgnoreCase(nick) || moderator) {
                            brd = brd.replace("%edit%", getTemplate("boards_thread_edit", request, map));
                        } else {
                            brd = brd.replace("%edit%", "");
                        }
                        brd = brd.replace("%uid%", db.getData(user, "id"));
                        brd = brd.replace("%user%", user);
                        brd = brd.replace("%color%", db.getData(user, "color"));
                        brd = brd.replace("%board%", String.valueOf(boards[4]));
                        brd = brd.replace("%posted%", ut.getTime((Long) boards[5]));
                        brd = brd.replace("%ip%", String.valueOf(boards[6]));
                        brd = brd.replace("%cat%", String.valueOf(boards[7]));
                        brd = brd.replace("%id%", String.valueOf(boards[8]));
                        brd = brd.replace("%closed%", String.valueOf(boards[9]));
                        brd = brd.replace("%deleted%", String.valueOf(boards[10]));
                        brd = brd.replace("%signature%", signature);
                        if (db.isBanned(user)) {
                            brd = brd.replace("%banned%", getTemplate("boards_thread_banned", request, map));
                        } else {
                            brd = brd.replace("%banned%", "");
                        }
                        id[0] = (Long) boards[7];
                        sb.append(brd);
                    } else {
                        var brd = getTemplate("boards_thread_deleted", request, map);
                        topic[0] = String.valueOf(boards[0]);
                        brd = brd.replace("%topic%", ut.parseHtml(String.valueOf(boards[0])));
                        var content = String.valueOf(boards[1]);
                        brd = brd.replace("%content%", ut.parseBb(ut.parseHtml(content)));
                        brd = brd.replace("%ref%", String.valueOf(boards[2]));
                        var user = db.getData((Long) boards[3], "nick2");
                        if (db.getData(user, "moderator").equals("1")) {
                            brd = brd.replace("%delete%", getTemplate("boards_thread_undelete", request, map));
                        } else {
                            brd = brd.replace("%delete%", "");
                        }
                        brd = brd.replace("%user%", user);
                        brd = brd.replace("%color%", db.getData(user, "color"));
                        brd = brd.replace("%board%", String.valueOf(boards[4]));
                        brd = brd.replace("%posted%", ut.getTime((Long) boards[5]));
                        brd = brd.replace("%ip%", String.valueOf(boards[6]));
                        brd = brd.replace("%cat%", String.valueOf(boards[7]));
                        brd = brd.replace("%id%", String.valueOf(boards[8]));
                        brd = brd.replace("%closed%", String.valueOf(boards[9]));
                        brd = brd.replace("%deleted%", String.valueOf(boards[10]));
                        if (db.isBanned(user)) {
                            brd = brd.replace("%banned%", getTemplate("boards_thread_banned", request, map));
                        } else {
                            brd = brd.replace("%banned%", "");
                        }
                        id[0] = (Long) boards[7];
                        sb.append(brd);
                    }
                });
                if (sb.toString().isBlank()) {
                    text = text.replace("%boards%", getTemplate("boards_empty2", request, map));
                } else {
                    text = text.replace("%boards%", sb.toString());

                }
                text = text.replace("%id%", String.valueOf(id[0]));
                text = text.replace("%pid%", pid);
                text = text.replace("%re%", topic[0].startsWith(conf.getString("board_re")) ? "" : conf.getString("board_re"));
                text = text.replace("%topic%", ut.parseHtml(topic[0]));
            }
        } else if (!search.isBlank()) {
            ArrayList<Object[]> board = db.searchBoard(Long.valueOf(ref), Long.valueOf(page), search);
            var count = db.countBoardTreads3(search);
            StringBuilder sb = new StringBuilder();
            String[] topic = new String[1];
            long[] id = new long[1];
            id[0] = 0;
            long pages = (long) Math.ceil((float) count / conf.getLong("board_pages"));
            text = getTemplate("boards3", request, map);
            StringBuilder pageContent = new StringBuilder();
            for (long l = 1; l <= pages; l++) {
                if (l != Long.valueOf(page)) {
                    var content = getTemplate("boards_page", request, map);
                    content = content.replace("%page%", String.valueOf(l));
                    content = content.replace("%pid%", pid);
                    content = content.replace("%id%", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                } else {
                    var content = getTemplate("boards_page_active", request, map);
                    content = content.replace("%page%", String.valueOf(l));
                    content = content.replace("%pid%", pid);
                    content = content.replace("%id%", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                }
            }
            if (page.equals("1")) {
                text = text.replace("%prev%", getTemplate("boards_page_prev_disabled", request, map));
            } else {
                var content = getTemplate("boards_page_prev_enabled", request, map);
                content = content.replace("%page%", String.valueOf(Long.valueOf(page) - 1));
                content = content.replace("%pid%", pid);
                content = content.replace("%id%", bid);
                text = text.replace("%prev%", content);
            }
            if (Long.valueOf(page) == pages) {
                text = text.replace("%next%", getTemplate("boards_page_next_disabled", request, map));
            } else {
                var content = getTemplate("boards_page_next_enabled", request, map);
                content = content.replace("%page%", String.valueOf(Long.valueOf(page) + 1));
                content = content.replace("%pid%", pid);
                content = content.replace("%id%", bid);
                text = text.replace("%next%", content);
            }
            text = text.replace("%pages%", pageContent.toString());
            text = text.replace("%count%", String.valueOf(count));
            topic[0] = "";
            board.forEach((boards) -> {
                if ((Long) boards[10] == 0) {
                    var brd = getTemplate("boards_thread", request, map);
                    topic[0] = String.valueOf(boards[0]);
                    brd = brd.replace("%topic%", ut.parseHtml(String.valueOf(boards[0])));
                    var content = String.valueOf(boards[1]);
                    brd = brd.replace("%content%", ut.parseBb(ut.parseHtml(content)));
                    brd = brd.replace("%ref%", String.valueOf(boards[2]));
                    var user = db.getData((Long) boards[3], "nick2");
                    if (user.equalsIgnoreCase(nick) || moderator) {
                        brd = brd.replace("%edit%", getTemplate("boards_thread_edit", request, map));
                    } else {
                        brd = brd.replace("%edit%", "");
                    }
                    if (moderator) {
                        if (db.isBanned(user)) {
                            brd = brd.replace("%ban%", getTemplate("boards_thread_unban", request, map));
                        } else {
                            brd = brd.replace("%ban%", getTemplate("boards_thread_ban", request, map));
                        }
                    } else {
                        brd = brd.replace("%ban%", "");
                    }
                    var signature = ut.parseBb(ut.parseHtml(db.getData((Long) boards[3], "signature")));
                    brd = brd.replace("%delete%", "");
                    brd = brd.replace("%close%", "");
                    brd = brd.replace("%user%", user);
                    brd = brd.replace("%color%", db.getData(user, "color"));
                    brd = brd.replace("%uid%", String.valueOf(boards[3]));
                    brd = brd.replace("%board%", String.valueOf(boards[4]));
                    brd = brd.replace("%posted%", ut.getTime((Long) boards[5]));
                    brd = brd.replace("%ip%", String.valueOf(boards[6]));
                    brd = brd.replace("%cat%", String.valueOf(boards[7]));
                    brd = brd.replace("%id%", String.valueOf(boards[8]));
                    brd = brd.replace("%closed%", String.valueOf(boards[9]));
                    brd = brd.replace("%deleted%", String.valueOf(boards[10]));
                    brd = brd.replace("%signature%", signature);
                    if (db.isBanned(user)) {
                        brd = brd.replace("%banned%", getTemplate("boards_thread_banned", request, map));
                    } else {
                        brd = brd.replace("%banned%", "");
                    }
                    id[0] = (Long) boards[7];
                    sb.append(brd);
                } else {
                    var brd = getTemplate("boards_thread_deleted", request, map);
                    topic[0] = String.valueOf(boards[0]);
                    brd = brd.replace("%topic%", ut.parseHtml(String.valueOf(boards[0])));
                    var content = String.valueOf(boards[1]);
                    brd = brd.replace("%content%", ut.parseBb(ut.parseHtml(content)));
                    brd = brd.replace("%ref%", String.valueOf(boards[2]));
                    var user = db.getData((Long) boards[3], "nick2");
                    if (db.getData(user, "moderator").equals("1")) {
                        brd = brd.replace("%delete%", getTemplate("boards_thread_undelete", request, map));
                    } else {
                        brd = brd.replace("%delete%", "");
                    }
                    brd = brd.replace("%user%", user);
                    brd = brd.replace("%color%", db.getData(user, "color"));
                    brd = brd.replace("%board%", String.valueOf(boards[4]));
                    brd = brd.replace("%posted%", ut.getTime((Long) boards[5]));
                    brd = brd.replace("%ip%", String.valueOf(boards[6]));
                    brd = brd.replace("%cat%", String.valueOf(boards[7]));
                    brd = brd.replace("%id%", String.valueOf(boards[8]));
                    brd = brd.replace("%closed%", String.valueOf(boards[9]));
                    brd = brd.replace("%deleted%", String.valueOf(boards[10]));
                    if (db.isBanned(user)) {
                        brd = brd.replace("%banned%", getTemplate("boards_thread_banned", request, map));
                    } else {
                        brd = brd.replace("%banned%", "");
                    }
                    id[0] = (Long) boards[7];
                    sb.append(brd);
                }
            });
            if (sb.toString().isBlank()) {
                text = text.replace("%boards%", getTemplate("boards_empty2", request, map));
            } else {
                text = text.replace("%boards%", sb.toString());

            }
            text = text.replace("%id%", String.valueOf(id[0]));
            text = text.replace("%pid%", pid);
            text = text.replace("%re%", topic[0].startsWith(conf.getString("board_re")) ? "" : conf.getString("board_re"));
            text = text.replace("%topic%", ut.parseHtml(topic[0]));
        } else if (!start.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");

            if (locked && !moderator) {
                text = getTemplate("boards_locked", request, map);
            } else {
                var add = map.getOrDefault("add", "");
                if (add.isBlank()) {
                    text = getTemplate("boards_add", request, map);
                    text = text.replace("%id%", bid);
                } else {
                    db.addThread(map.getOrDefault("topic", ""), map.getOrDefault("thread", ""), 0, Long.valueOf(db.getData(nick, "id")), 0, ip, Long.valueOf(bid));
                    text = getTemplate("boards_added", request, map);
                    pid = String.valueOf(db.getBoardId());
                    db.updateThread(Long.valueOf(pid), "ref", pid);
                    text = text.replace("%id%", bid);
                    text = text.replace("%pid%", pid);
                }
            }
        } else if (!delCat.isBlank()) {
            if (!moderator) {
                text = getTemplate("board_add_moderator", request, map);
            } else {
                var cat = map.getOrDefault("cat", "");
                var deleted = db.getBoardCat(Long.valueOf(cat), "deleted");
                if (deleted.equals("0")) {
                    db.updateBoardCats(Long.valueOf(cat), "deleted", "1");
                    text = getTemplate("board_deleted_board", request, map);
                } else {
                    db.updateBoardCats(Long.valueOf(cat), "deleted", "0");
                    text = getTemplate("board_opened_board", request, map);
                }
            }
        } else if (!editCat.isBlank()) {
            if (!moderator) {
                text = getTemplate("board_add_moderator", request, map);
            } else {
                var topic = map.getOrDefault("topic", "");
                var description = map.getOrDefault("description", "");
                var cat = map.getOrDefault("cat", "");
                if (topic.isBlank()) {
                    text = getTemplate("board_edit_board", request, map);
                    text = text.replace("%cat%", cat);
                    text = text.replace("%topic%", ut.parseHtml(db.getBoardCat(Long.valueOf(cat), "topic")));
                    text = text.replace("%description%", ut.parseHtml(db.getBoardCat(Long.valueOf(cat), "description")));
                } else {
                    db.updateBoardCats(Long.valueOf(cat), "topic", topic);
                    db.updateBoardCats(Long.valueOf(cat), "description", description);
                    text = getTemplate("board_edited_board", request, map);
                }
            }
        } else if (!addCat.isBlank()) {
            if (!moderator) {
                text = getTemplate("board_add_moderator", request, map);
            } else {
                var topic = map.getOrDefault("topic", "");
                var description = map.getOrDefault("description", "");
                var cat = map.getOrDefault("cat", "");
                if (topic.isBlank()) {
                    text = getTemplate("board_add_board", request, map);
                    text = text.replace("%cat%", cat);
                } else {
                    db.addBoards(topic, description, Long.valueOf(cat));
                    text = getTemplate("board_added_board", request, map);
                }
            }
        } else if (!addBoard.isBlank()) {
            if (!moderator) {
                text = getTemplate("board_add_moderator", request, map);
            } else {
                var topic = map.getOrDefault("topic", "");
                var description = map.getOrDefault("description", "");
                if (topic.isBlank()) {
                    text = getTemplate("board_add", request, map);
                } else {
                    db.addBoardCat(topic, description);
                    text = getTemplate("board_added", request, map);
                }
            }
        } else if (bid.equals("0")) {
            if (!moderator) {
                text = getTemplate("board", request, map);
            } else {
                text = getTemplate("board_moderated", request, map);
            }
            TreeMap<Long, Object[]> cat = db.getBoardCat();
            var category = "";
            for (Object[] cats : cat.values()) {
                var id = String.valueOf(cats[0]);
                var topic = String.valueOf(cats[1]);
                var description = String.valueOf(cats[2]);
                var deleted = (Integer) cats[3];
                if (moderator) {
                    category = category + getTemplate("board_cats_moderated2", request, map);
                    if (deleted == 0) {
                        category = category.replace("%delete%", getTemplate("board_cats_del", request, map));
                    } else {
                        category = category.replace("%delete%", getTemplate("board_cats_open", request, map));
                    }
                    category = category.replace("%edit%", getTemplate("board_cats_edit", request, map));
                } else {
                    if (deleted == 1) {
                        continue;
                    }
                    category = category + getTemplate("board_cats", request, map);
                    category = category.replace("%delete%", "");
                    category = category.replace("%edit%", "");
                }
                category = category.replace("%id%", id);
                category = category.replace("%topic%", ut.parseHtml(topic));
                category = category.replace("%description%", ut.parseBb(ut.parseHtml(description)));
                TreeMap<Long, Object[]> board = db.getBoards(Long.valueOf(id));
                var in = "";
                var locked = false;
                for (Object[] boards : board.values()) {
                    locked = String.valueOf(boards[2]).equals("1");
                    var deleted2 = String.valueOf(boards[6]).equals("1");
                    if (!moderator) {
                        if (deleted2) {
                            continue;
                        }
                        in = in + getTemplate("board_boards", request, map);

                    } else {
                        in = in + getTemplate("board_boards_moderated", request, map);
                        if (!deleted2) {
                            in = in.replace("%del%", getTemplate("board_boards_moderated_del", request, map));
                        } else {
                            in = in.replace("%del%", getTemplate("board_boards_moderated_undel", request, map));
                        }
                        in = in.replace("%edit%", getTemplate("board_boards_moderated_edit", request, map));
                        var readonly = String.valueOf(boards[2]).equals("1");
                        if (readonly) {
                            in = in.replace("%lock%", getTemplate("board_boards_moderated_open", request, map));
                        } else {
                            in = in.replace("%lock%", getTemplate("board_boards_moderated_close", request, map));
                        }

                    }

                    in = in.replace("%id%", String.valueOf(boards[5]));
                    in = in.replace("%cat%", String.valueOf(boards[0]));
                    in = in.replace("%topics%", String.valueOf(db.countBoardTreads2((Long) boards[5])));
                    in = in.replace("%threads%", String.valueOf(db.countBoardTreads((Long) boards[5])));
                    in = in.replace("%topic%", ut.parseHtml(String.valueOf(boards[1])));
                    in = in.replace("%description%", ut.parseBb(ut.parseHtml(String.valueOf(boards[3]))));
                }
                if (in.isBlank()) {
                    if (!moderator && locked) {
                        category = category.replace("%boards%", getTemplate("board_boards_empty_locked", request, map));
                    } else if (moderator) {
                        category = category.replace("%boards%", getTemplate("board_boards_empty_moderated", request, map));
                        category = category.replace("%cat%", id);
                    } else {
                        category = category.replace("%boards%", getTemplate("board_boards_empty", request, map));
                    }
                } else {
                    category = category.replace("%boards%", in);
                }
            }
            if (category.isBlank()) {
                if (moderator) {
                    text = text.replace("%boards%", getTemplate("board_cats_empty_moderated", request, map));
                } else {
                    text = text.replace("%boards%", getTemplate("board_cats_empty", request, map));

                }
            } else {
                text = text.replace("%boards%", category);
            }
        } else {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = getTemplate("boards3", request, map);
            } else {
                text = getTemplate("boards", request, map);
            }
            ArrayList<Object[]> board = db.getBoardFromIdStripped2(Long.valueOf(bid), Long.valueOf(page));
            var count = db.countBoardTreads2(Long.valueOf(bid));
            if (count == 0) {
                count = 1;
            }
            long pages = (long) Math.ceil((float) count / conf.getLong("board_pages"));
            StringBuilder pageContent = new StringBuilder();
            for (long l = 1; l <= pages; l++) {
                if (l != Long.valueOf(page)) {
                    var content = getTemplate("boards_page", request, map);
                    content = content.replace("%page%", String.valueOf(l));
                    content = content.replace("%pid%", pid);
                    content = content.replace("%id%", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                } else {
                    var content = getTemplate("boards_page_active", request, map);
                    content = content.replace("%page%", String.valueOf(l));
                    content = content.replace("%pid%", pid);
                    content = content.replace("%id%", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                }
            }
            if (page.equals("1")) {
                text = text.replace("%prev%", getTemplate("boards_page_prev_disabled", request, map));
            } else {
                var content = getTemplate("boards_page_prev_enabled2", request, map);
                content = content.replace("%page%", String.valueOf(Long.valueOf(page) - 1));
                content = content.replace("%pid%", pid);
                content = content.replace("%id%", bid);
                text = text.replace("%prev%", content);
            }
            if (Long.valueOf(page) == pages) {
                text = text.replace("%next%", getTemplate("boards_page_next_disabled", request, map));
            } else {
                var content = getTemplate("boards_page_next_enabled2", request, map);
                content = content.replace("%page%", String.valueOf(Long.valueOf(page) + 1));
                content = content.replace("%pid%", pid);
                content = content.replace("%id%", bid);
                text = text.replace("%next%", content);
            }
            text = text.replace("%pages%", pageContent.toString());
            text = text.replace("%count%", String.valueOf(count));
            StringBuilder sb = new StringBuilder();
            board.forEach((boards) -> {
                if ((Long) boards[10] == 0) {
                    var brd = getTemplate("boards_thread2", request, map);
                    brd = brd.replace("%topic%", ut.parseHtml(db.getBoardRef((Long) boards[2], "topic")));
                    var content = String.valueOf(db.getBoardRef((Long) boards[2], "content"));
                    if (content.length() > 255) {
                        content = content.substring(0, 255);
                        content = content + "...";
                    }
                    var user = db.getData(Long.valueOf(db.getBoardRef((Long) boards[2], "user")), "nick2");
                    if (user.equalsIgnoreCase(nick) || moderator) {
                        brd = brd.replace("%edit%", getTemplate("boards_thread_edit", request, map));
                    } else {
                        brd = brd.replace("%edit%", "");
                    }
                    brd = brd.replace("%content%", ut.removeBb(ut.parseHtml(content)));
                    brd = brd.replace("%ref%", String.valueOf(boards[2]));
                    if (moderator) {
                        brd = brd.replace("%delete%", getTemplate("boards_thread_delete", request, map));
                        if (db.getBoardRef((Long) boards[2], "id").equals(db.getBoardRef((Long) boards[2], "ref"))) {
                            brd = brd.replace("%close%", getTemplate("boards_thread_close", request, map));
                        } else {
                            brd = brd.replace("%close%", "");
                        }
                        if (db.isBanned(user)) {
                            brd = brd.replace("%ban%", getTemplate("boards_thread_unban", request, map));
                        } else {
                            brd = brd.replace("%ban%", getTemplate("boards_thread_ban", request, map));
                        }
                    } else {
                        brd = brd.replace("%delete%", "");
                        brd = brd.replace("%close%", "");
                        brd = brd.replace("%ban%", "");
                    }
                    brd = brd.replace("%uid%", String.valueOf(boards[3]));
                    brd = brd.replace("%user%", user);
                    brd = brd.replace("%user%", user);
                    brd = brd.replace("%color%", db.getData(user, "color"));
                    brd = brd.replace("%board%", String.valueOf(boards[4]));
                    brd = brd.replace("%posted%", ut.getTime(Long.valueOf(db.getBoardRef((Long) boards[2], "posted"))));
                    brd = brd.replace("%ip%", String.valueOf(boards[6]));
                    brd = brd.replace("%cat%", String.valueOf(boards[7]));
                    brd = brd.replace("%id%", String.valueOf(boards[8]));
                    brd = brd.replace("%closed%", String.valueOf(boards[9]));
                    brd = brd.replace("%deleted%", String.valueOf(boards[10]));
                    brd = brd.replace("%signature%", "");
                    if (db.isBanned(user)) {
                        brd = brd.replace("%banned%", getTemplate("boards_thread_banned", request, map));
                    } else {
                        brd = brd.replace("%banned%", "");
                    }
                    sb.append(brd);
                }
            });
            if (sb.toString().isBlank()) {
                text = text.replace("%boards%", getTemplate("boards_empty", request, map));
                text = text.replace("%id%", bid);
            } else {
                text = text.replace("%boards%", sb.toString());
                text = text.replace("%id%", bid);
            }
        }
        text = text.replace("%board_error_if_one%", "");
        ut.submitContent(text, response);
    }

    /**
     * Deinen Account verwalten
     *
     * @param out Die Output-Klasse
     */
    private void accountForm2(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var session = request.getSession(false);
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        if (session == null) {
            printTemplate("timeout", request, response, map);
            return;
        }
        cm.getUsersCommunity().get((String) session.getAttribute("nick")).setTimeoutTimer(0);
        String text = null;
        var service = map.getOrDefault("service", "");
        var data = map.getOrDefault("data", "");
        var db = conf.getDb();
        var skin = (String) session.getAttribute("skin");

        var sid = session.getId();
        skin = skin != null ? skin : map.getOrDefault("skin", "");
        skin = ut.parseHost(skin, request)[1];
        var reg = false;
        var nick = (String) session.getAttribute("nick");
        reg = db.isRegistered(nick);
        if (!reg) {
            text = getTemplate("nick_not_registered_com", request, map);
        } else if (service.equals("account")) {
            String homepage = null;
            String city = null;
            String hobby = null;
            String description = null;
            String slogan = null;
            String signature = null;
            String icq = null;
            String live = null;
            String yahoo = null;
            String facebook = null;
            String twitter = null;
            String irc = null;
            String youtube = null;
            String fam = null;
            switch (data) {
                case "" -> {
                    homepage = db.getData(nick, "homepage");
                    city = db.getData(nick, "city");
                    hobby = db.getData(nick, "hobby");
                    description = db.getData(nick, "description");
                    slogan = db.getData(nick, "slogan");
                    signature = db.getData(nick, "signature");
                    icq = db.getData(nick, "icq");
                    live = db.getData(nick, "live");
                    yahoo = db.getData(nick, "yahoo");
                    facebook = db.getData(nick, "facebook");
                    twitter = db.getData(nick, "twitter");
                    irc = db.getData(nick, "irc");
                    youtube = db.getData(nick, "youtube");
                    fam = db.getData(nick, "fam_status");
                    text = getTemplate("account_form_profile_com", request, map);
                    text = text.replace("%homepage%", homepage);
                    text = text.replace("%city%", city);
                    text = text.replace("%hobby%", hobby);
                    text = text.replace("%description%", description);
                    text = text.replace("%slogan%", slogan);
                    text = text.replace("%signature%", signature);
                    text = text.replace("%icq%", icq);
                    text = text.replace("%live%", live);
                    text = text.replace("%yahoo%", yahoo);
                    text = text.replace("%facebook%", facebook);
                    text = text.replace("%twitter%", twitter);
                    text = text.replace("%irc%", irc);
                    text = text.replace("%youtube%", youtube);
                    text = text.replace("%fam_status%", fam);
                }
                case "change" -> {
                    homepage = map.getOrDefault("homepage", "");
                    city = map.getOrDefault("city", "");
                    hobby = map.getOrDefault("hobby", "");
                    description = map.getOrDefault("description", "");
                    slogan = map.getOrDefault("slogan", "");
                    signature = map.getOrDefault("signature", "");
                    icq = map.getOrDefault("icq", "");
                    live = map.getOrDefault("live", "");
                    yahoo = map.getOrDefault("yahoo", "");
                    facebook = map.getOrDefault("facebook", "");
                    twitter = map.getOrDefault("twitter", "");
                    irc = map.getOrDefault("irc", "");
                    youtube = map.getOrDefault("youtube", "");
                    fam = map.getOrDefault("fam_status", "");
                    text = getTemplate("account_form_profile_success_com", request, map);
                    text = text.replace("%homepage%", homepage);
                    text = text.replace("%city%", city);
                    text = text.replace("%hobby%", hobby);
                    text = text.replace("%description%", description);
                    text = text.replace("%slogan%", slogan);
                    text = text.replace("%signature%", signature);
                    text = text.replace("%icq%", icq);
                    text = text.replace("%live%", live);
                    text = text.replace("%yahoo%", yahoo);
                    text = text.replace("%facebook%", facebook);
                    text = text.replace("%twitter%", twitter);
                    text = text.replace("%irc%", irc);
                    text = text.replace("%youtube%", youtube);
                    text = text.replace("%fam_status%", fam);
                    db.updateNick(nick, "homepage", homepage);
                    db.updateNick(nick, "city", city);
                    db.updateNick(nick, "hobby", hobby);
                    db.updateNick(nick, "description", description);
                    db.updateNick(nick, "slogan", slogan);
                    db.updateNick(nick, "signature", signature);
                    db.updateNick(nick, "icq", icq);
                    db.updateNick(nick, "live", live);
                    db.updateNick(nick, "yahoo", yahoo);
                    db.updateNick(nick, "facebook", facebook);
                    db.updateNick(nick, "twitter", twitter);
                    db.updateNick(nick, "irc", irc);
                    db.updateNick(nick, "youtube", youtube);
                    db.updateNick(nick, "fam_status", fam);
                }
                default -> {
                    text = getTemplate("account_form_unknown_com", request, map);
                }
            }
        } else if (service.equals("password")) {
            switch (data) {
                case "change" -> {
                    var oldPwd = map.getOrDefault("old_pwd", "");
                    var newPwd = map.getOrDefault("new_pwd", "");
                    var newPwd2 = map.getOrDefault("new_pwd2", "");
                    String errorMessage = null;
                    if (!db.checkPassword(nick, oldPwd)) {
                        errorMessage = getTemplate("account_error_message_pass_wrong", request, map);
                    } else if (newPwd.length() < conf.getInt("min_pwd_length")) {
                        errorMessage = getTemplate("account_error_message_pass_short", request, map);
                    } else if (newPwd.length() > conf.getInt("max_pwd_length")) {
                        errorMessage = getTemplate("account_error_message_pass_long", request, map);
                    } else if (!newPwd.equals(newPwd2)) {
                        errorMessage = getTemplate("account_error_message_pass_same", request, map);
                    }
                    if (errorMessage == null) {
                        text = getTemplate("account_form_password_success_com", request, map);
                        db.updatePassword(nick, newPwd);
                    } else {
                        text = getTemplate("account_form_password_com", request, map);
                        text = text.replace("%account_error_if_one%", getTemplate("account_failed", request, map));
                        text = text.replace("%error_message%", errorMessage);
                    }
                }
                case "" -> {
                    text = getTemplate("account_form_password_com", request, map);
                }
                default -> {
                    text = getTemplate("account_form_unknown_com", request, map);
                }
            }
        } else if (service.equals("delete")) {
            switch (data) {
                case "delete" -> {
                    var pwd = map.getOrDefault("pwd", "");
                    var pwd2 = map.getOrDefault("pwd2", "");
                    String errorMessage = null;
                    if (!pwd.equals(pwd2)) {
                        errorMessage = getTemplate("account_error_message_pass_same", request, map);
                    } else if (!db.checkPassword(nick, pwd)) {
                        errorMessage = getTemplate("account_error_message_pass_wrong", request, map);
                    }
                    if (errorMessage == null) {
                        text = getTemplate("account_form_delete_success_com", request, map);
                        cm.quit(nick);
                        cm.removeFriend(nick);
                        db.delFriendsFromList(nick);
                        db.delNick(nick);
                    } else {
                        text = getTemplate("account_form_delete_com", request, map);
                        text = text.replace("%account_error_if_one%", getTemplate("account_failed", request, map));
                        text = text.replace("%error_message%", errorMessage);
                    }
                }
                case "" -> {
                    text = getTemplate("account_form_delete_com", request, map);
                }
                default -> {
                    getTemplate("account_form_unknown_com", request, map);
                }
            }
        } else if (service.equals("napping")) {
            String title = "";
            String bg_color_1 = "";
            String bg_color_2 = "";
            String color = "";
            String border_color = "";
            String link_color = "";
            String room = "";
            switch (data) {
                case "change" -> {
                    title = map.getOrDefault("title", "");
                    bg_color_1 = map.getOrDefault("bg_color_1", "");
                    bg_color_2 = map.getOrDefault("bg_color_2", "");
                    color = map.getOrDefault("color", "");
                    link_color = map.getOrDefault("link_color", "");
                    border_color = map.getOrDefault("border_color", "");
                    room = map.getOrDefault("room2", "");
                    if (bg_color_1.length() != 6 || !bg_color_1.matches("[a-fA-F0-9]*")) {
                        text = getTemplate("account_form_napping_color_com", request, map);
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (bg_color_2.length() != 6 || !bg_color_2.matches("[a-fA-F0-9]*")) {
                        text = getTemplate("account_form_napping_color_com", request, map);
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (border_color.length() != 6 || !border_color.matches("[a-fA-F0-9]*")) {
                        text = getTemplate("account_form_napping_color_com", request, map);
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (color.length() != 6 || !color.matches("[a-fA-F0-9]*")) {
                        text = getTemplate("account_form_napping_color_com", request, map);
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (link_color.length() != 6 || !link_color.matches("[a-fA-F0-9]*")) {
                        text = getTemplate("account_form_napping_color_com", request, map);
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (db.ownerExists(nick) && !db.getRoomNameByOwner(nick).equals(room)) {
                        text = getTemplate("account_form_napping_used_com", request, map);
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (db.roomExists(room) && db.getRoomData(room, "owner").equalsIgnoreCase(nick)) {
                        db.updateRoomData(room, "owner", nick);
                        db.updateRoomData(room, "first_bgcolor", bg_color_1);
                        db.updateRoomData(room, "second_bgcolor", bg_color_2);
                        db.updateRoomData(room, "bordercolor", border_color);
                        db.updateRoomData(room, "linkcolor", link_color);
                        db.updateRoomData(room, "textcolor", color);
                        db.updateRoomData(room, "page_title", title);
                        text = getTemplate("account_form_napping_change_com", request, map);
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else if (!db.roomExists(room)) {
                        db.addRoomData(nick, room, title, bg_color_1, bg_color_2, color, border_color, link_color);
                        db.updateRoomData(room, "su", nick);
                        text = getTemplate("account_form_napping_add_com", request, map);
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    } else {
                        text = getTemplate("account_form_napping_error_com", request, map);
                        text = text.replace("%title%", title);
                        text = text.replace("%bg_color_1%", bg_color_1);
                        text = text.replace("%bg_color_2%", bg_color_2);
                        text = text.replace("%color%", color);
                        text = text.replace("%link_color%", link_color);
                        text = text.replace("%border_color%", border_color);
                        text = text.replace("%room2%", room);
                        text = text.replace("%nick%", nick);
                    }
                }
                case "" -> {
                    room = db.getRoomNameByOwner(nick.toLowerCase());
                    if (!room.isBlank()) {
                        title = db.getRoomData(room, "page_title");
                        bg_color_1 = db.getRoomData(room, "first_bgcolor");
                        bg_color_2 = db.getRoomData(room, "second_bgcolor");
                        color = db.getRoomData(room, "textcolor");
                        border_color = db.getRoomData(room, "bordercolor");
                        link_color = db.getRoomData(room, "linkcolor");
                        room = db.getRoomData(room, "room");
                    }
                    if (!map.getOrDefault("room", "").isBlank()) {
                        map.replace("room", map.getOrDefault("room", ""), room);
                    }
                    text = getTemplate("account_form_napping_com", request, map);
                    text = text.replace("%title%", title);
                    text = text.replace("%bg_color_1%", bg_color_1);
                    text = text.replace("%bg_color_2%", bg_color_2);
                    text = text.replace("%color%", color);
                    text = text.replace("%link_color%", link_color);
                    text = text.replace("%border_color%", border_color);
                    text = text.replace("%room2%", room);
                    text = text.replace("%nick%", nick);
                }
                default -> {
                    text = getTemplate("account_form_unknown_com", request, map);
                }
            }
        } else if (service.equals("data")) {
            String errorMessage = null;
            String mail = null;
            String gender = null;
            String day = null;
            String month = null;
            String year = null;
            switch (data) {
                case "change" -> {
                    mail = map.getOrDefault("mail", "");
                    gender = map.getOrDefault("gender", "");
                    day = map.getOrDefault("day", "");
                    month = map.getOrDefault("month", "");
                    year = map.getOrDefault("year", "");
                    if (!gender.equals("-") && !gender.equals("m") && !gender.equals("f")) {
                        errorMessage = getTemplate("account_error_message_gender_invalid", request, map);
                    } else if (!ut.isValidDate(day, month, year)) {
                        errorMessage = getTemplate("account_error_message_date", request, map);
                    } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                        errorMessage = getTemplate("account_error_message_age_old", request, map);
                    } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                        errorMessage = getTemplate("account_error_message_age_young", request, map);
                    } else if (!mail.contains("@")) {
                        errorMessage = getTemplate("account_error_message_mail_invalid", request, map);
                    } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                        errorMessage = getTemplate("account_error_message_mail_invalid", request, map);
                    }
                    if (errorMessage == null) {
                        text = getTemplate("account_form_default_success_com", request, map);
                        db.updateNick(nick, "sex", gender);
                        db.updateNick(nick, "bday_day", day);
                        db.updateNick(nick, "bday_month", month);
                        db.updateNick(nick, "bday_year", year);
                        db.updateNick(nick, "mail", mail);
                    } else {
                        text = getTemplate("account_form_default_com", request, map);
                        text = ut.parseGender(text, gender);
                        text = text.replace("%account_error_if_one%", getTemplate("account_failed", request, map));
                        text = text.replace("%error_message%", errorMessage);
                        text = text.replace("%mail%", mail);
                        text = text.replace("%day%", day);
                        text = text.replace("%month%", month);
                        text = text.replace("%year%", year);
                    }
                }
                case "" -> {
                    mail = db.getData(nick, "mail");
                    gender = db.getData(nick, "sex");
                    day = db.getData(nick, "bday_day");
                    month = db.getData(nick, "bday_month");
                    year = db.getData(nick, "bday_year");
                    mail = mail == null ? "" : mail;
                    gender = gender == null ? "" : gender;
                    day = day == null ? "" : day;
                    month = month == null ? "" : month;
                    year = year == null ? "" : year;
                    text = getTemplate("account_form_default_com", request, map);
                    text = ut.parseGender(text, gender);
                    text = text.replace("%mail%", mail);
                    text = text.replace("%day%", day);
                    text = text.replace("%month%", month);
                    text = text.replace("%year%", year);
                }
                default -> {
                    text = getTemplate("account_form_unknown_com", request, map);
                }
            }
        } else if (service.equals("picture")) {
            text = getTemplate("picture_com", request, map);
        } else if (service.equals("picture_del")) {
            switch (data) {
                case "delete":
                    db.updateNick(nick, "image_upload", null);
                    db.updateNick(nick, "image_url", null);
                    text = getTemplate("picture_del_success_com", request, map);
                    break;
                case "":
                    text = getTemplate("picture_del_com", request, map);
                    break;
                default:
                    text = getTemplate("picture_del_unkown_com", request, map);
                    break;
            }

        } else if (!service.equals("")) {
            text = getTemplate("account_form_unknown_com", request, map);
        } else {
            text = getTemplate("account_form_com", request, map);
        }
        text = text.replace("%account_error_if_one%", "");
        ut.submitContent(text, response);
    }

    /**
     * Registrierungsformular
     *
     * @param out Die Output-Klasse
     */
    private void regForm(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var service = map.getOrDefault("service", "");
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var mailer = Bootstrap.boot.getSendMail();
        var ut = Bootstrap.boot.getUtil();
        var gender = map.getOrDefault("reg_gender", "");
        var nick = map.getOrDefault("reg_nick", "");
        var mail = map.getOrDefault("reg_mail", "");
        var mail2 = map.getOrDefault("reg_mail2", "");
        var question = map.getOrDefault("reg_reminder_question", "");
        var answer = map.getOrDefault("reg_reminder_answer", "");
        var captcha = map.getOrDefault("reg_captcha", "");
        var cid = map.getOrDefault("reg_cid", "");
        var day = map.getOrDefault("reg_day", "");
        var month = map.getOrDefault("reg_month", "");
        var year = map.getOrDefault("reg_year", "");
        var owner = map.getOrDefault("owner", "");
        var errorMessage = "";
        var regSuccess = false;
        var skin = map.getOrDefault("skin", "");
        skin = ut.parseHost(skin, request)[1];
        var db = conf.getDb();
        var ip = request.getLocalAddr();
        String proxyIp = null;
        if (conf.getString("use_proxy").equals("1")) {
            proxyIp = request.getHeader(conf.getString("real_ip"));
        }
        if (proxyIp != null && !proxyIp.equals("")) {
            ip = proxyIp;
        }
        String ipResolved = null;
        try {
            ipResolved = conf.getString("resolve_ip").equals("1") ? getByName(ip).getCanonicalHostName() : ip;
        } catch (UnknownHostException uhe) {
            ipResolved = ip;
        }
        if (!gender.equals("-") && !gender.equals("m") && !gender.equals("f")) {
            errorMessage = getTemplate("reg_error_message_gender_invalid", request, map);
        } else if (!nick.matches(conf.getString("allowed_chars"))) {
            errorMessage = getTemplate("reg_error_message_nick_chars", request, map);
        } else if ((conf.getInt("guest") == 1 && nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase()))) {
            errorMessage = getTemplate("reg_error_message_nick_guest", request, map);
        } else if (nick.length() < conf.getInt("min_nick_length")) {
            errorMessage = getTemplate("reg_error_message_nick_short", request, map);
        } else if (nick.length() > conf.getInt("max_nick_length")) {
            errorMessage = getTemplate("reg_error_message_nick_long", request, map);
        } else if (nickIsCommand(nick)) {
            errorMessage = getTemplate("reg_error_message_nick_command", request, map);
        } else if (!ut.isValidDate(day, month, year)) {
            errorMessage = getTemplate("reg_error_message_date", request, map);
        } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
            errorMessage = getTemplate("reg_error_message_age_old", request, map);
        } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
            errorMessage = getTemplate("reg_error_message_age_young", request, map);
        } else if (db.isRegistered(nick)) {
            errorMessage = getTemplate("reg_error_message_nick_registered", request, map);
        } else if (db.isBanned(ip)) {
            errorMessage = getTemplate("reg_error_message_nick_banned", request, map);
        } else if (db.isBanned(ipResolved)) {
            errorMessage = getTemplate("reg_error_message_nick_banned", request, map);
        } else if (!mail.equals(mail2)) {
            errorMessage = getTemplate("reg_error_message_mail_same", request, map);
        } else if (!mail.contains("@")) {
            errorMessage = getTemplate("reg_error_message_mail_invalid", request, map);
        } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
            errorMessage = getTemplate("reg_error_message_mail_invalid", request, map);
        } else if (!cm.isCorrectCaptcha(captcha.toLowerCase(), cid)) {
            errorMessage = getTemplate("reg_error_message_captcha", request, map);
        } else {
            regSuccess = true;
        }
        var pwd = ut.createRandomPassword();
        if (regSuccess) {
            var text = getMail("register", request, map);
            text = text.replace("%pwd%", pwd);
            text = text.replace("%nick%", nick);
            try {
                mailer.sendEmail(text, conf.getString("register_subject_" + skin), mail);

            } catch (MessagingException ex) {
                Logger.getLogger(ChatServices.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        String rf = null;
        String roomName = "";
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        if (!regSuccess) {
            if (roomName.isBlank()) {
                rf = getTemplate("reg_form", request, map);
            } else {
                rf = getTemplate("reg_form_napping", request, map);
                rf = rf.replace("%owner%", owner);
                rf = rf.replace("%title%", db.getRoomData(roomName, "page_title"));
            }
            if (service.equals("reg_done")) {
                rf = rf.replace("%reg_error_if_one%", getTemplate("reg_failed", request, map));
                rf = rf.replace("%error_message%", errorMessage);
            } else {
                rf = rf.replace("%reg_error_if_one%", "");
            }
            cid = generateSid();
        } else {
            String color = null;
            if (conf.getInt("random_color") == 1) {
                color = ut.createRandomColor();
            } else {
                color = conf.getString("default_color");
            }
            db.addNick(gender, nick, mail, color, pwd, question, answer, day, month, year);
            if (roomName.isBlank()) {
                rf = getTemplate("reg_form_success", request, map);
            } else {
                rf = getTemplate("reg_form_success_napping", request, map);
                rf = rf.replace("%owner%", owner);
                rf = rf.replace("%title%", db.getRoomData(roomName, "page_title"));
            }
        }
        rf = rf.replace("%reg_nick%", nick);
        rf = rf.replace("%reg_mail%", mail);
        rf = rf.replace("%reg_cid%", cid);
        rf = rf.replace("%reg_captcha%", captcha);
        rf = rf.replace("%reg_reminder_question%", question);
        rf = rf.replace("%reg_reminder_answer%", answer);
        rf = rf.replace("%reg_day%", day);
        rf = rf.replace("%reg_month%", month);
        rf = rf.replace("%reg_year%", year);
        rf = rf.replace("%skin%", skin);
        rf = ut.parseGender(rf, gender);
        ut.submitContent(rf, response);
    }

    /**
     * Speicherinfo zu Debug-Zwecken (Wird in finaler Version deaktiviert!)
     *
     * @param out Die Output-Klasse
     */
    private void meminfo(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var runtime = getRuntime();
        var ut = Bootstrap.boot.getUtil();
        var total = runtime.totalMemory();
        var free = runtime.freeMemory();
        var used = total - free;
        var memUsed = used >> 20;
        var memTotal = total >> 20;
        var sb = new StringBuilder();
        sb.append("Chatserver: ");
        sb.append(memUsed);
        sb.append(" MB used (");
        sb.append(memTotal);
        sb.append(" MB reserved)<br>\r\n");
        ut.submitContent(sb.toString(), response);
    }

    /**
     * Vorgegebene Chat-Services und Seiten
     *
     * @param out Die Output-Klasse
     */
    private void out(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var service = map.getOrDefault("service", "");
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        switch (service) {
            case "page" -> {
                // Zeigt individuelle Templates an
                response.setContentType("text/html; charset=" + conf.getString("charset"));
                printPage(map.getOrDefault("doc", ""), request, response, map);
            }
            case "page_com" -> {
                HttpSession session = null;
                // Zeigt individuelle Templates an
                if ((session = request.getSession(false)) != null) {
                    response.setContentType("text/html; charset=" + conf.getString("charset"));
                    printPage(map.getOrDefault("doc", ""), request, response, map);
                    cm.getUsersCommunity().get((String) session.getAttribute("nick")).setTimeoutTimer(0);

                } else {
                    response.setContentType("text/html; charset=" + conf.getString("charset"));
                    printTemplate("timeout", request, response, map);
                }
            }
            case "style" -> {
                // Stylesheets
                response.setContentType("text/css");
                printStyleSheet(map.getOrDefault("doc", ""), request, response, map);
            }
            case "js" -> {
                // Scripte
                response.setContentType("text/javascript");
                printScript(map.getOrDefault("doc", ""), request, response, map);
            }
            default -> {
                response.setStatus(404);
                response.setContentType("text/html; charset=" + conf.getString("charset"));
                printTemplate("error", request, response, map);
            }
        }
    }

    /**
     * Platzhalter f&uuml;r ehemalige Debuggingseiten
     *
     * @param out Die Output-Klasse
     */
    private void removed(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var sb = new StringBuilder();
        var ut = Bootstrap.boot.getUtil();
        response.setStatus(404);
        sb.append("<html>\r\n");
        sb.append("<head>\r\n");
        sb.append("<title>Document removed!</title>\r\n");
        sb.append("</head>\r\n");
        sb.append("<body>\r\n");
        sb.append("<h1>Document removed!</h1>\r\n");
        sb.append("<p>\r\n");
        sb.append("The requested document was integrated for troubleshooting and is no longer available.\r\n");
        sb.append("</p>\r\n");
        sb.append("<hr>\r\n");
        sb.append("<address>%SERVER_SOFTWARE% %SERVER_VERSION%-%SERVER_STATUS%</address>\r\n");
        sb.append("</body>\r\n");
        sb.append("<html>\r\n");
        var data = sb.toString();
        data = ut.replaceServerInfo(data);
        ut.submitContent(data, response);
    }

    /**
     * Platzhalter f&uuml;r k&uuml;nftige Seiten
     *
     * @param out Die Output-Klasse
     */
    private void notIntegrated(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var sb = new StringBuilder();
        var ut = Bootstrap.boot.getUtil();
        sb.append("<html>\r\n");
        sb.append("<head>\r\n");
        sb.append("<title>Document not integrated!</title>\r\n");
        sb.append("</head>\r\n");
        sb.append("<body>\r\n");
        sb.append("<h1>Document not integrated!</h1>\r\n");
        sb.append("<p>\r\n");
        sb.append("The requested document is currently not integrated! It will be activated in later versions!\r\n");
        sb.append("</p>\r\n");
        sb.append("<hr>\r\n");
        sb.append("<address>%SERVER_SOFTWARE% %SERVER_VERSION%-%SERVER_STATUS%</address>\r\n");
        sb.append("</body>\r\n");
        sb.append("<html>\r\n");
        var data = sb.toString();
        data = ut.replaceServerInfo(data);
        ut.submitContent(data, response);
    }

    /**
     * Der Login-Frame
     *
     * @param out Die Output-Klasse
     */
    private void loginFrame(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var nick = map.getOrDefault("nick", "");
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        var target = map.getOrDefault("target", "");
        var skin = map.getOrDefault("skin", "");
        if (db.isRegistered(nick)) {
            if (!nick.equalsIgnoreCase(db.getData(nick, "nick2"))) {
                nick = db.getData(nick, "nick2");
            }
            map.replace("nick", nick);
        }
        var session = request.getSession();
        var pwd = map.getOrDefault("pwd", "");
        var sid = map.getOrDefault("sid", "");
        sid = !sid.isBlank() ? sid : generateSid();
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
            realIp = conf.getString("resolve_ip").equals("1") ? getByName(realIp).getCanonicalHostName() : realIp;
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
            ipResolved = conf.getString("resolve_ip").equals("1") ? getByName(ip).getCanonicalHostName() : ip;
        } catch (UnknownHostException uhe) {
            ipResolved = ip;
        }
        try {
            ipResolved2 = conf.getString("resolve_ip").equals("1") ? getByName(realIp).getCanonicalHostName() : realIp;
        } catch (UnknownHostException uhe) {
            ipResolved2 = realIp;
        }
        if (db.isRegistered(nick) && !db.getData(nick, "pwd2").isEmpty() && db.checkPassword2(nick, pwd)) {
            db.updateNick(nick, "pwd2", "");
            db.updatePassword(nick, pwd);
        }
        var online = false;
        if (session != null && !session.isNew()) {
            nick = !nick.isBlank() ? nick : (String) session.getAttribute("nick");
            online = true;
            pwd = !pwd.isBlank() ? pwd : (String) session.getAttribute("pwd");
            skin = !skin.isBlank() ? skin : (String) session.getAttribute("skin");

        }
        reg = db.isRegistered(nick);
        if (session == null) {
            printTemplate("timeout", request, response, map);
        } else if (!nick.matches(conf.getString("allowed_chars"))) {
            printTemplate("chars", request, response, map);
        } else if (!online && nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase())) {
            printTemplate("guest", request, response, map);
        } else if ((conf.getInt("guest") == 1 && !nick.equals("")) && nick.length() < conf.getInt("min_nick_length")) {
            printTemplate("nick", request, response, map);
        } else if (nickIsCommand(nick)) {
            printTemplate("command", request, response, map);
        } else if (reg && !db.checkPassword(nick, pwd)) {
            printTemplate("pwd", request, response, map);
        } else if (db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(nick));
            printTemplate("banned", request, response, map);
        } else if (db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ip));
            printTemplate("banned", request, response, map);
        } else if (db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ipResolved));
            printTemplate("banned", request, response, map);
        } else if (db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ipResolved2));
            printTemplate("banned", request, response, map);
        } else if (db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(realIp));
            printTemplate("banned", request, response, map);
        } else if (db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(nick));
            printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(ip));
            printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(ipResolved));
            printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(ipResolved2));
            printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(realIp));
            printTemplate("banned_temp", request, response, map);
        } else if (!reg && (conf.getInt("guest") == 1 && !nick.equals("") && conf.getInt("only_registered_users") == 1)) {
            printTemplate("reg", request, response, map);
        } else {
            var context = request.getServletContext();
            if (session.isNew() || cm.getUsersCommunity().get((String) session.getAttribute("nick")) == null) {
                if (conf.getInt("guest") == 1 && nick.equals("")) {
                    var i = 1;
                    while (cm.getUsersCommunity().containsKey(conf.getString("guest_prefix") + Integer.toString(i))) {
                        i++;
                        nick = conf.getString("guest_prefix") + Integer.toString(i);
                    }
                    if (!cm.getUsersCommunity().containsKey(conf.getString("guest_prefix") + Integer.toString(1))) {
                        nick = conf.getString("guest_prefix") + Integer.toString(1);
                    }
                }
                session.setMaxInactiveInterval(-1);
                session.setAttribute("nick", nick);
                session.setAttribute("pwd", pwd);
                session.setAttribute("skin", skin);
                session.setAttribute("ip", ip);
                session.setAttribute("user-agent", request.getHeader("user-agent"));
                session.setAttribute("referer", request.getHeader("referer") != null ? request.getHeader("referer") : "");
                session.setAttribute("os-name", request.getHeader("sec-ch-ua-platform") != null ? request.getHeader("sec-ch-ua-platform") : "unknown");
                session.setAttribute("os-version", request.getHeader("sec-ch-ua-platform-version") != null ? request.getHeader("sec-ch-ua-platform-version") : "");
                session.setAttribute("os-mobile", request.getHeader("sec-ch-ua-mobile") != null ? request.getHeader("sec-ch-ua-mobile") : "?0");
                session.setAttribute("browser", request.getHeader("sec-ch-ua") != null ? request.getHeader("sec-ch-ua") : "");
                session.setAttribute("server", context.getServerInfo());
                session.setAttribute("community", "true");
                cm.getUsersCommunity().put(nick, new UsersCommunity(Bootstrap.boot, session));
            } else {
                cm.getUsersCommunity().get((String) session.getAttribute("nick")).setTimeoutTimer(0);
            }
            if (reg) {
                printTemplate("frameset_community_reg", request, response, map);
            } else {
                printTemplate("frameset_community", request, response, map);
            }
        }
    }

    /**
     * Der Login-Frame
     *
     * @param out Die Output-Klasse
     */
    private void chatFrame(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var nick = map.getOrDefault("nick", "");
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        var target = map.getOrDefault("target", "");
        var skin = map.getOrDefault("skin", "");
        var session = request.getSession();
        if ((target.isBlank() && session == null) || (target.isBlank() && session.isNew())) {
            if (db.isRegistered(nick)) {
                if (!nick.equalsIgnoreCase(db.getData(nick, "nick2"))) {
                    nick = db.getData(nick, "nick2");
                }
                map.replace("nick", nick);
            }
            var pwd = map.getOrDefault("pwd", "");
            var sid = map.getOrDefault("sid", "");
            sid = !sid.isBlank() ? sid : generateSid();
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
                realIp = conf.getString("resolve_ip").equals("1") ? getByName(realIp).getCanonicalHostName() : realIp;
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
                ipResolved = conf.getString("resolve_ip").equals("1") ? getByName(ip).getCanonicalHostName() : ip;
            } catch (UnknownHostException uhe) {
                ipResolved = ip;
            }
            try {
                ipResolved2 = conf.getString("resolve_ip").equals("1") ? getByName(realIp).getCanonicalHostName() : realIp;
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
                printTemplate("room", request, response, map);
            } else if (!nick.matches(conf.getString("allowed_chars"))) {
                if (roomName.isBlank()) {
                    printTemplate("chars", request, response, map);
                } else {
                    var text = getTemplate("chars_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase())) {
                if (roomName.isBlank()) {
                    printTemplate("guest", request, response, map);
                } else {
                    var text = getTemplate("guest_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if ((conf.getInt("guest") == 1 && !nick.equals("")) && nick.length() < conf.getInt("min_nick_length")) {
                if (roomName.isBlank()) {
                    printTemplate("nick", request, response, map);
                } else {
                    var text = getTemplate("nick_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (nickIsCommand(nick)) {
                if (roomName.isBlank()) {
                    printTemplate("command", request, response, map);
                } else {
                    var text = getTemplate("command_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if ((reg = db.isRegistered(nick)) && !db.checkPassword(nick, pwd)) {
                if (roomName.isBlank()) {
                    printTemplate("pwd", request, response, map);
                } else {
                    var text = getTemplate("pwd_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(nick));
                if (roomName.isBlank()) {
                    printTemplate("banned", request, response, map);
                } else {
                    var text = getTemplate("banned_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ip));
                if (roomName.isBlank()) {
                    printTemplate("banned", request, response, map);
                } else {
                    var text = getTemplate("banned_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved));
                if (roomName.isBlank()) {
                    printTemplate("banned", request, response, map);
                } else {
                    var text = getTemplate("banned_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved2));
                if (roomName.isBlank()) {
                    printTemplate("banned", request, response, map);
                } else {
                    var text = getTemplate("banned_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(realIp));
                if (roomName.isBlank()) {
                    printTemplate("banned", request, response, map);
                } else {
                    var text = getTemplate("banned_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(nick));
                if (roomName.isBlank()) {
                    printTemplate("banned_temp", request, response, map);
                } else {
                    var text = getTemplate("banned_temp_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ip));
                if (roomName.isBlank()) {
                    printTemplate("banned_temp", request, response, map);
                } else {
                    var text = getTemplate("banned_temp_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved));
                if (roomName.isBlank()) {
                    printTemplate("banned_temp", request, response, map);
                } else {
                    var text = getTemplate("banned_temp_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved2));
                if (roomName.isBlank()) {
                    printTemplate("banned_temp", request, response, map);
                } else {
                    var text = getTemplate("banned_temp_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(realIp));
                if (roomName.isBlank()) {
                    printTemplate("banned_temp", request, response, map);
                } else {
                    var text = getTemplate("banned_temp_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (!reg && (conf.getInt("guest") == 1 && !nick.equals("") && conf.getInt("only_registered_users") == 1)) {

                if (roomName.isBlank()) {
                    printTemplate("reg", request, response, map);
                } else {
                    var text = getTemplate("reg_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (!reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));

                if (roomName.isBlank()) {
                    printTemplate("locked_room", request, response, map);
                } else {
                    var text = getTemplate("locked_room_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("lock_status")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));

                if (roomName.isBlank()) {
                    printTemplate("locked_room", request, response, map);
                } else {
                    var text = getTemplate("locked_room_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (cm.isOnline(nick)) {

                if (roomName.isBlank()) {
                    printTemplate("online", request, response, map);
                } else {
                    var text = getTemplate("online_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
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
                cm.getUsersCommunity().put(nick, new UsersCommunity(Bootstrap.boot, session));
                if (owner.equals("")) {
                    if (reg) {
                        printTemplate("frameset_reg", request, response, map);
                    } else {
                        printTemplate("frameset", request, response, map);
                    }
                } else {
                    String text = null;
                    if (reg) {
                        text = getTemplate("frameset_reg_napping", request, map);
                    } else {
                        text = getTemplate("frameset_napping", request, map);
                    }

                    var room2 = db.getRoomNameByOwner(owner.toLowerCase());
                    var title = db.getRoomData(room2, "page_title");
                    var bg_color_1 = db.getRoomData(room2, "first_bgcolor");
                    var bg_color_2 = db.getRoomData(room2, "second_bgcolor");
                    var color = db.getRoomData(room2, "textcolor");
                    var link_color = db.getRoomData(room2, "linkcolor");
                    var border_color = db.getRoomData(room2, "bordercolor");
                    text = text.replace("%title%", title);
                    text = text.replace("%bg_color_1%", bg_color_1);
                    text = text.replace("%bg_color_2%", bg_color_2);
                    text = text.replace("%color%", color);
                    text = text.replace("%link_color%", link_color);
                    text = text.replace("%border_color%", border_color);
                    text = text.replace("%owner%", owner);
                    ut.submitContent(text, response);

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
            sid = sid != null ? sid : generateSid();
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
                realIp = conf.getString("resolve_ip").equals("1") ? getByName(realIp).getCanonicalHostName() : realIp;
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
                ipResolved = conf.getString("resolve_ip").equals("1") ? getByName(ip).getCanonicalHostName() : ip;
            } catch (UnknownHostException uhe) {
                ipResolved = ip;
            }
            try {
                ipResolved2 = conf.getString("resolve_ip").equals("1") ? getByName(realIp).getCanonicalHostName() : realIp;
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
                printTemplate("room", request, response, map);
            } else if (!roomName.isBlank() && !nick.matches(conf.getString("allowed_chars"))) {
                var text = getTemplate("chars_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase())) {
                var text = getTemplate("guest_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && (conf.getInt("guest") == 1 && !nick.equals("")) && nick.length() < conf.getInt("min_nick_length")) {
                var text = getTemplate("nick_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && nickIsCommand(nick)) {
                var text = getTemplate("command_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && !reg && !db.checkPassword(nick, pwd)) {
                var text = getTemplate("pwd_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(nick));
                var text = getTemplate("banned_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ip));
                var text = getTemplate("banned_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved));
                var text = getTemplate("banned_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved2));
                var text = getTemplate("banned_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(realIp));
                var text = getTemplate("banned_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(nick));
                var text = getTemplate("banned_temp_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ip));
                var text = getTemplate("banned_temp_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved));
                var text = getTemplate("banned_temp_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                var text = getTemplate("banned_temp_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(realIp));
                var text = getTemplate("banned_temp_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && !reg && (conf.getInt("guest") == 1 && !nick.equals("") && conf.getInt("only_registered_users") == 1)) {
                var text = getTemplate("reg_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && !reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));
                var text = getTemplate("locked_room_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("lock_status")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));
                var text = getTemplate("locked_room_napping", request, map);
                text = text.replace("%owner%", owner);
                text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (cm.isOnline(nick)) {
                if (roomName.isBlank()) {
                    printTemplate("online", request, response, map);
                } else {
                    var text = getTemplate("online_napping", request, map);
                    text = text.replace("%owner%", owner);
                    text = text.replace("%title%", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
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
                    if (reg) {
                        printTemplate("frameset_reg_com", request, response, map);
                    } else {
                        printTemplate("frameset_com", request, response, map);
                    }
                } else {
                    String text = null;
                    if (reg) {
                        text = getTemplate("frameset_reg_com_napping", request, map);
                    } else {
                        text = getTemplate("frameset_com_napping", request, map);
                    }

                    var room2 = db.getRoomNameByOwner(owner.toLowerCase());
                    var title = db.getRoomData(room2, "page_title");
                    var bg_color_1 = db.getRoomData(room2, "first_bgcolor");
                    var bg_color_2 = db.getRoomData(room2, "second_bgcolor");
                    var color = db.getRoomData(room2, "textcolor");
                    var link_color = db.getRoomData(room2, "linkcolor");
                    var border_color = db.getRoomData(room2, "bordercolor");
                    text = text.replace("%title%", title);
                    text = text.replace("%bg_color_1%", bg_color_1);
                    text = text.replace("%bg_color_2%", bg_color_2);
                    text = text.replace("%color%", color);
                    text = text.replace("%link_color%", link_color);
                    text = text.replace("%border_color%", border_color);
                    text = text.replace("%owner%", owner);
                    ut.submitContent(text, response);

                }
            }
        } else {
            String text = null;
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
            if (owner.isBlank()) {
                text = getTemplate("frameset_privchat", request, map);
                text = text.replace("%oldnick%", oldnick);
                ut.submitContent(text, response);

            } else {

                text = getTemplate("frameset_privchat_napping", request, map);
                var room2 = db.getRoomNameByOwner(owner.toLowerCase());
                var title = db.getRoomData(room2, "page_title");
                var bg_color_1 = db.getRoomData(room2, "first_bgcolor");
                var bg_color_2 = db.getRoomData(room2, "second_bgcolor");
                var color = db.getRoomData(room2, "textcolor");
                var link_color = db.getRoomData(room2, "linkcolor");
                var border_color = db.getRoomData(room2, "bordercolor");
                text = text.replace("%oldnick%", oldnick);
                text = text.replace("%title%", title);
                text = text.replace("%bg_color_1%", bg_color_1);
                text = text.replace("%bg_color_2%", bg_color_2);
                text = text.replace("%color%", color);
                text = text.replace("%link_color%", link_color);
                text = text.replace("%border_color%", border_color);
                text = text.replace("%owner%", owner);
                ut.submitContent(text, response);

            }
        }
    }

    /**
     * &Uuml;berpr&uuml;ft ob ein Nickname einem Befehl entspricht
     *
     * @param nick Der Nickname
     */
    private boolean nickIsCommand(String nick) {
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        if (nick.equalsIgnoreCase("nick")) {
            return true;
        } else if (nick.equalsIgnoreCase("moderator")) {
            return true;
        } else if (nick.equalsIgnoreCase("query")) {
            return true;
        } else if (nick.equalsIgnoreCase("asu")) {
            return true;
        } else if (nick.equalsIgnoreCase("dsu")) {
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
     * Liefert ein Mail-Template als String aus
     *
     * @param template Der Templatename
     * @param request
     * @return
     */
    protected String getMail(String template, HttpServletRequest request, Map<String, String> map) {
        var skin = map.getOrDefault("skin", "");
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        skin = ut.parseHost(skin, request)[1];
        var data = db.getMail(template, skin);
        if (data.toLowerCase().startsWith("error")) {
            var temp = data;
            data = "Mail template not Found\r\n%missing_template%";
            data = data.replace("%missing_template%", temp);
        }
        data = ut.replacePaths(data);
        data = ut.replaceFilePaths(data);
        data = ut.replaceServerInfo(data);
        data = data.replace("%host_ws%", "ws://" + request.getHeader("host"));
        data = data.replace("%host_http%", "http://" + request.getHeader("host"));
        return data;
    }

    /**
     * Liefert ein Template als String aus
     *
     * @param template Der Templatename
     * @param request
     * @return
     */
    protected String getConsole(String template, HttpServletRequest request, Map<String, String> map) {
        var sid = map.getOrDefault("sid", "");
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        var skin = map.getOrDefault("skin", "");
        if (sid.isBlank()) {
            sid = (String) request.getAttribute("sid");
        }
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        skin = ut.parseHost(skin, request)[1];
        var data = db.getConsole(template, skin);
        if (data.toLowerCase().startsWith("error")) {
            var temp = data;
            data = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p><b>%missing_template%</b></p><hr><address>%SERVER_SOFTWARE% %SERVER_VERSION%-%SERVER_STATUS%</address></body><html>";
            data = data.replace("%missing_template%", temp);
        }
        data = parseHeader(data, template, request, map);
        data = ut.replacePaths(data);
        data = ut.replaceFilePaths(data);
        var host = request.getHeader("host");
        host = host != null ? host : "";
        data = ut.replaceServerInfo(data);
        data = data.replace("%skin%", skin);
        return data;
    }

    /**
     * Parst Header
     *
     * @param value Der Text zum parsen
     * @return
     */
    protected String parseHeader(String value, String template, HttpServletRequest request, Map<String, String> map) {
        if (value.matches("(?s)<!-- design=.*? -->(.*?)")) {
            var valueSplitted = value.split("\n");
            var valueLine = valueSplitted[0].replaceFirst("<!-- design=(.*?) -->", "$1").strip();
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < valueSplitted.length; i++) {
                sb.append(valueSplitted[i]);
                sb.append("\n");
            }
            var secData = sb.toString();
            value = getPageDesign(valueLine, request, map);
            value = value.replace("%content%", secData);
        }
        return value;
    }

    /**
     * Liefert ein Template als String aus
     *
     * @param template Der Templatename
     * @param request
     * @return
     */
    protected String getTemplate(String template, HttpServletRequest request, Map<String, String> map) {
        var sid = map.getOrDefault("sid", "");
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        String nick = null;
        String pwd = null;
        String skin = null;
        String room = null;
        String owner = null;
        String roomName = "";
        String target = null;
        var reason = (String) request.getAttribute("reason");
        if (sid.isBlank()) {
            sid = (String) request.getAttribute("sid");
        }
        var session = request.getSession(false);
        if (session != null && !session.isNew()) {
            nick = map.getOrDefault("nick", "").isBlank() ? (String) session.getAttribute("nick") : map.getOrDefault("nick", "");
            skin = map.getOrDefault("skin", "").isBlank() ? (String) session.getAttribute("skin") : map.getOrDefault("skin", "");
            room = map.getOrDefault("room", "").isBlank() ? (String) session.getAttribute("room") : map.getOrDefault("room", "");
            pwd = map.getOrDefault("pwd", "").isBlank() ? (String) session.getAttribute("pwd") : map.getOrDefault("pwd", "");
            owner = map.getOrDefault("owner", "").isBlank() ? (String) session.getAttribute("owner") : map.getOrDefault("owner", "");
        }
        nick = nick != null ? nick : (cm.getNameFromId(sid != null ? sid : "") == null || map.getOrDefault("nick", "").length() > 0 ? map.getOrDefault("nick", "") : cm.getUser(cm.getNameFromId(sid)).getName());
        skin = skin != null ? skin : map.getOrDefault("skin", "");
        room = room != null ? room : map.getOrDefault("room", "");
        pwd = pwd != null ? pwd : map.getOrDefault("pwd", "");
        owner = owner != null ? owner : map.getOrDefault("owner", "");
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        target = map.getOrDefault("target", "");
        if (session != null && session.isNew()) {
            nick = (String) session.getAttribute("nick");
            nick = nick != null ? nick : map.getOrDefault("nick", "");
        }
        var db = conf.getDb();
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        skin = ut.parseHost(skin, request)[1];
        skin = skin != null ? skin : map.getOrDefault("skin", "");
        var data = db.getTemplate(template, skin);
        if (data.toLowerCase().startsWith("error")) {
            var temp = data;
            data = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p><b>%missing_template%</b></p><hr><address>%SERVER_SOFTWARE% %SERVER_VERSION%-%SERVER_STATUS%</address></body><html>";
            data = data.replace("%missing_template%", temp);
        }
        data = parseHeader(data, template, request, map);
        data = ut.replacePaths(data);
        data = ut.replaceFilePaths(data);
        reason = reason != null ? reason : "";
        var host = request.getHeader("host");
        host = host != null ? host : "";
        data = ut.replaceDefaultReplacements(data, nick, sid, skin, room, reason, host);
        data = ut.replaceServerInfo(data);
        data = data.replace("%owner%", owner);
        data = data.replace("%target%", target);
        data = data.replace("%title%", db.getRoomData(roomName, "page_title"));
        return data;
    }

    /**
     * Zeigt das Template im Browser an
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    protected void printTemplate(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var tmp = getTemplate(template, request, map);
        var ut = Bootstrap.boot.getUtil();
        ut.submitContent(tmp, response);
    }

    /**
     * Liefert ein Stylesheet als String aus
     *
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    private String getStyleSheet(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var skin = map.getOrDefault("skin", "");
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        var owner = map.getOrDefault("owner", "");
        skin = ut.parseHost(skin, request)[1];
        var data = db.getStyle(template, skin);
        data = ut.replacePaths(data);
        data = ut.replaceFilePaths(data);
        data = data.replace("%skin%", skin);
        data = ut.replaceServerInfo(data);
        data = ut.replaceDefaultReplacements(data, "", "", skin, "", "", request.getHeader("host"));
        var room = db.getRoomNameByOwner(owner.toLowerCase());
        if (room != null) {
            data = data.replace("%bg_color_1%", db.getRoomData(room, "first_bgcolor"));
            data = data.replace("%bg_color_2%", db.getRoomData(room, "second_bgcolor"));
            data = data.replace("%color%", db.getRoomData(room, "textcolor"));
            data = data.replace("%link_color%", db.getRoomData(room, "linkcolor"));
            data = data.replace("%border_color%", db.getRoomData(room, "bordercolor"));
        }
        return data;
    }

    /**
     * Zeigt ein Stylesheet im Browser an
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    private void printStyleSheet(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var tmp = getStyleSheet(template, request, response, map);
        var ut = Bootstrap.boot.getUtil();
        ut.submitContent(tmp, response);
    }

    /**
     * Liefert ein Script als String aus
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    private String getScript(String template, HttpServletRequest request, Map<String, String> map) {
        var skin = map.getOrDefault("skin", "");
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        var nick = map.getOrDefault("nick", "");
        var room = map.getOrDefault("room2", "");
        var owner = map.getOrDefault("owner", "");
        var target = map.getOrDefault("target", "");
        var sid = map.getOrDefault("sid", "");
        skin = ut.parseHost(skin, request)[1];
        var data = db.getScript(template, skin);
        data = ut.replacePaths(data);
        data = ut.replaceFilePaths(data);
        data = data.replace("%skin%", skin);
        data = data.replace("%room2%", room);
        data = data.replace("%owner%", owner);
        data = data.replace("%target%", target);
        data = ut.replaceServerInfo(data);
        data = ut.replaceDefaultReplacements(data, nick, sid, skin, room, "", request.getHeader("host"));
        return data;
    }

    /**
     * Zeigt ein Script im Browser an
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    private void printScript(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var tmp = getScript(template, request, map);
        var ut = Bootstrap.boot.getUtil();
        ut.submitContent(tmp, response);
    }

    /**
     * Liefert ein Design als String aus
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    private String getPageDesign(String template, HttpServletRequest request, Map<String, String> map) {
        var skin = map.getOrDefault("skin", "");
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        var ut = Bootstrap.boot.getUtil();
        skin = ut.parseHost(skin, request)[1];
        var data = db.getPageDesign(template, skin);
        if (data.toLowerCase().startsWith("error")) {
            var temp = data;
            data = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p><b>%missing_template%</b></p><hr><address>%SERVER_SOFTWARE% %SERVER_VERSION%-%SERVER_STATUS%</address></body><html>";
            data = data.replace("%missing_template%", temp);
        }
        return data;
    }

    /**
     * Liefert eine Seite als String aus
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    private String getPage(String template, HttpServletRequest request, Map<String, String> map) {
        var sid = map.getOrDefault("sid", "");
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        String nick = null;
        String pwd = null;
        String skin = null;
        String room = null;
        String roomName = "";
        var db = conf.getDb();
        var owner = map.getOrDefault("owner", "");
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        var reason = map.getOrDefault("reason", "");
        nick = cm.getNameFromId(sid) == null || map.getOrDefault("nick", "").length() > 0 ? map.getOrDefault("nick", "") : cm.getUser(cm.getNameFromId(sid)).getName();
        if (nick == null || nick.isBlank()) {
            nick = (String) request.getSession().getAttribute("nick");
        }
        if (nick == null) {
            nick = "";
        }
        pwd = map.getOrDefault("pwd", "");
        skin = map.getOrDefault("skin", "");
        room = map.getOrDefault("room", "");
        skin = ut.parseHost(skin, request)[1];
        var data = db.getPage(template, skin);

        if (data.toLowerCase().startsWith("error")) {
            var temp = data;
            data = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p><b>%missing_template%</b></p><hr><address>%SERVER_SOFTWARE% %SERVER_VERSION%-%SERVER_STATUS%</address></body><html>";
            data = data.replace("%missing_template%", temp);
        } else {
            data = parseHeader(data, template, request, map);
            data = ut.replacePaths(data);
            data = ut.replaceFilePaths(data);
            reason = reason != null ? reason : "";
            var host = request.getHeader("host");
            host = host != null ? host : "";
            data = ut.replaceDefaultReplacements(data, nick, sid, skin, room, reason, host);
        }
        data = ut.replaceServerInfo(data);
        data = data.replace("%owner%", owner);
        data = data.replace("%title%", db.getRoomData(roomName, "page_title"));

        return data;
    }

    /**
     * Zeigt eine Seite im Browser an
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    private void printPage(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var tmp = getPage(template, request, map);
        var ut = Bootstrap.boot.getUtil();
        ut.submitContent(tmp, response);
    }

    /**
     * Erzeugt eine neue Sessionid
     *
     * @return Sessionid
     */
    protected String generateSid() {
        var ut = Bootstrap.boot.getUtil();
        ut.setRnd(new Random(currentTimeMillis()));
        return ut.createRandomId();
    }

    /**
     * Zeigt die aktuell hinterlegten Nachrichten an :)
     *
     * @param out Die Output Klasse
     */
    private void readMessage(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        String text = null;
        var sid = map.getOrDefault("sid", "");
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        var skin = map.getOrDefault("skin", "");
        var owner = map.getOrDefault("owner", "");
        skin = ut.parseHost(skin, request)[1];
        var db = conf.getDb();
        var session = request.getSession(false);
        sid = (String) session.getAttribute("sid");
        var sidv = cm.isValidConnectionId(sid);
        var reg = false;
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        String nick = null;
        if (sidv) {
            nick = cm.getNameFromId(sid);
            reg = db.isRegistered(nick);
        }
        String roomName = "";
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        if (!sidv) {
            if (roomName.isBlank()) {
                text = getTemplate("message_invalid_session_id", request, map);
            } else {
                text = getTemplate("message_invalid_session_id_napping", request, map);
            }
        } else if (!reg) {
            if (roomName.isBlank()) {
                text = getTemplate("message_nick_not_registered", request, map);
            } else {
                text = getTemplate("message_invalid_session_id_napping", request, map);
            }
        } else if (db.countMessage(nick) != 0) {
            if (roomName.isBlank()) {
                text = getTemplate("message_new", request, map);
            } else {
                text = getTemplate("message_new_napping", request, map);
            }
            text = text.replace("%count%", String.valueOf(db.countMessage(nick)));
            text = text.replace("%color%", db.getData(nick, "color"));
            text = text.replace("%messages%", db.getMessages(nick, request, map));
            db.delMessages(nick);
        } else {
            if (roomName.isBlank()) {
                text = getTemplate("message_no", request, map);
            } else {
                text = getTemplate("message_no_napping", request, map);
            }
            text = text.replace("%color%", db.getData(nick, "color"));
        }
        ut.submitContent(text, response);
    }

}
