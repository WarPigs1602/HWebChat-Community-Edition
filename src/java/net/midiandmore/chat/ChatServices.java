/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.midiandmore.chat;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import static java.lang.System.currentTimeMillis;
import static java.net.InetAddress.getByName;
import java.util.HashMap;
import java.util.Map;
import static net.midiandmore.chat.Bootstrap.logError;

/**
 *
 * @author windo
 */
public class ChatServices {

    /**
     * Prepares the style sheet of a JSP for the browser: delivered file paths
     * get the context path.
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
    private String readLang(HttpServletRequest request) {
        return I18n.language(request);
    }

    private void applyLang(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var lang = readLang(request);
        var session = request.getSession(false);
        if (session != null) {
            session.setAttribute("lang", lang);
        }
        var requestedLang = map.getOrDefault("lang", "");
        if (response != null && requestedLang != null && !requestedLang.isBlank()) {
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
        return I18n.language(request);
    }

    private String detectScheme(HttpServletRequest request) {
        var xForwardedProto = request.getHeader("X-Forwarded-Proto");
        if (xForwardedProto != null && !xForwardedProto.isBlank()) {
            return xForwardedProto.split(",")[0].trim().toLowerCase();
        }
        return request.isSecure() ? "https" : "http";
    }

    public String stripHash(String value) {
        if (value != null && value.startsWith("#")) {
            return value.substring(1);
        }
        return value;
    }

    private boolean setupRequired(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (Bootstrap.boot.isSetupNeeded()) {
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            var view = new View("system", "setup_required", setupLang(request), new java.util.HashMap<>());
            view.set("setup_url", request.getContextPath() + "/setup.jsp")
                    .set("SERVER_SOFTWARE", Bootstrap.boot.getConfig().getChatName())
                    .set("SERVER_YEAR", Software.SERVER_YEAR)
                    .set("SERVER_VENDOR", Software.SERVER_VENDOR);
            response.getWriter().print(view);
            return true;
        }
        return false;
    }

    Map<String, String> requestMap(HttpServletRequest request) {
        Map<String, String> values = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> values.put(key, value.length == 0 ? "" : value[0]));
        return values;
    }

    private Map<String, String> requestValues(HttpServletRequest request, HttpServletResponse response) {
        var values = requestMap(request);
        applyLang(request, response, values);
        return values;
    }

    protected void parsePage(String page, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (setupRequired(request, response)) {
            return;
        }
        var conf = Bootstrap.boot.getConfig();
        var map2 = requestValues(request, response);
        if (page.equals("start")) {
            // Die Startseite
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            startPage("start", request, response, map2);
        } else {
            response.setStatus(404);
            response.setContentType("text/html; charset=" + conf.getString("charset"));
            printTemplate("error", request, response, map2);
        }
    }





    /**
     * Registrierungsformular
     *
     * @param out Die Output-Klasse
     */
    /**
     * Prepares a JSP that is requested directly: checks that the chat is set
     * up, takes over the language and provides the values of the templates
     * (<code>${nick}</code>, <code>${paths.login}</code>, ...) as request
     * attributes.
     *
     * @param kind <code>plain</code> (only the language), <code>page</code>,
     * <code>document</code> (a fixed document; with the parameter
     * <code>community=true</code> only for users of the community),
     * <code>script</code> or <code>style</code>
     * @return <code>false</code> if the response is complete already (chat not
     * set up, community session expired)
     */
    boolean begin(String kind, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (setupRequired(request, response)) {
            return false;
        }
        var map = requestValues(request, response);
        var charset = Bootstrap.boot.getConfig().getString("charset");
        switch (kind) {
            case "page" ->
                response.setContentType("text/html; charset=" + charset);
            case "document" -> {
                response.setContentType("text/html; charset=" + charset);
                if ("true".equalsIgnoreCase(map.getOrDefault("community", ""))) {
                    var community = activeCommunitySession(request.getSession(false), Bootstrap.boot.getChatManager());
                    if (community == null) {
                        printTemplate("timeout", request, response, map);
                        return false;
                    }
                    community.setTimeoutTimer(0);
                }
            }
            case "script" ->
                response.setContentType("text/javascript");
            case "style" ->
                response.setContentType("text/css");
            default -> {
            }
        }
        expose(kind, request);
        return true;
    }

