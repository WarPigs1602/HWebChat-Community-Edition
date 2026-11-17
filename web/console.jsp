<%@ page session="false" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.*, java.io.*, java.net.*, java.nio.file.*, java.sql.*, java.util.*, java.util.logging.*, jakarta.servlet.*, jakarta.servlet.http.*, jakarta.mail.*" %>
<%@ page import="static net.midiandmore.chat.Web.*" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="fn" uri="jakarta.tags.functions" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }
    Map<String, String> map = Web.requestValues(request);
    var lang = (String) request.getAttribute("lang");
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter();
        var ut = Bootstrap.getBoot().getUtil();
        var conf = Bootstrap.getBoot().getConfig();
        var cm = Bootstrap.getBoot().getChatManager();
        try {
            filter.init(Bootstrap.getBoot());
        } catch (ServletException ex) {
            Bootstrap.logError(ex);
            response.setStatus(500);
            return;
        }
        try {
            filter.doFilter(request, response);
        } catch (IOException ex) {
            Bootstrap.logError(ex);
            response.setStatus(500);
            return;
        } catch (ServletException ex) {
            Bootstrap.logError(ex);
            response.setStatus(500);
            return;
        }
        // The console templates see the same defaults as the former console views
        var consoleSid = map.getOrDefault("sid", "");
        if (consoleSid.isBlank()) {
            consoleSid = (String) request.getAttribute("sid");
        }
        consoleSid = consoleSid != null ? consoleSid : map.getOrDefault("sid", "");
        var consoleSkin = Web.skin(map.getOrDefault("skin", ""), request);
        var consoleValues = new HashMap<String, Object>();
        consoleValues.put("nick", "");
        consoleValues.put("sid", consoleSid);
        consoleValues.put("skin", consoleSkin != null ? consoleSkin : "");
        consoleValues.put("room", "");
        consoleValues.put("reason", "");
        Web.view("page", request, consoleValues);
        var panel = "";
        if (!filter.hasError()) {
            var service = map.getOrDefault("service", "");
            var change = map.getOrDefault("change", "");
            if (service.isBlank()) {
                panel = "index";
            } else if (service.equals("news")) {
                if (Web.isModuleEnabled("news")) {
                    Bootstrap.getBoot().getChatServices().news(request, response, map, true);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else if (service.equals("modules")) {
                panel = "modules";
                var update = map.getOrDefault("update", "");
                TreeMap<String, String[]> p = conf.loadModulesWithDefaults();
                TreeMap<String, String> p1 = new TreeMap<>();
                var rows = new ArrayList<String[]>();
                if (!update.isBlank()) {
                    for (var key : p.keySet()) {
                        String[] vals = request.getParameterValues(key);
                        var val = "0";
                        if (vals != null) {
                            for (var v : vals) { if ("1".equals(v)) { val = "1"; break; } }
                        }
                        p1.put(key, val);
                    }
                    conf.setModules(p1);
                    conf.saveModules();
                    request.setAttribute("console_success", true);
                    p = conf.loadModulesWithDefaults();
                }
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    rows.add(new String[]{ut.parseHtml(key), ut.parseHtml(arr[0]), arr[1]});
                }
                request.setAttribute("console_rows", rows);
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
                    panel = "rooms";
                } else if (change.equals("search")) {
                    if (!exists) {
                        panel = "rooms_fail";
                        request.setAttribute("room", room);
                    } else {
                        panel = "rooms_config";
                        topic = db.getRoomData(room, "topic");
                        lock = db.getRoomData(room, "locked");
                        lockReason = db.getRoomData(room, "lock_reason");
                        standard = db.getRoomData(room, "standard");
                        allowSmilies = db.getRoomData(room, "allow_smilies");
                        request.setAttribute("room", room);
                        request.setAttribute("topic", topic);
                        request.setAttribute("lock", lock);
                        request.setAttribute("lock_reason", lockReason);
                        request.setAttribute("standard", standard);
                        request.setAttribute("allow_smilies", allowSmilies);
                    }
                } else if (change.equals("modify")) {
                    if (!exists) {
                        panel = "rooms_fail";
                        request.setAttribute("room", room);
                    } else {
                        panel = "rooms_modify";
                        db.updateRoomData(room, "topic", topic);
                        db.updateRoomData(room, "locked", lock);
                        db.updateRoomData(room, "lock_reason", lockReason);
                        db.updateRoomData(room, "standard", standard);
                        db.updateRoomData(room, "allow_smilies", allowSmilies);
                        request.setAttribute("room", room);
                        request.setAttribute("topic", topic);
                        request.setAttribute("lock", lock);
                        request.setAttribute("lock_reason", lockReason);
                        request.setAttribute("standard", standard);
                        request.setAttribute("allow_smilies", allowSmilies);
                    }
                } else if (change.equals("add_room")) {
                    if (exists) {
                        panel = "rooms_exist";
                        request.setAttribute("room", room);
                    } else {
                        panel = "rooms_add";
                        request.setAttribute("room", room);
                        request.setAttribute("topic", topic);
                        request.setAttribute("lock", "0");
                        request.setAttribute("lock_reason", lockReason);
                        request.setAttribute("standard", "0");
                        request.setAttribute("allow_smilies", "1");
                    }
                } else if (change.equals("set_room")) {
                    if (exists) {
                        panel = "rooms_exist";
                        request.setAttribute("room", room);
                    } else {
                        panel = "rooms_set";
                        request.setAttribute("room", room);
                        db.addRoom(room, topic, Integer.valueOf(lock), lockReason, Integer.valueOf(standard), Integer.valueOf(allowSmilies));
                    }
                } else if (change.equals("delete")) {
                    if (!exists) {
                        panel = "rooms_fail";
                        request.setAttribute("room", room);
                    } else {
                        panel = "rooms_delete";
                        request.setAttribute("room", room);
                        db.delRoom(room);
                    }
                } else {
                    panel = "rooms_404";
                    request.setAttribute("room", room);
                }
            } else if (service.equals("chat")) {
                panel = "chat";
                TreeMap<String, String[]> p = conf.loadDataFromJSON("config.json", "name", "value", "description");
                TreeMap<String, String[]> p1 = new TreeMap<>();
                var rows = new ArrayList<String[]>();
                var update = map.getOrDefault("update", "");

                if (!update.isBlank()) {
                    for (var key1 : map.keySet()) {
                        if (key1.equals("service") || key1.equals("update") || key1.equals("skin") || key1.equals("page") || key1.startsWith("path_")) {
                            continue;
                        } else {
                            String[] arr2 = {"", ""};
                            var arr = p.getOrDefault(key1, arr2);
                            var description = arr[1];
                            String[] arr1 = {map.getOrDefault(key1, ""), description};
                            p1.put(key1, arr1);
                        }
                    }
                    p.forEach((key, value) -> {
                        if (key.startsWith("path_")) {
                            p1.put(key, value);
                        }
                    });
                    conf.saveDataToJSON("config.json", p1, "name", "value", "description");
                    conf.hash();
                    request.setAttribute("console_success", true);
                }
                p = conf.loadDataFromJSON("config.json", "name", "value", "description");
                for (var key : p.keySet()) {
                    if (key.startsWith("path_")) {
                        continue;
                    }
                    var arr = p.get(key);
                    var value = arr[0];
                    var description = arr[1];
                    rows.add(new String[]{ut.parseHtml(key), ut.parseHtml(value), description});
                }
                request.setAttribute("console_rows", rows);
            } else if (service.equals("fun")) {
                var update = map.getOrDefault("update", "");

                panel = "fun";
                TreeMap<String, String[]> p = conf.loadDataFromJSON("fun.json", "command", "content", "description");
                TreeMap<String, String[]> p1 = new TreeMap<>();
                var rows = new ArrayList<String[]>();
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
                    request.setAttribute("console_success", true);
                    conf.hash();
                }
                p = conf.loadDataFromJSON("fun.json", "command", "content", "description");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr[0];
                    var description = arr[1];
                    rows.add(new String[]{ut.parseHtml(key), ut.parseHtml(value), description});
                }
                request.setAttribute("console_rows", rows);
            } else if (service.equals("cmdrights")) {
                var update = map.getOrDefault("update", "");

                panel = "cmdcfg";
                TreeMap<String, String> p = conf.loadDataFromJSON("cmdcfg.json", "name", "status");
                TreeMap<String, String> p1 = new TreeMap<>();
                var rows = new ArrayList<String[]>();
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
                    request.setAttribute("console_success", true);
                    conf.hash();
                }
                p = conf.loadDataFromJSON("cmdcfg.json", "name", "status");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    rows.add(new String[]{ut.parseHtml(key), ut.parseHtml(value), ""});
                }
                request.setAttribute("console_rows", rows);
            } else if (service.equals("cmd")) {
                var update = map.getOrDefault("update", "");

                panel = "commands";
                TreeMap<String, String> p = conf.loadDataFromJSON("commands.json", "name", "command");
                TreeMap<String, String> p1 = new TreeMap<>();
                var rows = new ArrayList<String[]>();
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
                    request.setAttribute("console_success", true);
                    conf.hash();
                }
                p = conf.loadDataFromJSON("commands.json", "name", "command");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    rows.add(new String[]{ut.parseHtml(key), ut.parseHtml(value), ""});
                }
                request.setAttribute("console_rows", rows);
            } else if (service.equals("host")) {
                var update = map.getOrDefault("update", "");

                panel = "host";
                TreeMap<String, String> p = conf.loadDataFromJSON("hosts.json", "host", "skin");
                TreeMap<String, String> p1 = new TreeMap<>();
                var rows = new ArrayList<String[]>();
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
                    request.setAttribute("console_success", true);
                    conf.hash();
                }
                p = conf.loadDataFromJSON("hosts.json", "host", "skin");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    rows.add(new String[]{ut.parseHtml(key), ut.parseHtml(value), ""});
                }
                request.setAttribute("console_rows", rows);
            } else if (service.equals("theme")) {
                var update = map.getOrDefault("update", "");
                panel = "theme";
                var themes = conf.loadDataFromJSON("themes.json", "skin", "data");
                var themeSkin = map.getOrDefault("theme_skin", map.getOrDefault("skin_name", "native"));
                if (themeSkin.isBlank()) themeSkin = "native";
                var dataJson = themes.getOrDefault(themeSkin, "{}");
                var primary = "#0d6efd"; var primaryDark = "#0a58ca"; var dark = "#212529"; var muted = "#6c757d";
                var border = "#dee2e6"; var bg = "#f0f2f5"; var surface = "#ffffff"; var customCss = "";
                var link = "#0d6efd"; var radius = "0.75rem"; var fontSize = "0.9375rem"; var fontFamily = "system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif";
                var navBg = "#212529"; var navText = "#ffffff"; var success = "#198754"; var warning = "#ffc107"; var danger = "#dc3545"; var bodyGradient = "1"; var logo = ""; var chatName = "";
                try {
                    var jr = jakarta.json.Json.createReader(new java.io.StringReader(dataJson));
                    var jo = jr.readObject(); jr.close();
                    primary = jo.getString("primary", primary); primaryDark = jo.getString("primaryDark", primaryDark);
                    dark = jo.getString("dark", dark); muted = jo.getString("muted", muted);
                    border = jo.getString("border", border); bg = jo.getString("bg", bg); surface = jo.getString("surface", surface);
                    customCss = jo.getString("customCss", customCss);
                    link = jo.getString("link", link); radius = jo.getString("radius", radius);
                    fontSize = jo.getString("fontSize", fontSize); fontFamily = jo.getString("fontFamily", fontFamily);
                    navBg = jo.getString("navBg", navBg); navText = jo.getString("navText", navText);
                    success = jo.getString("success", success); warning = jo.getString("warning", warning); danger = jo.getString("danger", danger);
                    bodyGradient = jo.getString("bodyGradient", bodyGradient);
                    logo = jo.getString("logo", logo);
                    chatName = jo.getString("chatName", chatName);
                } catch (Exception e) {}
                if (!update.isBlank()) {
                    themeSkin = map.getOrDefault("theme_skin", themeSkin);
                    if (themeSkin.isBlank()) themeSkin = "native";
                    primary = map.getOrDefault("primary", primary); primaryDark = map.getOrDefault("primaryDark", primaryDark);
                    dark = map.getOrDefault("dark", dark); muted = map.getOrDefault("muted", muted);
                    border = map.getOrDefault("border", border); bg = map.getOrDefault("bg", bg); surface = map.getOrDefault("surface", surface);
                    customCss = map.getOrDefault("customCss", customCss);
                    link = map.getOrDefault("link", link); radius = map.getOrDefault("radius", radius);
                    fontSize = map.getOrDefault("fontSize", fontSize); fontFamily = map.getOrDefault("fontFamily", fontFamily);
                    navBg = map.getOrDefault("navBg", navBg); navText = map.getOrDefault("navText", navText);
                    success = map.getOrDefault("success", success); warning = map.getOrDefault("warning", warning); danger = map.getOrDefault("danger", danger);
                    String[] bgv = request.getParameterValues("bodyGradient");
                    bodyGradient = "0";
                    if (bgv != null) { for (var v : bgv) { if ("1".equals(v)) { bodyGradient = "1"; break; } } }
                    logo = map.getOrDefault("logo", logo).trim();
                    chatName = map.getOrDefault("chatName", chatName).trim();
                    var jo = jakarta.json.Json.createObjectBuilder()
                        .add("primary", primary).add("primaryDark", primaryDark).add("dark", dark)
                        .add("muted", muted).add("border", border).add("bg", bg).add("surface", surface)
                        .add("customCss", customCss).add("link", link).add("radius", radius)
                        .add("fontSize", fontSize).add("fontFamily", fontFamily).add("navBg", navBg).add("navText", navText)
                        .add("success", success).add("warning", warning).add("danger", danger).add("bodyGradient", bodyGradient).add("logo", logo).add("chatName", chatName).build();
                    themes.put(themeSkin, jo.toString());
                    conf.saveDataToJSON("themes.json", themes, "skin", "data");
                    request.setAttribute("console_success", true);
                    conf.hash();
                }
                request.setAttribute("theme_skin", themeSkin);
                request.setAttribute("theme_primary", primary); request.setAttribute("theme_primaryDark", primaryDark);
                request.setAttribute("theme_dark", dark); request.setAttribute("theme_muted", muted);
                request.setAttribute("theme_border", border); request.setAttribute("theme_bg", bg); request.setAttribute("theme_surface", surface);
                request.setAttribute("theme_customCss", customCss);
                request.setAttribute("theme_link", link); request.setAttribute("theme_radius", radius);
                request.setAttribute("theme_fontSize", fontSize); request.setAttribute("theme_fontFamily", fontFamily);
                request.setAttribute("theme_navBg", navBg); request.setAttribute("theme_navText", navText);
                request.setAttribute("theme_success", success); request.setAttribute("theme_warning", warning); request.setAttribute("theme_danger", danger);
                request.setAttribute("theme_bodyGradient", bodyGradient); request.setAttribute("theme_logo", logo); request.setAttribute("theme_chatName", chatName);
                request.setAttribute("theme_skins", themes.keySet());
            } else if (service.equals("emot")) {
                var update = map.getOrDefault("update", "");

                panel = "emot";
                TreeMap<String, String> p = conf.loadDataFromJSON("emot.json", "name", "emot");
                TreeMap<String, String> p1 = new TreeMap<>();
                var rows = new ArrayList<String[]>();
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
                    request.setAttribute("console_success", true);
                    conf.hash();
                }
                p = conf.loadDataFromJSON("emot.json", "name", "emot");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    rows.add(new String[]{ut.parseHtml(key), ut.parseHtml(value), ""});
                }
                request.setAttribute("console_rows", rows);
            } else if (service.equals("mime")) {
                var update = map.getOrDefault("update", "");

                panel = "mime";
                TreeMap<String, String> p = conf.loadDataFromJSON("mime-types.json", "suffix", "type");
                TreeMap<String, String> p1 = new TreeMap<>();
                var rows = new ArrayList<String[]>();
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
                    request.setAttribute("console_success", true);
                    conf.hash();
                }
                p = conf.loadDataFromJSON("mime-types.json", "suffix", "type");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    rows.add(new String[]{ut.parseHtml(key), ut.parseHtml(value), ""});
                }
                request.setAttribute("console_rows", rows);
            } else if (service.equals("mail")) {
                var update = map.getOrDefault("update", "");

                panel = "mail";
                TreeMap<String, String> p = conf.loadDataFromJSON("mail.json", "name", "value");
                TreeMap<String, String> p1 = new TreeMap<>();
                var rows = new ArrayList<String[]>();
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
                    request.setAttribute("console_success", true);
                    conf.hash();
                }
                p = conf.loadDataFromJSON("mail.json", "name", "value");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    rows.add(new String[]{ut.parseHtml(key), ut.parseHtml(value), ""});
                }
                request.setAttribute("console_rows", rows);
            } else if (service.equals("path")) {
                response.setStatus(404);
                panel = "404";
            } else if (service.equals("help")) {
                var update = map.getOrDefault("update", "");

                panel = "help";
                TreeMap<String, String> p = conf.loadDataFromJSON("help.json", "name", "html");
                TreeMap<String, String> p1 = new TreeMap<>();
                var rows = new ArrayList<String[]>();
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
                    request.setAttribute("console_success", true);
                    conf.hash();
                }
                p = conf.loadDataFromJSON("help.json", "name", "html");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    rows.add(new String[]{ut.parseHtml(key), ut.parseHtml(value), ""});
                }
                request.setAttribute("console_rows", rows);
            } else if (service.equals("users")) {
                var update = map.getOrDefault("update", "");
                if (!update.isBlank()) {
                    var nick = map.getOrDefault("user", "");
                    Database db = conf.getDb();
                    if (db.isRegistered(nick)) {
                        if (change.isBlank()) {
                            panel = "users_page";
                            request.setAttribute("nick", nick);

                        } else if (change.equals("password")) {
                            var newPwd = map.getOrDefault("new_pwd", "");
                            var newPwd2 = map.getOrDefault("new_pwd2", "");
                            Object errorMessage = null;
                            if (!newPwd.isBlank()) {
                                if (newPwd.length() < conf.getInt("min_pwd_length")) {
                                    errorMessage = Web.message(lang, "common.your_password_is_too_short");
                                } else if (newPwd.length() > conf.getInt("max_pwd_length")) {
                                    errorMessage = Web.message(lang, "common.your_password_is_too_long");
                                } else if (!newPwd.equals(newPwd2)) {
                                    errorMessage = Web.message(lang, "common.your_passwords_do_not_match") + "!";
                                }
                                if (errorMessage == null) {
                                    panel = "users_pwd_success";
                                    request.setAttribute("nick", nick);
                                    db.updatePassword(nick, newPwd);
                                } else {
                                    panel = "users_pwd";
                                    request.setAttribute("console_failed", true);
                                    request.setAttribute("error_message", errorMessage);
                                    request.setAttribute("nick", nick);
                                }
                            } else {
                                panel = "users_pwd";
                                request.setAttribute("nick", nick);
                            }
                        } else if (change.equals("delete")) {
                            panel = "users_delete";
                            request.setAttribute("nick", nick);
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
                            Object errorMessage = null;
                            if (!gender.equals("-") && !gender.equals("m") && !gender.equals("f")) {
                                errorMessage = Web.message(lang, "common.your_gender_is_flawed");
                            } else if (!ut.isValidDate(day, month, year)) {
                                errorMessage = Web.message(lang, "common.your_date_of_birth_is") + "!";
                            } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                                errorMessage = Web.message(lang, "common.your_age_is_unrealistic");
                            } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                                errorMessage = Web.message(lang, "common.you_are_too_young_for") + "!";
                            } else if (!mail.contains("@")) {
                                errorMessage = Web.message(lang, "page.reg_error_message_mail_invalid.your_email_address_is_incorrect");
                            } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                                errorMessage = Web.message(lang, "page.reg_error_message_mail_invalid.your_email_address_is_incorrect");
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
                            panel = "users_account";
                            request.setAttribute("selected_no", gender.equals("m") || gender.equals("f") ? "" : " selected");
                            request.setAttribute("selected_male", gender.equals("m") ? " selected" : "");
                            request.setAttribute("selected_female", gender.equals("f") ? " selected" : "");
                            request.setAttribute("mail", mail);
                            request.setAttribute("day", day);
                            request.setAttribute("month", month);
                            request.setAttribute("year", year);
                            request.setAttribute("status", status);
                            request.setAttribute("nick", nick);
                            request.setAttribute("account_error_if_one", errorMessage == null ? "" : errorMessage);
                        } else if (change.equals("profile")) {
                            panel = "users_reg";
                            var nick2 = db.getData(nick, "nick2");
                            request.setAttribute("nick", nick2 != null ? nick2 : "");
                            for (var field : new String[]{"homepage", "city", "hobby", "description", "slogan", "signature", "icq", "live", "yahoo", "facebook", "twitter", "irc", "youtube", "instagram", "linkedin", "tiktok", "discord", "twitch", "github", "reddit", "snapchat", "pinterest", "whatsapp", "telegram", "fam_status"}) {
                                request.setAttribute(field, db.getData(nick, field));
                            }
                        } else if (change.equals("profile_change")) {
                            panel = "users_reg";
                            request.setAttribute("console_success", true);
                            var fields = new String[]{"homepage", "city", "hobby", "description", "slogan", "signature", "icq", "live", "yahoo", "facebook", "twitter", "irc", "youtube", "instagram", "linkedin", "tiktok", "discord", "twitch", "github", "reddit", "snapchat", "pinterest", "whatsapp", "telegram", "fam_status"};
                            var nick2 = db.getData(nick, "nick2");
                            request.setAttribute("nick", nick2 != null ? nick2 : "");
                            for (var field : fields) {
                                request.setAttribute(field, map.getOrDefault(field, ""));
                            }
                            for (var field : fields) {
                                db.updateNick(nick, field, map.getOrDefault(field, ""));
                            }
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
                        Object errorMessage = null;
                        var regSuccess = false;
                        var skin = map.getOrDefault("skin", "");
                        consoleValues.put("skin", skin);
                        Web.view("page", request, consoleValues);
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
                            ipResolved = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(ip).getCanonicalHostName() : ip;
                        } catch (UnknownHostException uhe) {
                            ipResolved = ip;
                        }
                        if (!pwd.equals(pwd2)) {
                            errorMessage = Web.message(lang, "common.your_passwords_do_not_match") + "!";
                        } else if (!gender.equals("-") && !gender.equals("m") && !gender.equals("f")) {
                            errorMessage = Web.message(lang, "common.your_gender_is_flawed");
                        } else if (!nick.matches(conf.getString("allowed_chars"))) {
                            errorMessage = Web.message(lang, "page.reg_error_message_nick_chars.your_nickname_must_contain_only");
                        } else if ((conf.getInt("guest") == 1 && nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase()))) {
                            errorMessage = Web.message(lang, "page.reg_error_message_nick_guest.your_nickname_may_not_be");
                        } else if (nick.length() < conf.getInt("min_nick_length")) {
                            errorMessage = Web.message(lang, "page.reg_error_message_nick_short.your_nickname_is_too_short");
                        } else if (nick.length() > conf.getInt("max_nick_length")) {
                            errorMessage = Web.message(lang, "page.reg_error_message_nick_long.your_nickname_is_too_long");
                        } else if (isCommandNick(nick)) {
                            errorMessage = Web.message(lang, "page.reg_error_message_nick_command.your_nickname_corresponds_to_a");
                        } else if (!ut.isValidDate(day, month, year)) {
                            errorMessage = Web.message(lang, "common.your_date_of_birth_is") + "!";
                        } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                            errorMessage = Web.message(lang, "common.your_age_is_unrealistic");
                        } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                            errorMessage = Web.message(lang, "common.you_are_too_young_for") + "!";
                        } else if (db.isRegistered(nick)) {
                            errorMessage = Web.message(lang, "page.reg_error_message_nick_registered.your_nickname_is_already_registered");
                        } else if (!mail.equals(mail2)) {
                            errorMessage = Web.message(lang, "common.your_email_addresses_do_not") + "!";
                        } else if (!mail.contains("@")) {
                            errorMessage = Web.message(lang, "page.reg_error_message_mail_invalid.your_email_address_is_incorrect");
                        } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                            errorMessage = Web.message(lang, "page.reg_error_message_mail_invalid.your_email_address_is_incorrect");
                        } else {
                            regSuccess = true;
                        }
                        if (!regSuccess) {
                            panel = "reg_form";
                            if (service.equals("reg_done")) {
                                request.setAttribute("console_failed", true);
                                request.setAttribute("error_message", errorMessage);
                            }
                            cid = newCaptchaId();
                        } else {
                            var color = ut.createRandomColor();
                            db.addNick(gender, nick, mail, color, pwd, question, answer, day, month, year);
                            panel = "reg_form_success";
                        }
                        request.setAttribute("reg_nick", nick);
                        request.setAttribute("reg_mail", mail);
                        request.setAttribute("reg_cid", cid);
                        request.setAttribute("reg_captcha", captcha);
                        request.setAttribute("reg_reminder_question", question);
                        request.setAttribute("reg_reminder_answer", answer);
                        request.setAttribute("reg_day", day);
                        request.setAttribute("reg_month", month);
                        request.setAttribute("reg_year", year);
                        request.setAttribute("selected_no", gender.equals("m") || gender.equals("f") ? "" : " selected");
                        request.setAttribute("selected_male", gender.equals("m") ? " selected" : "");
                        request.setAttribute("selected_female", gender.equals("f") ? " selected" : "");
                    } else {
                        panel = "users_fail";
                        request.setAttribute("nick", nick);
                    }
                } else {
                    panel = "users";
                }
            } else {
                panel = "404";
            }
        } else {
            response.setStatus(401);
            panel = "401";
        }
        pageContext.setAttribute("panel", panel);

