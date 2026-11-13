/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.midiandmore.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.net.InetAddress.getByName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import static net.midiandmore.chat.Bootstrap.logError;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author windo
 */
public class ChatServices {

    // Style sheets: everything behind this comment belongs to the dark theme only
    static final String DARK_THEME_MARKER = "/* theme:dark";

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
    private String readLang(HttpServletRequest request, Map<String, String> map) {
        var ut = Bootstrap.boot.getUtil();
        var cookieLang = ut.readCookieValue(request, "lang");
        if (!cookieLang.isBlank()) {
            return cookieLang;
        }
        var paramLang = map.getOrDefault("lang", "");
        if (paramLang != null && !paramLang.isBlank()) {
            return paramLang;
        }
        var acceptLang = request.getHeader("Accept-Language");
        if (acceptLang != null && acceptLang.toLowerCase().startsWith("de")) {
            return "de";
        }
        return "en";
    }

    private void applyLang(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var ut = Bootstrap.boot.getUtil();
        var lang = readLang(request, map);
        if (lang.isBlank()) {
            return;
        }
        var session = request.getSession(false);
        if (session != null) {
            session.setAttribute("lang", lang);
        }
        var existingCookie = ut.readCookieValue(request, "lang");
        var requestedLang = map.getOrDefault("lang", "");
        if (response != null && (requestedLang != null && !requestedLang.isBlank() || existingCookie.isBlank())) {
            var cookie = new jakarta.servlet.http.Cookie("lang", lang);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24 * 365);
            cookie.setHttpOnly(false);
            response.addCookie(cookie);
        }
    }

    /**
     * Language for pages shown before the chat is configured (no utilities
     * available yet).
     */
    private String setupLang(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if (cookie.getName().equals("lang") && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }
        var acceptLang = request.getHeader("Accept-Language");
        return acceptLang != null && acceptLang.toLowerCase().startsWith("de") ? "de" : "en";
    }

    private String detectScheme(HttpServletRequest request) {
        var xForwardedProto = request.getHeader("X-Forwarded-Proto");
        if (xForwardedProto != null && !xForwardedProto.isBlank()) {
            return xForwardedProto.split(",")[0].trim().toLowerCase();
        }
        return request.isSecure() ? "https" : "http";
    }

    private String stripHash(String value) {
        if (value != null && value.startsWith("#")) {
            return value.substring(1);
        }
        return value;
    }

    protected void parsePage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var map = request.getParameterMap();
        if (Bootstrap.boot.isSetupNeeded()) {
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            var view = new View("system", "setup_required", setupLang(request), new java.util.HashMap<>());
            view.set("setup_url", request.getContextPath() + "/Setup")
                    .set("SERVER_SOFTWARE", Software.SERVER_SOFTWARE)
                    .set("SERVER_YEAR", Software.SERVER_YEAR)
                    .set("SERVER_VENDOR", Software.SERVER_VENDOR);
            response.getWriter().print(view);
            return;
        }
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var cm = Bootstrap.boot.getChatManager();
        Map<String, String> map2 = new HashMap<>();
        for (var key : map.keySet()) {
            String[] arr = {""};
            var value = map.getOrDefault(key, arr);
            map2.put(key, value[0]);
        }
        applyLang(request, response, map2);
        if (map2.getOrDefault("page", "").isBlank()) {
            // Die Startseite
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            startPage("start", request, response, map2);
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
            var captcha = Bootstrap.boot.getCaptcha();
            if (captcha != null) {
                captcha.drawCaptcha(request, response);
            } else {
                response.setStatus(500);
                response.setContentType("text/html; charset=" + conf.getString("charset"));
                ut.submitContent(new View("system", "captcha_unavailable", readLang(request, map2), null), response);
            }
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
        } else if (map2.getOrDefault("page", "").equals(conf.getString("path_stats"))) {
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            statistics(request, response, map2);
        } else if (map2.getOrDefault("page", "").equals("news")) {
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            news(request, response, map2, false);
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
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            help(request, response, map2);
        } else {
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
            logError(ex);
            response.setStatus(500);
            return;
        }
        try {
            filter.doFilter(request, response);
        } catch (IOException ex) {
            logError(ex);
            response.setStatus(500);
            return;
        } catch (ServletException ex) {
            logError(ex);
            response.setStatus(500);
            return;
        }
        if (!filter.error) {
            var service = map.getOrDefault("service", "");
            var change = map.getOrDefault("change", "");
            View text = null;
            if (service.isBlank()) {
                text = getConsole("index", request, map);
                ut.submitContent(text, response);
            } else if (service.equals("news")) {
                news(request, response, map, true);
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
                        text = text.set("room", room);
                    } else {
                        text = getConsole("rooms_config", request, map);
                        topic = db.getRoomData(room, "topic");
                        lock = db.getRoomData(room, "locked");
                        lockReason = db.getRoomData(room, "lock_reason");
                        standard = db.getRoomData(room, "standard");
                        allowSmilies = db.getRoomData(room, "allow_smilies");
                        text = text.set("room", room);
                        text = text.set("topic", topic);
                        text = text.set("lock", lock);
                        text = text.set("lock_reason", lockReason);
                        text = text.set("standard", standard);
                        text = text.set("allow_smilies", allowSmilies);
                    }
                } else if (change.equals("modify")) {
                    if (!exists) {
                        text = getConsole("rooms_fail", request, map);
                        text = text.set("room", room);
                    } else {
                        text = getConsole("rooms_modify", request, map);
                        db.updateRoomData(room, "topic", topic);
                        db.updateRoomData(room, "locked", lock);
                        db.updateRoomData(room, "lock_reason", lockReason);
                        db.updateRoomData(room, "standard", standard);
                        db.updateRoomData(room, "allow_smilies", allowSmilies);
                        text = text.set("room", room);
                        text = text.set("topic", topic);
                        text = text.set("lock", lock);
                        text = text.set("lock_reason", lockReason);
                        text = text.set("standard", standard);
                        text = text.set("allow_smilies", allowSmilies);
                    }
                } else if (change.equals("add_room")) {
                    if (exists) {
                        text = getConsole("rooms_exist", request, map);
                        text = text.set("room", room);
                    } else {
                        text = getConsole("rooms_add", request, map);
                        text = text.set("room", room);
                        text = text.set("topic", topic);
                        text = text.set("lock", "0");
                        text = text.set("lock_reason", lockReason);
                        text = text.set("standard", "0");
                        text = text.set("allow_smilies", "1");
                    }
                } else if (change.equals("set_room")) {
                    if (exists) {
                        text = getConsole("rooms_exist", request, map);
                        text = text.set("room", room);
                    } else {
                        text = getConsole("rooms_set", request, map);
                        text = text.set("room", room);
                        text = text.set("topic", topic);
                        text = text.set("lock", lock);
                        text = text.set("lock_reason", lockReason);
                        text = text.set("standard", standard);
                        text = text.set("allow_smilies", allowSmilies);
                        db.addRoom(room, topic, Integer.valueOf(lock), lockReason, Integer.valueOf(standard), Integer.valueOf(allowSmilies));
                    }
                } else if (change.equals("delete")) {
                    if (!exists) {
                        text = getConsole("rooms_fail", request, map);
                        text = text.set("room", room);
                    } else {
                        text = getConsole("rooms_delete", request, map);
                        text = text.set("room", room);
                        db.delRoom(room);
                    }
                } else {
                    text = getConsole("rooms_404", request, map);
                    text = text.set("room", room);
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
                    text = text.set("success", getConsole("success", request, map));

                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("config.json", "name", "value", "description");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr[0];
                    var description = arr[1];
                    var table = getConsole("chat_table", request, map);
                    table = table.set("description", description);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
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
                    text = text.set("success", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("fun.json", "command", "content", "description");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr[0];
                    var description = arr[1];
                    var table = getConsole("fun_table", request, map);
                    table = table.set("description", description);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
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
                    text = text.set("success", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("cmdcfg.json", "name", "status");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("cmdcfg_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
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
                    text = text.set("success", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("commands.json", "name", "command");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("commands_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
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
                    text = text.set("success", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("hosts.json", "host", "skin");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("host_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
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
                    text = text.set("success", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("emot.json", "name", "emot");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("emot_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
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
                    text = text.set("success", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("mime-types.json", "suffix", "type");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("mime_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
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
                    text = text.set("success", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("mail.json", "name", "value");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("mail_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
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
                    text = text.set("success", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("paths.json", "name", "path");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("path_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
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
                    text = text.set("success", getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("help.json", "name", "html");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = getConsole("help_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
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
                            text = text.set("nick", nick);

                        } else if (change.equals("password")) {
                            var newPwd = map.getOrDefault("new_pwd", "");
                            var newPwd2 = map.getOrDefault("new_pwd2", "");
                            View errorMessage = null;
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
                                    text = text.set("nick", nick);
                                    db.updatePassword(nick, newPwd);
                                } else {
                                    text = getConsole("users_pwd", request, map);
                                    text = text.set("account_error_if_one", getTemplate("account_failed", request, map));
                                    text = text.set("error_message", errorMessage);
                                    text = text.set("nick", nick);
                                }
                            } else {
                                text = getConsole("users_pwd", request, map);
                                text = text.set("account_error_if_one", "");
                                text = text.set("error_message", "");
                                text = text.set("nick", nick);
                            }
                        } else if (change.equals("delete")) {
                            text = getConsole("users_delete", request, map);
                            text = text.set("nick", nick);
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
                            View errorMessage = null;
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
                            text = text.set("mail", mail);
                            text = text.set("day", day);
                            text = text.set("month", month);
                            text = text.set("year", year);
                            text = text.set("status", status);
                            text = text.set("nick", nick);
                            if (errorMessage == null) {
                                text = text.set("account_error_if_one", "");
                            } else {
                                text = text.set("account_error_if_one", errorMessage);
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
                            var instagram = db.getData(nick, "instagram");
                            var linkedin = db.getData(nick, "linkedin");
                            var tiktok = db.getData(nick, "tiktok");
                            var discord = db.getData(nick, "discord");
                            var twitch = db.getData(nick, "twitch");
                            var github = db.getData(nick, "github");
                            var reddit = db.getData(nick, "reddit");
                            var snapchat = db.getData(nick, "snapchat");
                            var pinterest = db.getData(nick, "pinterest");
                            var whatsapp = db.getData(nick, "whatsapp");
                            var telegram = db.getData(nick, "telegram");
                            var fam = db.getData(nick, "fam_status");
                            text = getConsole("users_reg", request, map);
                            text = text.set("nick", db.getData(nick, "nick2"));
                            text = text.set("homepage", homepage);
                            text = text.set("city", city);
                            text = text.set("hobby", hobby);
                            text = text.set("description", description);
                            text = text.set("slogan", slogan);
                            text = text.set("signature", signature);
                            text = text.set("icq", icq);
                            text = text.set("live", live);
                            text = text.set("yahoo", yahoo);
                            text = text.set("facebook", facebook);
                            text = text.set("twitter", twitter);
                            text = text.set("irc", irc);
                            text = text.set("youtube", youtube);
                            text = text.set("instagram", instagram);
                            text = text.set("linkedin", linkedin);
                            text = text.set("tiktok", tiktok);
                            text = text.set("discord", discord);
                            text = text.set("twitch", twitch);
                            text = text.set("github", github);
                            text = text.set("reddit", reddit);
                            text = text.set("snapchat", snapchat);
                            text = text.set("pinterest", pinterest);
                            text = text.set("whatsapp", whatsapp);
                            text = text.set("telegram", telegram);
                            text = text.set("fam_status", fam);
                            text = text.set("success", "");
                        } else if (change.equals("profile_change")) {
                            text = getConsole("users_reg", request, map);
                            text = text.set("success", getConsole("success", request, map));
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
                            var instagram = map.getOrDefault("instagram", "");
                            var linkedin = map.getOrDefault("linkedin", "");
                            var tiktok = map.getOrDefault("tiktok", "");
                            var discord = map.getOrDefault("discord", "");
                            var twitch = map.getOrDefault("twitch", "");
                            var github = map.getOrDefault("github", "");
                            var reddit = map.getOrDefault("reddit", "");
                            var snapchat = map.getOrDefault("snapchat", "");
                            var pinterest = map.getOrDefault("pinterest", "");
                            var whatsapp = map.getOrDefault("whatsapp", "");
                            var telegram = map.getOrDefault("telegram", "");
                            var fam = map.getOrDefault("fam_status", "");
                            text = text.set("nick", db.getData(nick, "nick2"));
                            text = text.set("homepage", homepage);
                            text = text.set("city", city);
                            text = text.set("hobby", hobby);
                            text = text.set("description", description);
                            text = text.set("slogan", slogan);
                            text = text.set("signature", signature);
                            text = text.set("icq", icq);
                            text = text.set("live", live);
                            text = text.set("yahoo", yahoo);
                            text = text.set("facebook", facebook);
                            text = text.set("twitter", twitter);
                            text = text.set("irc", irc);
                            text = text.set("youtube", youtube);
                            text = text.set("instagram", instagram);
                            text = text.set("linkedin", linkedin);
                            text = text.set("tiktok", tiktok);
                            text = text.set("discord", discord);
                            text = text.set("twitch", twitch);
                            text = text.set("github", github);
                            text = text.set("reddit", reddit);
                            text = text.set("snapchat", snapchat);
                            text = text.set("pinterest", pinterest);
                            text = text.set("whatsapp", whatsapp);
                            text = text.set("telegram", telegram);
                            text = text.set("fam_status", fam);
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
                            db.updateNick(nick, "instagram", instagram);
                            db.updateNick(nick, "linkedin", linkedin);
                            db.updateNick(nick, "tiktok", tiktok);
                            db.updateNick(nick, "discord", discord);
                            db.updateNick(nick, "twitch", twitch);
                            db.updateNick(nick, "github", github);
                            db.updateNick(nick, "reddit", reddit);
                            db.updateNick(nick, "snapchat", snapchat);
                            db.updateNick(nick, "pinterest", pinterest);
                            db.updateNick(nick, "whatsapp", whatsapp);
                            db.updateNick(nick, "telegram", telegram);
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
                        View errorMessage = null;
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
                        View rf = null;

                        if (!regSuccess) {
                            rf = getConsole("reg_form", request, map);
                            if (service.equals("reg_done")) {
                                rf = rf.set("reg_error_if_one", getTemplate("reg_failed", request, map));
                                rf = rf.set("error_message", errorMessage);
                            } else {
                                rf = rf.set("reg_error_if_one", "");
                            }
                            cid = generateSid();
                        } else {
                            var color = ut.createRandomColor();
                            db.addNick(gender, nick, mail, color, pwd, question, answer, day, month, year);
                            rf = getConsole("reg_form_success", request, map);
                        }
                        rf = rf.set("reg_nick", nick);
                        rf = rf.set("reg_mail", mail);
                        rf = rf.set("reg_cid", cid);
                        rf = rf.set("reg_captcha", captcha);
                        rf = rf.set("reg_reminder_question", question);
                        rf = rf.set("reg_reminder_answer", answer);
                        rf = rf.set("reg_day", day);
                        rf = rf.set("reg_month", month);
                        rf = rf.set("reg_year", year);
                        rf = rf.set("skin", skin);
                        rf = ut.parseGender(rf, gender);
                        ut.submitContent(rf, response);
                    } else {
                        text = getConsole("users_fail", request, map);
                        text = text.set("nick", nick);
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
            View text = null;
            text = getConsole("401", request, map);
            text = text.set("message", filter.message);
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
        var lang = readLang(request, map);
        String roomName = "";
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var cm = Bootstrap.boot.getChatManager();
        var mailer = Bootstrap.boot.getSendMail();
        var db = conf.getDb();
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        View errorMessage = null;
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
        View rf = null;
        var pwd = ut.createRandomPassword();
        if (regSuccess) {
            try {
                var text = getMail("password", request, map);
                text = text.set("pwd", pwd);
                text = text.set("nick", nick);
                if (mailer != null) {
                    mailer.sendEmail(text, db.getCommand("password_subject", lang), mail);
                }
            } catch (MessagingException me) {
                errorMessage = View.of(me.getLocalizedMessage());
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
                rf = rf.set("reg_error_if_one", getTemplate("pass_failed", request, map));
                rf = rf.set("error_message", errorMessage);
            } else {
                rf = rf.set("reg_error_if_one", "");
            }
            cid = generateSid();
        } else {
            if (roomName.isBlank()) {
                rf = getTemplate("pass_form_success", request, map);
            } else {
                rf = getTemplate("pass_form_success_napping", request, map);
            }
            rf = rf.set("reg_error_if_one", "");
            db.updatePassword2(nick, pwd);
        }
        rf = rf.set("reg_nick", nick);
        rf = rf.set("reg_mail", mail);
        rf = rf.set("reg_cid", cid);
        rf = rf.set("reg_captcha", captcha);
        rf = rf.set("reg_day", day);
        rf = rf.set("reg_month", month);
        rf = rf.set("reg_year", year);
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
        text = text.set("link", ut.parseHtml(url));
        ut.submitContent(text, response);
    }

    /**
     * Zeigt die Topliste an!
     *
     * @param out Die Output-Klasse
     */
    private void profilePicture(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var nick = map.getOrDefault("nick", "");
        var co = Bootstrap.boot.getConfig();
        var db = co.getDb();
        byte[] picture = null;
        String contentType = "image/png";
        if (!nick.isEmpty() && db.isRegistered(nick.toLowerCase())) {
            picture = db.getPicture(nick);
            if (picture != null && picture.length > 0) {
                var dbContentType = db.getData(nick, "image_url");
                if (dbContentType != null && !dbContentType.isEmpty()) {
                    contentType = dbContentType;
                }
            }
        }
        if (picture == null || picture.length == 0) {
            var sb = new StringBuilder();
            sb.append(co.getUh());
            sb.append(co.getFs());
            sb.append(".homewebcom");
            sb.append(co.getFs());
            sb.append("pictures");
            sb.append(co.getFs());
            sb.append("notfound.svg");
            var path = Paths.get(sb.toString());
            try {
                picture = Files.readAllBytes(path);
                contentType = "image/svg+xml";
            } catch (IOException ex) {
                ErrorLog.LOG.log(Level.SEVERE, "Failed to load default profile picture", ex);
                picture = new byte[0];
            }
        }
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType(contentType);
        response.setContentLength(picture.length);
        try (OutputStream out = response.getOutputStream()) {
            out.write(picture);
            out.flush();
        } catch (IOException ex) {
            ErrorLog.LOG.log(Level.SEVERE, "Failed to send profile picture", ex);
        }
    }

    /**
     * Zeigt die Topliste an!
     *
     * @param out Die Output-Klasse
     */
    private void toplist(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        View text;
        var session = request.getSession(false);
        if (session != null && !session.isNew() && "true".equals(session.getAttribute("community"))) {
            var nick = (String) session.getAttribute("nick");
            var reg = nick != null && db.isRegistered(nick);
            if (reg) {
                text = getTemplate("toplist_com_reg", request, map);
            } else {
                text = getTemplate("toplist_com", request, map);
            }
        } else {
            text = getTemplate("toplist", request, map);
        }
        text = text.set("toplist", db.getToplist(request, response, map));
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
        if (session != null) {
            sid = (String) session.getAttribute("sid");
        }
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
        var lang = readLang(request, map);
        var owner = map.getOrDefault("owner", "");
        String roomName = null;
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        View text = null;
        if (roomName == null) {
            text = getTemplate("help", request, map);
        } else {
            text = getTemplate("help_napping", request, map);
        }
        text = ut.parseHelp(text, status, lang);
        ut.submitContent(text, response);
    }


    private void emot(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var skin = map.getOrDefault("skin", "");
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        skin = ut.parseHost(skin, request)[1];
        var db = conf.getDb();
        View text = null;
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
            text = text.set("emot", sb.toString());

        } catch (NullPointerException npe) {
            text = View.of(ut.getStackTrace(npe));
        }

        text = text.set("path_file", conf.getString("path_file"));
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
        View text = null;
        var session = request.getSession(false);
        sid = (String) session.getAttribute("sid");
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        var nick = (String) session.getAttribute("nick");
        nick = nick != null ? nick : map.getOrDefault("nick", "");
        var roomName = "";
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        if (!map.getOrDefault("guestbook", "").isBlank() && sid != null && cm.isValidConnectionId(sid)) {
            var gbNick = cm.getNameFromId(sid);
            var gbText = map.getOrDefault("guestbook_text", "").trim();
            if (!gbText.isEmpty() && gbText.length() <= 1024) {
                db.addGuestbookEntry(user, gbNick, gbText);
            }
        }
        if (!map.getOrDefault("guestbook_delete", "").isBlank() && sid != null && cm.isValidConnectionId(sid) && nick.equalsIgnoreCase(user)) {
            try {
                db.deleteGuestbookEntry(Integer.parseInt(map.get("guestbook_delete")), user);
            } catch (NumberFormatException ignored) {
            }
        }
        if (!cm.isValidConnectionId(sid)) {
            if (!roomName.isBlank()) {
                text = getTemplate("profile_invalid_session_id", request, map);
            } else {
                text = getTemplate("profile_invalid_session_id_napping", request, map);
            }

            text = text.set("user", user);
        } else if (!reg) {
            if (roomName.isBlank()) {
                text = getTemplate("profile_nick_not_registered", request, map);
            } else {
                text = getTemplate("profile_nick_not_registered_napping", request, map);
            }

            text = text.set("user", user);
        } else {
            try {
                var lang = readLang(request, map);
                var profile = lang != null && lang.equalsIgnoreCase("en") && db.getProfileEn() != null
                        ? db.getProfileEn()
                        : db.getProfile();
                if (roomName.isBlank()) {
                    text = getTemplate("profile_user", request, map);
                } else {
                    text = getTemplate("profile_user_napping", request, map);
                }
                var labels = new java.util.HashMap<String, String>();
                var buf = db.getData(user, "homepage");
                var prof = profile.getOrDefault("homepage", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("homepage", prof);
                    text = text.set("homepage", buf);
                } else {
                    labels.put("homepage", "");
                }
                buf = db.getData(user, "city");
                if (buf != null && !buf.equals("")) {
                    text.set("city", buf);
                } else {
                    text.set("city", profile.getOrDefault("unknown", ""));
                }
                buf = db.getData(user, "hobby");
                prof = profile.getOrDefault("hobby", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("hobby", prof);
                    text = text.set("hobby", buf);
                } else {
                    labels.put("hobby", "");
                }
                buf = db.getData(user, "status");
                prof = profile.getOrDefault("status", "");
                var stat = db.getLongData(user, "status").intValue();
                if (stat >= 10) {
                    buf = Bootstrap.boot.getChatManager().getCommand(nick, "status_10");
                } else if (stat == 9) {
                    buf = Bootstrap.boot.getChatManager().getCommand(nick, "status_9");
                } else if (stat == 8) {
                    buf = Bootstrap.boot.getChatManager().getCommand(nick, "status_8");
                } else if (stat == 7) {
                    buf = Bootstrap.boot.getChatManager().getCommand(nick, "status_7");
                } else if (stat == 6) {
                    buf = Bootstrap.boot.getChatManager().getCommand(nick, "status_6");
                } else if (stat == 5) {
                    buf = Bootstrap.boot.getChatManager().getCommand(nick, "status_5");
                } else if (stat == 4) {
                    buf = Bootstrap.boot.getChatManager().getCommand(nick, "status_4");
                } else if (stat == 3) {
                    buf = Bootstrap.boot.getChatManager().getCommand(nick, "status_3");
                } else if (stat == 2) {
                    buf = Bootstrap.boot.getChatManager().getCommand(nick, "status_2");
                } else {
                    buf = Bootstrap.boot.getChatManager().getCommand(nick, "status_1");
                }
                if (buf != null && !buf.equals("")) {
                    labels.put("status", prof);
                    text = text.set("status", buf);
                } else {
                    labels.put("status", "");
                }
                buf = db.getData(user, "description");
                prof = profile.getOrDefault("description", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("description", prof);
                    text = text.set("description", buf);
                } else {
                    labels.put("description", "");
                }
                buf = db.getData(user, "slogan");
                prof = profile.getOrDefault("slogan", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("slogan", prof);
                    text = text.set("slogan", buf);
                } else {
                    labels.put("slogan", "");
                }
                buf = db.getData(user, "signature");
                prof = profile.getOrDefault("signature", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("signature", prof);
                    text = text.set("signature", buf);
                } else {
                    labels.put("signature", "");
                }
                buf = db.getLongData(user, "icq") != 0 ? db.getData(user, "icq") : null;
                prof = profile.getOrDefault("icq", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("icq", prof);
                    text = text.set("icq", buf);
                } else {
                    labels.put("icq", "");
                }
                buf = db.getData(user, "live");
                prof = profile.getOrDefault("skype", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("skype", prof);
                    text = text.set("live", buf);
                } else {
                    labels.put("skype", "");
                }
                buf = db.getData(user, "yahoo");
                prof = profile.getOrDefault("yahoo", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("yahoo", prof);
                    text = text.set("yahoo", buf);
                } else {
                    labels.put("yahoo", "");
                }
                buf = db.getData(user, "facebook");
                prof = profile.getOrDefault("facebook", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("facebook", prof);
                    text = text.set("facebook", buf);
                } else {
                    labels.put("facebook", "");
                }
                buf = db.getData(user, "twitter");
                prof = profile.getOrDefault("twitter", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("twitter", prof);
                    text = text.set("twitter", buf);
                } else {
                    labels.put("twitter", "");
                }
                buf = db.getData(user, "irc");
                prof = profile.getOrDefault("irc", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("irc", prof);
                    text = text.set("irc", buf);
                } else {
                    labels.put("irc", "");
                }
                buf = db.getData(user, "youtube");
                prof = profile.getOrDefault("youtube", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("youtube", prof);
                    text = text.set("youtube", buf);
                } else {
                    labels.put("youtube", "");
                }
                buf = db.getData(user, "instagram");
                prof = profile.getOrDefault("instagram", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("instagram", prof);
                    text = text.set("instagram", buf);
                } else {
                    labels.put("instagram", "");
                }
                buf = db.getData(user, "linkedin");
                prof = profile.getOrDefault("linkedin", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("linkedin", prof);
                    text = text.set("linkedin", buf);
                } else {
                    labels.put("linkedin", "");
                }
                buf = db.getData(user, "tiktok");
                prof = profile.getOrDefault("tiktok", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("tiktok", prof);
                    text = text.set("tiktok", buf);
                } else {
                    labels.put("tiktok", "");
                }
                buf = db.getData(user, "discord");
                prof = profile.getOrDefault("discord", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("discord", prof);
                    text = text.set("discord", buf);
                } else {
                    labels.put("discord", "");
                }
                buf = db.getData(user, "twitch");
                prof = profile.getOrDefault("twitch", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("twitch", prof);
                    text = text.set("twitch", buf);
                } else {
                    labels.put("twitch", "");
                }
                buf = db.getData(user, "github");
                prof = profile.getOrDefault("github", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("github", prof);
                    text = text.set("github", buf);
                } else {
                    labels.put("github", "");
                }
                buf = db.getData(user, "reddit");
                prof = profile.getOrDefault("reddit", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("reddit", prof);
                    text = text.set("reddit", buf);
                } else {
                    labels.put("reddit", "");
                }
                buf = db.getData(user, "snapchat");
                prof = profile.getOrDefault("snapchat", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("snapchat", prof);
                    text = text.set("snapchat", buf);
                } else {
                    labels.put("snapchat", "");
                }
                buf = db.getData(user, "pinterest");
                prof = profile.getOrDefault("pinterest", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("pinterest", prof);
                    text = text.set("pinterest", buf);
                } else {
                    labels.put("pinterest", "");
                }
                buf = db.getData(user, "whatsapp");
                prof = profile.getOrDefault("whatsapp", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("whatsapp", prof);
                    text = text.set("whatsapp", buf);
                } else {
                    labels.put("whatsapp", "");
                }
                buf = db.getData(user, "telegram");
                prof = profile.getOrDefault("telegram", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("telegram", prof);
                    text = text.set("telegram", buf);
                } else {
                    labels.put("telegram", "");
                }
                buf = db.getData(user, "visitors");
                prof = profile.getOrDefault("last_visitor", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("last_visitor", prof);
                    text = text.set("visitors", buf);
                } else {
                    labels.put("last_visitor", "");
                }
                buf = db.getData(user, "fam_status");
                prof = profile.getOrDefault("fam_status", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("fam_status", prof);
                    text = text.set("fam_status", buf);
                } else {
                    labels.put("fam_status", "");
                }
                buf = db.getData(user, "image_url");
                prof = profile.getOrDefault("image", "");
                if (buf != null && !buf.equals("")) {
                    labels.put("image", prof);
                    text = text.set("image_url", buf);
                } else {
                    labels.put("image", "");
                }
                buf = db.getData(user, "sex");
                buf = switch (buf) {
                    case "m" ->
                        profile.getOrDefault("gender_m", "");
                    case "f" ->
                        profile.getOrDefault("gender_f", "");
                    default ->
                        profile.getOrDefault("gender_n", "");
                };
                text.set("profile", labels);
                text = text.set("sex", buf);
                text = text.set("user", db.getData(user, "nick2"));
                text = text.set("homepage", db.getData(user, "homepage") == null ? "" : db.getData(user, "homepage"));
                text = text.set("points", db.getData(user, "points"));
                text = text.set("timestamp_login", ut.getTime(db.getLongData(user, "timestamp_login")));
                text = text.set("timestamp_reg", ut.getTime(db.getLongData(user, "timestamp_reg")));
                text = text.set("bday_day", db.getData(user, "bday_day") == null ? "" : db.getData(user, "bday_day"));
                text = text.set("bday_month", db.getData(user, "bday_month") == null ? "" : db.getData(user, "bday_month"));
                text = text.set("bday_year", db.getData(user, "bday_year") == null ? "" : db.getData(user, "bday_year"));
                text = text.set("login_room", db.getData(user, "login_room") == null ? "" : db.getData(user, "login_room"));
                text = text.set("color", db.getData(user, "color") == null ? "" : db.getData(user, "color"));
                var gbSb = new StringBuilder();
                var gbEntries = db.getGuestbook(user);
                var isOwner = nick.equalsIgnoreCase(user);
                String pathProfile = "";
                String pathProfileFile = "";
                if (isOwner) {
                    pathProfile = text.expand("%PATH_[profile]%");
                    pathProfileFile = text.expand("%path_profile%");
                }
                for (var entry : gbEntries) {
                    var eText = getTemplate("guestbook_entry", request, map);
                    eText = eText.set("gbk_id", entry[0]);
                    eText = eText.set("gbk_nick", entry[1]);
                    eText = eText.set("gbk_color", entry[2]);
                    eText = eText.set("gbk_text", entry[3]);
                    eText = eText.set("gbk_time", entry[4]);
                    if (isOwner) {
                        var deleteForm = "<form action=\"" + pathProfile + "\" method=\"get\" accept-charset=\"utf-8\" style=\"display:inline;margin-left:8px;\">" +
                                "<input type=\"hidden\" name=\"page\" value=\"" + pathProfileFile + "\">" +
                                "<input type=\"hidden\" name=\"user\" value=\"" + map.getOrDefault("user", "") + "\">" +
                                "<input type=\"hidden\" name=\"owner\" value=\"" + owner + "\">" +
                                "<input type=\"hidden\" name=\"skin\" value=\"" + skin + "\">" +
                                "<input type=\"hidden\" name=\"guestbook_delete\" value=\"" + entry[0] + "\">" +
                                "<button class=\"btn btn-sm btn-outline-danger\" type=\"submit\">Löschen</button></form>";
                        eText = eText.set("gbk_delete_form", deleteForm);
                    } else {
                        eText = eText.set("gbk_delete_form", "");
                    }
                    gbSb.append(eText);
                }
                if (db.isRegistered(nick) && sid != null && cm.isValidConnectionId(sid)) {
                    var form = getTemplate("guestbook_form", request, map);
                    form = form.set("user", user);
                    form = form.set("nick", nick);
                    gbSb.append(form);
                }
                text = text.set("GUESTBOOK", gbSb.toString());
            } catch (NullPointerException npe) {
                text = View.of(ut.getStackTrace(npe));
            }
        }
        db.updateNick(user, "visitors", nick);
        ut.submitContent(text, response);
    }

    /**
     * Deinen Account verwalten
     *
     * @param out Die Output-Klasse
     */
    private void accountForm(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        View text = null;
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
            String instagram = null;
            String linkedin = null;
            String tiktok = null;
            String discord = null;
            String twitch = null;
            String github = null;
            String reddit = null;
            String snapchat = null;
            String pinterest = null;
            String whatsapp = null;
            String telegram = null;
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
                    instagram = db.getData(nick, "instagram");
                    linkedin = db.getData(nick, "linkedin");
                    tiktok = db.getData(nick, "tiktok");
                    discord = db.getData(nick, "discord");
                    twitch = db.getData(nick, "twitch");
                    github = db.getData(nick, "github");
                    reddit = db.getData(nick, "reddit");
                    snapchat = db.getData(nick, "snapchat");
                    pinterest = db.getData(nick, "pinterest");
                    whatsapp = db.getData(nick, "whatsapp");
                    telegram = db.getData(nick, "telegram");
                    fam = db.getData(nick, "fam_status");
                    if (!roomName.isBlank()) {
                        text = getTemplate("account_form_profile", request, map);
                    } else {
                        text = getTemplate("account_form_profile_napping", request, map);
                    }
                    text = text.set("homepage", homepage);
                    text = text.set("city", city);
                    text = text.set("hobby", hobby);
                    text = text.set("description", description);
                    text = text.set("slogan", slogan);
                    text = text.set("signature", signature);
                    text = text.set("icq", icq);
                    text = text.set("live", live);
                    text = text.set("yahoo", yahoo);
                    text = text.set("facebook", facebook);
                    text = text.set("twitter", twitter);
                    text = text.set("irc", irc);
                    text = text.set("youtube", youtube);
                    text = text.set("instagram", instagram);
                    text = text.set("linkedin", linkedin);
                    text = text.set("tiktok", tiktok);
                    text = text.set("discord", discord);
                    text = text.set("twitch", twitch);
                    text = text.set("github", github);
                    text = text.set("reddit", reddit);
                    text = text.set("snapchat", snapchat);
                    text = text.set("pinterest", pinterest);
                    text = text.set("whatsapp", whatsapp);
                    text = text.set("telegram", telegram);
                    text = text.set("fam_status", fam);
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
                    instagram = map.getOrDefault("instagram", "");
                    linkedin = map.getOrDefault("linkedin", "");
                    tiktok = map.getOrDefault("tiktok", "");
                    discord = map.getOrDefault("discord", "");
                    twitch = map.getOrDefault("twitch", "");
                    github = map.getOrDefault("github", "");
                    reddit = map.getOrDefault("reddit", "");
                    snapchat = map.getOrDefault("snapchat", "");
                    pinterest = map.getOrDefault("pinterest", "");
                    whatsapp = map.getOrDefault("whatsapp", "");
                    telegram = map.getOrDefault("telegram", "");
                    fam = map.getOrDefault("fam_status", "");
                    if (!roomName.isBlank()) {
                        text = getTemplate("account_form_profile_success", request, map);
                    } else {
                        text = getTemplate("account_form_profile_success_napping", request, map);
                    }
                    text = text.set("homepage", homepage);
                    text = text.set("city", city);
                    text = text.set("hobby", hobby);
                    text = text.set("description", description);
                    text = text.set("slogan", slogan);
                    text = text.set("signature", signature);
                    text = text.set("icq", icq);
                    text = text.set("live", live);
                    text = text.set("yahoo", yahoo);
                    text = text.set("facebook", facebook);
                    text = text.set("twitter", twitter);
                    text = text.set("irc", irc);
                    text = text.set("youtube", youtube);
                    text = text.set("instagram", instagram);
                    text = text.set("linkedin", linkedin);
                    text = text.set("tiktok", tiktok);
                    text = text.set("discord", discord);
                    text = text.set("twitch", twitch);
                    text = text.set("github", github);
                    text = text.set("reddit", reddit);
                    text = text.set("snapchat", snapchat);
                    text = text.set("pinterest", pinterest);
                    text = text.set("whatsapp", whatsapp);
                    text = text.set("telegram", telegram);
                    text = text.set("fam_status", fam);
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
                    db.updateNick(nick, "instagram", instagram);
                    db.updateNick(nick, "linkedin", linkedin);
                    db.updateNick(nick, "tiktok", tiktok);
                    db.updateNick(nick, "discord", discord);
                    db.updateNick(nick, "twitch", twitch);
                    db.updateNick(nick, "github", github);
                    db.updateNick(nick, "reddit", reddit);
                    db.updateNick(nick, "snapchat", snapchat);
                    db.updateNick(nick, "pinterest", pinterest);
                    db.updateNick(nick, "whatsapp", whatsapp);
                    db.updateNick(nick, "telegram", telegram);
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
                    View errorMessage = null;
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
                        text = text.set("account_error_if_one", getTemplate("account_failed", request, map));
                        text = text.set("error_message", errorMessage);
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
                    View errorMessage = null;
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

                        text = text.set("account_error_if_one", getTemplate("account_failed", request, map));
                        text = text.set("error_message", errorMessage);
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
                    bg_color_1 = stripHash(map.getOrDefault("bg_color_1", ""));
                    bg_color_2 = stripHash(map.getOrDefault("bg_color_2", ""));
                    color = stripHash(map.getOrDefault("color", ""));
                    link_color = stripHash(map.getOrDefault("link_color", ""));
                    border_color = stripHash(map.getOrDefault("border_color", ""));
                    room = map.getOrDefault("room2", "");
                    if (bg_color_1.length() != 6 || !bg_color_1.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = getTemplate("account_form_napping_color_napping", request, map);
                        }
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (bg_color_2.length() != 6 || !bg_color_2.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = getTemplate("account_form_napping_color_napping", request, map);
                        }
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (border_color.length() != 6 || !border_color.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = getTemplate("account_form_napping_color_napping", request, map);
                        }
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (color.length() != 6 || !color.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = getTemplate("account_form_napping_color_napping", request, map);
                        }
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (link_color.length() != 6 || !link_color.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = getTemplate("account_form_napping_color_napping", request, map);
                        }
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (db.ownerExists(nick) && !db.getRoomNameByOwner(nick).equals(room)) {
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_used", request, map);
                        } else {
                            text = getTemplate("account_form_napping_used_napping", request, map);
                        }
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
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
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (!db.roomExists(room)) {
                        db.addRoomData(nick, room, title, bg_color_1, bg_color_2, color, border_color, link_color);
                        db.updateRoomData(room, "su", nick);
                        if (roomName.isBlank()) {
                            text = getTemplate("account_form_napping_add", request, map);
                        } else {
                            text = getTemplate("account_form_napping_add_napping", request, map);
                        }
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else {
                        text = getTemplate("account_form_napping_error", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
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
                    text = text.set("title", title);
                    text = text.set("bg_color_1", bg_color_1);
                    text = text.set("bg_color_2", bg_color_2);
                    text = text.set("color", color);
                    text = text.set("link_color", link_color);
                    text = text.set("border_color", border_color);
                    text = text.set("room2", room);
                    text = text.set("nick", nick);
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
            View errorMessage = null;
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
                        text = text.set("account_error_if_one", getTemplate("account_failed", request, map));
                        text = text.set("error_message", errorMessage);
                        text = text.set("mail", mail);
                        text = text.set("day", day);
                        text = text.set("month", month);
                        text = text.set("year", year);
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
                    text = text.set("mail", mail);
                    text = text.set("day", day);
                    text = text.set("month", month);
                    text = text.set("year", year);
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
        text = text.set("account_error_if_one", "");
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
        View text = null;
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
        var edit = map.getOrDefault("edit", "");
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
            text = text.set("color", color);
            text = text.set("nick", nick);
            text = text.set("user", db.getData(target, "nick2"));
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
                var newPostId = db.addThread(map.getOrDefault("topic", ""), map.getOrDefault("thread", ""), Long.valueOf(ref), Long.valueOf(db.getData(nick, "id")), 0, ip, Long.valueOf(bid));
                text = getTemplate("boards_added", request, map);
                text = text.set("id", bid);
                pid = String.valueOf(newPostId);
                text = text.set("pid", pid);
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
                    text = text.set("id", bid);
                    text = text.set("topic", ut.parseHtml(topic));
                    text = text.set("description", ut.parseHtml(description));
                }
            }
        } else if (!edit.isBlank()) {
            var edited = map.getOrDefault("edited", "").equalsIgnoreCase("true");
            var threadId = Long.valueOf(pid);
            var thread = db.getBoardThreadById(threadId);
            if (thread == null) {
                text = getTemplate("boards_not_exists", request, map);
            } else if (!moderator && !((Long) thread[3]).equals(Long.valueOf(db.getData(nick, "id")))) {
                text = getTemplate("boards_thread_moderator", request, map);
            } else {
                if (edited) {
                    var topic = map.getOrDefault("topic", "");
                    var content = map.getOrDefault("thread", "");
                    db.updateThread(threadId, "topic", topic);
                    db.updateThread(threadId, "content", content);
                    text = getTemplate("boards_thread_edited", request, map);
                    text = text.set("id", bid);
                    text = text.set("pid", pid);
                } else {
                    text = getTemplate("boards_thread_edit_form", request, map);
                    text = text.set("id", bid);
                    text = text.set("pid", pid);
                    text = text.set("topic", ut.parseHtml(String.valueOf(thread[0])));
                    text = text.set("content", ut.parseHtml(String.valueOf(thread[1])));
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
                    text = text.set("id", bid);
                    text = text.set("pid", pid);
                } else {
                    db.updateBoards(Long.valueOf(bid), "deleted", "1");
                    text = getTemplate("boards_deleted", request, map);
                    text = text.set("id", bid);;
                    text = text.set("pid", pid);
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
                    text = text.set("id", bid);
                    text = text.set("pid", pid);
                } else {
                    db.updateBoards(Long.valueOf(bid), "readonly", "1");
                    text = getTemplate("boards_close", request, map);
                    text = text.set("id", bid);;
                    text = text.set("pid", pid);
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
                if (pages == 0) {
                    pages = 1;
                }
                StringBuilder pageContent = new StringBuilder();
                for (long l = 1; l <= pages; l++) {
                    if (l != Long.valueOf(page)) {
                        var content = getTemplate("boards_page", request, map);
                        content = content.set("page", String.valueOf(l));
                        content = content.set("pid", pid);
                        content = content.set("id", bid);
                        pageContent.append(content);
                        pageContent.append(" ");
                    } else {
                        var content = getTemplate("boards_page_active", request, map);
                        content = content.set("page", String.valueOf(l));
                        content = content.set("pid", pid);
                        content = content.set("id", bid);
                        pageContent.append(content);
                        pageContent.append(" ");
                    }
                }
                if (page.equals("1")) {
                    text = text.set("prev", getTemplate("boards_page_prev_disabled", request, map));
                } else {
                    var content = getTemplate("boards_page_prev_enabled", request, map);
                    content = content.set("page", String.valueOf(Long.valueOf(page) - 1));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    text = text.set("prev", content);
                }
                if (Long.valueOf(page) == pages) {
                    text = text.set("next", getTemplate("boards_page_next_disabled", request, map));
                } else {
                    var content = getTemplate("boards_page_next_enabled", request, map);
                    content = content.set("page", String.valueOf(Long.valueOf(page) + 1));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    text = text.set("next", content);
                }
                text = text.set("pages", pageContent.toString());
                text = text.set("count", String.valueOf(count));
                topic[0] = "";
                board.forEach((boards) -> {
                    if ((Long) boards[10] == 0) {
                        var brd = getTemplate("boards_thread", request, map);
                        topic[0] = String.valueOf(boards[0]);
                        brd = brd.set("topic", ut.parseHtml(String.valueOf(boards[0])));
                        var content = String.valueOf(boards[1]);
                        brd = brd.set("content", ut.parseBb(ut.parseHtml(content)));
                        brd = brd.set("ref", String.valueOf(boards[2]));
                        var user = db.getData((Long) boards[3], "nick2");
                        var signature = ut.parseBb(ut.parseHtml(db.getData((Long) boards[3], "signature")));
                        if (moderator) {
                            brd = brd.set("delete", getTemplate("boards_thread_delete", request, map));
                            if (boards[2].equals(boards[8]) && String.valueOf(boards[9]).equals("0")) {
                                brd = brd.set("close", getTemplate("boards_thread_close", request, map));
                            } else if (String.valueOf(boards[9]).equals("1")) {
                                brd = brd.set("close", getTemplate("boards_thread_open", request, map));
                            } else {
                                brd = brd.set("close", "");
                            }
                            if (db.isBanned(user)) {
                                brd = brd.set("ban", getTemplate("boards_thread_unban", request, map));
                            } else {
                                brd = brd.set("ban", getTemplate("boards_thread_ban", request, map));
                            }
                        } else {
                            brd = brd.set("delete", "");
                            brd = brd.set("close", "");
                            brd = brd.set("ban", "");
                        }
                        if (user.equalsIgnoreCase(nick) || moderator) {
                            brd = brd.set("edit", getTemplate("boards_thread_edit", request, map));
                        } else {
                            brd = brd.set("edit", "");
                        }
                        brd = brd.set("uid", db.getData(user, "id"));
                        brd = brd.set("user", user);
                        brd = brd.set("color", db.getData(user, "color"));
                        brd = brd.set("board", String.valueOf(boards[4]));
                        brd = brd.set("posted", ut.getTime((Long) boards[5]));
                        brd = brd.set("ip", String.valueOf(boards[6]));
                        brd = brd.set("cat", String.valueOf(boards[7]));
                        brd = brd.set("id", String.valueOf(boards[8]));
                        brd = brd.set("closed", String.valueOf(boards[9]));
                        brd = brd.set("deleted", String.valueOf(boards[10]));
                        brd = brd.set("signature", signature);
                        if (db.isBanned(user)) {
                            brd = brd.set("banned", getTemplate("boards_thread_banned", request, map));
                        } else {
                            brd = brd.set("banned", "");
                        }
                        id[0] = (Long) boards[7];
                        sb.append(brd);
                    } else {
                        var brd = getTemplate("boards_thread_deleted", request, map);
                        topic[0] = String.valueOf(boards[0]);
                        brd = brd.set("topic", ut.parseHtml(String.valueOf(boards[0])));
                        var content = String.valueOf(boards[1]);
                        brd = brd.set("content", ut.parseBb(ut.parseHtml(content)));
                        brd = brd.set("ref", String.valueOf(boards[2]));
                        var user = db.getData((Long) boards[3], "nick2");
                        if (db.getData(user, "moderator").equals("1")) {
                            brd = brd.set("delete", getTemplate("boards_thread_undelete", request, map));
                        } else {
                            brd = brd.set("delete", "");
                        }
                        brd = brd.set("user", user);
                        brd = brd.set("color", db.getData(user, "color"));
                        brd = brd.set("board", String.valueOf(boards[4]));
                        brd = brd.set("posted", ut.getTime((Long) boards[5]));
                        brd = brd.set("ip", String.valueOf(boards[6]));
                        brd = brd.set("cat", String.valueOf(boards[7]));
                        brd = brd.set("id", String.valueOf(boards[8]));
                        brd = brd.set("closed", String.valueOf(boards[9]));
                        brd = brd.set("deleted", String.valueOf(boards[10]));
                        if (db.isBanned(user)) {
                            brd = brd.set("banned", getTemplate("boards_thread_banned", request, map));
                        } else {
                            brd = brd.set("banned", "");
                        }
                        id[0] = (Long) boards[7];
                        sb.append(brd);
                    }
                });
                if (sb.toString().isBlank()) {
                    text = text.set("boards", getTemplate("boards_empty2", request, map));
                } else {
                    text = text.set("boards", sb.toString());

                }
                text = text.set("id", String.valueOf(id[0]));
                text = text.set("pid", pid);
                text = text.set("re", topic[0].startsWith(conf.getString("board_re")) ? "" : conf.getString("board_re"));
                text = text.set("topic", ut.parseHtml(topic[0]));
            }
        } else if (!search.isBlank()) {
            ArrayList<Object[]> board = db.searchBoard(Long.valueOf(ref), Long.valueOf(page), search);
            var count = db.countBoardTreads3(search);
            StringBuilder sb = new StringBuilder();
            String[] topic = new String[1];
            long[] id = new long[1];
            id[0] = 0;
            long pages = (long) Math.ceil((float) count / conf.getLong("board_pages"));
            if (pages == 0) {
                pages = 1;
            }
            text = getTemplate("boards3", request, map);
            StringBuilder pageContent = new StringBuilder();
            for (long l = 1; l <= pages; l++) {
                if (l != Long.valueOf(page)) {
                    var content = getTemplate("boards_page", request, map);
                    content = content.set("page", String.valueOf(l));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                } else {
                    var content = getTemplate("boards_page_active", request, map);
                    content = content.set("page", String.valueOf(l));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                }
            }
            if (page.equals("1")) {
                text = text.set("prev", getTemplate("boards_page_prev_disabled", request, map));
            } else {
                var content = getTemplate("boards_page_prev_enabled", request, map);
                content = content.set("page", String.valueOf(Long.valueOf(page) - 1));
                content = content.set("pid", pid);
                content = content.set("id", bid);
                text = text.set("prev", content);
            }
            if (Long.valueOf(page) == pages) {
                text = text.set("next", getTemplate("boards_page_next_disabled", request, map));
            } else {
                var content = getTemplate("boards_page_next_enabled", request, map);
                content = content.set("page", String.valueOf(Long.valueOf(page) + 1));
                content = content.set("pid", pid);
                content = content.set("id", bid);
                text = text.set("next", content);
            }
            text = text.set("pages", pageContent.toString());
            text = text.set("count", String.valueOf(count));
            topic[0] = "";
            board.forEach((boards) -> {
                if ((Long) boards[10] == 0) {
                    var brd = getTemplate("boards_thread", request, map);
                    topic[0] = String.valueOf(boards[0]);
                    brd = brd.set("topic", ut.parseHtml(String.valueOf(boards[0])));
                    var content = String.valueOf(boards[1]);
                    brd = brd.set("content", ut.parseBb(ut.parseHtml(content)));
                    brd = brd.set("ref", String.valueOf(boards[2]));
                    var user = db.getData((Long) boards[3], "nick2");
                    if (user.equalsIgnoreCase(nick) || moderator) {
                        brd = brd.set("edit", getTemplate("boards_thread_edit", request, map));
                    } else {
                        brd = brd.set("edit", "");
                    }
                    if (moderator) {
                        if (db.isBanned(user)) {
                            brd = brd.set("ban", getTemplate("boards_thread_unban", request, map));
                        } else {
                            brd = brd.set("ban", getTemplate("boards_thread_ban", request, map));
                        }
                    } else {
                        brd = brd.set("ban", "");
                    }
                    var signature = ut.parseBb(ut.parseHtml(db.getData((Long) boards[3], "signature")));
                    brd = brd.set("delete", "");
                    brd = brd.set("close", "");
                    brd = brd.set("user", user);
                    brd = brd.set("color", db.getData(user, "color"));
                    brd = brd.set("uid", String.valueOf(boards[3]));
                    brd = brd.set("board", String.valueOf(boards[4]));
                    brd = brd.set("posted", ut.getTime((Long) boards[5]));
                    brd = brd.set("ip", String.valueOf(boards[6]));
                    brd = brd.set("cat", String.valueOf(boards[7]));
                    brd = brd.set("id", String.valueOf(boards[8]));
                    brd = brd.set("closed", String.valueOf(boards[9]));
                    brd = brd.set("deleted", String.valueOf(boards[10]));
                    brd = brd.set("signature", signature);
                    if (db.isBanned(user)) {
                        brd = brd.set("banned", getTemplate("boards_thread_banned", request, map));
                    } else {
                        brd = brd.set("banned", "");
                    }
                    id[0] = (Long) boards[7];
                    sb.append(brd);
                } else {
                    var brd = getTemplate("boards_thread_deleted", request, map);
                    topic[0] = String.valueOf(boards[0]);
                    brd = brd.set("topic", ut.parseHtml(String.valueOf(boards[0])));
                    var content = String.valueOf(boards[1]);
                    brd = brd.set("content", ut.parseBb(ut.parseHtml(content)));
                    brd = brd.set("ref", String.valueOf(boards[2]));
                    var user = db.getData((Long) boards[3], "nick2");
                    if (db.getData(user, "moderator").equals("1")) {
                        brd = brd.set("delete", getTemplate("boards_thread_undelete", request, map));
                    } else {
                        brd = brd.set("delete", "");
                    }
                    brd = brd.set("user", user);
                    brd = brd.set("color", db.getData(user, "color"));
                    brd = brd.set("board", String.valueOf(boards[4]));
                    brd = brd.set("posted", ut.getTime((Long) boards[5]));
                    brd = brd.set("ip", String.valueOf(boards[6]));
                    brd = brd.set("cat", String.valueOf(boards[7]));
                    brd = brd.set("id", String.valueOf(boards[8]));
                    brd = brd.set("closed", String.valueOf(boards[9]));
                    brd = brd.set("deleted", String.valueOf(boards[10]));
                    if (db.isBanned(user)) {
                        brd = brd.set("banned", getTemplate("boards_thread_banned", request, map));
                    } else {
                        brd = brd.set("banned", "");
                    }
                    id[0] = (Long) boards[7];
                    sb.append(brd);
                }
            });
            if (sb.toString().isBlank()) {
                text = text.set("boards", getTemplate("boards_empty2", request, map));
            } else {
                text = text.set("boards", sb.toString());

            }
            text = text.set("id", String.valueOf(id[0]));
            text = text.set("pid", pid);
            text = text.set("re", topic[0].startsWith(conf.getString("board_re")) ? "" : conf.getString("board_re"));
            text = text.set("topic", ut.parseHtml(topic[0]));
        } else if (!start.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");

            if (locked && !moderator) {
                text = getTemplate("boards_locked", request, map);
            } else {
                var add = map.getOrDefault("add", "");
                if (add.isBlank()) {
                    text = getTemplate("boards_add", request, map);
                    text = text.set("id", bid);
                } else {
                    var newPostId = db.addThread(map.getOrDefault("topic", ""), map.getOrDefault("thread", ""), 0, Long.valueOf(db.getData(nick, "id")), 0, ip, Long.valueOf(bid));
                    text = getTemplate("boards_added", request, map);
                    pid = String.valueOf(newPostId);
                    db.updateThread(Long.valueOf(pid), "ref", pid);
                    text = text.set("id", bid);
                    text = text.set("pid", pid);
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
                    text = text.set("cat", cat);
                    text = text.set("topic", ut.parseHtml(db.getBoardCat(Long.valueOf(cat), "topic")));
                    text = text.set("description", ut.parseHtml(db.getBoardCat(Long.valueOf(cat), "description")));
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
                    text = text.set("cat", cat);
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
            var category = new StringBuilder();
            for (Object[] cats : cat.values()) {
                var id = String.valueOf(cats[0]);
                var topic = String.valueOf(cats[1]);
                var description = String.valueOf(cats[2]);
                var deleted = (Integer) cats[3];
                View categoryRow;
                if (moderator) {
                    categoryRow = getTemplate("board_cats_moderated2", request, map);
                    categoryRow.set("delete", getTemplate(deleted == 0 ? "board_cats_del" : "board_cats_open", request, map));
                    categoryRow.set("edit", getTemplate("board_cats_edit", request, map));
                } else {
                    if (deleted == 1) {
                        continue;
                    }
                    categoryRow = getTemplate("board_cats", request, map);
                    categoryRow.set("delete", "");
                    categoryRow.set("edit", "");
                }
                categoryRow.set("id", id);
                categoryRow.set("topic", ut.parseHtml(topic));
                categoryRow.set("description", ut.parseBb(ut.parseHtml(description)));
                TreeMap<Long, Object[]> board = db.getBoards(Long.valueOf(id));
                var in = new StringBuilder();
                var locked = false;
                for (Object[] boards : board.values()) {
                    locked = String.valueOf(boards[2]).equals("1");
                    var deleted2 = String.valueOf(boards[6]).equals("1");
                    View boardRow;
                    if (!moderator) {
                        if (deleted2) {
                            continue;
                        }
                        boardRow = getTemplate("board_boards", request, map);
                    } else {
                        boardRow = getTemplate("board_boards_moderated", request, map);
                        boardRow.set("del", getTemplate(!deleted2 ? "board_boards_moderated_del" : "board_boards_moderated_undel", request, map));
                        boardRow.set("edit", getTemplate("board_boards_moderated_edit", request, map));
                        var readonly = String.valueOf(boards[2]).equals("1");
                        boardRow.set("lock", getTemplate(readonly ? "board_boards_moderated_open" : "board_boards_moderated_close", request, map));
                    }
                    boardRow.set("id", String.valueOf(boards[5]));
                    boardRow.set("cat", String.valueOf(boards[0]));
                    boardRow.set("topics", String.valueOf(db.countBoardTreads2((Long) boards[5])));
                    boardRow.set("threads", String.valueOf(db.countBoardTreads((Long) boards[5])));
                    boardRow.set("topic", ut.parseHtml(String.valueOf(boards[1])));
                    boardRow.set("description", ut.parseBb(ut.parseHtml(String.valueOf(boards[3]))));
                    in.append(boardRow);
                }
                if (in.toString().isBlank()) {
                    if (!moderator && locked) {
                        categoryRow.set("boards", getTemplate("board_boards_empty_locked", request, map));
                    } else if (moderator) {
                        categoryRow.set("boards", getTemplate("board_boards_empty_moderated", request, map));
                        categoryRow.set("cat", id);
                    } else {
                        categoryRow.set("boards", getTemplate("board_boards_empty", request, map));
                    }
                } else {
                    categoryRow.set("boards", in.toString());
                }
                category.append(categoryRow);
            }
            if (category.toString().isBlank()) {
                if (moderator) {
                    text = text.set("boards", getTemplate("board_cats_empty_moderated", request, map));
                } else {
                    text = text.set("boards", getTemplate("board_cats_empty", request, map));

                }
            } else {
                text = text.set("boards", category);
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
            long pages = (long) Math.ceil((float) count / conf.getLong("board_pages"));
            if (pages == 0) {
                pages = 1;
            }
            StringBuilder pageContent = new StringBuilder();
            for (long l = 1; l <= pages; l++) {
                if (l != Long.valueOf(page)) {
                    var content = getTemplate("boards_page", request, map);
                    content = content.set("page", String.valueOf(l));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                } else {
                    var content = getTemplate("boards_page_active", request, map);
                    content = content.set("page", String.valueOf(l));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                }
            }
            if (page.equals("1")) {
                text = text.set("prev", getTemplate("boards_page_prev_disabled", request, map));
            } else {
                var content = getTemplate("boards_page_prev_enabled2", request, map);
                content = content.set("page", String.valueOf(Long.valueOf(page) - 1));
                content = content.set("pid", pid);
                content = content.set("id", bid);
                text = text.set("prev", content);
            }
            if (Long.valueOf(page) == pages) {
                text = text.set("next", getTemplate("boards_page_next_disabled", request, map));
            } else {
                var content = getTemplate("boards_page_next_enabled2", request, map);
                content = content.set("page", String.valueOf(Long.valueOf(page) + 1));
                content = content.set("pid", pid);
                content = content.set("id", bid);
                text = text.set("next", content);
            }
            text = text.set("pages", pageContent.toString());
            text = text.set("count", String.valueOf(count));
            StringBuilder sb = new StringBuilder();
            board.forEach((boards) -> {
                if ((Long) boards[10] == 0) {
                    var brd = getTemplate("boards_thread2", request, map);
                    brd = brd.set("topic", ut.parseHtml(db.getBoardRef((Long) boards[2], "topic")));
                    var content = String.valueOf(db.getBoardRef((Long) boards[2], "content"));
                    if (content.length() > 255) {
                        content = content.substring(0, 255);
                        content = content + "...";
                    }
                    var user = db.getData(Long.valueOf(db.getBoardRef((Long) boards[2], "user")), "nick2");
                    if (user.equalsIgnoreCase(nick) || moderator) {
                        brd = brd.set("edit", getTemplate("boards_thread_edit", request, map));
                    } else {
                        brd = brd.set("edit", "");
                    }
                    brd = brd.set("content", ut.removeBb(ut.parseHtml(content)));
                    brd = brd.set("ref", String.valueOf(boards[2]));
                    if (moderator) {
                        brd = brd.set("delete", getTemplate("boards_thread_delete", request, map));
                        if (db.getBoardRef((Long) boards[2], "id").equals(db.getBoardRef((Long) boards[2], "ref"))) {
                            brd = brd.set("close", getTemplate("boards_thread_close", request, map));
                        } else {
                            brd = brd.set("close", "");
                        }
                        if (db.isBanned(user)) {
                            brd = brd.set("ban", getTemplate("boards_thread_unban", request, map));
                        } else {
                            brd = brd.set("ban", getTemplate("boards_thread_ban", request, map));
                        }
                    } else {
                        brd = brd.set("delete", "");
                        brd = brd.set("close", "");
                        brd = brd.set("ban", "");
                    }
                    brd = brd.set("uid", String.valueOf(boards[3]));
                    brd = brd.set("user", user);
                    brd = brd.set("user", user);
                    brd = brd.set("color", db.getData(user, "color"));
                    brd = brd.set("board", String.valueOf(boards[4]));
                    brd = brd.set("posted", ut.getTime(Long.valueOf(db.getBoardRef((Long) boards[2], "posted"))));
                    brd = brd.set("ip", String.valueOf(boards[6]));
                    brd = brd.set("cat", String.valueOf(boards[7]));
                    brd = brd.set("id", String.valueOf(boards[8]));
                    brd = brd.set("closed", String.valueOf(boards[9]));
                    brd = brd.set("deleted", String.valueOf(boards[10]));
                    brd = brd.set("signature", "");
                    if (db.isBanned(user)) {
                        brd = brd.set("banned", getTemplate("boards_thread_banned", request, map));
                    } else {
                        brd = brd.set("banned", "");
                    }
                    sb.append(brd);
                }
            });
            if (sb.toString().isBlank()) {
                text = text.set("boards", getTemplate("boards_empty", request, map));
                text = text.set("id", bid);
            } else {
                text = text.set("boards", sb.toString());
                text = text.set("id", bid);
            }
        }
        text = text.set("board_error_if_one", "");
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
        View text = null;
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
            String instagram = null;
            String linkedin = null;
            String tiktok = null;
            String discord = null;
            String twitch = null;
            String github = null;
            String reddit = null;
            String snapchat = null;
            String pinterest = null;
            String whatsapp = null;
            String telegram = null;
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
                    instagram = db.getData(nick, "instagram");
                    linkedin = db.getData(nick, "linkedin");
                    tiktok = db.getData(nick, "tiktok");
                    discord = db.getData(nick, "discord");
                    twitch = db.getData(nick, "twitch");
                    github = db.getData(nick, "github");
                    reddit = db.getData(nick, "reddit");
                    snapchat = db.getData(nick, "snapchat");
                    pinterest = db.getData(nick, "pinterest");
                    whatsapp = db.getData(nick, "whatsapp");
                    telegram = db.getData(nick, "telegram");
                    fam = db.getData(nick, "fam_status");
                    text = getTemplate("account_form_profile_com", request, map);
                    text = text.set("homepage", homepage);
                    text = text.set("city", city);
                    text = text.set("hobby", hobby);
                    text = text.set("description", description);
                    text = text.set("slogan", slogan);
                    text = text.set("signature", signature);
                    text = text.set("icq", icq);
                    text = text.set("live", live);
                    text = text.set("yahoo", yahoo);
                    text = text.set("facebook", facebook);
                    text = text.set("twitter", twitter);
                    text = text.set("irc", irc);
                    text = text.set("youtube", youtube);
                    text = text.set("instagram", instagram);
                    text = text.set("linkedin", linkedin);
                    text = text.set("tiktok", tiktok);
                    text = text.set("discord", discord);
                    text = text.set("twitch", twitch);
                    text = text.set("github", github);
                    text = text.set("reddit", reddit);
                    text = text.set("snapchat", snapchat);
                    text = text.set("pinterest", pinterest);
                    text = text.set("whatsapp", whatsapp);
                    text = text.set("telegram", telegram);
                    text = text.set("fam_status", fam);
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
                    instagram = map.getOrDefault("instagram", "");
                    linkedin = map.getOrDefault("linkedin", "");
                    tiktok = map.getOrDefault("tiktok", "");
                    discord = map.getOrDefault("discord", "");
                    twitch = map.getOrDefault("twitch", "");
                    github = map.getOrDefault("github", "");
                    reddit = map.getOrDefault("reddit", "");
                    snapchat = map.getOrDefault("snapchat", "");
                    pinterest = map.getOrDefault("pinterest", "");
                    whatsapp = map.getOrDefault("whatsapp", "");
                    telegram = map.getOrDefault("telegram", "");
                    fam = map.getOrDefault("fam_status", "");
                    text = getTemplate("account_form_profile_success_com", request, map);
                    text = text.set("homepage", homepage);
                    text = text.set("city", city);
                    text = text.set("hobby", hobby);
                    text = text.set("description", description);
                    text = text.set("slogan", slogan);
                    text = text.set("signature", signature);
                    text = text.set("icq", icq);
                    text = text.set("live", live);
                    text = text.set("yahoo", yahoo);
                    text = text.set("facebook", facebook);
                    text = text.set("twitter", twitter);
                    text = text.set("irc", irc);
                    text = text.set("youtube", youtube);
                    text = text.set("instagram", instagram);
                    text = text.set("linkedin", linkedin);
                    text = text.set("tiktok", tiktok);
                    text = text.set("discord", discord);
                    text = text.set("twitch", twitch);
                    text = text.set("github", github);
                    text = text.set("reddit", reddit);
                    text = text.set("snapchat", snapchat);
                    text = text.set("pinterest", pinterest);
                    text = text.set("whatsapp", whatsapp);
                    text = text.set("telegram", telegram);
                    text = text.set("fam_status", fam);
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
                    db.updateNick(nick, "instagram", instagram);
                    db.updateNick(nick, "linkedin", linkedin);
                    db.updateNick(nick, "tiktok", tiktok);
                    db.updateNick(nick, "discord", discord);
                    db.updateNick(nick, "twitch", twitch);
                    db.updateNick(nick, "github", github);
                    db.updateNick(nick, "reddit", reddit);
                    db.updateNick(nick, "snapchat", snapchat);
                    db.updateNick(nick, "pinterest", pinterest);
                    db.updateNick(nick, "whatsapp", whatsapp);
                    db.updateNick(nick, "telegram", telegram);
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
                    View errorMessage = null;
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
                        text = text.set("account_error_if_one", getTemplate("account_failed", request, map));
                        text = text.set("error_message", errorMessage);
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
                    View errorMessage = null;
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
                        text = text.set("account_error_if_one", getTemplate("account_failed", request, map));
                        text = text.set("error_message", errorMessage);
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
                    bg_color_1 = stripHash(map.getOrDefault("bg_color_1", ""));
                    bg_color_2 = stripHash(map.getOrDefault("bg_color_2", ""));
                    color = stripHash(map.getOrDefault("color", ""));
                    link_color = stripHash(map.getOrDefault("link_color", ""));
                    border_color = stripHash(map.getOrDefault("border_color", ""));
                    room = map.getOrDefault("room2", "");
                    if (bg_color_1.length() != 6 || !bg_color_1.matches("[a-fA-F0-9]*")) {
                        text = getTemplate("account_form_napping_color_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (bg_color_2.length() != 6 || !bg_color_2.matches("[a-fA-F0-9]*")) {
                        text = getTemplate("account_form_napping_color_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (border_color.length() != 6 || !border_color.matches("[a-fA-F0-9]*")) {
                        text = getTemplate("account_form_napping_color_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (color.length() != 6 || !color.matches("[a-fA-F0-9]*")) {
                        text = getTemplate("account_form_napping_color_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (link_color.length() != 6 || !link_color.matches("[a-fA-F0-9]*")) {
                        text = getTemplate("account_form_napping_color_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (db.ownerExists(nick) && !db.getRoomNameByOwner(nick).equals(room)) {
                        text = getTemplate("account_form_napping_used_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (db.roomExists(room) && db.getRoomData(room, "owner").equalsIgnoreCase(nick)) {
                        db.updateRoomData(room, "owner", nick);
                        db.updateRoomData(room, "first_bgcolor", bg_color_1);
                        db.updateRoomData(room, "second_bgcolor", bg_color_2);
                        db.updateRoomData(room, "bordercolor", border_color);
                        db.updateRoomData(room, "linkcolor", link_color);
                        db.updateRoomData(room, "textcolor", color);
                        db.updateRoomData(room, "page_title", title);
                        text = getTemplate("account_form_napping_change_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (!db.roomExists(room)) {
                        db.addRoomData(nick, room, title, bg_color_1, bg_color_2, color, border_color, link_color);
                        db.updateRoomData(room, "su", nick);
                        text = getTemplate("account_form_napping_add_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else {
                        text = getTemplate("account_form_napping_error_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
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
                    text = text.set("title", title);
                    text = text.set("bg_color_1", bg_color_1);
                    text = text.set("bg_color_2", bg_color_2);
                    text = text.set("color", color);
                    text = text.set("link_color", link_color);
                    text = text.set("border_color", border_color);
                    text = text.set("room2", room);
                    text = text.set("nick", nick);
                }
                default -> {
                    text = getTemplate("account_form_unknown_com", request, map);
                }
            }
        } else if (service.equals("data")) {
            View errorMessage = null;
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
                        text = text.set("account_error_if_one", getTemplate("account_failed", request, map));
                        text = text.set("error_message", errorMessage);
                        text = text.set("mail", mail);
                        text = text.set("day", day);
                        text = text.set("month", month);
                        text = text.set("year", year);
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
                    text = text.set("mail", mail);
                    text = text.set("day", day);
                    text = text.set("month", month);
                    text = text.set("year", year);
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
        text = text.set("account_error_if_one", "");
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
        View errorMessage = null;
        var regSuccess = false;
        var skin = map.getOrDefault("skin", "");
        skin = ut.parseHost(skin, request)[1];
        var lang = readLang(request, map);
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
            text = text.set("pwd", pwd);
            text = text.set("nick", nick);
            try {
                if (mailer != null) {
                    mailer.sendEmail(text, db.getCommand("register_subject", lang), mail);
                }
            } catch (MessagingException ex) {
                logError(ex);
            }
        }
        View rf = null;
        String roomName = "";
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        if (!regSuccess) {
            if (roomName.isBlank()) {
                rf = getTemplate("reg_form", request, map);
            } else {
                rf = getTemplate("reg_form_napping", request, map);
                rf = rf.set("owner", owner);
                rf = rf.set("title", db.getRoomData(roomName, "page_title"));
            }
            if (service.equals("reg_done")) {
                rf = rf.set("reg_error_if_one", getTemplate("reg_failed", request, map));
                rf = rf.set("error_message", errorMessage);
            } else {
                rf = rf.set("reg_error_if_one", "");
            }
            cid = generateSid();
        } else {
            var color = ut.createRandomColor();
            db.addNick(gender, nick, mail, color, pwd, question, answer, day, month, year);
            if (roomName.isBlank()) {
                rf = getTemplate("reg_form_success", request, map);
            } else {
                rf = getTemplate("reg_form_success_napping", request, map);
                rf = rf.set("owner", owner);
                rf = rf.set("title", db.getRoomData(roomName, "page_title"));
            }
        }
        rf = rf.set("reg_nick", nick);
        rf = rf.set("reg_mail", mail);
        rf = rf.set("reg_cid", cid);
        rf = rf.set("reg_captcha", captcha);
        rf = rf.set("reg_reminder_question", question);
        rf = rf.set("reg_reminder_answer", answer);
        rf = rf.set("reg_day", day);
        rf = rf.set("reg_month", month);
        rf = rf.set("reg_year", year);
        rf = rf.set("skin", skin);
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
                var community = activeCommunitySession(request.getSession(false), cm);
                if (community != null) {
                    response.setContentType("text/html; charset=" + conf.getString("charset"));
                    community.setTimeoutTimer(0);
                    printPage(map.getOrDefault("doc", ""), request, response, map);
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
        applyLang(request, response, map);
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
        } else if (nick == null || !nick.matches(conf.getString("allowed_chars"))) {
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
            var sessionNick = (String) session.getAttribute("nick");
            if (session.isNew() || sessionNick == null || cm.getUsersCommunity().get(sessionNick) == null) {
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
                startPage("frameset_community_reg", request, response, map);
            } else {
                startPage("frameset_community", request, response, map);
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
        applyLang(request, response, map);
        if (target.isBlank() && (session.isNew() || session.getAttribute("nick") == null)) {
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
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase())) {
                if (roomName.isBlank()) {
                    printTemplate("guest", request, response, map);
                } else {
                    var text = getTemplate("guest_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if ((conf.getInt("guest") == 1 && !nick.equals("")) && nick.length() < conf.getInt("min_nick_length")) {
                if (roomName.isBlank()) {
                    printTemplate("nick", request, response, map);
                } else {
                    var text = getTemplate("nick_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (nickIsCommand(nick)) {
                if (roomName.isBlank()) {
                    printTemplate("command", request, response, map);
                } else {
                    var text = getTemplate("command_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if ((reg = db.isRegistered(nick)) && !db.checkPassword(nick, pwd)) {
                if (roomName.isBlank()) {
                    printTemplate("pwd", request, response, map);
                } else {
                    var text = getTemplate("pwd_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(nick));
                if (roomName.isBlank()) {
                    printTemplate("banned", request, response, map);
                } else {
                    var text = getTemplate("banned_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ip));
                if (roomName.isBlank()) {
                    printTemplate("banned", request, response, map);
                } else {
                    var text = getTemplate("banned_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved));
                if (roomName.isBlank()) {
                    printTemplate("banned", request, response, map);
                } else {
                    var text = getTemplate("banned_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved2));
                if (roomName.isBlank()) {
                    printTemplate("banned", request, response, map);
                } else {
                    var text = getTemplate("banned_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(realIp));
                if (roomName.isBlank()) {
                    printTemplate("banned", request, response, map);
                } else {
                    var text = getTemplate("banned_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(nick));
                if (roomName.isBlank()) {
                    printTemplate("banned_temp", request, response, map);
                } else {
                    var text = getTemplate("banned_temp_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ip));
                if (roomName.isBlank()) {
                    printTemplate("banned_temp", request, response, map);
                } else {
                    var text = getTemplate("banned_temp_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved));
                if (roomName.isBlank()) {
                    printTemplate("banned_temp", request, response, map);
                } else {
                    var text = getTemplate("banned_temp_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved2));
                if (roomName.isBlank()) {
                    printTemplate("banned_temp", request, response, map);
                } else {
                    var text = getTemplate("banned_temp_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(realIp));
                if (roomName.isBlank()) {
                    printTemplate("banned_temp", request, response, map);
                } else {
                    var text = getTemplate("banned_temp_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (!reg && (conf.getInt("guest") == 1 && !nick.equals("") && conf.getInt("only_registered_users") == 1)) {

                if (roomName.isBlank()) {
                    printTemplate("reg", request, response, map);
                } else {
                    var text = getTemplate("reg_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (!reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));

                if (roomName.isBlank()) {
                    printTemplate("locked_room", request, response, map);
                } else {
                    var text = getTemplate("locked_room_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("lock_status")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));

                if (roomName.isBlank()) {
                    printTemplate("locked_room", request, response, map);
                } else {
                    var text = getTemplate("locked_room_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (cm.isOnline(nick)) {

                if (roomName.isBlank()) {
                    printTemplate("online", request, response, map);
                } else {
                    var text = getTemplate("online_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
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
                    var room2 = map.getOrDefault("room", "");
                    if (!room2.isBlank() && db.roomExists(room2) && db.getRoomData(room2, "chat_napping").equals("1")) {
                        View text = null;
                        if (reg) {
                            text = getTemplate("frameset_reg_napping", request, map);
                        } else {
                            text = getTemplate("frameset_napping", request, map);
                        }
                        text = text.set("owner", db.getRoomData(room2, "owner"));
                        ut.submitContent(text, response);
                    } else {
                        if (reg) {
                            printTemplate("frameset_reg", request, response, map);
                        } else {
                            printTemplate("frameset", request, response, map);
                        }
                    }
                } else {
                    View text = null;
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
                    text = text.set("title", title);
                    text = text.set("bg_color_1", bg_color_1);
                    text = text.set("bg_color_2", bg_color_2);
                    text = text.set("color", color);
                    text = text.set("link_color", link_color);
                    text = text.set("border_color", border_color);
                    text = text.set("owner", owner);
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
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase())) {
                var text = getTemplate("guest_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && (conf.getInt("guest") == 1 && !nick.equals("")) && nick.length() < conf.getInt("min_nick_length")) {
                var text = getTemplate("nick_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && nickIsCommand(nick)) {
                var text = getTemplate("command_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && !reg && !db.checkPassword(nick, pwd)) {
                var text = getTemplate("pwd_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(nick));
                var text = getTemplate("banned_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ip));
                var text = getTemplate("banned_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved));
                var text = getTemplate("banned_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved2));
                var text = getTemplate("banned_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(realIp));
                var text = getTemplate("banned_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(nick));
                var text = getTemplate("banned_temp_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ip));
                var text = getTemplate("banned_temp_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved));
                var text = getTemplate("banned_temp_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                var text = getTemplate("banned_temp_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(realIp));
                var text = getTemplate("banned_temp_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && !reg && (conf.getInt("guest") == 1 && !nick.equals("") && conf.getInt("only_registered_users") == 1)) {
                var text = getTemplate("reg_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && !reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));
                var text = getTemplate("locked_room_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("lock_status")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));
                var text = getTemplate("locked_room_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (cm.isOnline(nick)) {
                if (roomName.isBlank()) {
                    printTemplate("online", request, response, map);
                } else {
                    var text = getTemplate("online_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
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
                    var room2 = map.getOrDefault("room", "");
                    if (!room2.isBlank() && db.roomExists(room2) && db.getRoomData(room2, "chat_napping").equals("1")) {
                        View text = null;
                        if (reg) {
                            text = getTemplate("frameset_reg_com_napping", request, map);
                        } else {
                            text = getTemplate("frameset_com_napping", request, map);
                        }
                        text = text.set("owner", db.getRoomData(room2, "owner"));
                        ut.submitContent(text, response);
                    } else {
                        if (reg) {
                            printTemplate("frameset_reg_com", request, response, map);
                        } else {
                            printTemplate("frameset_com", request, response, map);
                        }
                    }
                } else {
                    View text = null;
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
                    text = text.set("title", title);
                    text = text.set("bg_color_1", bg_color_1);
                    text = text.set("bg_color_2", bg_color_2);
                    text = text.set("color", color);
                    text = text.set("link_color", link_color);
                    text = text.set("border_color", border_color);
                    text = text.set("owner", owner);
                    ut.submitContent(text, response);

                }
            }
        } else {
            View text = null;
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
                text = text.set("oldnick", oldnick);
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
                text = text.set("oldnick", oldnick);
                text = text.set("title", title);
                text = text.set("bg_color_1", bg_color_1);
                text = text.set("bg_color_2", bg_color_2);
                text = text.set("color", color);
                text = text.set("link_color", link_color);
                text = text.set("border_color", border_color);
                text = text.set("owner", owner);
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
     * Creates the view of a template. The values every template can use come
     * from the request; pages add their own values with
     * {@link View#set(String, Object)}.
     */
    private View newView(String type, String name, HttpServletRequest request, Map<String, String> map,
            String nick, String sid, String skin, String room, String reason) {
        var ut = Bootstrap.boot.getUtil();
        var db = Bootstrap.boot.getConfig().getDb();
        var host = request.getHeader("host");
        host = host != null ? host : "";
        var scheme = detectScheme(request);
        var owner = map.getOrDefault("owner", "");
        var roomName = owner.isBlank() ? "" : db.getRoomNameByOwner(owner.toLowerCase());
        var view = ViewFactory.create(type, name, readLang(request, map), new ViewFactory.Request(
                nick != null ? nick : "", sid != null ? sid : "", skin != null ? skin : "", room != null ? room : "",
                reason != null ? reason : "", host, owner, map.getOrDefault("target", ""), map.getOrDefault("user", ""),
                roomName, (scheme.equals("https") ? "wss" : "ws") + "://" + host, scheme + "://" + host));
        var session = request.getSession(false);
        view.defaults().put("theme", readTheme(request, map));
        view.defaults().put("designs", new ViewFactory.Texts(design -> selectCommunityDesign(design, session,
                Bootstrap.boot.getChatManager(), db)));
        return view;
    }

    /**
     * Liefert ein Mail-Template aus
     *
     * @param template Der Templatename
     * @param request
     * @return
     */
    protected View getMail(String template, HttpServletRequest request, Map<String, String> map) {
        var skin = Bootstrap.boot.getUtil().parseHost(map.getOrDefault("skin", ""), request)[1];
        return newView("mail", template, request, map, "", map.getOrDefault("sid", ""), skin, "", "");
    }

    /**
     * Liefert ein Console-Template aus
     *
     * @param template Der Templatename
     * @param request
     * @return
     */
    protected View getConsole(String template, HttpServletRequest request, Map<String, String> map) {
        var sid = map.getOrDefault("sid", "");
        if (sid.isBlank()) {
            sid = (String) request.getAttribute("sid");
        }
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        var skin = Bootstrap.boot.getUtil().parseHost(map.getOrDefault("skin", ""), request)[1];
        return newView("console", template, request, map, "", sid, skin, "", "");
    }

    /**
     * Liefert ein Template aus
     *
     * @param template Der Templatename
     * @param request
     * @return
     */
    protected View getTemplate(String template, HttpServletRequest request, Map<String, String> map) {
        var sid = map.getOrDefault("sid", "");
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        String nick = null;
        String skin = null;
        String room = null;
        var reason = (String) request.getAttribute("reason");
        if (sid.isBlank()) {
            sid = (String) request.getAttribute("sid");
        }
        var session = request.getSession(false);
        if (session != null && !session.isNew()) {
            nick = map.getOrDefault("nick", "").isBlank() ? (String) session.getAttribute("nick") : map.getOrDefault("nick", "");
            skin = map.getOrDefault("skin", "").isBlank() ? (String) session.getAttribute("skin") : map.getOrDefault("skin", "");
            room = map.getOrDefault("room", "").isBlank() ? (String) session.getAttribute("room") : map.getOrDefault("room", "");
        }
        nick = nick != null ? nick : (cm.getNameFromId(sid != null ? sid : "") == null || map.getOrDefault("nick", "").length() > 0 ? map.getOrDefault("nick", "") : cm.getUser(cm.getNameFromId(sid)).getName());
        skin = skin != null ? skin : map.getOrDefault("skin", "");
        room = room != null ? room : map.getOrDefault("room", "");
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        if (session != null && session.isNew()) {
            nick = (String) session.getAttribute("nick");
            nick = nick != null ? nick : map.getOrDefault("nick", "");
        }
        skin = ut.parseHost(skin, request)[1];
        skin = skin != null ? skin : map.getOrDefault("skin", "");
        return newView("template", template, request, map, nick, sid, skin, room, reason);
    }

    /**
     * Zeigt das Template im Browser an
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    protected void printTemplate(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        Bootstrap.boot.getUtil().submitContent(getTemplate(template, request, map).toString(), response);
    }

    /**
     * Liefert ein Stylesheet aus
     *
     * @param template Der Templatename
     */
    private View getStyleSheet(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var db = Bootstrap.boot.getConfig().getDb();
        var skin = Bootstrap.boot.getUtil().parseHost(map.getOrDefault("skin", ""), request)[1];
        var owner = map.getOrDefault("owner", "");
        var room = map.getOrDefault("room", "");
        var view = newView("style", template, request, map, "", "", skin, "", "");
        String roomName = null;
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        } else if (!room.isBlank()) {
            roomName = room;
        }
        if (roomName != null && db.roomExists(roomName) && db.getRoomData(roomName, "chat_napping").equals("1")) {
            view.set("bg_color_1", db.getRoomData(roomName, "first_bgcolor"));
            view.set("bg_color_2", db.getRoomData(roomName, "second_bgcolor"));
            view.set("color", db.getRoomData(roomName, "textcolor"));
            view.set("link_color", db.getRoomData(roomName, "linkcolor"));
            view.set("border_color", db.getRoomData(roomName, "bordercolor"));
        }
        return view;
    }

    /**
     * Zeigt ein Stylesheet im Browser an
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    private void printStyleSheet(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var css = getStyleSheet(template, request, response, map).toString();
        if (!"dark".equals(readTheme(request, map))) {
            css = stripDarkTheme(css);
        }
        // The delivered style sheet depends on the "theme" cookie
        response.setHeader("Vary", "Cookie");
        response.setHeader("Cache-Control", "no-cache");
        Bootstrap.boot.getUtil().submitContent(css, response);
    }

    /**
     * Theme of the request: the "theme" parameter, else the "theme" cookie,
     * else the light theme.
     *
     * @return "dark" or "light"
     */
    private String readTheme(HttpServletRequest request, Map<String, String> map) {
        var theme = map.getOrDefault("theme", "");
        if (theme == null || theme.isBlank()) {
            theme = Bootstrap.boot.getUtil().readCookieValue(request, "theme");
        }
        return "dark".equalsIgnoreCase(theme) ? "dark" : "light";
    }

    /**
     * Removes the part of a style sheet that is only meant for the dark theme.
     * The part starts with the comment {@link #DARK_THEME_MARKER} and ends with
     * the style sheet.
     */
    static String stripDarkTheme(String css) {
        var start = css.indexOf(DARK_THEME_MARKER);
        return start < 0 ? css : css.substring(0, start);
    }

    /**
     * Liefert ein Script aus
     *
     * @param template Der Templatename
     */
    private View getScript(String template, HttpServletRequest request, Map<String, String> map) {
        var skin = Bootstrap.boot.getUtil().parseHost(map.getOrDefault("skin", ""), request)[1];
        var view = newView("script", template, request, map, map.getOrDefault("nick", ""), map.getOrDefault("sid", ""),
                skin, map.getOrDefault("room2", ""), "");
        return view.set("room2", map.getOrDefault("room2", ""));
    }

    /**
     * Zeigt ein Script im Browser an
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    private void printScript(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        Bootstrap.boot.getUtil().submitContent(getScript(template, request, map).toString(), response);
    }

    static UsersCommunity activeCommunitySession(HttpSession session, ChatManager manager) {
        if (session == null || !"true".equals(session.getAttribute("community"))) {
            return null;
        }
        var nick = session.getAttribute("nick");
        if (!(nick instanceof String name) || name.isBlank()) {
            return null;
        }
        var community = manager.getUsersCommunity().get(name);
        if (community == null || community.getHttpSession() == null
                || !session.getId().equals(community.getHttpSession().getId())) {
            return null;
        }
        return community;
    }

    static String selectCommunityDesign(String design, HttpSession session, ChatManager manager, Database db) {
        if (!design.equals("start") && !design.equals("frameset_community")
                && !design.equals("frameset_community_reg")) {
            return design;
        }
        var community = activeCommunitySession(session, manager);
        if (community == null) {
            return "start";
        }
        return db.isRegistered(community.getName().toLowerCase())
                ? "frameset_community_reg" : "frameset_community";
    }

    /**
     * Liefert eine Seite aus
     *
     * @param template Der Templatename
     */
    private View getPage(String template, HttpServletRequest request, Map<String, String> map) {
        var sid = map.getOrDefault("sid", "");
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        var nick = cm.getNameFromId(sid) == null || map.getOrDefault("nick", "").length() > 0 ? map.getOrDefault("nick", "") : cm.getUser(cm.getNameFromId(sid)).getName();
        if (nick == null || nick.isBlank()) {
            nick = (String) request.getSession().getAttribute("nick");
        }
        var skin = ut.parseHost(map.getOrDefault("skin", ""), request)[1];
        return newView("pages", template, request, map, nick != null ? nick : "", sid, skin, map.getOrDefault("room", ""),
                map.getOrDefault("reason", ""));
    }

    /**
     * Zeigt eine Seite im Browser an
     *
     * @param template Der Templatename
     * @param out Die Output-Klasse
     */
    private void printPage(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        Bootstrap.boot.getUtil().submitContent(getPage(template, request, map).toString(), response);
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
        View text = null;
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
            text = text.set("count", String.valueOf(db.countMessage(nick)));
            text = text.set("color", db.getData(nick, "color"));
            text = text.set("messages", db.getMessages(nick, request, map));
            db.delMessages(nick);
        } else {
            if (roomName.isBlank()) {
                text = getTemplate("message_no", request, map);
            } else {
                text = getTemplate("message_no_napping", request, map);
            }
            text = text.set("color", db.getData(nick, "color"));
        }
        ut.submitContent(text, response);
    }

    private void startPage(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var boot = Bootstrap.boot;
        var db = boot.getConfig().getDb();
        var util = boot.getUtil();
        var cm = boot.getChatManager();
        var community = activeCommunitySession(request.getSession(false), cm);
        var pageTemplate = template.equals("start") && community != null ? "start_logged_in" : template;
        try {
            var counts = db.getHomepageCounts();
            var latest = new NewsRepository(db, boot.getConfig().getP().getProperty("sql.prefix", "")).latest();
            var homepageExtra = getTemplate("homepage_extra", request, map)
                    .set("homepage_chatters", org.owasp.encoder.Encode.forHtmlContent(cm.getUserSizeInChat()))
                    .set("homepage_rooms", org.owasp.encoder.Encode.forHtmlContent(String.valueOf(cm.getRooms().size())))
                    .set("homepage_registered", org.owasp.encoder.Encode.forHtmlContent(String.valueOf(counts.registered())))
                    .set("homepage_messages", org.owasp.encoder.Encode.forHtmlContent(String.valueOf(counts.messages())));
            View homepageNews = null;
            if (latest.isPresent()) {
                var article = latest.get();
                homepageNews = getTemplate("homepage_news", request, map)
                        .set("homepage_news_title", org.owasp.encoder.Encode.forHtmlContent(article.title()))
                        .set("homepage_news_author", org.owasp.encoder.Encode.forHtmlContent(article.author()))
                        .set("homepage_news_published", org.owasp.encoder.Encode.forHtmlContent(util.getTime(article.published())))
                        .set("homepage_news_body", org.owasp.encoder.Encode.forHtmlContent(article.body()))
                        .set("homepage_news_url", org.owasp.encoder.Encode.forHtmlAttribute(
                                request.getContextPath() + "/Start?page=news"));
            }
            homepageExtra = homepageExtra.set("homepage_news", homepageNews);
            var homepageNick = community == null ? "" : org.owasp.encoder.Encode.forHtmlContent(community.getName());
            var page = getTemplate(pageTemplate, request, map)
                    .set("homepage_extra", homepageExtra)
                    .set("homepage_nick", homepageNick);
            util.submitContent(page, response);
        } catch (java.sql.SQLException ex) {
            logError(ex);
            response.setStatus(503);
            util.submitContent(getTemplate("homepage_unavailable", request, map), response);
        }
    }

    private void news(HttpServletRequest request, HttpServletResponse response, Map<String, String> map, boolean admin) {
        var conf = Bootstrap.boot.getConfig();
        var util = Bootstrap.boot.getUtil();
        var lang = readLang(request, map);
        var consoleUrl = request.getContextPath() + "/Start?page=" + util.encodeHtml(conf.getString("path_console"))
                + "&service=news";
        try {
            var repository = new NewsRepository(conf.getDb(), conf.getP().getProperty("sql.prefix", ""));
            if (admin && "POST".equalsIgnoreCase(request.getMethod())) {
                var session = request.getSession(false);
                var token = session == null ? null : (String) session.getAttribute("newsCsrf");
                var submitted = map.getOrDefault("csrf", "");
                if (token == null || !java.security.MessageDigest.isEqual(
                        token.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        submitted.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                    response.sendError(403);
                    return;
                }
                var action = map.getOrDefault("action", "");
                var title = map.getOrDefault("title", "").strip();
                var body = map.getOrDefault("body", "").strip();
                if ((action.equals("create") || action.equals("update"))
                        && (title.isBlank() || title.length() > 200 || body.isBlank() || body.length() > 20000)) {
                    response.sendError(400, I18n.get(lang, "news.invalid_input"));
                    return;
                }
                if (action.equals("create")) {
                    repository.create(title, body, conf.getString("admin_username"));
                } else if (action.equals("update") || action.equals("delete")) {
                    long id;
                    try {
                        id = Long.parseLong(map.getOrDefault("id", ""));
                        if (id <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException ex) {
                        response.sendError(400, "Invalid news ID");
                        return;
                    }
                    boolean changed = action.equals("update") ? repository.update(id, title, body) : repository.delete(id);
                    if (!changed) {
                        response.sendError(404, "News not found");
                        return;
                    }
                } else {
                    response.sendError(400, "Invalid news action");
                    return;
                }
                response.sendRedirect(response.encodeRedirectURL(consoleUrl));
                return;
            }
            if (!"GET".equalsIgnoreCase(request.getMethod())) {
                response.setHeader("Allow", admin ? "GET, POST" : "GET");
                response.sendError(405);
                return;
            }
            String token = "";
            if (admin) {
                var session = request.getSession(true);
                synchronized (session) {
                    token = (String) session.getAttribute("newsCsrf");
                    if (token == null) {
                        var bytes = new byte[32];
                        new java.security.SecureRandom().nextBytes(bytes);
                        token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                        session.setAttribute("newsCsrf", token);
                    }
                }
                response.setHeader("Cache-Control", "no-store");
            }
            var content = new StringBuilder("<h1>").append(I18n.get(lang, admin ? "news.manage" : "news.title")).append("</h1>");
            if (admin) {
                content.append(newsForm(consoleUrl, token, null, lang));
            }
            var articles = repository.list();
            if (articles.isEmpty()) {
                content.append("<p>").append(I18n.get(lang, "news.empty")).append("</p>");
            }
            for (var article : articles) {
                content.append("<article class=\"card mb-3\"><div class=\"card-body\"><h2 class=\"h4\">")
                        .append(org.owasp.encoder.Encode.forHtmlContent(article.title())).append("</h2><p class=\"text-muted\">")
                        .append(org.owasp.encoder.Encode.forHtmlContent(article.author())).append(" · ")
                        .append(org.owasp.encoder.Encode.forHtmlContent(util.getTime(article.published())))
                        .append("</p><div style=\"white-space:pre-wrap\">")
                        .append(org.owasp.encoder.Encode.forHtmlContent(article.body())).append("</div>");
                if (admin) {
                    content.append("<details class=\"mt-3\"><summary>").append(I18n.get(lang, "news.edit"))
                            .append("</summary>").append(newsForm(consoleUrl, token, article, lang)).append("</details>");
                    content.append("<form method=\"post\" action=\"").append(org.owasp.encoder.Encode.forHtmlAttribute(consoleUrl))
                            .append("\" class=\"mt-2\"><input type=\"hidden\" name=\"csrf\" value=\"").append(token)
                            .append("\"><input type=\"hidden\" name=\"action\" value=\"delete\"><input type=\"hidden\" name=\"id\" value=\"")
                            .append(article.id()).append("\"><button class=\"btn btn-danger btn-sm\" type=\"submit\">")
                            .append(I18n.get(lang, "news.delete")).append("</button></form>");
                }
                content.append("</div></article>");
            }
            util.submitContent(getTemplate("news", request, map)
                    .set("newsDesign", admin ? "console" : "start")
                    .set("content", content.toString()), response);
        } catch (java.sql.SQLException | IOException ex) {
            logError(ex);
            response.setStatus(500);
            util.submitContent(I18n.get(lang, "news.load_failed"), response);
        }
    }

    private String newsForm(String url, String token, NewsRepository.Article article, String lang) {
        boolean edit = article != null;
        var html = new StringBuilder("<form method=\"post\" class=\"mb-4\" action=\"")
                .append(org.owasp.encoder.Encode.forHtmlAttribute(url))
                .append("\"><input type=\"hidden\" name=\"csrf\" value=\"").append(token)
                .append("\"><input type=\"hidden\" name=\"action\" value=\"").append(edit ? "update" : "create").append("\">");
        if (edit) html.append("<input type=\"hidden\" name=\"id\" value=\"").append(article.id()).append("\">");
        html.append("<label>").append(I18n.get(lang, "news.form.title")).append("<input class=\"form-control\" name=\"title\" maxlength=\"200\" required value=\"")
                .append(org.owasp.encoder.Encode.forHtmlAttribute(edit ? article.title() : "")).append("\"></label>")
                .append("<label class=\"d-block\">").append(I18n.get(lang, "news.form.text"))
                .append("<textarea class=\"form-control\" name=\"body\" maxlength=\"20000\" rows=\"5\" required>")
                .append(org.owasp.encoder.Encode.forHtmlContent(edit ? article.body() : ""))
                .append("</textarea></label><button class=\"btn btn-primary\" type=\"submit\">")
                .append(I18n.get(lang, edit ? "news.form.save" : "news.form.publish"))
                .append("</button></form>");
        return html.toString();
    }

    private void statistics(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var conf = Bootstrap.boot.getConfig();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        var cm = Bootstrap.boot.getChatManager();
        var lang = readLang(request, map);

        var hourly = db.getStatsHourly();
        var daily = db.getStatsDaily();
        var monthly = db.getStatsMonthly();
        var peaks = db.getPeaks();
        var topRooms = db.getTopRooms();
        var relationships = db.getUserRelationships();

        var relSb = new StringBuilder();
        int relCount = 0;
        for (var rel : relationships) {
            if (relCount++ >= 10) break;
            relSb.append("<tr><td>").append(ut.preReplace((String) rel.get("user1")));
            relSb.append("</td><td>").append(ut.preReplace((String) rel.get("user2")));
            relSb.append("</td><td>").append(rel.get("interaction_count"));
            relSb.append("</td><td>").append(ut.getTime((Long) rel.get("last_interaction")));
            relSb.append("</td></tr>");
        }
        if (relationships.isEmpty()) {
            relSb.append("<tr><td colspan=\"4\" class=\"text-center text-muted\">").append(I18n.get(lang, "stats.no_relationships")).append("</td></tr>");
        }

        String t_msgs = I18n.get(lang, "stats.messages");
        String t_peak_users = I18n.get(lang, "stats.peak_users");
        String t_avg_users = I18n.get(lang, "stats.avg_users");
        String t_active_users = I18n.get(lang, "stats.active_users");

        var chartSb = new StringBuilder();
        chartSb.append("<script>");
        chartSb.append("const dailyData=").append(toJson(daily)).append(";");
        chartSb.append("const hourlyData=").append(toJson(hourly)).append(";");
        chartSb.append("const monthlyData=").append(toJson(monthly)).append(";");
        chartSb.append("const peaksData=").append(toJson(peaks)).append(";");
        chartSb.append("const topRoomsData=").append(toJson(topRooms)).append(";");
        chartSb.append("const relData=").append(toJson(relationships.subList(0, Math.min(10, relationships.size())))).append(";");
        chartSb.append("function buildLabels(data,key){return data.map(function(it){return it[key];});}");
        chartSb.append("function buildSeries(data,valKey){return data.map(function(it){return it[valKey];});}");
        chartSb.append("if(dailyData.length){new Chart(document.getElementById('dailyMsgChart'),{type:'bar',data:{labels:buildLabels(dailyData,'day_bucket'),datasets:[{label:'").append(t_msgs).append("',data:buildSeries(dailyData,'messages'),backgroundColor:'#0d6efd'}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}");
        chartSb.append("if(dailyData.length){new Chart(document.getElementById('dailyUserChart'),{type:'bar',data:{labels:buildLabels(dailyData,'day_bucket'),datasets:[{label:'").append(t_peak_users).append("',data:buildSeries(dailyData,'peak_users'),backgroundColor:'#ffc107'}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}");
        chartSb.append("if(hourlyData.length){new Chart(document.getElementById('hourlyMsgChart'),{type:'line',data:{labels:buildLabels(hourlyData,'hour_bucket'),datasets:[{label:'").append(t_msgs).append("',data:buildSeries(hourlyData,'messages'),borderColor:'#198754',backgroundColor:'rgba(25,135,84,0.1)',fill:true,tension:0.3}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}");
        chartSb.append("if(hourlyData.length){new Chart(document.getElementById('hourlyUserChart'),{type:'line',data:{labels:buildLabels(hourlyData,'hour_bucket'),datasets:[{label:'").append(t_avg_users).append("',data:buildSeries(hourlyData,'avg_users'),borderColor:'#fd7e14',tension:0.3}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}");
        chartSb.append("if(monthlyData.length){new Chart(document.getElementById('monthlyMsgChart'),{type:'bar',data:{labels:buildLabels(monthlyData,'month_bucket'),datasets:[{label:'").append(t_msgs).append("',data:buildSeries(monthlyData,'messages'),backgroundColor:'#6610f2'}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}");
        chartSb.append("if(monthlyData.length){new Chart(document.getElementById('monthlyUserChart'),{type:'bar',data:{labels:buildLabels(monthlyData,'month_bucket'),datasets:[{label:'").append(t_peak_users).append("',data:buildSeries(monthlyData,'peak_users'),backgroundColor:'#dc3545'}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}");
        chartSb.append("if(topRoomsData.length){new Chart(document.getElementById('topRoomsMsgChart'),{type:'bar',data:{labels:buildLabels(topRoomsData,'room'),datasets:[{label:'").append(t_msgs).append("',data:buildSeries(topRoomsData,'msg_count'),backgroundColor:'#20c997'}]},options:{indexAxis:'y',responsive:true,scales:{x:{beginAtZero:true}}}});}");
        chartSb.append("if(topRoomsData.length){new Chart(document.getElementById('topRoomsUserChart'),{type:'bar',data:{labels:buildLabels(topRoomsData,'room'),datasets:[{label:'").append(t_active_users).append("',data:buildSeries(topRoomsData,'users'),backgroundColor:'#0dcaf0'}]},options:{indexAxis:'y',responsive:true,scales:{x:{beginAtZero:true}}}});}");
        chartSb.append("if(peaksData.length){new Chart(document.getElementById('peaksChart'),{type:'line',data:{labels:buildLabels(peaksData,'date'),datasets:[{label:'").append(t_peak_users).append("',data:buildSeries(peaksData,'peak_users'),borderColor:'#d63384',backgroundColor:'rgba(214,51,132,0.1)',fill:true,tension:0.3}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}");
        chartSb.append("if(relData.length){new Chart(document.getElementById('relationsChart'),{type:'doughnut',data:{labels:relData.map(function(it){return it.user1+' ↔ '+it.user2;}),datasets:[{data:relData.map(function(it){return it.interaction_count;}),backgroundColor:['#0d6efd','#6610f2','#d63384','#dc3545','#fd7e14','#ffc107','#198754','#20c997','#0dcaf0','#6f42c1']}]},options:{responsive:true}});}");
        chartSb.append("</script>");

        var skin = ut.parseHost(map.getOrDefault("skin", ""), request)[1];
        skin = skin != null ? skin : map.getOrDefault("skin", "");
        var session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("nick") != null;

        var templateName = loggedIn ? "stats_community" : "stats";
        var data = getTemplate(templateName, request, map);
        data = data.set("stat_online", cm.getUserSizeInChat());
        data = data.set("stat_total_messages", String.valueOf(db.countPublicMessages() > -1 ? db.countPublicMessages() : 0));
        data = data.set("stat_registered", String.valueOf(db.countChatter() > -1 ? db.countChatter() : 0));
        data = data.set("stat_rooms", String.valueOf(cm.getRooms().size()));
        data = data.set("stat_relationships", relSb.toString());
        data = data.set("stat_chart_script", chartSb.toString());

        ut.submitContent(data, response);
    }

    private String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof java.util.Map<?, ?> map) {
            var sb = new StringBuilder();
            sb.append("{");
            var iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                var e = iter.next();
                sb.append("\"").append(e.getKey().toString().replace("\\", "\\\\").replace("\"", "\\\"")).append("\":");
                sb.append(toJson(e.getValue()));
                if (iter.hasNext()) sb.append(",");
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Iterable<?> list) {
            var sb = new StringBuilder();
            sb.append("[");
            var iter = list.iterator();
            while (iter.hasNext()) {
                sb.append(toJson(iter.next()));
                if (iter.hasNext()) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof String s) {
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
        }
        if (obj instanceof Boolean b) return b.toString();
        if (obj instanceof Number n) return n.toString();
        return "\"" + obj.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

}
