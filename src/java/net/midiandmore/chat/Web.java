package net.midiandmore.chat;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * Entry points and data-access helpers for directly requested JSPs. The chat
 * classes are package private, so JSPs reach them through this class.
 */
public final class Web {

    private static Bootstrap master;
    private static volatile String contextPath = "";

    private Web() {
    }

    /**
     * Starts the chat on first use. Safe to call on every request.
     *
     * @param context the servlet context of the web application
     * @throws ServletException if the chat cannot be initialized
     */
    public static synchronized void start(ServletContext context) throws ServletException {
        JspTemplates.init(context);
        contextPath = context.getContextPath();
        if (master != null) {
            return;
        }
        try {
            var bootstrap = new Bootstrap();
            bootstrap.init();
            master = bootstrap;
        } catch (Exception ex) {
            throw new ServletException("Could not initialize the chat", ex);
        }
    }

    /**
     * Handles a request to the start page of the chat.
     *
     * @param page the name of the directly requested page
     * @param request the request
     * @param response the response
     * @throws IOException if an I/O error occurs
     */
    public static void page(String page, HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCharset(request, response);
        master.getChatServices().parsePage(page, request, response);
    }

    private static void setCharset(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var config = master.getConfig();
        if (config != null) {
            request.setCharacterEncoding(config.getString("charset"));
            response.setCharacterEncoding(config.getString("charset"));
        }
    }

    /**
     * Prepares a JSP that is requested directly. It has to be called before
     * the JSP prints anything. See {@link ChatServices#begin}.
     *
     * @param kind <code>plain</code>, <code>page</code>, <code>document</code>,
     * <code>script</code> or <code>style</code>
     * @param request the request
     * @param response the response
     * @return <code>false</code> if the response is complete and the JSP has to
     * stop
     * @throws IOException if an I/O error occurs
     */
    public static boolean begin(String kind, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCharset(request, response);
        return master.getChatServices().begin(kind, request, response);
    }

    /**
     * Provides the values of the templates as request attributes again, for
     * example after the session was changed.
     */
    public static void view(String kind, HttpServletRequest request) {
        master.getChatServices().expose(kind, request);
    }

    /**
     * Provides the values of the templates and additional values as request
     * attributes. The additional values can be used by the texts of the chat
     * as well (<code>%count%</code>).
     */
    public static void view(String kind, HttpServletRequest request, Map<String, ?> values) {
        master.getChatServices().expose(kind, request, values);
    }

    /** Prepares the style sheet of a JSP for the browser. */
    public static String css(String css, HttpServletRequest request, HttpServletResponse response) {
        return master.getChatServices().processStyleSheet(css, request, response);
    }

    /** Delivers a script that is a file of the skin. */
    public static void contentScript(String name, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCharset(request, response);
        master.getChatServices().nativeAsset(false, name, request, response);
    }

    /** Delivers a style sheet that is a file of the skin. */
    public static void contentStyle(String name, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCharset(request, response);
        master.getChatServices().nativeAsset(true, name, request, response);
    }

    static String contextPath() {
        return contextPath;
    }

    /** Text of the resource bundles in a language ("de" or "en"). */
    public static String message(String lang, String key) {
        return I18n.get(lang, key);
    }

    /** Draws the captcha of the request; <code>false</code> if there is none. */
    public static boolean drawCaptcha(HttpServletRequest request, HttpServletResponse response) {
        var captcha = Bootstrap.boot.getCaptcha();
        if (captcha == null) {
            return false;
        }
        captcha.drawCaptcha(request, response);
        return true;
    }

    /** Information for chat portals as text or JSON. */
    public static String webChat(boolean json) {
        var manager = Bootstrap.boot.getChatManager();
        return json ? manager.getWebChatJson() : manager.getWebChat();
    }

    /** Decodes the HTML entities of an address. */
    public static String decodeHtml(String value) {
        return Bootstrap.boot.getUtil().decodeHtml(value);
    }

    /** Encodes a value for the output in HTML. */
    public static String parseHtml(String value) {
        return Bootstrap.boot.getUtil().parseHtml(value);
    }

    /** Formats a time stamp. */
    public static String formatTime(long time) {
        return Bootstrap.boot.getUtil().getTime(time);
    }

    /** Published news articles for the public news page. */
    public static java.util.List<Map<String, Object>> newsArticles() throws SQLException {
        var config = Bootstrap.boot.getConfig();
        var repository = new NewsRepository(config.getDb(), config.getP().getProperty("sql.prefix", ""));
        var articles = new ArrayList<Map<String, Object>>();
        for (var article : repository.list()) {
            articles.add(Map.of("title", article.title(), "author", article.author(),
                    "published", formatTime(article.published()), "body", article.body()));
        }
        return articles;
    }

    /** Formats a text of a user: links and BB code. */
    public static String markup(String value) {
        return Bootstrap.boot.getUtil().preReplace(value);
    }

    /** Checks whether a nick is in the chat. */
    public static boolean isOnline(String nick) {
        return Bootstrap.boot.getChatManager().isOnline(nick);
    }

    /** Removes a user from the community. */
    public static void removeCommunityUser(String nick) {
        Bootstrap.boot.getChatManager().getUsersCommunity().remove(nick);
    }

    /** The rows of the top list. */
    public static String toplist(HttpServletRequest request, HttpServletResponse response) {
        return Bootstrap.boot.getConfig().getDb().getToplist(request, response,
                Bootstrap.boot.getChatServices().requestMap(request));
    }

    /** Name of the room of a user, <code>null</code> if there is none. */
    public static String roomNameByOwner(String owner) {
        return Bootstrap.boot.getConfig().getDb().getRoomNameByOwner(owner.toLowerCase());
    }

    /** A string-valued chat setting used by JSP page logic. */
    public static String configValue(String key) {
        return Bootstrap.boot.getConfig().getString(key);
    }

    /** An integer chat setting used by JSP page logic. */
    public static int configInt(String key) {
        return Bootstrap.boot.getConfig().getInt(key);
    }

    /** Checks whether a nickname is reserved for a chat command. */
    public static boolean isCommandNick(String nick) {
        return Bootstrap.boot.getChatServices().nickIsCommand(nick);
    }

    /** Calculates a user's age from the supplied birth date. */
    public static int age(String day, String month, String year) {
        return Bootstrap.boot.getUtil().getAge(day, month, year);
    }

    /** Checks whether an address is banned. */
    public static boolean isBanned(String address) {
        return Bootstrap.boot.getConfig().getDb().isBanned(address);
    }

    public record RegistrationAddresses(String raw, String resolved) {
    }

    /** Resolves the local address according to the registration settings. */
    public static RegistrationAddresses registrationAddresses(HttpServletRequest request) {
        var config = Bootstrap.boot.getConfig();
        var raw = request.getLocalAddr();
        if ("1".equals(config.getString("use_proxy"))) {
            var proxyAddress = request.getHeader(config.getString("real_ip"));
            if (proxyAddress != null && !proxyAddress.isEmpty()) {
                raw = proxyAddress;
            }
        }
        var resolved = raw;
        if ("1".equals(config.getString("resolve_ip"))) {
            try {
                resolved = java.net.InetAddress.getByName(raw).getCanonicalHostName();
            } catch (UnknownHostException ex) {
                resolved = raw;
            }
        }
        return new RegistrationAddresses(raw, resolved);
    }

    /** Normalizes a skin name for the current host. */
    public static String skin(String skin, HttpServletRequest request) {
        return Bootstrap.boot.getUtil().parseHost(skin, request)[1];
    }

    /** Creates a random color for a new registered user. */
    public static String randomColor() {
        return Bootstrap.boot.getUtil().createRandomColor();
    }

    /** Registers a new user with the supplied form values. */
    public static void addRegisteredUser(String gender, String nick, String mail, String color, String password,
            String question, String answer, String day, String month, String year) {
        Bootstrap.boot.getConfig().getDb().addNick(gender, nick, mail, color, password, question, answer,
                day, month, year);
    }

    /** Sends the registration mail; failures are logged and do not cancel registration. */
    public static void sendRegistrationMail(HttpServletRequest request, Map<String, String> values,
            String nick, String password, String mail, String lang) {
        var mailer = Bootstrap.boot.getSendMail();
        if (mailer == null) {
            return;
        }
        var db = Bootstrap.boot.getConfig().getDb();
        var message = Bootstrap.boot.getChatServices().getMail("register", request, values)
                .set("pwd", password)
                .set("nick", nick);
        try {
            mailer.sendEmail(message, db.getCommand("register_subject", lang), mail);
        } catch (MessagingException ex) {
            Bootstrap.logError(ex);
        }
    }

    /** Reads a room setting by room name and field. */
    public static String roomData(String room, String field) {
        return Bootstrap.boot.getConfig().getDb().getRoomData(room, field);
    }

    public record LoginAddresses(String address, String resolvedAddress, String forwardedAddress,
            String resolvedForwardedAddress) {
    }

    /** Resolves the addresses used by the legacy login ban checks. */
    public static LoginAddresses loginAddresses(HttpServletRequest request) {
        var config = Bootstrap.boot.getConfig();
        var address = request.getLocalAddr();
        var forwarded = request.getHeader("x-forwarded-for");
        forwarded = forwarded == null ? "" : forwarded;
        if (forwarded.contains(", ")) {
            forwarded = forwarded.substring(0, forwarded.indexOf(", "));
        }
        if ("1".equals(config.getString("cloudflare"))) {
            address = forwarded;
            forwarded = "";
        }
        if ("1".equals(config.getString("resolve_ip"))) {
            try {
                forwarded = InetAddress.getByName(forwarded).getCanonicalHostName();
            } catch (UnknownHostException ignored) {
            }
        }
        if ("1".equals(config.getString("use_proxy"))) {
            var proxyAddress = request.getHeader(config.getString("real_ip"));
            if (proxyAddress != null && !proxyAddress.isEmpty()) {
                address = proxyAddress;
            }
        }
        var resolvedAddress = resolveAddress(address, config.getString("resolve_ip"));
        var resolvedForwardedAddress = resolveAddress(forwarded, config.getString("resolve_ip"));
        return new LoginAddresses(address, resolvedAddress, forwarded, resolvedForwardedAddress);
    }