%><c:if test="${not empty panel}"><t:layout design="console"><c:choose>
<c:when test="${panel eq 'index'}"><h1><fmt:message key="console.index.admin_console"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${panel eq '401'}"><h1>${SERVER_SOFTWARE}-401</h1></c:when>
<c:when test="${panel eq '404'}"><h1>${SERVER_SOFTWARE}-404</h1></c:when>
<c:when test="${panel eq 'chat'}"><h1><fmt:message key="console.chat.chat_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${panel eq 'cmdcfg'}"><h1><fmt:message key="console.cmdcfg.rights_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${panel eq 'commands'}"><h1><fmt:message key="console.commands.command_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${panel eq 'emot'}"><h1><fmt:message key="console.emot.emoticon_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${panel eq 'fun'}"><h1><fmt:message key="console.fun.fun_command_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${panel eq 'help'}"><h1><fmt:message key="console.help.help_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${panel eq 'host'}"><h1><fmt:message key="console.host.host_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${panel eq 'mail'}"><h1><fmt:message key="console.mail.mail_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${panel eq 'mime'}"><h1><fmt:message key="console.mime.mime_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${panel eq 'modules'}"><h1>Module configuration</h1></c:when>
<c:when test="${panel eq 'theme'}"><h1>WYSIWYG Theme-Creator</h1></c:when>
<c:when test="${fn:startsWith(panel, 'rooms')}"><h1><fmt:message key="common.room_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${fn:startsWith(panel, 'users')}"><h1><fmt:message key="common.user_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1></c:when>
<c:when test="${fn:startsWith(panel, 'reg_form')}"><h1>
<fmt:message key="common.user_management"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1></c:when>
</c:choose>
<c:choose>
<c:when test="${panel eq 'index'}">
<p>
<fmt:message key="console.index.welcome_to_the_admin_console"/>
</p>
</c:when>
<c:when test="${panel eq '401'}">
<p>
<fmt:message key="console.401.you_are_not_authorized_to"/><br>
<fmt:message key="console.401.in_order_to_use_this"/>!
</p>
</c:when>
<c:when test="${panel eq '404'}">
<p>
<fmt:message key="console.404.service_not_found"/>
</p>
</c:when>
<c:when test="${panel eq 'chat'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>
<table>
<form action="${paths.config_chat}&update=true" method="POST">
<c:forEach var="row" items="${console_rows}"><tr><td>
${row[2]}: </td><td><input class="form-control form-control-sm" type="text" name="${row[0]}" value="${row[1]}">
</td></tr>
</c:forEach>
<td>&nbsp;</td><td>
<input type="submit" class="btn btn-secondary btn-sm" name="" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</c:when>
<c:when test="${panel eq 'cmdcfg'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>
<table>
<form action="${paths.config_cmdrights}&update=true" method="POST">
<c:forEach var="row" items="${console_rows}"><tr><td>
${row[0]}: </td><td><input class="form-control form-control-sm" type="text" name="${row[0]}" value="${row[1]}">
</td></tr>
</c:forEach>
<td>&nbsp;</td><td>
<input type="submit" class="btn btn-secondary btn-sm" name="" value="<fmt:message key="console.cmdcfg.submit"/>">
</form>
</td></tr>
</table>
</p>
</c:when>
<c:when test="${panel eq 'commands'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>
<table>
<form [formGroup]="form" action="${paths.config_cmd}&update=true" method="POST">
<c:forEach var="row" items="${console_rows}"><tr><td>
${row[0]}</td><td><textarea name="${row[0]}" spellcheck="false" contenteditable>${row[1]}</textarea>
</td></tr>
</c:forEach>
<td>&nbsp;</td><td>
<input type="submit" class="btn btn-secondary btn-sm" name="" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</c:when>
<c:when test="${panel eq 'emot'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>
<span style="font-weight: bold"><fmt:message key="common.note"/></span> <fmt:message key="common.an_empty_field_removes_the"/>
<table>
<form action="${paths.config_emot}&update=true" method="POST">
<c:forEach var="row" items="${console_rows}"><tr><td>
${row[0]}: </td><td><input class="form-control form-control-sm" type="text" name="${row[0]}" value="${row[1]}">
</td></tr>
</c:forEach>
<td><input type="text" name="name" class="form-control form-control-sm" value=""> </td><td><input type="text" class="form-control form-control-sm" name="value" value=""></td></tr>
<td>&nbsp;</td><td>
<input type="submit" class="btn btn-secondary btn-sm" name="" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</c:when>
<c:when test="${panel eq 'fun'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>
<span style="font-weight: bold"><fmt:message key="common.note"/></span> <fmt:message key="common.an_empty_field_removes_the"/>
<table>
<form action="${paths.config_fun}&update=true" method="POST">
<c:forEach var="row" items="${console_rows}"><tr><td>
${row[0]}</td><td>${row[2]}: </td><td><textarea class="form-control form-control-sm" name="${row[0]}">${row[1]}</textarea>
</td></tr>
</c:forEach>
<td><textarea class="form-control form-control-sm" name="command"></textarea></td><td><textarea class="form-control form-control-sm" name="description"></textarea></td><td><textarea class="form-control form-control-sm" name="content"></textarea></td></tr>
<td>&nbsp;</td><td>
<input type="submit" class="btn btn-secondary btn-sm" name="" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</c:when>
<c:when test="${panel eq 'help'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>
<table>
<form action="${paths.config_help}&update=true" method="POST">
<c:forEach var="row" items="${console_rows}"><tr><td>
${row[0]}</td><td><textarea class="form-control form-control-sm" name="${row[0]}" spellcheck="false" contenteditable>${row[1]}</textarea>
</td></tr>
</c:forEach>
<td>&nbsp;</td><td>
<input type="submit" name="" class="btn btn-secondary btn-sm" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</c:when>
<c:when test="${panel eq 'host'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>
<span style="font-weight: bold"><fmt:message key="common.note"/></span> <fmt:message key="common.an_empty_field_removes_the"/>
<table>
<form action="${paths.config_host}&update=true" method="POST">
<c:forEach var="row" items="${console_rows}"><tr><td>
${row[0]}: </td><td><input class="form-control form-control-sm" type="text" name="${row[0]}" value="${row[1]}">
</td></tr>
</c:forEach>
<td><input type="text" class="form-control form-control-sm"  name="name" value=""> </td><td><input type="text" class="form-control form-control-sm"  name="value" value=""></td></tr>
<td>&nbsp;</td><td>
<input type="submit" class="btn btn-secondary btn-sm" name="" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</c:when>
<c:when test="${panel eq 'mail'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>
<table>
<form action="${paths.config_mail}&update=true" method="POST">
<c:forEach var="row" items="${console_rows}"><tr><td>
${row[0]}: </td><td><input class="form-control form-control-sm" type="text" name="${row[0]}" value="${row[1]}">
</td></tr>
</c:forEach>
<td>&nbsp;</td><td>
<input type="submit" class="btn btn-secondary btn-sm" name="" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</c:when>
<c:when test="${panel eq 'mime'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>
<table>
<form action="${paths.config_mime}&update=true" method="POST">
<c:forEach var="row" items="${console_rows}"><tr><td>
${row[0]}: </td><td><input class="form-control form-control-sm" type="text" name="${row[0]}" value="${row[1]}">
</td></tr>
</c:forEach>
<td><input type="text" name="name" class="form-control form-control-sm" value=""> </td><td><input type="text" class="form-control form-control-sm" name="value" value=""></td></tr>
<td>&nbsp;</td><td>
<input type="submit" name="" class="btn btn-secondary btn-sm" value="<fmt:message key="common.submit"/>">
</form>
</td></tr>
</table>
</p>
</c:when>
<c:when test="${panel eq 'theme'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>WYSIWYG-Theme-Creator für das bestehende Skin-System (z. B. <code>skin=native</code>, host-basierte Skins via Host-/Skin-Konfiguration). Wähle einen Skin-Namen, passe Farben, Typografie und Layout an und speichere das Theme.</p>
<form action="${paths.config_index}&service=theme&update=true" method="POST" accept-charset="utf-8">
  <table class="table table-sm" style="max-width:860px">
    <tr><th colspan="2">Allgemein</th></tr>
    <tr><td><strong>Skin</strong></td><td><input class="form-control form-control-sm" type="text" name="theme_skin" value="<c:out value='${theme_skin}'/>"> <span class="form-hint">z. B. native (aktiv per <code>?skin=...</code> oder Host-Mapping)</span></td></tr>
    <tr><td>Chatname</td><td><input class="form-control form-control-sm" type="text" id="tchatname" name="chatName" value="<c:out value='${theme_chatName}'/>" placeholder="z. B. Mein Chat"> <span class="form-hint">Leer = globaler Chatname (config <code>chat_name</code>). Wird als <code>${SERVER_SOFTWARE}</code> in Seiten angezeigt.</span></td></tr>
    <tr><td>Logo-URL</td><td><input class="form-control form-control-sm" type="text" id="tlogo" name="logo" value="<c:out value='${theme_logo}'/>" placeholder="${path_file}gfx/logo.svg oder https://..."> <span class="form-hint">Leer = Standard-Logo. Relativ zu <code>path_file</code> oder absolute URL.</span></td></tr>
    <tr><th colspan="2">Farben</th></tr>
    <tr><td>Primary</td><td><input type="color" id="tp" value="${theme_primary}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tpt" name="primary" value="${theme_primary}"></td></tr>
    <tr><td>Primary dark</td><td><input type="color" id="tpd" value="${theme_primaryDark}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tpdt" name="primaryDark" value="${theme_primaryDark}"></td></tr>
    <tr><td>Link</td><td><input type="color" id="tl" value="${theme_link}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tlt" name="link" value="${theme_link}"></td></tr>
    <tr><td>Text/Dark</td><td><input type="color" id="td" value="${theme_dark}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tdt" name="dark" value="${theme_dark}"></td></tr>
    <tr><td>Muted</td><td><input type="color" id="tm" value="${theme_muted}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tmt" name="muted" value="${theme_muted}"></td></tr>
    <tr><td>Border</td><td><input type="color" id="tb" value="${theme_border}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tbt" name="border" value="${theme_border}"></td></tr>
    <tr><td>Background</td><td><input type="color" id="tbg" value="${theme_bg}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tbgt" name="bg" value="${theme_bg}"></td></tr>
    <tr><td>Surface</td><td><input type="color" id="ts" value="${theme_surface}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tst" name="surface" value="${theme_surface}"></td></tr>
    <tr><td>Nav Background</td><td><input type="color" id="tnb" value="${theme_navBg}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tnbt" name="navBg" value="${theme_navBg}"></td></tr>
    <tr><td>Nav Text</td><td><input type="color" id="tnt" value="${theme_navText}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tntt" name="navText" value="${theme_navText}"></td></tr>
    <tr><td>Success</td><td><input type="color" id="tsu" value="${theme_success}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tsut" name="success" value="${theme_success}"></td></tr>
    <tr><td>Warning</td><td><input type="color" id="tw" value="${theme_warning}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="twt" name="warning" value="${theme_warning}"></td></tr>
    <tr><td>Danger</td><td><input type="color" id="tda" value="${theme_danger}"> <input class="form-control form-control-sm d-inline-block" style="width:120px" id="tdat" name="danger" value="${theme_danger}"></td></tr>
    <tr><th colspan="2">Layout &amp; Typografie</th></tr>
    <tr><td>Border-Radius</td><td><input class="form-control form-control-sm" type="text" id="trad" name="radius" value="<c:out value='${theme_radius}'/>" placeholder="z. B. 0.75rem, 8px, 0"></td></tr>
    <tr><td>Schriftgröße</td><td><input class="form-control form-control-sm" type="text" id="tfs" name="fontSize" value="<c:out value='${theme_fontSize}'/>" placeholder="z. B. 0.9375rem, 15px"></td></tr>
    <tr><td>Schriftart</td><td><input class="form-control form-control-sm" type="text" id="tff" name="fontFamily" value="<c:out value='${theme_fontFamily}'/>" placeholder="CSS font-family"></td></tr>
    <tr><td>Hintergrund-Verlauf</td><td><input type="hidden" name="bodyGradient" value="0"><label><input type="checkbox" id="tgrad" name="bodyGradient" value="1" ${theme_bodyGradient eq '1' ? 'checked' : ''}> Verlauf auf Seitenhintergrund aktivieren</label></td></tr>
    <tr><th colspan="2">Erweitert</th></tr>
    <tr><td>Custom CSS</td><td><textarea class="form-control form-control-sm" name="customCss" rows="5" style="font-family:monospace"><c:out value="${theme_customCss}"/></textarea></td></tr>
  </table>
  <p><strong>Vorschau</strong></p>
  <div id="theme-preview" style="border:1px solid ${theme_border};border-radius:${theme_radius};overflow:hidden;max-width:860px;margin-bottom:1rem;background:${theme_bg};color:${theme_dark};font-family:${theme_fontFamily};font-size:${theme_fontSize}">
    <div id="tp-nav" style="background:${theme_navBg};color:${theme_navText};padding:.75rem 1rem;font-weight:600;display:flex;gap:1rem;align-items:center"><span>Beispiel-Navigation (${theme_skin})</span><span style="opacity:.85">Start</span><span style="opacity:.85">Chat</span></div>
    <div id="tp-body" style="padding:1rem;background:${theme_surface}">
      <h3 id="tp-h" style="margin:0 0 .5rem 0;color:${theme_dark}">Überschrift</h3>
      <p id="tp-p" style="color:${theme_muted};margin:0 0 .75rem 0">Dies ist eine Vorschau des gewählten Themes (Farben, Schrift, Radius).</p>
      <a id="tp-btn" href="#" onclick="return false" style="display:inline-block;background:${theme_primary};color:#fff;padding:.45rem .9rem;border-radius:${theme_radius};text-decoration:none;margin-right:.5rem">Button</a>
      <a id="tp-link" href="#" onclick="return false" style="color:${theme_link};margin-right:.75rem">Link</a>
      <span id="tp-succ" style="display:inline-block;background:${theme_success};color:#fff;padding:.2rem .5rem;border-radius:4px;font-size:.85em;margin-right:.25rem">OK</span>
      <span id="tp-warn" style="display:inline-block;background:${theme_warning};color:#212529;padding:.2rem .5rem;border-radius:4px;font-size:.85em;margin-right:.25rem">Warn</span>
      <span id="tp-dang" style="display:inline-block;background:${theme_danger};color:#fff;padding:.2rem .5rem;border-radius:4px;font-size:.85em">Fehler</span>
      <div style="margin-top:.75rem;padding:.6rem;border:1px solid ${theme_border};border-radius:${theme_radius};background:${theme_bg}">Beispiel-Box / Eingabeumgebung</div>
    </div>
  </div>
  <input type="submit" class="btn btn-secondary btn-sm" value="<fmt:message key="common.submit"/>">