    /**
     * Provides the values of the templates as request attributes, see
     * {@link #begin(String, HttpServletRequest, HttpServletResponse)}.
     */
    void expose(String kind, HttpServletRequest request) {
        expose(kind, request, Map.of());
    }

    /**
     * Provides the values of the templates and the given additional values as
     * request attributes. The additional values can be used by texts of the
     * chat as well (<code>%count%</code>).
     */
    void expose(String kind, HttpServletRequest request, Map<String, ?> values) {
        var map = requestMap(request);
        var view = switch (kind) {
            case "page" ->
                getTemplate("", request, map);
            case "document" ->
                getPage("", request, map);
            case "script" ->
                getScript("", request, map);
            case "style" ->
                getStyleSheet("", request, null, map);
            default -> {
                var lang = readLang(request);
                request.setAttribute("lang", lang);
                request.setAttribute(JspTemplates.FMT_LOCALE, I18n.locale(lang));
                yield null;
            }
        };
        if (view != null) {
            values.forEach(view::set);
            view.expose(request);
        }
    }


    /**
     * &Uuml;berpr&uuml;ft ob ein Nickname einem Befehl entspricht
     *
     * @param nick Der Nickname
     */
    boolean nickIsCommand(String nick) {
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
        var view = ViewFactory.create(type, name, readLang(request), new ViewFactory.Request(
                nick != null ? nick : "", sid != null ? sid : "", skin != null ? skin : "", room != null ? room : "",
                reason != null ? reason : "", host, owner, map.getOrDefault("target", ""), map.getOrDefault("user", ""),
                roomName, (scheme.equals("https") ? "wss" : "ws") + "://" + host, scheme + "://" + host));
        var session = request.getSession(false);
        if ("template".equals(type) || "pages".equals(type)) {
            view.defaults().put("console_link_visible", Web.consoleAdministrator(request) != null);
        }
        var userAgent = request.getHeader("User-Agent");
        view.defaults().put("user_agent", userAgent != null ? userAgent : "");
        view.defaults().put("designs", new ViewFactory.Texts(design -> selectCommunityDesign(design, session,
                Bootstrap.boot.getChatManager(), db)));
        try {
            var themes = Bootstrap.boot.getConfig().loadDataFromJSON("themes.json", "skin", "data");
            var tjson = themes.getOrDefault(skin, null);
            if (tjson == null && "native".equals(skin)) tjson = themes.get("native");
            if (tjson == null) tjson = themes.get("native");
            if (tjson != null) {
                var jr = jakarta.json.Json.createReader(new java.io.StringReader(tjson));
                var jo = jr.readObject(); jr.close();
                view.defaults().put("theme_logo", jo.getString("logo", ""));
                var tcn = jo.getString("chatName", "").trim();
                if (!tcn.isBlank()) {
                    view.defaults().put("theme_chat_name", tcn);
                    view.defaults().put("SERVER_SOFTWARE", tcn);
                } else {
                    view.defaults().put("theme_chat_name", "");
                }
            } else {
                view.defaults().put("theme_logo", "");
                view.defaults().put("theme_chat_name", "");
            }
        } catch (Exception e) {
            view.defaults().put("theme_logo", "");
            view.defaults().put("theme_chat_name", "");
        }
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
    public View getConsole(String template, HttpServletRequest request, Map<String, String> map) {
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
    public View getTemplate(String template, HttpServletRequest request, Map<String, String> map) {
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
    public void printTemplate(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
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
        try {
            var themes = Bootstrap.boot.getConfig().loadDataFromJSON("themes.json", "skin", "data");
            var tjson = themes.getOrDefault(skin, null);
            if (tjson == null && "native".equals(skin)) tjson = themes.get("native");
            if (tjson != null) {
                var jr = jakarta.json.Json.createReader(new java.io.StringReader(tjson));
                var jo = jr.readObject(); jr.close();
                view.set("theme_primary", jo.getString("primary", "#0d6efd"));
                view.set("theme_primary_dark", jo.getString("primaryDark", "#0a58ca"));
                view.set("theme_dark", jo.getString("dark", "#212529"));
                view.set("theme_muted", jo.getString("muted", "#6c757d"));
                view.set("theme_border", jo.getString("border", "#dee2e6"));
                view.set("theme_bg", jo.getString("bg", "#f0f2f5"));
                view.set("theme_surface", jo.getString("surface", "#ffffff"));
                view.set("theme_custom_css", jo.getString("customCss", ""));
                view.set("theme_link", jo.getString("link", jo.getString("primary", "#0d6efd")));
                view.set("theme_radius", jo.getString("radius", "0.75rem"));
                view.set("theme_font_size", jo.getString("fontSize", "0.9375rem"));
                view.set("theme_font_family", jo.getString("fontFamily", "system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif"));
                view.set("theme_nav_bg", jo.getString("navBg", "#212529"));
                view.set("theme_nav_text", jo.getString("navText", "#ffffff"));
                view.set("theme_success", jo.getString("success", "#198754"));
                view.set("theme_warning", jo.getString("warning", "#ffc107"));
                view.set("theme_danger", jo.getString("danger", "#dc3545"));
                view.set("theme_body_gradient", jo.getString("bodyGradient", "1"));
                view.set("theme_logo", jo.getString("logo", ""));
            }
        } catch (Exception e) {}
        return view;
    }

    /**
     * Prepares the style sheet of a JSP for the browser: delivered file paths
     * get the context path.
     *
     * @param css the style sheet
     * @return the style sheet for the request
     */
    String processStyleSheet(String css, HttpServletRequest request, HttpServletResponse response) {
        css = css.replace("url(\"file/", "url(\"" + request.getContextPath() + "/file/")
                .replace("url('file/", "url('" + request.getContextPath() + "/file/")
                .replace("url(file/", "url(" + request.getContextPath() + "/file/");
        response.setHeader("Cache-Control", "no-cache");
        return css;
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
     * Erzeugt eine neue Sessionid
     *
     * @return Sessionid
     */
    protected String generateSid() {
        var ut = Bootstrap.boot.getUtil();
        ut.setRnd(new Random(currentTimeMillis()));
        return ut.createRandomId();
    }

    void startPage(String template, HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var boot = Bootstrap.boot;
        var db = boot.getConfig().getDb();
        var util = boot.getUtil();
        var cm = boot.getChatManager();
        var community = activeCommunitySession(request.getSession(false), cm);
        var pageTemplate = template.equals("start") && community != null ? "start_logged_in" : template;
        try {
            var counts = db.getHomepageCounts();
            var latest = new NewsRepository(db, boot.getConfig().getP().getProperty("sql.prefix", "")).latest();
            var homepageNick = community == null ? "" : org.owasp.encoder.Encode.forHtmlContent(community.getName());
            var roomNames = new java.util.ArrayList<String>(cm.getRooms().keySet());
            java.util.Collections.sort(roomNames, String.CASE_INSENSITIVE_ORDER);
            var configuredDefault = boot.getConfig().getString("default_room");
            var dr = configuredDefault == null ? "" : configuredDefault.trim();
            if (dr.isEmpty()) {
                dr = "Lounge";
            }
            var defaultRoomName = dr;
            roomNames.removeIf(r -> r.equalsIgnoreCase(defaultRoomName));
            roomNames.add(0, defaultRoomName);
            var page = getTemplate(pageTemplate, request, map)
                    .set("homepage_chatters", org.owasp.encoder.Encode.forHtmlContent(cm.getUserSizeInChat()))
                    .set("homepage_rooms", org.owasp.encoder.Encode.forHtmlContent(String.valueOf(cm.getRooms().size())))
                    .set("homepage_registered", org.owasp.encoder.Encode.forHtmlContent(String.valueOf(counts.registered())))
                    .set("homepage_messages", org.owasp.encoder.Encode.forHtmlContent(String.valueOf(counts.messages())))
                    .set("homepage_nick", homepageNick)
                    .set("chat_rooms", roomNames)
                    .set("default_room", org.owasp.encoder.Encode.forHtmlAttribute(defaultRoomName));
            if (latest.isPresent()) {
                var article = latest.get();
                page = page
                        .set("homepage_news_title", org.owasp.encoder.Encode.forHtmlContent(article.title()))
                        .set("homepage_news_author", org.owasp.encoder.Encode.forHtmlContent(article.author()))
                        .set("homepage_news_published", org.owasp.encoder.Encode.forHtmlContent(util.getTime(article.published())))
                        .set("homepage_news_body", util.parseBb(util.parseHtml(article.body())))
                        .set("homepage_news_url", org.owasp.encoder.Encode.forHtmlAttribute(
                                request.getContextPath() + "/news.jsp"));
            } else {
                page = page.set("homepage_news_url", "");
            }
            util.submitContent(page, response);
        } catch (java.sql.SQLException ex) {
            logError(ex);
            response.setStatus(503);
            util.submitContent(getTemplate("homepage_unavailable", request, map), response);
        }
    }

    public void news(HttpServletRequest request, HttpServletResponse response, Map<String, String> map, boolean admin) {
        var conf = Bootstrap.boot.getConfig();
        var util = Bootstrap.boot.getUtil();
        var lang = readLang(request);
        var consoleUrl = request.getContextPath() + "/" + Web.consolePath() + "?service=news";
        try {
            var repository = new NewsRepository(conf.getDb(), conf.getP().getProperty("sql.prefix", ""));
            if (admin && "POST".equalsIgnoreCase(request.getMethod())) {
                var session = request.getSession(false);
                var token = session == null ? null : (String) session.getAttribute("consoleCsrf");
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
                    repository.create(title, body, (String) request.getAttribute("console_administrator"));
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
                token = Web.consoleCsrf(request.getSession(false));
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
                        .append("</p><div class=\"news-body\">")
                        .append(util.parseBb(util.parseHtml(article.body()))).append("</div>");
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
        var id = edit ? ("news-body-" + article.id()) : "news-body-new";
        var html = new StringBuilder("<form method=\"post\" class=\"mb-4\" action=\"")
                .append(org.owasp.encoder.Encode.forHtmlAttribute(url))
                .append("\"><input type=\"hidden\" name=\"csrf\" value=\"").append(token)
                .append("\"><input type=\"hidden\" name=\"action\" value=\"").append(edit ? "update" : "create").append("\">");
        if (edit) html.append("<input type=\"hidden\" name=\"id\" value=\"").append(article.id()).append("\">");
        html.append("<label>").append(I18n.get(lang, "news.form.title")).append("<input class=\"form-control\" name=\"title\" maxlength=\"200\" required value=\"")
                .append(org.owasp.encoder.Encode.forHtmlAttribute(edit ? article.title() : "")).append("\"></label>")
                .append("<label class=\"d-block mt-2\">").append(I18n.get(lang, "news.form.text")).append("</label>")
                .append("<div class=\"btn-toolbar mb-2 flex-wrap\" role=\"toolbar\">")
                .append("<div class=\"btn-group btn-group-sm mr-2 mb-1\">")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','b')\"><b>B</b></button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','i')\"><i>I</i></button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','u')\"><u>U</u></button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','s')\"><s>S</s></button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','sup')\">x²</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','sub')\">x₂</button>")
                .append("</div><div class=\"btn-group btn-group-sm mr-2 mb-1\">")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','left')\">L</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','center')\">C</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','right')\">R</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','justify')\">J</button>")
                .append("</div><div class=\"btn-group btn-group-sm mr-2 mb-1\">")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsQuote('").append(id).append("')\">Quote</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','spoiler')\">Spoiler</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','code')\">Code</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsTag('").append(id).append("','[list]\\n[*]','\\n[/list]')\">List</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsTag('").append(id).append("','[list=1]\\n[*]','\\n[/list]')\">1.</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsTag('").append(id).append("','[hr]','')\">HR</button>")
                .append("</div><div class=\"btn-group btn-group-sm mb-1\">")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','url')\">Link</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','img')\">IMG</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsBB('").append(id).append("','email')\">Mail</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsColor('").append(id).append("')\">Color</button>")
                .append("<button type=\"button\" class=\"btn btn-outline-secondary\" onclick=\"insertNewsSize('").append(id).append("')\">Size</button>")
                .append("</div></div>")
                .append("<textarea id=\"").append(id).append("\" class=\"form-control\" name=\"body\" maxlength=\"20000\" rows=\"10\" required>")
                .append(org.owasp.encoder.Encode.forHtmlContent(edit ? article.body() : ""))
                .append("</textarea><button class=\"btn btn-primary mt-2\" type=\"submit\">")
                .append(I18n.get(lang, edit ? "news.form.save" : "news.form.publish"))
                .append("</button>")
                .append("<script>if(!window.insertNewsBB){window.insertNewsBB=function(id,tag){var a=document.getElementById(id);if(!a)return;var s=a.selectionStart,e=a.selectionEnd,v=a.value,sel=v.substring(s,e),ins;"
                + "if(tag==='url'){var u=prompt('URL:','https://');if(u===null)return;u=u.trim()||'https://';ins='[url='+u+']'+(sel||u)+'[/url]';}"
                + "else if(tag==='img'){var u=prompt('Bild-URL:','https://');if(u===null)return;u=u.trim();if(!u)return;ins='[img]'+u+'[/img]';}"
                + "else if(tag==='email'){var u=prompt('E-Mail:','');if(u===null)return;u=u.trim();if(!u)return;ins='[email]'+u+'[/email]';}"
                + "else{ins='['+tag+']'+sel+'[/'+tag+']';}"
                + "a.value=v.substring(0,s)+ins+v.substring(e);a.focus();a.selectionStart=s;a.selectionEnd=s+ins.length;};"
                + "window.insertNewsTag=function(id,b,aft){var a=document.getElementById(id);if(!a)return;var s=a.selectionStart,e=a.selectionEnd,v=a.value,sel=v.substring(s,e),ins=b+sel+aft;a.value=v.substring(0,s)+ins+v.substring(e);a.focus();a.selectionStart=s+b.length;a.selectionEnd=s+b.length+sel.length;};"
                + "window.insertNewsQuote=function(id){var w=prompt('Zitat von (optional):','');if(w===null)return;w=(w||'').trim();window.insertNewsTag(id,w?'[quote='+w+']':'[quote]','[/quote]');};"
                + "window.insertNewsSize=function(id){var z=prompt('Größe (px):','16');if(z===null)return;z=(z||'').replace(/[^0-9]/g,'')||'16';window.insertNewsTag(id,'[size='+z+']','[/size]');};"
                + "window.insertNewsColor=function(id){var c=prompt('Farbe (hex ohne #):','ff0000');if(c===null)return;c=c.replace(/^#/,'').trim()||'ff0000';window.insertNewsTag(id,'[color='+c+']','[/color]');};}</script>")
                .append("</form>");
        return html.toString();
    }

}
