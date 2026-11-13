package net.midiandmore.chat;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Provides the values every template can use: the request data (nick, sid,
 * skin, room, ...), server information, the configured paths and texts and
 * some counters.
 */
final class ViewFactory {

    private static final String[][] FILE_PATHS = {
        {"path_account", "path_account"}, {"path_account_com", "path_account_com"}, {"path_message", "path_message"},
        {"path_chat", "path_chat"}, {"path_profile", "path_profile"}, {"path_emot", "path_emot"},
        {"path_help", "path_help"}, {"path_output", "path_output"}, {"path_start", "path_start"},
        {"path_image", "path_image"}, {"path_memory", "path_memory"}, {"path_captcha", "path_captcha"},
        {"path_register", "path_reg_form"}, {"path_password", "path_password"}, {"path_login", "path_login"},
        {"path_logout", "path_logout"}, {"path_link", "path_link"}, {"path_file", "path_file"},
        {"path_toplist", "path_toplist"}, {"path_webchat", "path_webchat"}, {"path_hwebchat", "path_hwebchat"},
        {"path_login_chat", "path_login_chat"}, {"path_upload", "path_upload"},
        {"path_console_index", "path_console"}, {"path_board", "path_board"}, {"path_stats", "path_stats"}};

    private ViewFactory() {
    }

    /**
     * Request related values of a template.
     */
    record Request(String nick, String sid, String skin, String room, String reason, String host, String owner,
            String target, String user, String roomName, String hostWs, String hostHttp) {
    }

    static View create(String type, String name, String lang, Request request) {
        var view = new View(type, name, lang, new java.util.HashMap<>());
        var values = view.defaults();
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        var cm = Bootstrap.boot.getChatManager();
        for (var path : FILE_PATHS) {
            var value = conf.getString(path[1]);
            values.put(path[0], value == null ? "" : value);
        }
        values.put("SERVER_SOFTWARE", Software.SERVER_SOFTWARE);
        values.put("SERVER_VERSION", Software.SERVER_VERSION);
        values.put("SERVER_STATUS", Software.SERVER_STATUS);
        values.put("SERVER_PROJECT_NAME", Software.SERVER_PROJECT_NAME);
        values.put("SERVER_VENDOR", Software.SERVER_VENDOR);
        values.put("SERVER_HOMEPAGE", Software.SERVER_HOMEPAGE);
        values.put("SERVER_YEAR", Software.SERVER_YEAR);
        values.put("SERVER_MAIL", Software.SERVER_MAIL);
        values.put("lang", lang);
        values.put("paths", new Texts(key -> {
            var raw = db.getPaths().get(key);
            if (raw == null) {
                return null;
            }
            var ut = Bootstrap.boot.getUtil();
            return view.expand(ut.replaceFilePaths(ut.replaceNoCookies(raw)));
        }));
        values.put("com", new Texts(key -> view.expand(db.getCommand(key, lang))));
        values.put("nick", request.nick());
        values.put("sid", request.sid());
        values.put("skin", request.skin());
        values.put("room", request.room());
        values.put("reason", request.reason());
        values.put("host", request.host());
        values.put("owner", request.owner());
        values.put("target", request.target());
        values.put("user", request.user());
        values.put("host_ws", request.hostWs());
        values.put("host_http", request.hostHttp());
        values.put("title", (Supplier<String>) () -> db.getRoomData(request.roomName(), "page_title"));
        values.put("ulist", (Supplier<String>) () -> cm.getUserList(request.sid(), request.skin(), cm.getUserLang(request.nick())));
        values.put("reg_count", (Supplier<String>) () -> String.valueOf(db.countChatter()));
        values.put("user_count", (Supplier<String>) () -> String.valueOf(cm.getUserSizeInChat()));
        values.put("user_count_community", (Supplier<String>) () -> String.valueOf(cm.getUsersCommunity().size()));
        return view;
    }

    /**
     * A read-only map whose values are computed when they are requested.
     */
    static final class Texts extends AbstractMap<String, String> {

        private final java.util.function.Function<String, String> source;

        Texts(java.util.function.Function<String, String> source) {
            this.source = source;
        }

        @Override
        public String get(Object key) {
            return key instanceof String name ? source.apply(name) : null;
        }

        @Override
        public boolean containsKey(Object key) {
            return get(key) != null;
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            return Set.of();
        }
    }
}