</form>
<c:if test="${not empty theme_skins}"><p class="mt-3"><strong>Vorhandene Skin-Themes:</strong> <c:forEach var="ts" items="${theme_skins}" varStatus="st"><a href="${paths.config_index}&service=theme&theme_skin=<c:out value='${ts}'/>"><code><c:out value='${ts}'/></code></a><c:if test="${!st.last}">, </c:if></c:forEach></p></c:if>
<script>(function(){function by(id){return document.getElementById(id);}function val(id){var e=by(id);return e?e.value:'';}function sync(c,t){if(!by(c)||!by(t))return;by(c).addEventListener('input',function(){by(t).value=by(c).value;upd();});by(t).addEventListener('input',function(){if(/^#[0-9a-fA-F]{6}$/.test(by(t).value))by(c).value=by(t).value;upd();});}
function upd(){var box=by('theme-preview');if(!box)return;var pv=val('tp'),dk=val('td'),mu=val('tm'),bo=val('tb'),bg=val('tbg'),su=val('ts'),ln=val('tl'),nb=val('tnb'),nt=val('tnt'),sc=val('tsu'),wa=val('tw'),da=val('tda'),ra=val('trad')||'0.75rem',fs=val('tfs')||'0.9375rem',ff=val('tff')||'sans-serif';
box.style.background=bg;box.style.color=dk;box.style.borderColor=bo;box.style.borderRadius=ra;box.style.fontFamily=ff;box.style.fontSize=fs;
var nav=by('tp-nav');if(nav){nav.style.background=nb;nav.style.color=nt;} var body=by('tp-body');if(body)body.style.background=su;
var h=by('tp-h');if(h)h.style.color=dk; var p=by('tp-p');if(p)p.style.color=mu; var btn=by('tp-btn');if(btn){btn.style.background=pv;btn.style.borderRadius=ra;} var lnk=by('tp-link');if(lnk)lnk.style.color=ln;
var ok=by('tp-succ');if(ok)ok.style.background=sc; var w=by('tp-warn');if(w)w.style.background=wa; var d=by('tp-dang');if(d)d.style.background=da;
var bx=body?body.querySelector('div[style*="margin-top"]'):null;if(bx){bx.style.borderColor=bo;bx.style.borderRadius=ra;bx.style.background=bg;}}
[['tp','tpt'],['tpd','tpdt'],['tl','tlt'],['td','tdt'],['tm','tmt'],['tb','tbt'],['tbg','tbgt'],['ts','tst'],['tnb','tnbt'],['tnt','tntt'],['tsu','tsut'],['tw','twt'],['tda','tdat']].forEach(function(x){sync(x[0],x[1]);});
['trad','tfs','tff'].forEach(function(id){var e=by(id);if(e)e.addEventListener('input',upd);});
})();</script>
</c:when>
<c:when test="${panel eq 'modules'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>Enable/disable modules. Unchecked modules are removed from the navbar. The forum remains publicly viewable in read-only mode; registered users keep existing write permissions. When the chat module is enabled, direct chat login can be used instead of community login.</p>
<form action="${paths.config_index}&service=modules&update=true" method="POST">
<table class="table table-sm">
  <thead><tr><th>Module</th><th>Enabled</th><th>Description</th></tr></thead>
  <tbody>
  <c:forEach var="row" items="${console_rows}">
    <tr>
      <td><strong>${row[0]}</strong></td>
      <td>
        <input type="hidden" name="${row[0]}" value="0">
        <input type="checkbox" name="${row[0]}" value="1" ${row[1] eq '1' ? 'checked' : ''}>
      </td>
      <td>${row[2]}</td>
    </tr>
  </c:forEach>
  </tbody>
</table>
<input type="submit" class="btn btn-secondary btn-sm" value="<fmt:message key="common.submit"/>">
</form>
</c:when>
<c:when test="${panel eq 'rooms'}">
<p>
<form action="${paths.config_rooms}&update=true&change=search" method="POST">
<fmt:message key="console.rooms.search_for_space"/> <input type="text" class="form-control form-control-sm" name="room" value="">
<input type="submit" name="" class="btn btn-secondary btn-sm" value="<fmt:message key="common.submit"/>">
</form>
</p>
</c:when>
<c:when test="${panel eq 'rooms_404'}">
<p>
<fmt:message key="common.the_requested_page_does_not"/>
</p>
</c:when>
<c:when test="${panel eq 'rooms_add' or panel eq 'rooms_config' or panel eq 'rooms_modify'}">
<c:choose>
<c:when test="${panel eq 'rooms_add'}"><fmt:message key="console.rooms_add.the_room"/> <span style="font-weight: bold">${room}</span> <fmt:message key="console.rooms_add.create"/>
<form action="${paths.config_rooms}&update=true&room=${room}&change=set_room" method="POST" accept-charset="utf-8"></c:when>
<c:when test="${panel eq 'rooms_config'}">
<p>
<a href="${paths.config_rooms}&update=true&room=${room}&change=delete"><fmt:message key="console.rooms_config.here"/></a> <fmt:message key="console.rooms_config.you_can_remove_the_room"/>
</p>
<form action="${paths.config_rooms}&update=true&room=${room}&change=modify" method="POST" accept-charset="utf-8"></c:when>
<c:otherwise>
<p style="font-weight: bold"><fmt:message key="common.change_successfully"/>!</p>
<form action="${paths.config_rooms}&update=true&room=${room}&change=modify" method="POST" accept-charset="utf-8"></c:otherwise>
</c:choose>
  <p>
    <table>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="common.the_spatial_theme"/> </span>
		</td>
		<td>
	      <input type="text" size="50" maxlength="255" class="form-control form-control-sm"  name="topic" value="${topic}">
        </td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="common.if_the_room_is_closed"/> </span>
		</td>
		<td>
	      <input type="text" size="50" maxlength="255" class="form-control form-control-sm"  name="lock" value="${lock}">
        </td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="common.the_blocking_reason"/> </span>
		</td>
		<td>
	      <input type="text" size="50" maxlength="255" class="form-control form-control-sm"  name="lock_reason" value="${lock_reason}">
        </td>
      </tr>	  
      <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="common.is_a_standard_room"/> </span>
		</td>
		<td>
	      <input type="text" size="50" maxlength="255" class="form-control form-control-sm"  name="standard" value="${standard}">
        </td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="common.allow_smilies"/> </span>
		</td>
		<td>
	      <input type="text" size="50" maxlength="255" class="form-control form-control-sm"  name="allow_smilies" value="${allow_smilies}">
        </td>
      </tr>
    </table>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.send_a_change"/>!!">
  </p>
</form>
</c:when>
<c:when test="${panel eq 'rooms_delete'}">
<p>
<fmt:message key="common.the_room"/> <span style="font-weight: bold">${room}</span> <fmt:message key="console.rooms_delete.it_was_successfully_removed"/>
</p>
</c:when>
<c:when test="${panel eq 'rooms_exist'}">
<p>
<fmt:message key="common.the_room"/> <span style="font-weight: bold">${room}</span> <fmt:message key="console.rooms_exist.already_exists"/><br>
<fmt:message key="console.rooms_exist.you_can"/> <a href="${paths.config_rooms}&update=true&change=delete&room=${room}"><fmt:message key="common.here"/><a/> <fmt:message key="console.rooms_exist.click_to_remove_it"/>
</p>
</c:when>
<c:when test="${panel eq 'rooms_fail'}">
<p>
<fmt:message key="common.the_room"/> <span style="font-weight: bold">${room}</span> <fmt:message key="common.does_not_exist"/><br>
<fmt:message key="common.please_click"/> <a href="${paths.config_rooms}&update=true&change=add_room&room=${room}"><fmt:message key="common.here"/></a> <fmt:message key="console.rooms_fail.to_create_the_room"/>
</p>
</c:when>
<c:when test="${panel eq 'rooms_set'}">
<p>
<fmt:message key="console.rooms_set.space"/> <span style="font-weight: bold">${room}</span> <fmt:message key="console.rooms_set.it_was_created"/>
</span>
</c:when>
<c:when test="${panel eq 'users'}">
<p>
<form action="${paths.config_users}&update=true" method="POST">
<fmt:message key="console.users.search_for_chatter"/> <input type="text" name="user" class="form-control form-control-sm" value="">
<input type="submit" name="" class="btn btn-secondary btn-sm" value="<fmt:message key="common.submit"/>">
</form>
</p>
</c:when>
<c:when test="${panel eq 'users_fail'}">
<p>
<fmt:message key="common.the_user"/> <span style="font-weight: bold">${nick}</span> <fmt:message key="common.does_not_exist"/><br>
<fmt:message key="common.please_click"/> <a href="${paths.config_users}&update=true&user=${nick}&change=reg"><fmt:message key="common.here"/></a> <fmt:message key="console.users_fail.to_create_the_user"/>
</p>
</c:when>
<c:when test="${panel eq 'users_delete'}">
<p>
<fmt:message key="common.the_user"/> <span style="font-weight:bold">${nick}</span> <fmt:message key="console.users_delete.has_been_removed"/>
</p>
</c:when>
<c:when test="${panel eq 'users_pwd_success'}">
<p>
<fmt:message key="console.users_pwd_success.password_of"/> <span style="font-weight:bold">${nick}</span> <fmt:message key="console.users_pwd_success.changed"/>!
</p>
</c:when>
<c:when test="${panel eq 'users_page'}">
<form action="${paths.config_users}&update=true&user=${nick}" method="POST" accept-charset="utf-8">
  <p class="form-section-title">
    <fmt:message key="console.users_page.here_you_can_edit_the"><fmt:param value="${nick}"/></fmt:message>
  </p>
  <p>
  <fmt:message key="console.users_page.what_do_you_want_to"/></br>
  <select class="form-control form-control-sm col-md-6 mb-3" name="change" size="4">
    <option value="<fmt:message key="console.users_page.profile"/>" selected><fmt:message key="common.processing_profile"/></option>
    <option value="password"><fmt:message key="common.processing_the_password"/></option>
    <option value="<fmt:message key="console.users_page.data"/>"><fmt:message key="common.processing_account_data"/></option>
    <option value="delete"><fmt:message key="common.delete_account"/></option>
  </select>
  </p>
  <p class="form-section-title">
    <fmt:message key="console.users_page.and_then_we_go_on"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.continue_to_the_next_page"/>">
  </p>
</form>
</c:when>
<c:when test="${panel eq 'users_pwd'}">

<form action="${paths.config_users}&update=true&user=${nick}&change=password" method="POST" accept-charset="utf-8"><c:if test="${console_failed}">
  <p class="form-section-title">
    <fmt:message key="page.account_failed.an_error_has_occurred"/>
  </p>
  <p style="font-weight: bold; color: #ff0000">
    ${error_message}
  </p>
</c:if>
  <p class="form-section-title">
    <fmt:message key="common.necessary_information"/>
  </p>
  <p>
  <fmt:message key="console.users_pwd.the_new_password"/><br />
  <span class="form-hint"><fmt:message key="common.should_be_at_least_four"/></span><br />
  <input type="password" size="10" maxlength="30" class="form-control form-control-sm col-md-5 mb-2" name="new_pwd" value="">
  <fmt:message key="console.users_pwd.repeat_new_password"/><br />
  <span class="form-hint"><fmt:message key="console.users_pwd.to_make_sure_you_are"/></span><br />
  <input type="password" size="10" maxlength="30" class="form-control form-control-sm col-md-5 mb-2" name="new_pwd2" value="">
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.send_a_change"/>!!">
  </p>
</form>
</c:when>
<c:when test="${panel eq 'users_account'}">
<form action="${paths.config_users}&update=true&user=${nick}&change=data" method="POST" accept-charset="utf-8">${account_error_if_one}
  <p class="form-section-title">
    <fmt:message key="common.necessary_information"/>
  </p>
  <p>
    <table>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="console.users_account.the_sex"/> </span>
		</td>
		<td>
            <select class="form-control form-control-sm col-md-3 mb-2" name="gender" size="1">
            <option value="-"${selected_no}><fmt:message key="common.not_specified"/></option>
            <option value="m"${selected_male}><fmt:message key="common.man"/></option>
            <option value="f"${selected_female}><fmt:message key="common.woman"/></option>
          </select>
        </td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="console.users_account.the_email_address"/> </span>
		</td>
		<td>
	      <input type="email" size="50" maxlength="255" class="form-control form-control-sm"  name="mail" value="${mail}">
        </td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="console.users_account.date_of_birth"/> </span>
		</td>
		<td>
		  <div class="input-group">
	      <input type="text" size="2" maxlength="2" class="form-control form-control-sm col-md-2 mb-1" name="day" value="${day}"><input type="text" size="2" maxlength="2" class="form-control form-control-sm col-md-2 mb-1" name="month" value="${month}"><input type="text" size="4" maxlength="4" class="form-control form-control-sm col-md-2 mb-1" name="year" value="${year}">
          </div>
		</td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="console.users_account.status"/> </span>
		</td>
		<td>
	      <input type="text" size="3" maxlength="3" class="form-control form-control-sm"  name="status" value="${status}">
        </td>
      </tr>
    </table>
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.send_a_change"/>!!">
  </p>
</form>
</c:when>
<c:when test="${panel eq 'users_reg'}">
<span style="font-weight: bold"><c:if test="${console_success}"><fmt:message key="common.change_successfully"/>!</c:if></span>
<p>
<form action="${paths.config_users}&update=true&change=profile_change" method="POST">
	      <input type="hidden" name="user" value="${nick}">
    <table>
      <tr><td><span style="font-weight: bold">Homepage: </span></td><td><input type="url" size="50" maxlength="255" class="form-control form-control-sm" name="homepage" value="${homepage}"></td></tr>
      <tr><td><span style="font-weight: bold"><fmt:message key="console.users_reg.place_of_residence"/> </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="city" value="${city}"></td></tr>
      <tr><td><span style="font-weight: bold">Hobby: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="hobby" value="${hobby}"></td></tr>
      <tr><td><span style="font-weight: bold"><fmt:message key="common.description"/> </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="description" value="${description}"></td></tr>
      <tr><td><span style="font-weight: bold">Motto: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="slogan" value="${slogan}"></td></tr>
      <tr><td><span style="font-weight: bold"><fmt:message key="common.signature"/> </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="signature" value="${signature}"></td></tr>
      <tr><td><span style="font-weight: bold">ICQ-UIN: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="icq" value="${icq}"></td></tr>
      <tr><td><span style="font-weight: bold">Skype: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="live" value="${live}"></td></tr>
      <tr><td><span style="font-weight: bold">Yahoo: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="yahoo" value="${yahoo}"></td></tr>
      <tr><td><span style="font-weight: bold">Facebook: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="facebook" value="${facebook}"></td></tr>
      <tr><td><span style="font-weight: bold">Twitter: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="twitter" value="${twitter}"></td></tr>
      <tr><td><span style="font-weight: bold">IRC: </span></td><td><input type="url" size="50" maxlength="255" class="form-control form-control-sm" name="irc" value="${irc}"></td></tr>
      <tr><td><span style="font-weight: bold"><fmt:message key="console.users_reg.youtube"/> </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="youtube" value="${youtube}"></td></tr>
      <tr><td><span style="font-weight: bold">Instagram: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="instagram" value="${instagram}"></td></tr>
      <tr><td><span style="font-weight: bold">LinkedIn: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="linkedin" value="${linkedin}"></td></tr>
      <tr><td><span style="font-weight: bold">TikTok: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="tiktok" value="${tiktok}"></td></tr>
      <tr><td><span style="font-weight: bold">Discord: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="discord" value="${discord}"></td></tr>
      <tr><td><span style="font-weight: bold">Twitch: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="twitch" value="${twitch}"></td></tr>
      <tr><td><span style="font-weight: bold">GitHub: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="github" value="${github}"></td></tr>
      <tr><td><span style="font-weight: bold">Reddit: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="reddit" value="${reddit}"></td></tr>
      <tr><td><span style="font-weight: bold">Snapchat: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="snapchat" value="${snapchat}"></td></tr>
      <tr><td><span style="font-weight: bold">Pinterest: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="pinterest" value="${pinterest}"></td></tr>
      <tr><td><span style="font-weight: bold">WhatsApp: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="whatsapp" value="${whatsapp}"></td></tr>
      <tr><td><span style="font-weight: bold">Telegram: </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="telegram" value="${telegram}"></td></tr>
      <tr><td><span style="font-weight: bold"><fmt:message key="common.family_status"/> </span></td><td><input type="text" size="50" maxlength="255" class="form-control form-control-sm" name="fam_status" value="${fam_status}"></td></tr>
    </table>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.send_a_change"/>!!">
  </p>
</form>
</p>
</c:when>
<c:when test="${panel eq 'reg_form'}">
<form action="${paths.config_users}&update=true&user=${nick}&change=reg" method="POST" accept-charset="utf-8"><c:if test="${console_failed}">
  <p class="form-section-title">
    <fmt:message key="page.reg_failed.registration_is_not_possible"/>!
  </p>
  <p style="font-weight: bold; color: #ff0000">
    ${error_message}
  </p>
</c:if>
  <input type="hidden" name="service" value="reg_done">
  <p class="form-section-title">
    <fmt:message key="common.necessary_information"/>
  </p>
  <p>
  <div class="input-group">
  <select class="form-control form-control-sm col-md-1 mb-2" name="reg_gender" size="1">
    <option value="-"${selected_no}><fmt:message key="common.gender"/></option>
    <option value="m"${selected_male}><fmt:message key="console.reg_form.the"/></option>
    <option value="f"${selected_female}><fmt:message key="console.reg_form.the_2"/></option>
  </select>
  <input type="text" size="20" maxlength="20" class="form-control form-control-sm col-md-6 mb-2" name="reg_nick" value="${reg_nick}" placeholder="<fmt:message key="console.reg_form.the_nickname_at_least_two"/>)">
  </div>
    <div class="input-group">
  <input type="text" size="2" maxlength="2" class="form-control form-control-sm col-md-1 mb-1" name="reg_day" value="${reg_day}" placeholder="<fmt:message key="console.reg_form.birthday_day"/>"><input type="text" size="2" maxlength="2" class="form-control form-control-sm col-md-1 mb-1" name="reg_month" value="${reg_month}" placeholder="<fmt:message key="common.month"/>"><input type="text" size="4" maxlength="4" class="form-control form-control-sm col-md-1 mb-1" name="reg_year" value="${reg_year}" placeholder="<fmt:message key="common.year"/>">
  </div>
  <input type="email" size="50" maxlength="255" class="form-control form-control-sm col-md-5 mb-2" name="reg_mail" value="${reg_mail}" placeholder="<fmt:message key="console.reg_form.the_e_mail_address"/>">
  <input type="email" size="50" maxlength="255" class="form-control form-control-sm col-md-5 mb-2" name="reg_mail2" value="" placeholder="<fmt:message key="console.reg_form.the_e_mail_address_repetition"/>)">  
  <input type="password" size="50" maxlength="20" class="form-control form-control-sm col-md-5 mb-2" name="pwd" value="" placeholder="<fmt:message key="console.reg_form.the_password"/>">
  <input type="password" size="50" maxlength="20" class="form-control form-control-sm col-md-5 mb-2" name="pwd2" value="" placeholder="<fmt:message key="console.reg_form.the_password_repetition"/>)">

  </p>
  <p class="form-section-title">
    <fmt:message key="console.reg_form.security_code"/>
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.complete_registration"/>!">
  </p>
</form>
</c:when>
<c:when test="${panel eq 'reg_form_success'}">
  <p>
    <fmt:message key="console.reg_form_success.you_have"/> <span style="font-weight: bold;">${reg_nick}</span> <fmt:message key="console.reg_form_success.successfully_registered"/>
  </p>
</c:when>
</c:choose></t:layout></c:if>