    private static String resolveAddress(String address, String resolveSetting) {
        if ("1".equals(resolveSetting)) {
            try {
                return InetAddress.getByName(address).getCanonicalHostName();
            } catch (UnknownHostException ex) {
                return address;
            }
        }
        return address;
    }

    public static boolean checkPassword(String nick, String password) {
        return Bootstrap.boot.getConfig().getDb().checkPassword(nick, password);
    }

    public static boolean checkTemporaryPassword(String nick, String password) {
        return Bootstrap.boot.getConfig().getDb().checkPassword2(nick, password);
    }

    public static void clearTemporaryPassword(String nick) {
        Bootstrap.boot.getConfig().getDb().updateNick(nick, "pwd2", "");
    }

    public static void updateLoginPassword(String nick, String password) {
        Bootstrap.boot.getConfig().getDb().updatePassword(nick, password);
    }

    public static boolean isTimedBanned(String value) {
        return Bootstrap.boot.getConfig().getDb().isTimedBanned(value);
    }

    public static String banReason(String value) {
        return Bootstrap.boot.getConfig().getDb().getBanReason(value);
    }

    public static String timedBanReason(String value) {
        return Bootstrap.boot.getConfig().getDb().getTimedBanReason(value);
    }

    public static boolean hasCommunityUser(String nick) {
        return Bootstrap.boot.getChatManager().getUsersCommunity().get(nick) != null;
    }

    public static boolean communityNickExists(String nick) {
        return Bootstrap.boot.getChatManager().getUsersCommunity().containsKey(nick);
    }

    public static void addCommunityUser(String nick, jakarta.servlet.http.HttpSession session) {
        Bootstrap.boot.getChatManager().getUsersCommunity().put(nick, new UsersCommunity(Bootstrap.boot, session));
    }

    public static void touchCommunityUser(String nick) {
        Bootstrap.boot.getChatManager().getUsersCommunity().get(nick).setTimeoutTimer(0);
    }

    public static void startCommunityPage(String template, HttpServletRequest request, HttpServletResponse response,
            Map<String, String> values) throws IOException {
        Bootstrap.boot.getChatServices().startPage(template, request, response, values);
    }

    /** Validates a date used by a registration form. */
    public static boolean isValidDate(String day, String month, String year) {
        return Bootstrap.boot.getUtil().isValidDate(day, month, year);
    }

    /** Validates a submitted captcha. */
    public static boolean isCorrectCaptcha(String captcha, String id) {
        return Bootstrap.boot.getChatManager().isCorrectCaptcha(captcha, id);
    }

    /** Creates a new captcha identifier. */
    public static String newCaptchaId() {
        return Bootstrap.boot.getChatServices().generateSid();
    }

    /** Creates a random password for a password-reset request. */
    public static String randomPassword() {
        return Bootstrap.boot.getUtil().createRandomPassword();
    }

    /** Sends the password-reset mail using the configured mail template. */
    public static void sendPasswordMail(HttpServletRequest request, Map<String, String> values,
            String nick, String password, String mail, String lang) throws MessagingException {
        var mailer = Bootstrap.boot.getSendMail();
        if (mailer == null) {
            return;
        }
        var db = Bootstrap.boot.getConfig().getDb();
        var message = Bootstrap.boot.getChatServices().getMail("password", request, values)
                .set("pwd", password)
                .set("nick", nick);
        mailer.sendEmail(message, db.getCommand("password_subject", lang), mail);
    }

    /** Stores the temporary password after a successful reset request. */
    public static void updatePassword(String nick, String password) {
        Bootstrap.boot.getConfig().getDb().updatePassword2(nick, password);
    }

    /** Reads the request parameters into a first-value map. */
    public static Map<String, String> requestValues(HttpServletRequest request) {
        return Bootstrap.boot.getChatServices().requestMap(request);
    }

    /** Checks whether the id belongs to a connected user. */
    public static boolean isValidConnectionId(String sid) {
        return sid != null && Bootstrap.boot.getChatManager().isValidConnectionId(sid);
    }

    /** Nick of the connection with the id. */
    public static String nickOfConnection(String sid) {
        return Bootstrap.boot.getChatManager().getNameFromId(sid);
    }

    /**
     * Status of the user of a connection, at least the status of a guest.
     */
    public static int userStatus(String sid) {
        var manager = Bootstrap.boot.getChatManager();
        if (sid == null || !manager.isValidConnectionId(sid)) {
            return 3;
        }
        return Math.max(3, manager.getUser(manager.getNameFromId(sid)).getStatus());
    }

    /** The texts of the help for a status. */
    public static Map<String, String> helpTexts(int status, String lang) {
        return Bootstrap.boot.getUtil().getHelpTexts(status, lang);
    }

    /** The smilies and their codes. */
    public static Map<String, String> smilies() {
        return Bootstrap.boot.getConfig().getDb().getSmilies();
    }

    /** Number of smilies in a row of the list. */
    public static int smiliesPerRow() {
        return Bootstrap.boot.getConfig().getInt("emots_per_row");
    }

    /** Number of messages that are left for a user. */
    public static int countMessages(String nick) {
        return Bootstrap.boot.getConfig().getDb().countMessage(nick);
    }

    /** A value of a registered user. */
    public static String userData(String nick, String field) {
        return Bootstrap.boot.getConfig().getDb().getData(nick, field);
    }

    /** The messages that are left for a user. */
    public static String messages(String nick, HttpServletRequest request) {
        return Bootstrap.boot.getConfig().getDb().getMessages(nick, request,
                Bootstrap.boot.getChatServices().requestMap(request));
    }

    /** Deletes the messages that are left for a user. */
    public static void deleteMessages(String nick) {
        Bootstrap.boot.getConfig().getDb().delMessages(nick);
    }

    /** The data of the statistics page. */
    public record Stats(java.util.List<Map<String, Object>> hourly, java.util.List<Map<String, Object>> daily,
            java.util.List<Map<String, Object>> monthly, java.util.List<Map<String, Object>> peaks,
            java.util.List<Map<String, Object>> topRooms, java.util.List<Map<String, Object>> relationships,
            int messages, int registered, int rooms) {
    }

    /** Reads the data of the statistics page. */
    public static Stats stats() {
        var db = Bootstrap.boot.getConfig().getDb();
        return new Stats(db.getStatsHourly(), db.getStatsDaily(), db.getStatsMonthly(), db.getPeaks(),
                db.getTopRooms(), db.getUserRelationships(), Math.max(0, db.countPublicMessages()),
                Math.max(0, db.countChatter()), Bootstrap.boot.getChatManager().getRooms().size());
    }

    /**
     * Converts maps, lists and values to JSON that can be written into a
     * <code>script</code> element.
     */
    public static String json(Object value) {
        var out = new StringBuilder();
        json(value, out);
        return out.toString();
    }

