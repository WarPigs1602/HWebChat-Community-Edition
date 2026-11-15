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
                Bootstrap.getBoot().getChatServices().news(request, response, map, true);
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
