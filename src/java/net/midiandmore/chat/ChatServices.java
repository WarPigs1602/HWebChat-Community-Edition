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

    String stripHash(String value) {
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
                    .set("SERVER_SOFTWARE", Software.SERVER_SOFTWARE)
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
                var lang = readLang(request, map);
                request.setAttribute("lang", lang);
                request.setAttribute(JspTemplates.FMT_LOCALE,
                        java.util.Locale.forLanguageTag("en".equalsIgnoreCase(lang) ? "en" : "de"));
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
        var view = ViewFactory.create(type, name, readLang(request, map), new ViewFactory.Request(
                nick != null ? nick : "", sid != null ? sid : "", skin != null ? skin : "", room != null ? room : "",
                reason != null ? reason : "", host, owner, map.getOrDefault("target", ""), map.getOrDefault("user", ""),
                roomName, (scheme.equals("https") ? "wss" : "ws") + "://" + host, scheme + "://" + host));
        var session = request.getSession(false);
        view.defaults().put("theme", readTheme(request, map));
        var userAgent = request.getHeader("User-Agent");
        view.defaults().put("user_agent", userAgent != null ? userAgent : "");
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
     * Prepares the style sheet of a JSP for the browser: the paths of the
     * delivered files get the context path and the dark theme part is only
     * kept for the dark theme. The headers depend on the "theme" cookie.
     *
     * @param css the style sheet
     * @return the style sheet for the request
     */
    String processStyleSheet(String css, HttpServletRequest request, HttpServletResponse response) {
        var map = requestMap(request);
        css = css.replace("url(\"file/", "url(\"" + request.getContextPath() + "/file/")
                .replace("url('file/", "url('" + request.getContextPath() + "/file/")
                .replace("url(file/", "url(" + request.getContextPath() + "/file/");
        if (!"dark".equals(readTheme(request, map))) {
            css = stripDarkTheme(css);
        }
        response.setHeader("Vary", "Cookie");
        response.setHeader("Cache-Control", "no-cache");
        return css;
    }

    /**
     * Delivers a script or style sheet that is a file of the skin and not a
     * JSP.
     *
     * @param style <code>true</code> for a style sheet, <code>false</code> for a script
     * @param name the name of the file without extension
     */
    void nativeAsset(boolean style, String name, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (setupRequired(request, response)) {
            return;
        }
        var map = requestValues(request, response);
        var lang = readLang(request, map);
        var db = Bootstrap.boot.getConfig().getDb();
        var content = style ? db.getStyle(name, "native", lang) : db.getScript(name, "native", lang);
        response.setContentType(style ? "text/css" : "text/javascript");
        if (content.toLowerCase().startsWith("error")) {
            response.setStatus(404);
            content = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p><b>Error: The requested "
                    + (style ? "style" : "script") + " '" + name + "' does not exist!</b></p><hr><address>"
                    + Software.SERVER_SOFTWARE + " " + Software.SERVER_VERSION + "-" + Software.SERVER_STATUS
                    + "</address></body><html>";
        } else if (style) {
            content = processStyleSheet(content, request, response);
        }
        Bootstrap.boot.getUtil().submitContent(content, response);
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
                                request.getContextPath() + "/news.jsp"));
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

    void news(HttpServletRequest request, HttpServletResponse response, Map<String, String> map, boolean admin) {
        var conf = Bootstrap.boot.getConfig();
        var util = Bootstrap.boot.getUtil();
        var lang = readLang(request, map);
        var consoleUrl = request.getContextPath() + "/console.jsp?service=news";
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

}