    private static void json(Object value, StringBuilder out) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof Map<?, ?> map) {
            out.append('{');
            var first = true;
            for (var entry : map.entrySet()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                jsonString(String.valueOf(entry.getKey()), out);
                out.append(':');
                json(entry.getValue(), out);
            }
            out.append('}');
        } else if (value instanceof Iterable<?> list) {
            out.append('[');
            var first = true;
            for (var item : list) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                json(item, out);
            }
            out.append(']');
        } else if (value instanceof Boolean || value instanceof Number) {
            out.append(value);
        } else {
            jsonString(String.valueOf(value), out);
        }
    }

    private static void jsonString(String value, StringBuilder out) {
        out.append('"');
        for (var i = 0; i < value.length(); i++) {
            var c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '<' -> out.append("\\u003c");
                case '>' -> out.append("\\u003e");
                case '&' -> out.append("\\u0026");
                case '\u2028' -> out.append("\\u2028");
                case '\u2029' -> out.append("\\u2029");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }

    /**
     * Checks whether the chat is configured and able to handle requests.
     *
     * @return <code>true</code> if the chat is ready
     */
    public static boolean isReady() {
        var boot = Bootstrap.boot;
        return boot != null && boot.getConfig() != null && !boot.isSetupNeeded();
    }

    /**
     * Returns the configured charset.
     *
     * @return the charset
     */
    public static String charset() {
        return Bootstrap.boot.getConfig().getString("charset");
    }

    /**
     * Checks whether a nick is registered.
     *
     * @param nick the nick
     * @return <code>true</code> if the nick is registered
     */
    public static boolean isRegistered(String nick) {
        return Bootstrap.boot.getConfig().getDb().isRegistered(nick);
    }

    /** Writes the registered user's picture, or the configured default picture. */
    public static void profilePicture(String nick, HttpServletResponse response) throws IOException {
        var config = Bootstrap.boot.getConfig();
        var db = config.getDb();
        byte[] picture = null;
        var contentType = "image/png";
        if (nick != null && !nick.isEmpty() && db.isRegistered(nick.toLowerCase())) {
            picture = db.getPicture(nick);
            if (picture != null && picture.length > 0) {
                var storedContentType = db.getData(nick, "image_url");
                if (storedContentType != null && !storedContentType.isEmpty()) {
                    contentType = storedContentType;
                }
            }
        }
        if (picture == null || picture.length == 0) {
            var path = Paths.get(config.getUh(), ".homewebcom", "pictures", "notfound.svg");
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
        try (var out = response.getOutputStream()) {
            out.write(picture);
            out.flush();
        }
    }

    /**
     * Stores the picture of a registered user.
     *
     * @param nick the nick
     * @param picture the picture
     * @param contentType the content type of the picture
     */
    public static void storePicture(String nick, InputStream picture, String contentType) {
        Bootstrap.boot.getConfig().getDb().updatePicture(nick, picture, contentType);
    }

    /**
     * Prints a template.
     *
     * @param template the name of the template
     * @param request the request
     * @param response the response
     * @param map the values for the template
     */
    public static void print(String template, HttpServletRequest request, HttpServletResponse response,
            Map<String, String> map) {
        Bootstrap.boot.getChatServices().printTemplate(template, request, response, map);
    }

    /**
     * Prints an error template with a message.
     *
     * @param template the name of the template
     * @param request the request
     * @param response the response
     * @param map the values for the template
     * @param message the message
     * @throws IOException if an I/O error occurs
     */
    public static void printError(String template, HttpServletRequest request, HttpServletResponse response,
            Map<String, String> map, String message) throws IOException {
        var view = Bootstrap.boot.getChatServices().getTemplate(template, request, map);
        view.set("message", message);
        response.getWriter().println(view);
    }

    public static void account(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
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
        sid = session != null ? (String) session.getAttribute("sid") : null;
        skin = session != null ? (String) session.getAttribute("skin") : null;
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
                text = Bootstrap.boot.getChatServices().getTemplate("invalid_session_id2", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("invalid_session_id2_napping", request, map);
            }
        } else if (!reg) {
            if (!roomName.isBlank()) {
                text = Bootstrap.boot.getChatServices().getTemplate("nick_not_registered", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("nick_not_registered_napping", request, map);

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
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_profile", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_profile_napping", request, map);
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
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_profile_success", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_profile_success_napping", request, map);
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
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_napping", request, map);
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
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_wrong", request, map);
                    } else if (newPwd.length() < conf.getInt("min_pwd_length")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_short", request, map);
                    } else if (newPwd.length() > conf.getInt("max_pwd_length")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_long", request, map);
                    } else if (!newPwd.equals(newPwd2)) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_same", request, map);
                    }
                    if (errorMessage == null) {
                        if (roomName.isBlank()) {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_password_success", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_password_success_napping", request, map);
                        }

                        db.updatePassword(nick, newPwd);
                    } else {
                        if (roomName.isBlank()) {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_password", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_password_napping", request, map);
                        }
                        text = text.set("account_error_if_one", Bootstrap.boot.getChatServices().getTemplate("account_failed", request, map));
                        text = text.set("error_message", errorMessage);
                    }
                }
                case "" -> {
                    if (roomName.isBlank()) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_password", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_password_napping", request, map);
                    }
                }
                default -> {
                    if (roomName.isBlank()) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_napping", request, map);
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
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_same", request, map);
                    } else if (!db.checkPassword(nick, pwd)) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_wrong", request, map);
                    }
                    if (errorMessage == null) {
                        if (roomName.isBlank()) {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_delete_success", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_delete_success_napping", request, map);
                        }
                        cm.quit(nick);
                        cm.removeFriend(nick);
                        db.delFriendsFromList(nick);
                        db.delNick(nick);
                    } else {
                        if (roomName.isBlank()) {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_delete", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_delete_napping", request, map);
                        }

                        text = text.set("account_error_if_one", Bootstrap.boot.getChatServices().getTemplate("account_failed", request, map));
                        text = text.set("error_message", errorMessage);
                    }
                }
                case "" -> {
                    if (roomName.isBlank()) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_delete", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_delete_napping", request, map);
                    }
                }
                default -> {
                    if (roomName.isBlank()) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_napping", request, map);
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
                    bg_color_1 = Bootstrap.boot.getChatServices().stripHash(map.getOrDefault("bg_color_1", ""));
                    bg_color_2 = Bootstrap.boot.getChatServices().stripHash(map.getOrDefault("bg_color_2", ""));
                    color = Bootstrap.boot.getChatServices().stripHash(map.getOrDefault("color", ""));
                    link_color = Bootstrap.boot.getChatServices().stripHash(map.getOrDefault("link_color", ""));
                    border_color = Bootstrap.boot.getChatServices().stripHash(map.getOrDefault("border_color", ""));
                    room = map.getOrDefault("room2", "");
                    if (bg_color_1.length() != 6 || !bg_color_1.matches("[a-fA-F0-9]*")) {
                        if (roomName.isBlank()) {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color_napping", request, map);
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
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color_napping", request, map);
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
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color_napping", request, map);
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
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color_napping", request, map);
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
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color_napping", request, map);
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
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_used", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_used_napping", request, map);
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
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_change", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_change_napping", request, map);
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
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_add", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_add_napping", request, map);
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
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_error", request, map);
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
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_napping", request, map);
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
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_napping", request, map);
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
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_gender_invalid", request, map);
                    } else if (!ut.isValidDate(day, month, year)) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_date", request, map);
                    } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_age_old", request, map);
                    } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_age_young", request, map);
                    } else if (!mail.contains("@")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_mail_invalid", request, map);
                    } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_mail_invalid", request, map);
                    }
                    if (errorMessage == null) {
                        if (roomName.isBlank()) {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_default_success", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_default_success_napping", request, map);
                        }
                        db.updateNick(nick, "sex", gender);
                        db.updateNick(nick, "bday_day", day);
                        db.updateNick(nick, "bday_month", month);
                        db.updateNick(nick, "bday_year", year);
                        db.updateNick(nick, "mail", mail);
                    } else {
                        if (roomName.isBlank()) {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_default", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("account_form_default_napping", request, map);
                        }
                        text = ut.parseGender(text, gender);
                        text = text.set("account_error_if_one", Bootstrap.boot.getChatServices().getTemplate("account_failed", request, map));
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
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_default", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_default_napping", request, map);
                    }
                    text = ut.parseGender(text, gender);
                    text = text.set("mail", mail);
                    text = text.set("day", day);
                    text = text.set("month", month);
                    text = text.set("year", year);
                }
                default -> {
                    if (roomName.isBlank()) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_napping", request, map);
                    }
                }
            }
        } else if (service.equals("picture")) {
            if (roomName.isBlank()) {
                text = Bootstrap.boot.getChatServices().getTemplate("picture", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("picture_napping", request, map);
            }
        } else if (service.equals("picture_del")) {
            switch (data) {
                case "delete":
                    db.updateNick(nick, "image_upload", null);
                    db.updateNick(nick, "image_url", null);
                    if (roomName.isBlank()) {
                        text = Bootstrap.boot.getChatServices().getTemplate("picture_del_success", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("picture_del_success_napping", request, map);
                    }
                    break;
                case "":
                    if (roomName.isBlank()) {
                        text = Bootstrap.boot.getChatServices().getTemplate("picture_del", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("picture_del_napping", request, map);
                    }
                    break;
                default:
                    if (roomName.isBlank()) {
                        text = Bootstrap.boot.getChatServices().getTemplate("picture_del_unkown", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("picture_del_unkown_napping", request, map);
                    }
                    break;
            }

        } else if (!service.equals("")) {
            if (roomName.isBlank()) {
                text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_napping", request, map);
            }
        } else {
            if (roomName.isBlank()) {
                text = Bootstrap.boot.getChatServices().getTemplate("account_form", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping2", request, map);
            }
        }
        text = text.set("account_error_if_one", "");
        ut.submitContent(text, response);
    }

    public static void accountCommunity(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var session = request.getSession(false);
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        if (session == null) {
            Bootstrap.boot.getChatServices().printTemplate("timeout", request, response, map);
            return;
        }
        cm.getUsersCommunity().get((String) session.getAttribute("nick")).setTimeoutTimer(0);
        View text = null;
        var service = map.getOrDefault("service", "");
        var data = map.getOrDefault("data", "");
        var db = conf.getDb();
        var skin = (String) session.getAttribute("skin");

        skin = skin != null ? skin : map.getOrDefault("skin", "");
        skin = ut.parseHost(skin, request)[1];
        var reg = false;
        var nick = (String) session.getAttribute("nick");
        reg = db.isRegistered(nick);
        if (!reg) {
            text = Bootstrap.boot.getChatServices().getTemplate("nick_not_registered_com", request, map);
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
                    text = Bootstrap.boot.getChatServices().getTemplate("account_form_profile_com", request, map);
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
                    text = Bootstrap.boot.getChatServices().getTemplate("account_form_profile_success_com", request, map);
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
                    text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_com", request, map);
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
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_wrong", request, map);
                    } else if (newPwd.length() < conf.getInt("min_pwd_length")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_short", request, map);
                    } else if (newPwd.length() > conf.getInt("max_pwd_length")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_long", request, map);
                    } else if (!newPwd.equals(newPwd2)) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_same", request, map);
                    }
                    if (errorMessage == null) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_password_success_com", request, map);
                        db.updatePassword(nick, newPwd);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_password_com", request, map);
                        text = text.set("account_error_if_one", Bootstrap.boot.getChatServices().getTemplate("account_failed", request, map));
                        text = text.set("error_message", errorMessage);
                    }
                }
                case "" -> {
                    text = Bootstrap.boot.getChatServices().getTemplate("account_form_password_com", request, map);
                }
                default -> {
                    text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_com", request, map);
                }
            }
        } else if (service.equals("delete")) {
            switch (data) {
                case "delete" -> {
                    var pwd = map.getOrDefault("pwd", "");
                    var pwd2 = map.getOrDefault("pwd2", "");
                    View errorMessage = null;
                    if (!pwd.equals(pwd2)) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_same", request, map);
                    } else if (!db.checkPassword(nick, pwd)) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_wrong", request, map);
                    }
                    if (errorMessage == null) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_delete_success_com", request, map);
                        cm.quit(nick);
                        cm.removeFriend(nick);
                        db.delFriendsFromList(nick);
                        db.delNick(nick);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_delete_com", request, map);
                        text = text.set("account_error_if_one", Bootstrap.boot.getChatServices().getTemplate("account_failed", request, map));
                        text = text.set("error_message", errorMessage);
                    }
                }
                case "" -> {
                    text = Bootstrap.boot.getChatServices().getTemplate("account_form_delete_com", request, map);
                }
                default -> {
                    Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_com", request, map);
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
                    bg_color_1 = Bootstrap.boot.getChatServices().stripHash(map.getOrDefault("bg_color_1", ""));
                    bg_color_2 = Bootstrap.boot.getChatServices().stripHash(map.getOrDefault("bg_color_2", ""));
                    color = Bootstrap.boot.getChatServices().stripHash(map.getOrDefault("color", ""));
                    link_color = Bootstrap.boot.getChatServices().stripHash(map.getOrDefault("link_color", ""));
                    border_color = Bootstrap.boot.getChatServices().stripHash(map.getOrDefault("border_color", ""));
                    room = map.getOrDefault("room2", "");
                    if (bg_color_1.length() != 6 || !bg_color_1.matches("[a-fA-F0-9]*")) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (bg_color_2.length() != 6 || !bg_color_2.matches("[a-fA-F0-9]*")) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (border_color.length() != 6 || !border_color.matches("[a-fA-F0-9]*")) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (color.length() != 6 || !color.matches("[a-fA-F0-9]*")) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (link_color.length() != 6 || !link_color.matches("[a-fA-F0-9]*")) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_color_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else if (db.ownerExists(nick) && !db.getRoomNameByOwner(nick).equals(room)) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_used_com", request, map);
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
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_change_com", request, map);
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
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_add_com", request, map);
                        text = text.set("title", title);
                        text = text.set("bg_color_1", bg_color_1);
                        text = text.set("bg_color_2", bg_color_2);
                        text = text.set("color", color);
                        text = text.set("link_color", link_color);
                        text = text.set("border_color", border_color);
                        text = text.set("room2", room);
                        text = text.set("nick", nick);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_error_com", request, map);
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
                    text = Bootstrap.boot.getChatServices().getTemplate("account_form_napping_com", request, map);
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
                    text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_com", request, map);
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
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_gender_invalid", request, map);
                    } else if (!ut.isValidDate(day, month, year)) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_date", request, map);
                    } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_age_old", request, map);
                    } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_age_young", request, map);
                    } else if (!mail.contains("@")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_mail_invalid", request, map);
                    } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                        errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_mail_invalid", request, map);
                    }
                    if (errorMessage == null) {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_default_success_com", request, map);
                        db.updateNick(nick, "sex", gender);
                        db.updateNick(nick, "bday_day", day);
                        db.updateNick(nick, "bday_month", month);
                        db.updateNick(nick, "bday_year", year);
                        db.updateNick(nick, "mail", mail);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("account_form_default_com", request, map);
                        text = ut.parseGender(text, gender);
                        text = text.set("account_error_if_one", Bootstrap.boot.getChatServices().getTemplate("account_failed", request, map));
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
                    text = Bootstrap.boot.getChatServices().getTemplate("account_form_default_com", request, map);
                    text = ut.parseGender(text, gender);
                    text = text.set("mail", mail);
                    text = text.set("day", day);
                    text = text.set("month", month);
                    text = text.set("year", year);
                }
                default -> {
                    text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_com", request, map);
                }
            }
        } else if (service.equals("picture")) {
            text = Bootstrap.boot.getChatServices().getTemplate("picture_com", request, map);
        } else if (service.equals("picture_del")) {
            switch (data) {
                case "delete":
                    db.updateNick(nick, "image_upload", null);
                    db.updateNick(nick, "image_url", null);
                    text = Bootstrap.boot.getChatServices().getTemplate("picture_del_success_com", request, map);
                    break;
                case "":
                    text = Bootstrap.boot.getChatServices().getTemplate("picture_del_com", request, map);
                    break;
                default:
                    text = Bootstrap.boot.getChatServices().getTemplate("picture_del_unkown_com", request, map);
                    break;
            }

        } else if (!service.equals("")) {
            text = Bootstrap.boot.getChatServices().getTemplate("account_form_unknown_com", request, map);
        } else {
            text = Bootstrap.boot.getChatServices().getTemplate("account_form_com", request, map);
        }
        text = text.set("account_error_if_one", "");
        ut.submitContent(text, response);
    }

    public static void console(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter();
        var ut = Bootstrap.boot.getUtil();
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        try {
            filter.init(Bootstrap.boot);
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
        if (!filter.error) {
            var service = map.getOrDefault("service", "");
            var change = map.getOrDefault("change", "");
            View text = null;
            if (service.isBlank()) {
                text = Bootstrap.boot.getChatServices().getConsole("index", request, map);
                ut.submitContent(text, response);
            } else if (service.equals("news")) {
                Bootstrap.boot.getChatServices().news(request, response, map, true);
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
                    text = Bootstrap.boot.getChatServices().getConsole("rooms", request, map);
                } else if (change.equals("search")) {
                    if (!exists) {
                        text = Bootstrap.boot.getChatServices().getConsole("rooms_fail", request, map);
                        text = text.set("room", room);
                    } else {
                        text = Bootstrap.boot.getChatServices().getConsole("rooms_config", request, map);
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
                        text = Bootstrap.boot.getChatServices().getConsole("rooms_fail", request, map);
                        text = text.set("room", room);
                    } else {
                        text = Bootstrap.boot.getChatServices().getConsole("rooms_modify", request, map);
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
                        text = Bootstrap.boot.getChatServices().getConsole("rooms_exist", request, map);
                        text = text.set("room", room);
                    } else {
                        text = Bootstrap.boot.getChatServices().getConsole("rooms_add", request, map);
                        text = text.set("room", room);
                        text = text.set("topic", topic);
                        text = text.set("lock", "0");
                        text = text.set("lock_reason", lockReason);
                        text = text.set("standard", "0");
                        text = text.set("allow_smilies", "1");
                    }
                } else if (change.equals("set_room")) {
                    if (exists) {
                        text = Bootstrap.boot.getChatServices().getConsole("rooms_exist", request, map);
                        text = text.set("room", room);
                    } else {
                        text = Bootstrap.boot.getChatServices().getConsole("rooms_set", request, map);
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
                        text = Bootstrap.boot.getChatServices().getConsole("rooms_fail", request, map);
                        text = text.set("room", room);
                    } else {
                        text = Bootstrap.boot.getChatServices().getConsole("rooms_delete", request, map);
                        text = text.set("room", room);
                        db.delRoom(room);
                    }
                } else {
                    text = Bootstrap.boot.getChatServices().getConsole("rooms_404", request, map);
                    text = text.set("room", room);
                }
                ut.submitContent(text, response);
            } else if (service.equals("chat")) {
                text = Bootstrap.boot.getChatServices().getConsole("chat", request, map);
                TreeMap<String, String[]> p = conf.loadDataFromJSON("config.json", "name", "value", "description");
                TreeMap<String, String[]> p1 = new TreeMap<>();
                StringBuilder sb = new StringBuilder();
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
                    text = Bootstrap.boot.getChatServices().getConsole("chat", request, map);
                    text = text.set("success", Bootstrap.boot.getChatServices().getConsole("success", request, map));

                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("config.json", "name", "value", "description");
                for (var key : p.keySet()) {
                    if (key.startsWith("path_")) {
                        continue;
                    }
                    var arr = p.get(key);
                    var value = arr[0];
                    var description = arr[1];
                    var table = Bootstrap.boot.getChatServices().getConsole("chat_table", request, map);
                    table = table.set("description", description);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("fun")) {
                var update = map.getOrDefault("update", "");

                text = Bootstrap.boot.getChatServices().getConsole("fun", request, map);
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
                    text = text.set("success", Bootstrap.boot.getChatServices().getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("fun.json", "command", "content", "description");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr[0];
                    var description = arr[1];
                    var table = Bootstrap.boot.getChatServices().getConsole("fun_table", request, map);
                    table = table.set("description", description);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("cmdrights")) {
                var update = map.getOrDefault("update", "");

                text = Bootstrap.boot.getChatServices().getConsole("cmdcfg", request, map);
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
                    text = text.set("success", Bootstrap.boot.getChatServices().getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("cmdcfg.json", "name", "status");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = Bootstrap.boot.getChatServices().getConsole("cmdcfg_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("cmd")) {
                var update = map.getOrDefault("update", "");

                text = Bootstrap.boot.getChatServices().getConsole("commands", request, map);
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
                    text = text.set("success", Bootstrap.boot.getChatServices().getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("commands.json", "name", "command");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = Bootstrap.boot.getChatServices().getConsole("commands_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("host")) {
                var update = map.getOrDefault("update", "");

                text = Bootstrap.boot.getChatServices().getConsole("host", request, map);
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
                    text = text.set("success", Bootstrap.boot.getChatServices().getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("hosts.json", "host", "skin");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = Bootstrap.boot.getChatServices().getConsole("host_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("emot")) {
                var update = map.getOrDefault("update", "");

                text = Bootstrap.boot.getChatServices().getConsole("emot", request, map);
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
                    text = text.set("success", Bootstrap.boot.getChatServices().getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("emot.json", "name", "emot");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = Bootstrap.boot.getChatServices().getConsole("emot_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("mime")) {
                var update = map.getOrDefault("update", "");

                text = Bootstrap.boot.getChatServices().getConsole("mime", request, map);
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
                    text = text.set("success", Bootstrap.boot.getChatServices().getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("mime-types.json", "suffix", "type");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = Bootstrap.boot.getChatServices().getConsole("mime_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("mail")) {
                var update = map.getOrDefault("update", "");

                text = Bootstrap.boot.getChatServices().getConsole("mail", request, map);
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
                    text = text.set("success", Bootstrap.boot.getChatServices().getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("mail.json", "name", "value");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = Bootstrap.boot.getChatServices().getConsole("mail_table", request, map);
                    table = table.set("name", ut.parseHtml(key));
                    table = table.set("value", ut.parseHtml(value));
                    sb.append(table);
                }
                text = text.set("config", sb.toString());
                ut.submitContent(text, response);
            } else if (service.equals("path")) {
                response.setStatus(404);
                ut.submitContent(Bootstrap.boot.getChatServices().getConsole("404", request, map), response);
            } else if (service.equals("help")) {
                var update = map.getOrDefault("update", "");

                text = Bootstrap.boot.getChatServices().getConsole("help", request, map);
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
                    text = text.set("success", Bootstrap.boot.getChatServices().getConsole("success", request, map));
                    conf.hash();
                } else {
                    text = text.set("success", "");
                }
                p = conf.loadDataFromJSON("help.json", "name", "html");
                for (var key : p.keySet()) {
                    var arr = p.get(key);
                    var value = arr;
                    var table = Bootstrap.boot.getChatServices().getConsole("help_table", request, map);
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
                            text = Bootstrap.boot.getChatServices().getConsole("users_page", request, map);
                            text = text.set("nick", nick);

                        } else if (change.equals("password")) {
                            var newPwd = map.getOrDefault("new_pwd", "");
                            var newPwd2 = map.getOrDefault("new_pwd2", "");
                            View errorMessage = null;
                            if (!newPwd.isBlank()) {
                                if (newPwd.length() < conf.getInt("min_pwd_length")) {
                                    errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_short", request, map);
                                } else if (newPwd.length() > conf.getInt("max_pwd_length")) {
                                    errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_long", request, map);
                                } else if (!newPwd.equals(newPwd2)) {
                                    errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_pass_same", request, map);
                                }
                                if (errorMessage == null) {
                                    text = Bootstrap.boot.getChatServices().getConsole("users_pwd_success", request, map);
                                    text = text.set("nick", nick);
                                    db.updatePassword(nick, newPwd);
                                } else {
                                    text = Bootstrap.boot.getChatServices().getConsole("users_pwd", request, map);
                                    text = text.set("account_error_if_one", Bootstrap.boot.getChatServices().getTemplate("account_failed", request, map));
                                    text = text.set("error_message", errorMessage);
                                    text = text.set("nick", nick);
                                }
                            } else {
                                text = Bootstrap.boot.getChatServices().getConsole("users_pwd", request, map);
                                text = text.set("account_error_if_one", "");
                                text = text.set("error_message", "");
                                text = text.set("nick", nick);
                            }
                        } else if (change.equals("delete")) {
                            text = Bootstrap.boot.getChatServices().getConsole("users_delete", request, map);
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
                                errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_gender_invalid", request, map);
                            } else if (!ut.isValidDate(day, month, year)) {
                                errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_date", request, map);
                            } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                                errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_age_old", request, map);
                            } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                                errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_age_young", request, map);
                            } else if (!mail.contains("@")) {
                                errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_mail_invalid", request, map);
                            } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                                errorMessage = Bootstrap.boot.getChatServices().getTemplate("account_error_message_mail_invalid", request, map);
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
                            text = Bootstrap.boot.getChatServices().getConsole("users_account", request, map);
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
                            text = Bootstrap.boot.getChatServices().getConsole("users_reg", request, map);
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
                            text = Bootstrap.boot.getChatServices().getConsole("users_reg", request, map);
                            text = text.set("success", Bootstrap.boot.getChatServices().getConsole("success", request, map));
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
                            ipResolved = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(ip).getCanonicalHostName() : ip;
                        } catch (UnknownHostException uhe) {
                            ipResolved = ip;
                        }
                        if (!pwd.equals(pwd2)) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_password_invalid", request, map);
                        } else if (!gender.equals("-") && !gender.equals("m") && !gender.equals("f")) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_gender_invalid", request, map);
                        } else if (!nick.matches(conf.getString("allowed_chars"))) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_nick_chars", request, map);
                        } else if ((conf.getInt("guest") == 1 && nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase()))) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_nick_guest", request, map);
                        } else if (nick.length() < conf.getInt("min_nick_length")) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_nick_short", request, map);
                        } else if (nick.length() > conf.getInt("max_nick_length")) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_nick_long", request, map);
                        } else if (isCommandNick(nick)) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_nick_command", request, map);
                        } else if (!ut.isValidDate(day, month, year)) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_date", request, map);
                        } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_age_old", request, map);
                        } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_age_young", request, map);
                        } else if (db.isRegistered(nick)) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_nick_registered", request, map);
                        } else if (!mail.equals(mail2)) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_mail_same", request, map);
                        } else if (!mail.contains("@")) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_mail_invalid", request, map);
                        } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                            errorMessage = Bootstrap.boot.getChatServices().getTemplate("reg_error_message_mail_invalid", request, map);
                        } else {
                            regSuccess = true;
                        }
                        View rf = null;

                        if (!regSuccess) {
                            rf = Bootstrap.boot.getChatServices().getConsole("reg_form", request, map);
                            if (service.equals("reg_done")) {
                                rf = rf.set("reg_error_if_one", Bootstrap.boot.getChatServices().getTemplate("reg_failed", request, map));
                                rf = rf.set("error_message", errorMessage);
                            } else {
                                rf = rf.set("reg_error_if_one", "");
                            }
                            cid = newCaptchaId();
                        } else {
                            var color = ut.createRandomColor();
                            db.addNick(gender, nick, mail, color, pwd, question, answer, day, month, year);
                            rf = Bootstrap.boot.getChatServices().getConsole("reg_form_success", request, map);
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
                        text = Bootstrap.boot.getChatServices().getConsole("users_fail", request, map);
                        text = text.set("nick", nick);
                    }
                } else {
                    text = Bootstrap.boot.getChatServices().getConsole("users", request, map);
                }
                ut.submitContent(text, response);
            } else {
                text = Bootstrap.boot.getChatServices().getConsole("404", request, map);
                ut.submitContent(text, response);
            }
        } else {
            response.setStatus(401);
            View text = null;
            text = Bootstrap.boot.getChatServices().getConsole("401", request, map);
            text = text.set("message", filter.message);
            ut.submitContent(text, response);
        }
    }

    public static void board(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var session = request.getSession(false);
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        TreeMap<String, UsersCommunity> com = cm.getUsersCommunity();
        if (session == null || com.get((String) session.getAttribute("nick")) == null) {
            Bootstrap.boot.getChatServices().printTemplate("timeout", request, response, map);
            return;
        }
        var db = conf.getDb();
        var nick = (String) session.getAttribute("nick");
        var moderator = db.getData(nick, "moderator").equals("1");
        com.get((String) session.getAttribute("nick")).setTimeoutTimer(0);
        View text = null;
        var service = map.getOrDefault("service", "");
        var skin = (String) session.getAttribute("skin");

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
            realIp = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
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
            ipResolved = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(ip).getCanonicalHostName() : ip;
        } catch (UnknownHostException uhe) {
            ipResolved = ip;
        }
        try {
            ipResolved2 = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
        } catch (UnknownHostException uhe) {
            ipResolved2 = realIp;
        }
        if (db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(nick));
            Bootstrap.boot.getChatServices().printTemplate("banned", request, response, map);
        } else if (db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ip));
            Bootstrap.boot.getChatServices().printTemplate("banned", request, response, map);
        } else if (db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ipResolved));
            Bootstrap.boot.getChatServices().printTemplate("banned", request, response, map);
        } else if (db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ipResolved2));
            Bootstrap.boot.getChatServices().printTemplate("banned", request, response, map);
        } else if (db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(realIp));
            Bootstrap.boot.getChatServices().printTemplate("banned", request, response, map);
        } else if (db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(nick));
            Bootstrap.boot.getChatServices().printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(ip));
            Bootstrap.boot.getChatServices().printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(ipResolved));
            Bootstrap.boot.getChatServices().printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(ipResolved2));
            Bootstrap.boot.getChatServices().printTemplate("banned_temp", request, response, map);
        } else if (db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

            request.setAttribute("reason", db.getTimedBanReason(realIp));
            Bootstrap.boot.getChatServices().printTemplate("banned_temp", request, response, map);
        } else if (!reg) {
            text = Bootstrap.boot.getChatServices().getTemplate("nick_not_registered_com", request, map);
        } else if (!service.isBlank()) {
            text = Bootstrap.boot.getChatServices().getTemplate("board_unknown", request, map);
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
                text = Bootstrap.boot.getChatServices().getTemplate("boards_moderated", request, map);
            } else if (target.isBlank()) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_no_user", request, map);
            } else if (banned) {
                db.delBans(nick, target2);
                text = Bootstrap.boot.getChatServices().getTemplate("boards_unban", request, map);
            } else {
                var cmd = Bootstrap.boot.getCommands();
                if (cm.isOnline(nick)) {
                    cmd.ban(target + " " + conf.getString("default_ban_reason_board"), nick);
                } else {
                    db.addBan(target2, conf.getString("default_ban_reason_board"), nick);
                }
                text = Bootstrap.boot.getChatServices().getTemplate("boards_ban", request, map);
            }
            text = text.set("color", color);
            text = text.set("nick", nick);
            text = text.set("user", db.getData(target, "nick2"));
        } else if (!undel.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_locked", request, map);
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "deleted", "0");
                text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_undel", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_moderator", request, map);
            }
        } else if (!del.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_locked", request, map);
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "deleted", "1");
                text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_del", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_moderator", request, map);
            }
        } else if (!open.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_locked", request, map);
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "closed", "0");
                text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_opened", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_moderator", request, map);
            }
        } else if (!close.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_locked", request, map);
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "closed", "1");
                text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_closed", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_moderator", request, map);
            }
        } else if (!answer.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_locked", request, map);
            } else {
                ref = db.getBoard(Long.valueOf(pid), "ref");
                var newPostId = db.addThread(map.getOrDefault("topic", ""), map.getOrDefault("thread", ""), Long.valueOf(ref), Long.valueOf(db.getData(nick, "id")), 0, ip, Long.valueOf(bid));
                text = Bootstrap.boot.getChatServices().getTemplate("boards_added", request, map);
                text = text.set("id", bid);
                pid = String.valueOf(newPostId);
                text = text.set("pid", pid);
            }
        } else if (!editBoards.isBlank()) {
            var edited = map.getOrDefault("edited", "").equalsIgnoreCase("true");
            if (!moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_locked", request, map);
            } else {
                if (edited) {
                    var topic = map.getOrDefault("topic", "");
                    var description = map.getOrDefault("description", "");
                    db.updateBoards(Long.valueOf(bid), "topic", topic);
                    db.updateBoards(Long.valueOf(bid), "description", description);
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_edit", request, map);
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_edited", request, map);
                } else {
                    var topic = db.getBoards(Long.valueOf(bid), "topic");
                    var description = db.getBoards(Long.valueOf(bid), "description");
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_edit", request, map);
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
                text = Bootstrap.boot.getChatServices().getTemplate("boards_not_exists", request, map);
            } else if (!moderator && !((Long) thread[3]).equals(Long.valueOf(db.getData(nick, "id")))) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_moderator", request, map);
            } else {
                if (edited) {
                    var topic = map.getOrDefault("topic", "");
                    var content = map.getOrDefault("thread", "");
                    db.updateThread(threadId, "topic", topic);
                    db.updateThread(threadId, "content", content);
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_edited", request, map);
                    text = text.set("id", bid);
                    text = text.set("pid", pid);
                } else {
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_thread_edit_form", request, map);
                    text = text.set("id", bid);
                    text = text.set("pid", pid);
                    text = text.set("topic", ut.parseHtml(String.valueOf(thread[0])));
                    text = text.set("content", ut.parseHtml(String.valueOf(thread[1])));
                }
            }
        } else if (!delBoards.isBlank()) {
            var deleted = db.getBoards(Long.valueOf(bid), "deleted").equals("1");
            if (!moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_locked", request, map);
            } else {
                if (deleted) {
                    db.updateBoards(Long.valueOf(bid), "deleted", "0");
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_undel", request, map);
                    text = text.set("id", bid);
                    text = text.set("pid", pid);
                } else {
                    db.updateBoards(Long.valueOf(bid), "deleted", "1");
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_deleted", request, map);
                    text = text.set("id", bid);;
                    text = text.set("pid", pid);
                }
            }
        } else if (!closeBoards.isBlank()) {
            var closed = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (!moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards_locked", request, map);
            } else {
                if (closed) {
                    db.updateBoards(Long.valueOf(bid), "readonly", "0");
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_open", request, map);
                    text = text.set("id", bid);
                    text = text.set("pid", pid);
                } else {
                    db.updateBoards(Long.valueOf(bid), "readonly", "1");
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_close", request, map);
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
                text = Bootstrap.boot.getChatServices().getTemplate("boards_not_exists", request, map);
            } else {
                if (db.getBoard(Long.valueOf(ref), "closed").equals("1")) {
                    text = Bootstrap.boot.getChatServices().getTemplate("boards3", request, map);
                } else if (locked && !moderator) {
                    text = Bootstrap.boot.getChatServices().getTemplate("boards3", request, map);
                } else {
                    text = Bootstrap.boot.getChatServices().getTemplate("boards2", request, map);
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
                        var content = Bootstrap.boot.getChatServices().getTemplate("boards_page", request, map);
                        content = content.set("page", String.valueOf(l));
                        content = content.set("pid", pid);
                        content = content.set("id", bid);
                        pageContent.append(content);
                        pageContent.append(" ");
                    } else {
                        var content = Bootstrap.boot.getChatServices().getTemplate("boards_page_active", request, map);
                        content = content.set("page", String.valueOf(l));
                        content = content.set("pid", pid);
                        content = content.set("id", bid);
                        pageContent.append(content);
                        pageContent.append(" ");
                    }
                }
                if (page.equals("1")) {
                    text = text.set("prev", Bootstrap.boot.getChatServices().getTemplate("boards_page_prev_disabled", request, map));
                } else {
                    var content = Bootstrap.boot.getChatServices().getTemplate("boards_page_prev_enabled", request, map);
                    content = content.set("page", String.valueOf(Long.valueOf(page) - 1));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    text = text.set("prev", content);
                }
                if (Long.valueOf(page) == pages) {
                    text = text.set("next", Bootstrap.boot.getChatServices().getTemplate("boards_page_next_disabled", request, map));
                } else {
                    var content = Bootstrap.boot.getChatServices().getTemplate("boards_page_next_enabled", request, map);
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
                        var brd = Bootstrap.boot.getChatServices().getTemplate("boards_thread", request, map);
                        topic[0] = String.valueOf(boards[0]);
                        brd = brd.set("topic", ut.parseHtml(String.valueOf(boards[0])));
                        var content = String.valueOf(boards[1]);
                        brd = brd.set("content", ut.parseBb(ut.parseHtml(content)));
                        brd = brd.set("ref", String.valueOf(boards[2]));
                        var user = db.getData((Long) boards[3], "nick2");
                        var signature = ut.parseBb(ut.parseHtml(db.getData((Long) boards[3], "signature")));
                        if (moderator) {
                            brd = brd.set("delete", Bootstrap.boot.getChatServices().getTemplate("boards_thread_delete", request, map));
                            if (boards[2].equals(boards[8]) && String.valueOf(boards[9]).equals("0")) {
                                brd = brd.set("close", Bootstrap.boot.getChatServices().getTemplate("boards_thread_close", request, map));
                            } else if (String.valueOf(boards[9]).equals("1")) {
                                brd = brd.set("close", Bootstrap.boot.getChatServices().getTemplate("boards_thread_open", request, map));
                            } else {
                                brd = brd.set("close", "");
                            }
                            if (db.isBanned(user)) {
                                brd = brd.set("ban", Bootstrap.boot.getChatServices().getTemplate("boards_thread_unban", request, map));
                            } else {
                                brd = brd.set("ban", Bootstrap.boot.getChatServices().getTemplate("boards_thread_ban", request, map));
                            }
                        } else {
                            brd = brd.set("delete", "");
                            brd = brd.set("close", "");
                            brd = brd.set("ban", "");
                        }
                        if (user.equalsIgnoreCase(nick) || moderator) {
                            brd = brd.set("edit", Bootstrap.boot.getChatServices().getTemplate("boards_thread_edit", request, map));
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
                            brd = brd.set("banned", Bootstrap.boot.getChatServices().getTemplate("boards_thread_banned", request, map));
                        } else {
                            brd = brd.set("banned", "");
                        }
                        id[0] = (Long) boards[7];
                        sb.append(brd);
                    } else {
                        var brd = Bootstrap.boot.getChatServices().getTemplate("boards_thread_deleted", request, map);
                        topic[0] = String.valueOf(boards[0]);
                        brd = brd.set("topic", ut.parseHtml(String.valueOf(boards[0])));
                        var content = String.valueOf(boards[1]);
                        brd = brd.set("content", ut.parseBb(ut.parseHtml(content)));
                        brd = brd.set("ref", String.valueOf(boards[2]));
                        var user = db.getData((Long) boards[3], "nick2");
                        if (db.getData(user, "moderator").equals("1")) {
                            brd = brd.set("delete", Bootstrap.boot.getChatServices().getTemplate("boards_thread_undelete", request, map));
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
                            brd = brd.set("banned", Bootstrap.boot.getChatServices().getTemplate("boards_thread_banned", request, map));
                        } else {
                            brd = brd.set("banned", "");
                        }
                        id[0] = (Long) boards[7];
                        sb.append(brd);
                    }
                });
                if (sb.toString().isBlank()) {
                    text = text.set("boards", Bootstrap.boot.getChatServices().getTemplate("boards_empty2", request, map));
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
            text = Bootstrap.boot.getChatServices().getTemplate("boards3", request, map);
            StringBuilder pageContent = new StringBuilder();
            for (long l = 1; l <= pages; l++) {
                if (l != Long.valueOf(page)) {
                    var content = Bootstrap.boot.getChatServices().getTemplate("boards_page", request, map);
                    content = content.set("page", String.valueOf(l));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                } else {
                    var content = Bootstrap.boot.getChatServices().getTemplate("boards_page_active", request, map);
                    content = content.set("page", String.valueOf(l));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                }
            }
            if (page.equals("1")) {
                text = text.set("prev", Bootstrap.boot.getChatServices().getTemplate("boards_page_prev_disabled", request, map));
            } else {
                var content = Bootstrap.boot.getChatServices().getTemplate("boards_page_prev_enabled", request, map);
                content = content.set("page", String.valueOf(Long.valueOf(page) - 1));
                content = content.set("pid", pid);
                content = content.set("id", bid);
                text = text.set("prev", content);
            }
            if (Long.valueOf(page) == pages) {
                text = text.set("next", Bootstrap.boot.getChatServices().getTemplate("boards_page_next_disabled", request, map));
            } else {
                var content = Bootstrap.boot.getChatServices().getTemplate("boards_page_next_enabled", request, map);
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
                    var brd = Bootstrap.boot.getChatServices().getTemplate("boards_thread", request, map);
                    topic[0] = String.valueOf(boards[0]);
                    brd = brd.set("topic", ut.parseHtml(String.valueOf(boards[0])));
                    var content = String.valueOf(boards[1]);
                    brd = brd.set("content", ut.parseBb(ut.parseHtml(content)));
                    brd = brd.set("ref", String.valueOf(boards[2]));
                    var user = db.getData((Long) boards[3], "nick2");
                    if (user.equalsIgnoreCase(nick) || moderator) {
                        brd = brd.set("edit", Bootstrap.boot.getChatServices().getTemplate("boards_thread_edit", request, map));
                    } else {
                        brd = brd.set("edit", "");
                    }
                    if (moderator) {
                        if (db.isBanned(user)) {
                            brd = brd.set("ban", Bootstrap.boot.getChatServices().getTemplate("boards_thread_unban", request, map));
                        } else {
                            brd = brd.set("ban", Bootstrap.boot.getChatServices().getTemplate("boards_thread_ban", request, map));
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
                        brd = brd.set("banned", Bootstrap.boot.getChatServices().getTemplate("boards_thread_banned", request, map));
                    } else {
                        brd = brd.set("banned", "");
                    }
                    id[0] = (Long) boards[7];
                    sb.append(brd);
                } else {
                    var brd = Bootstrap.boot.getChatServices().getTemplate("boards_thread_deleted", request, map);
                    topic[0] = String.valueOf(boards[0]);
                    brd = brd.set("topic", ut.parseHtml(String.valueOf(boards[0])));
                    var content = String.valueOf(boards[1]);
                    brd = brd.set("content", ut.parseBb(ut.parseHtml(content)));
                    brd = brd.set("ref", String.valueOf(boards[2]));
                    var user = db.getData((Long) boards[3], "nick2");
                    if (db.getData(user, "moderator").equals("1")) {
                        brd = brd.set("delete", Bootstrap.boot.getChatServices().getTemplate("boards_thread_undelete", request, map));
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
                        brd = brd.set("banned", Bootstrap.boot.getChatServices().getTemplate("boards_thread_banned", request, map));
                    } else {
                        brd = brd.set("banned", "");
                    }
                    id[0] = (Long) boards[7];
                    sb.append(brd);
                }
            });
            if (sb.toString().isBlank()) {
                text = text.set("boards", Bootstrap.boot.getChatServices().getTemplate("boards_empty2", request, map));
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
                text = Bootstrap.boot.getChatServices().getTemplate("boards_locked", request, map);
            } else {
                var add = map.getOrDefault("add", "");
                if (add.isBlank()) {
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_add", request, map);
                    text = text.set("id", bid);
                } else {
                    var newPostId = db.addThread(map.getOrDefault("topic", ""), map.getOrDefault("thread", ""), 0, Long.valueOf(db.getData(nick, "id")), 0, ip, Long.valueOf(bid));
                    text = Bootstrap.boot.getChatServices().getTemplate("boards_added", request, map);
                    pid = String.valueOf(newPostId);
                    db.updateThread(Long.valueOf(pid), "ref", pid);
                    text = text.set("id", bid);
                    text = text.set("pid", pid);
                }
            }
        } else if (!delCat.isBlank()) {
            if (!moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("board_add_moderator", request, map);
            } else {
                var cat = map.getOrDefault("cat", "");
                var deleted = db.getBoardCat(Long.valueOf(cat), "deleted");
                if (deleted.equals("0")) {
                    db.updateBoardCats(Long.valueOf(cat), "deleted", "1");
                    text = Bootstrap.boot.getChatServices().getTemplate("board_deleted_board", request, map);
                } else {
                    db.updateBoardCats(Long.valueOf(cat), "deleted", "0");
                    text = Bootstrap.boot.getChatServices().getTemplate("board_opened_board", request, map);
                }
            }
        } else if (!editCat.isBlank()) {
            if (!moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("board_add_moderator", request, map);
            } else {
                var topic = map.getOrDefault("topic", "");
                var description = map.getOrDefault("description", "");
                var cat = map.getOrDefault("cat", "");
                if (topic.isBlank()) {
                    text = Bootstrap.boot.getChatServices().getTemplate("board_edit_board", request, map);
                    text = text.set("cat", cat);
                    text = text.set("topic", ut.parseHtml(db.getBoardCat(Long.valueOf(cat), "topic")));
                    text = text.set("description", ut.parseHtml(db.getBoardCat(Long.valueOf(cat), "description")));
                } else {
                    db.updateBoardCats(Long.valueOf(cat), "topic", topic);
                    db.updateBoardCats(Long.valueOf(cat), "description", description);
                    text = Bootstrap.boot.getChatServices().getTemplate("board_edited_board", request, map);
                }
            }
        } else if (!addCat.isBlank()) {
            if (!moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("board_add_moderator", request, map);
            } else {
                var topic = map.getOrDefault("topic", "");
                var description = map.getOrDefault("description", "");
                var cat = map.getOrDefault("cat", "");
                if (topic.isBlank()) {
                    text = Bootstrap.boot.getChatServices().getTemplate("board_add_board", request, map);
                    text = text.set("cat", cat);
                } else {
                    db.addBoards(topic, description, Long.valueOf(cat));
                    text = Bootstrap.boot.getChatServices().getTemplate("board_added_board", request, map);
                }
            }
        } else if (!addBoard.isBlank()) {
            if (!moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("board_add_moderator", request, map);
            } else {
                var topic = map.getOrDefault("topic", "");
                var description = map.getOrDefault("description", "");
                if (topic.isBlank()) {
                    text = Bootstrap.boot.getChatServices().getTemplate("board_add", request, map);
                } else {
                    db.addBoardCat(topic, description);
                    text = Bootstrap.boot.getChatServices().getTemplate("board_added", request, map);
                }
            }
        } else if (bid.equals("0")) {
            if (!moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("board", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("board_moderated", request, map);
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
                    categoryRow = Bootstrap.boot.getChatServices().getTemplate("board_cats_moderated2", request, map);
                    categoryRow.set("delete", Bootstrap.boot.getChatServices().getTemplate(deleted == 0 ? "board_cats_del" : "board_cats_open", request, map));
                    categoryRow.set("edit", Bootstrap.boot.getChatServices().getTemplate("board_cats_edit", request, map));
                } else {
                    if (deleted == 1) {
                        continue;
                    }
                    categoryRow = Bootstrap.boot.getChatServices().getTemplate("board_cats", request, map);
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
                        boardRow = Bootstrap.boot.getChatServices().getTemplate("board_boards", request, map);
                    } else {
                        boardRow = Bootstrap.boot.getChatServices().getTemplate("board_boards_moderated", request, map);
                        boardRow.set("del", Bootstrap.boot.getChatServices().getTemplate(!deleted2 ? "board_boards_moderated_del" : "board_boards_moderated_undel", request, map));
                        boardRow.set("edit", Bootstrap.boot.getChatServices().getTemplate("board_boards_moderated_edit", request, map));
                        var readonly = String.valueOf(boards[2]).equals("1");
                        boardRow.set("lock", Bootstrap.boot.getChatServices().getTemplate(readonly ? "board_boards_moderated_open" : "board_boards_moderated_close", request, map));
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
                        categoryRow.set("boards", Bootstrap.boot.getChatServices().getTemplate("board_boards_empty_locked", request, map));
                    } else if (moderator) {
                        categoryRow.set("boards", Bootstrap.boot.getChatServices().getTemplate("board_boards_empty_moderated", request, map));
                        categoryRow.set("cat", id);
                    } else {
                        categoryRow.set("boards", Bootstrap.boot.getChatServices().getTemplate("board_boards_empty", request, map));
                    }
                } else {
                    categoryRow.set("boards", in.toString());
                }
                category.append(categoryRow);
            }
            if (category.toString().isBlank()) {
                if (moderator) {
                    text = text.set("boards", Bootstrap.boot.getChatServices().getTemplate("board_cats_empty_moderated", request, map));
                } else {
                    text = text.set("boards", Bootstrap.boot.getChatServices().getTemplate("board_cats_empty", request, map));

                }
            } else {
                text = text.set("boards", category);
            }
        } else {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                text = Bootstrap.boot.getChatServices().getTemplate("boards3", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("boards", request, map);
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
                    var content = Bootstrap.boot.getChatServices().getTemplate("boards_page", request, map);
                    content = content.set("page", String.valueOf(l));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                } else {
                    var content = Bootstrap.boot.getChatServices().getTemplate("boards_page_active", request, map);
                    content = content.set("page", String.valueOf(l));
                    content = content.set("pid", pid);
                    content = content.set("id", bid);
                    pageContent.append(content);
                    pageContent.append(" ");
                }
            }
            if (page.equals("1")) {
                text = text.set("prev", Bootstrap.boot.getChatServices().getTemplate("boards_page_prev_disabled", request, map));
            } else {
                var content = Bootstrap.boot.getChatServices().getTemplate("boards_page_prev_enabled2", request, map);
                content = content.set("page", String.valueOf(Long.valueOf(page) - 1));
                content = content.set("pid", pid);
                content = content.set("id", bid);
                text = text.set("prev", content);
            }
            if (Long.valueOf(page) == pages) {
                text = text.set("next", Bootstrap.boot.getChatServices().getTemplate("boards_page_next_disabled", request, map));
            } else {
                var content = Bootstrap.boot.getChatServices().getTemplate("boards_page_next_enabled2", request, map);
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
                    var brd = Bootstrap.boot.getChatServices().getTemplate("boards_thread2", request, map);
                    brd = brd.set("topic", ut.parseHtml(db.getBoardRef((Long) boards[2], "topic")));
                    var content = String.valueOf(db.getBoardRef((Long) boards[2], "content"));
                    if (content.length() > 255) {
                        content = content.substring(0, 255);
                        content = content + "...";
                    }
                    var user = db.getData(Long.valueOf(db.getBoardRef((Long) boards[2], "user")), "nick2");
                    if (user.equalsIgnoreCase(nick) || moderator) {
                        brd = brd.set("edit", Bootstrap.boot.getChatServices().getTemplate("boards_thread_edit", request, map));
                    } else {
                        brd = brd.set("edit", "");
                    }
                    brd = brd.set("content", ut.removeBb(ut.parseHtml(content)));
                    brd = brd.set("ref", String.valueOf(boards[2]));
                    if (moderator) {
                        brd = brd.set("delete", Bootstrap.boot.getChatServices().getTemplate("boards_thread_delete", request, map));
                        if (db.getBoardRef((Long) boards[2], "id").equals(db.getBoardRef((Long) boards[2], "ref"))) {
                            brd = brd.set("close", Bootstrap.boot.getChatServices().getTemplate("boards_thread_close", request, map));
                        } else {
                            brd = brd.set("close", "");
                        }
                        if (db.isBanned(user)) {
                            brd = brd.set("ban", Bootstrap.boot.getChatServices().getTemplate("boards_thread_unban", request, map));
                        } else {
                            brd = brd.set("ban", Bootstrap.boot.getChatServices().getTemplate("boards_thread_ban", request, map));
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
                        brd = brd.set("banned", Bootstrap.boot.getChatServices().getTemplate("boards_thread_banned", request, map));
                    } else {
                        brd = brd.set("banned", "");
                    }
                    sb.append(brd);
                }
            });
            if (sb.toString().isBlank()) {
                text = text.set("boards", Bootstrap.boot.getChatServices().getTemplate("boards_empty", request, map));
                text = text.set("id", bid);
            } else {
                text = text.set("boards", sb.toString());
                text = text.set("id", bid);
            }
        }
        text = text.set("board_error_if_one", "");
        ut.submitContent(text, response);
    }

    public static void profile(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
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
        sid = session != null ? (String) session.getAttribute("sid") : null;
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        var nick = session != null ? (String) session.getAttribute("nick") : null;
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
                text = Bootstrap.boot.getChatServices().getTemplate("profile_invalid_session_id", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("profile_invalid_session_id_napping", request, map);
            }

            text = text.set("user", user);
        } else if (!reg) {
            if (roomName.isBlank()) {
                text = Bootstrap.boot.getChatServices().getTemplate("profile_nick_not_registered", request, map);
            } else {
                text = Bootstrap.boot.getChatServices().getTemplate("profile_nick_not_registered_napping", request, map);
            }

            text = text.set("user", user);
        } else {
            try {
                var lang = (String) request.getAttribute("lang");
                var profile = lang != null && lang.equalsIgnoreCase("en") && db.getProfileEn() != null
                        ? db.getProfileEn()
                        : db.getProfile();
                if (roomName.isBlank()) {
                    text = Bootstrap.boot.getChatServices().getTemplate("profile_user", request, map);
                } else {
                    text = Bootstrap.boot.getChatServices().getTemplate("profile_user_napping", request, map);
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
                for (var label : labels.entrySet()) {
                    label.setValue(text.expand(label.getValue()));
                }
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
                    var eText = Bootstrap.boot.getChatServices().getTemplate("guestbook_entry", request, map);
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
                    var form = Bootstrap.boot.getChatServices().getTemplate("guestbook_form", request, map);
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
     * Selects the template set for a picture upload.
     *
     * @param community whether the user is in the community
     * @param uploadContext where the upload was started
     * @return <code>true</code> for the community templates
     */
    public static boolean useCommunityTemplate(boolean community, String uploadContext) {
        return community && !"chat".equals(uploadContext);
    }

    /** Handles the directly requested chat login frame. */
    public static void loginChat(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {
        var nick = map.getOrDefault("nick", "");
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var ut = Bootstrap.boot.getUtil();
        var db = conf.getDb();
        var target = map.getOrDefault("target", "");
        var skin = map.getOrDefault("skin", "");
        var session = request.getSession();
        if (target.isBlank() && (session.isNew() || session.getAttribute("nick") == null)) {
            if (db.isRegistered(nick)) {
                if (!nick.equalsIgnoreCase(db.getData(nick, "nick2"))) {
                    nick = db.getData(nick, "nick2");
                }
                map.replace("nick", nick);
            }
            var pwd = map.getOrDefault("pwd", "");
            var sid = map.getOrDefault("sid", "");
            sid = !sid.isBlank() ? sid : newCaptchaId();
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
                realIp = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
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
                ipResolved = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(ip).getCanonicalHostName() : ip;
            } catch (UnknownHostException uhe) {
                ipResolved = ip;
            }
            try {
                ipResolved2 = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
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
                Bootstrap.boot.getChatServices().printTemplate("room", request, response, map);
            } else if (!nick.matches(conf.getString("allowed_chars"))) {
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("chars", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("chars_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase())) {
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("guest", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("guest_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if ((conf.getInt("guest") == 1 && !nick.equals("")) && nick.length() < conf.getInt("min_nick_length")) {
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("nick", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("nick_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (isCommandNick(nick)) {
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("command", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("command_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if ((reg = db.isRegistered(nick)) && !db.checkPassword(nick, pwd)) {
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("pwd", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("pwd_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(nick));
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("banned", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("banned_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ip));
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("banned", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("banned_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved));
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("banned", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("banned_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved2));
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("banned", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("banned_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(realIp));
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("banned", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("banned_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(nick));
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("banned_temp", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("banned_temp_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ip));
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("banned_temp", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("banned_temp_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved));
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("banned_temp", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("banned_temp_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved2));
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("banned_temp", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("banned_temp_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(realIp));
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("banned_temp", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("banned_temp_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (!reg && (conf.getInt("guest") == 1 && !nick.equals("") && conf.getInt("only_registered_users") == 1)) {

                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("reg", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("reg_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (!reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));

                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("locked_room", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("locked_room_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("lock_status")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));

                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("locked_room", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("locked_room_napping", request, map);
                    text = text.set("owner", owner);
                    text = text.set("title", db.getRoomData(roomName, "page_title"));
                    ut.submitContent(text, response);
                }
            } else if (cm.isOnline(nick)) {

                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("online", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("online_napping", request, map);
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
                            text = Bootstrap.boot.getChatServices().getTemplate("frameset_reg_napping", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("frameset_napping", request, map);
                        }
                        text = text.set("owner", db.getRoomData(room2, "owner"));
                        ut.submitContent(text, response);
                    } else {
                        if (reg) {
                            Bootstrap.boot.getChatServices().printTemplate("frameset_reg", request, response, map);
                        } else {
                            Bootstrap.boot.getChatServices().printTemplate("frameset", request, response, map);
                        }
                    }
                } else {
                    View text = null;
                    if (reg) {
                        text = Bootstrap.boot.getChatServices().getTemplate("frameset_reg_napping", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("frameset_napping", request, map);
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
            sid = sid != null ? sid : newCaptchaId();
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
                realIp = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
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
                ipResolved = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(ip).getCanonicalHostName() : ip;
            } catch (UnknownHostException uhe) {
                ipResolved = ip;
            }
            try {
                ipResolved2 = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
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
                Bootstrap.boot.getChatServices().printTemplate("room", request, response, map);
            } else if (!roomName.isBlank() && !nick.matches(conf.getString("allowed_chars"))) {
                var text = Bootstrap.boot.getChatServices().getTemplate("chars_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && nick.toLowerCase().startsWith(conf.getString("guest_prefix").toLowerCase())) {
                var text = Bootstrap.boot.getChatServices().getTemplate("guest_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && (conf.getInt("guest") == 1 && !nick.equals("")) && nick.length() < conf.getInt("min_nick_length")) {
                var text = Bootstrap.boot.getChatServices().getTemplate("nick_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && isCommandNick(nick)) {
                var text = Bootstrap.boot.getChatServices().getTemplate("command_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && !reg && !db.checkPassword(nick, pwd)) {
                var text = Bootstrap.boot.getChatServices().getTemplate("pwd_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(nick));
                var text = Bootstrap.boot.getChatServices().getTemplate("banned_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ip));
                var text = Bootstrap.boot.getChatServices().getTemplate("banned_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved));
                var text = Bootstrap.boot.getChatServices().getTemplate("banned_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(ipResolved2));
                var text = Bootstrap.boot.getChatServices().getTemplate("banned_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                request.setAttribute("reason", db.getBanReason(realIp));
                var text = Bootstrap.boot.getChatServices().getTemplate("banned_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(nick));
                var text = Bootstrap.boot.getChatServices().getTemplate("banned_temp_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ip));
                var text = Bootstrap.boot.getChatServices().getTemplate("banned_temp_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(ipResolved));
                var text = Bootstrap.boot.getChatServices().getTemplate("banned_temp_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
                var text = Bootstrap.boot.getChatServices().getTemplate("banned_temp_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {

                request.setAttribute("reason", db.getTimedBanReason(realIp));
                var text = Bootstrap.boot.getChatServices().getTemplate("banned_temp_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && !reg && (conf.getInt("guest") == 1 && !nick.equals("") && conf.getInt("only_registered_users") == 1)) {
                var text = Bootstrap.boot.getChatServices().getTemplate("reg_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && !reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));
                var text = Bootstrap.boot.getChatServices().getTemplate("locked_room_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (!roomName.isBlank() && reg && db.roomExists(room) && db.getRoomData(room, "locked").equals("1") && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("lock_status")) {

                request.setAttribute("reason", db.getRoomData(room, "lock_reason"));
                var text = Bootstrap.boot.getChatServices().getTemplate("locked_room_napping", request, map);
                text = text.set("owner", owner);
                text = text.set("title", db.getRoomData(roomName, "page_title"));
                ut.submitContent(text, response);
            } else if (cm.isOnline(nick)) {
                if (roomName.isBlank()) {
                    Bootstrap.boot.getChatServices().printTemplate("online", request, response, map);
                } else {
                    var text = Bootstrap.boot.getChatServices().getTemplate("online_napping", request, map);
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
                            text = Bootstrap.boot.getChatServices().getTemplate("frameset_reg_com_napping", request, map);
                        } else {
                            text = Bootstrap.boot.getChatServices().getTemplate("frameset_com_napping", request, map);
                        }
                        text = text.set("owner", db.getRoomData(room2, "owner"));
                        ut.submitContent(text, response);
                    } else {
                        if (reg) {
                            Bootstrap.boot.getChatServices().printTemplate("frameset_reg_com", request, response, map);
                        } else {
                            Bootstrap.boot.getChatServices().printTemplate("frameset_com", request, response, map);
                        }
                    }
                } else {
                    View text = null;
                    if (reg) {
                        text = Bootstrap.boot.getChatServices().getTemplate("frameset_reg_com_napping", request, map);
                    } else {
                        text = Bootstrap.boot.getChatServices().getTemplate("frameset_com_napping", request, map);
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
                text = Bootstrap.boot.getChatServices().getTemplate("frameset_privchat", request, map);
                text = text.set("oldnick", oldnick);
                ut.submitContent(text, response);

            } else {

                text = Bootstrap.boot.getChatServices().getTemplate("frameset_privchat_napping", request, map);
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
}
