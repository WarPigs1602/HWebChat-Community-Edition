package net.midiandmore.chat;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Fixed application paths; the deployment context is supplied at runtime. */
final class FixedPaths {
    private static final Pattern LEGACY_QUERY = Pattern.compile(
            "(%PATH_\\[([^\\]%]+)\\]%)&(?:amp;)?(?=[A-Za-z_][A-Za-z0-9_.-]*=)");

    private static final Map<String, String> URLS = Map.ofEntries(
            Map.entry("account", "%path_base%/account.jsp?skin=%skin%"),
            Map.entry("account_com", "%path_base%/account_com.jsp?skin=%skin%"),
            Map.entry("ban_temp_url", "%path_base%/content/banned_temp.jsp?skin=%skin%&reason=%reason%"),
            Map.entry("ban_url", "%path_base%/content/banned.jsp?skin=%skin%&reason=%reason%"),
            Map.entry("board", "%path_base%/board.jsp?skin=%skin%"),
            Map.entry("captcha", "%path_base%/captcha.jsp?cid=%reg_cid%&skin=%skin%"),
            Map.entry("chat_start", "%path_hwebchat%?page=%path_chat_start%&skin=%skin%&skin=%skin%"),
            Map.entry("clear_chat_window", "%path_hwebchat%?page=%path_input%&msg=/c&skin=%skin%"),
            Map.entry("config_chat", "%path_console%?skin=%skin%&service=chat"),
            Map.entry("config_cmd", "%path_console%?skin=%skin%&service=cmd"),
            Map.entry("config_cmdrights", "%path_console%?skin=%skin%&service=cmdrights"),
            Map.entry("config_emot", "%path_console%?skin=%skin%&service=emot"),
            Map.entry("config_fun", "%path_console%?skin=%skin%&service=fun"),
            Map.entry("config_help", "%path_console%?skin=%skin%&service=help"),
            Map.entry("config_host", "%path_console%?skin=%skin%&service=host"),
            Map.entry("config_index", "%path_console%?skin=%skin%"),
            Map.entry("config_news", "%path_console%?service=news"),
            Map.entry("config_mail", "%path_console%?skin=%skin%&service=mail"),
            Map.entry("config_mime", "%path_console%?skin=%skin%&service=mime"),
            Map.entry("config_rooms", "%path_console%?skin=%skin%&service=room"),
            Map.entry("config_users", "%path_console%?skin=%skin%&service=users"),
            Map.entry("dataprotection", "%path_base%/content/dataprotection.jsp?skin=%skin%"),
            Map.entry("emot", "%path_base%/emot.jsp?skin=%skin%"),
            Map.entry("file", "%path_file%"),
            Map.entry("flood_url", "%path_base%/content/flood.jsp?skin=%skin%"),
            Map.entry("frame_chat_new", "%host_ws%%path_chat%"),
            Map.entry("frame_chat_new_napping", "%host_ws%%path_chat%"),
            Map.entry("frame_left", "%path_base%/content/left.jsp?skin=%skin%"),
            Map.entry("frame_left_reg", "%path_base%/content/left_reg.jsp?skin=%skin%"),
            Map.entry("frame_post", "%path_base%/content/post.jsp?skin=%skin%"),
            Map.entry("frame_post_reg", "%path_base%/content/post_reg.jsp?skin=%skin%"),
            Map.entry("frame_right", "about:blank"),
            Map.entry("frame_right_reg", "about:blank"),
            Map.entry("frame_top", "%path_base%/content/top.jsp?skin=%skin%"),
            Map.entry("frame_top_reg", "%path_base%/content/top_reg.jsp?skin=%skin%"),
            Map.entry("get_chatinfo", "%path_hwebchat%?page=%path_input%&msg=/info&skin=%skin%"),
            Map.entry("get_my_ip", "%path_hwebchat%?page=%path_input%&msg=/myip&skin=%skin%"),
            Map.entry("help", "%path_base%/help.jsp?skin=%skin%"),
            Map.entry("home", "%path_base%/start.jsp?skin=%skin%"),
            Map.entry("image", "%path_base%/image.jsp?nick=%nick%"),
            Map.entry("impressum", "%path_base%/content/impressum.jsp?skin=%skin%"),
            Map.entry("index", "%path_hwebchat%?skin=%skin%"),
            Map.entry("index_com", "%path_base%/login.jsp"),
            Map.entry("input", "%path_hwebchat%?page=%path_input%&skin=%skin%"),
            Map.entry("kick_url", "%path_base%/content/kicked.jsp?skin=%skin%&reason=%reason%"),
            Map.entry("login", "%path_base%/login.jsp"),
            Map.entry("login_chat", "%path_base%/login_chat.jsp"),
            Map.entry("login_frame", "%path_base%/login.jsp"),
            Map.entry("login_start", "%path_base%/login.jsp?service=start&skin=%skin%"),
            Map.entry("logout", "%path_base%/logout.jsp"),
            Map.entry("logout_com", "%path_hwebchat%?page=%path_logout_com%"),
            Map.entry("lorem_ipsum", "%path_base%/content/lorem_ipsum.jsp?skin=%skin%"),
            Map.entry("memory", "%path_base%/memory.jsp?skin=%skin%"),
            Map.entry("msg_privchat", "%path_hwebchat%?page=%path_msg_privchat%"),
            Map.entry("news", "%path_base%/news.jsp"),
            Map.entry("napping", "%path_hwebchat%?page=%path_napping%&skin=%skin%"),
            Map.entry("napping_form", "%path_hwebchat%?page=%path_napping_form%&skin=%skin%"),
            Map.entry("password", "%path_base%/password.jsp?skin=%skin%"),
            Map.entry("privchat", "%path_hwebchat%?page=%path_privchat%&skin=%skin%&user=%nick%"),
            Map.entry("profile", "%path_base%/profile.jsp?skin=%skin%&user=%nick%"),
            Map.entry("quit_chat", "%path_hwebchat%?page=%path_input%&msg=/q+Ausgeloggt+per+Mausklick!&skin=%skin%"),
            Map.entry("quit_error", "%path_base%/content/quit.jsp?skin=%skin%&reason=Ausgeloggt+durch+Streamabbruch!"),
            Map.entry("quit_url", "%path_base%/content/quit.jsp?skin=%skin%&reason=%reason%"),
            Map.entry("redirect", "%path_base%/link.jsp?url="),
            Map.entry("refresh_url2", "%path_base%/content/refreshstream2.jsp?skin=%skin%"),
            Map.entry("register", "%path_base%/register.jsp?skin=%skin%"),
            Map.entry("relogin_url", "%path_base%/content/relogin.jsp?skin=%skin%"),
            Map.entry("rules", "%path_base%/content/rules.jsp?skin=%skin%"),
            Map.entry("rules_com", "%path_base%/content/rules_com.jsp?skin=%skin%&community=true"),
            Map.entry("rules_com_reg", "%path_base%/content/rules_com_reg.jsp?skin=%skin%&community=true"),
            Map.entry("script_bootstrap", "%path_base%/content/bootstrap.js?skin=%skin%"),
            Map.entry("script_chat", "%path_base%/content/chat.js?skin=%skin%"),
            Map.entry("script_chat_privchat", "%path_base%/content/chat_privchat.js?skin=%skin%&target=%target%&nick=%oldnick%&sid=%sid%"),
            Map.entry("script_color", "%path_base%/content/color.js?skin=%skin%"),
            Map.entry("script_colorpicker", "%path_base%/content/colorpicker.js?skin=%skin%"),
            Map.entry("script_commands", "%path_base%/content/commands.js?skin=%skin%"),
            Map.entry("script_emoji", "%path_base%/content/emoji.js?skin=%skin%"),
            Map.entry("script_framekiller", "%path_base%/content/framekiller.js?skin=%skin%"),
            Map.entry("script_iframe", "%path_base%/content/iframe.js?skin=%skin%"),
            Map.entry("script_jquery", "%path_base%/content/jquery.js?skin=%skin%"),
            Map.entry("script_link", "%path_base%/content/link.js?skin=%skin%"),
            Map.entry("script_nav", "%path_base%/content/nav.js?skin=%skin%"),
            Map.entry("script_popper", "%path_base%/content/popper.js?skin=%skin%"),
            Map.entry("script_post", "%path_base%/content/post.js?skin=%skin%"),
            Map.entry("script_post_privchat", "%path_base%/content/post_privchat.js?skin=%skin%&nick=%oldnick%&target=%target%"),
            Map.entry("script_prototype", "%path_base%/content/prototype.js?skin=%skin%"),
            Map.entry("script_prism", "%path_base%/content/prism.js?skin=%skin%"),
            Map.entry("script_room", "%path_base%/content/room.js?skin=%skin%"),
            Map.entry("script_room_napping", "%path_base%/content/room_napping.js?skin=%skin%&room2=%room2%"),
            Map.entry("script_userlist", "%path_base%/content/userlist.js?skin=%skin%"),
            Map.entry("script_userlist_napping", "%path_base%/content/userlist_napping.js?skin=%skin%"),
            Map.entry("script_window", "%path_base%/content/window.js?skin=%skin%"),
            Map.entry("style", "%path_base%/content/style.css?skin=%skin%"),
            Map.entry("style_bootstrap", "%path_base%/content/bootstrap.css?skin=%skin%"),
            Map.entry("style_chat", "%path_base%/content/chat.css?skin=%skin%"),
            Map.entry("style_chat_privchat", "%path_base%/content/chat_privchat.css?skin=%skin%"),
            Map.entry("style_chat_privchat_napping", "%path_base%/content/chat_privchat_napping.css?skin=%skin%"),
            Map.entry("style_colorpicker", "%path_base%/content/colorpicker.css?skin=%skin%"),
            Map.entry("style_iframe", "%path_base%/content/iframe.css?skin=%skin%"),
            Map.entry("style_napping", "%path_base%/content/napping.css?skin=%skin%&owner=%owner%&room=%room%"),
            Map.entry("style_napping_privchat", "%path_base%/content/napping_privchat.css?skin=%skin%&owner=%owner%&room=%room%"),
            Map.entry("style_nav", "%path_base%/content/nav.css?skin=%skin%"),
            Map.entry("style_prism", "%path_base%/content/prism.css?skin=%skin%"),
            Map.entry("style_privchat", "%path_base%/content/privchat.css?skin=%skin%"),
            Map.entry("timeout_url", "%path_base%/content/timeout.jsp?skin=%skin%"),
            Map.entry("toplist", "%path_base%/top.jsp?skin=%skin%"),
            Map.entry("ua", "%path_base%/content/ua.jsp?skin=%skin%"),
            Map.entry("user_msg", "%path_base%/read_message.jsp?skin=%skin%"),
            Map.entry("stats", "%path_base%/stats.jsp?skin=%skin%"),
            Map.entry("webchat", "%path_base%/webchat_stats.jsp"),
            Map.entry("webchat_json", "%path_base%/webchat_stats.jsp?use_json=1"),
            Map.entry("webchat_json_napping", "%path_base%/webchat_stats.jsp?use_json=1&room2=%room2%"),
            Map.entry("whois_channel", "%path_hwebchat%?page=%path_input%&msg=/w&skin=%skin%"),
            Map.entry("whois_chat", "%path_hwebchat%?page=%path_input%&msg=/wc&skin=%skin%")
    );

