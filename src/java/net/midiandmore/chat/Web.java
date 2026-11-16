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
        Bootstrap.servletContext = context;
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

    static synchronized boolean isStarted() {
        return master != null;
    }

    static String contextPath() {
        return contextPath;
    }

    /** Path of the admin console relative to the context, as configured by <code>console.path</code>. */
    public static String consolePath() {
        var config = master != null ? master.getConfig() : null;
        return FixedPaths.consolePath(config != null ? config.getString("console.path") : null);
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
                    "published", formatTime(article.published()),
                    "body", Bootstrap.boot.getUtil().parseBb(Bootstrap.boot.getUtil().parseHtml(article.body()))));
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



    /** Checks whether a module is enabled (defaults to enabled if missing). */
    public static boolean isModuleEnabled(String name) {
        var config = master != null ? master.getConfig() : null;
        return config == null || config.isModuleEnabled(name);
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

}