    /** Default path of the admin console, relative to the context. */
    static final String DEFAULT_CONSOLE_PATH = "console.jsp";

    private static final Pattern CONSOLE_PATH = Pattern.compile("[A-Za-z0-9_.-]+(/[A-Za-z0-9_.-]+)*");

    /**
     * Normalizes the configured path of the admin console.
     *
     * @param configured the configured value (may start with a slash)
     * @return the path without leading slash, or the default if the value is empty or not usable
     */
    static String consolePath(String configured) {
        if (configured == null) {
            return DEFAULT_CONSOLE_PATH;
        }
        var path = configured.strip();
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!CONSOLE_PATH.matcher(path).matches() || path.contains("..")) {
            return DEFAULT_CONSOLE_PATH;
        }
        return path;
    }

    static Map<String, String> placeholders(String contextPath) {
        return placeholders(contextPath, DEFAULT_CONSOLE_PATH);
    }

    static Map<String, String> placeholders(String contextPath, String consolePath) {
        return Map.ofEntries(
                Map.entry("path_account", "account"),
                Map.entry("path_account_com", "account_com"),
                Map.entry("path_message", "read_message"),
                Map.entry("path_profile", "profile"),
                Map.entry("path_emot", "emot"),
                Map.entry("path_help", "help"),
                Map.entry("path_output", "content"),
                Map.entry("path_start", "/"),
                Map.entry("path_image", "image"),
                Map.entry("path_memory", "memory"),
                Map.entry("path_captcha", "captcha"),
                Map.entry("path_register", "register"),
                Map.entry("path_password", "password"),
                Map.entry("path_login", "login"),
                Map.entry("path_logout", "logout"),
                Map.entry("path_link", "link"),
                Map.entry("path_toplist", "top"),
                Map.entry("path_webchat", "webchat_stats"),
                Map.entry("path_login_chat", "login_chat"),
                Map.entry("path_console_index", "console"),
                Map.entry("path_console", contextPath + "/" + consolePath),
                Map.entry("path_board", "board"),
                Map.entry("path_stats", "stats"),
                Map.entry("path_chat", contextPath + "/Chat"),
                Map.entry("path_file", contextPath + "/file/"),
                Map.entry("path_base", contextPath),
                Map.entry("path_hwebchat", contextPath + "/start.jsp"),
                Map.entry("path_upload", contextPath + "/upload.jsp")
        );
    }

    static String get(String key) {
        return URLS.get(key);
    }

    static Map<String, String> all() {
        return URLS;
    }

    static String normalizeLegacyQuery(String text, Function<String, String> paths) {
        var matcher = LEGACY_QUERY.matcher(text);
        var result = new StringBuilder();
        while (matcher.find()) {
            var path = paths.apply(matcher.group(2));
            var replacement = path != null && !path.contains("?") ? matcher.group(1) + "?" : matcher.group();
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private FixedPaths() {
    }
}
