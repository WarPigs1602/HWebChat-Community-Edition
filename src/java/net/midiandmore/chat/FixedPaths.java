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
            Map.entry("config_chat", "%path_base%/console.jsp?skin=%skin%&service=chat"),
            Map.entry("config_cmd", "%path_base%/console.jsp?skin=%skin%&service=cmd"),
            Map.entry("config_cmdrights", "%path_base%/console.jsp?skin=%skin%&service=cmdrights"),
            Map.entry("config_emot", "%path_base%/console.jsp?skin=%skin%&service=emot"),
            Map.entry("config_fun", "%path_base%/console.jsp?skin=%skin%&service=fun"),
            Map.entry("config_help", "%path_base%/console.jsp?skin=%skin%&service=help"),
            Map.entry("config_host", "%path_base%/console.jsp?skin=%skin%&service=host"),
            Map.entry("config_index", "%path_base%/console.jsp?skin=%skin%"),
            Map.entry("config_mail", "%path_base%/console.jsp?skin=%skin%&service=mail"),
            Map.entry("config_mime", "%path_base%/console.jsp?skin=%skin%&service=mime"),
            Map.entry("config_rooms", "%path_base%/console.jsp?skin=%skin%&service=room"),
            Map.entry("config_users", "%path_base%/console.jsp?skin=%skin%&service=users"),
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
            Map.entry("script_bootstrap", "%path_base%/content/script_bootstrap.jsp?skin=%skin%"),
            Map.entry("script_chat", "%path_base%/content/script_chat.jsp?skin=%skin%"),
            Map.entry("script_chat_privchat", "%path_base%/content/script_chat_privchat.jsp?skin=%skin%&target=%target%&nick=%oldnick%&sid=%sid%"),
            Map.entry("script_color", "%path_base%/content/script_color.jsp?skin=%skin%"),
            Map.entry("script_colorpicker", "%path_base%/content/script_farbtastic.jsp?skin=%skin%"),
            Map.entry("script_commands", "%path_base%/content/script_commands.jsp?skin=%skin%"),
            Map.entry("script_emoji", "%path_base%/content/script_emoji.jsp?skin=%skin%"),
            Map.entry("script_framekiller", "%path_base%/content/script_framekiller.jsp?skin=%skin%"),
            Map.entry("script_iframe", "%path_base%/content/script_iframe.jsp?skin=%skin%"),
            Map.entry("script_jquery", "%path_base%/content/script_jquery.jsp?skin=%skin%"),
            Map.entry("script_link", "%path_base%/content/script_link.jsp?skin=%skin%"),
            Map.entry("script_nav", "%path_base%/content/script_nav.jsp?skin=%skin%"),
            Map.entry("script_popper", "%path_base%/content/script_popper.jsp?skin=%skin%"),
            Map.entry("script_post", "%path_base%/content/script_post.jsp?skin=%skin%"),
            Map.entry("script_post_privchat", "%path_base%/content/script_post_privchat.jsp?skin=%skin%&nick=%oldnick%&target=%target%"),
            Map.entry("script_prototype", "%path_base%/content/script_prototype.jsp?skin=%skin%"),
            Map.entry("script_prism", "%path_base%/content/script_prism.jsp?skin=%skin%"),
            Map.entry("script_room", "%path_base%/content/script_rooms.jsp?skin=%skin%"),
            Map.entry("script_room_napping", "%path_base%/content/script_rooms_napping.jsp?skin=%skin%&room2=%room2%"),
            Map.entry("script_userlist", "%path_base%/content/script_userlist.jsp?skin=%skin%"),
            Map.entry("script_userlist_napping", "%path_base%/content/script_userlist_napping.jsp?skin=%skin%"),
            Map.entry("script_window", "%path_base%/content/script_window.jsp?skin=%skin%"),
            Map.entry("style", "%path_base%/content/style_page.jsp?skin=%skin%"),
            Map.entry("style_bootstrap", "%path_base%/content/style_bootstrap.jsp?skin=%skin%"),
            Map.entry("style_chat", "%path_base%/content/style_chat.jsp?skin=%skin%"),
            Map.entry("style_chat_privchat", "%path_base%/content/style_chat_privchat.jsp?skin=%skin%"),
            Map.entry("style_chat_privchat_napping", "%path_base%/content/style_chat_privchat_napping.jsp?skin=%skin%"),
            Map.entry("style_colorpicker", "%path_base%/content/style_farbtastic.jsp?skin=%skin%"),
            Map.entry("style_iframe", "%path_base%/content/style_iframe.jsp?skin=%skin%"),
            Map.entry("style_napping", "%path_base%/content/style_page_napping.jsp?skin=%skin%&owner=%owner%&room=%room%"),
            Map.entry("style_napping_privchat", "%path_base%/content/style_page_napping_privchat.jsp?skin=%skin%&owner=%owner%&room=%room%"),
            Map.entry("style_nav", "%path_base%/content/style_nav.jsp?skin=%skin%"),
            Map.entry("style_prism", "%path_base%/content/style_prism.jsp?skin=%skin%"),
            Map.entry("style_privchat", "%path_base%/content/style_page_privchat.jsp?skin=%skin%"),
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

    static Map<String, String> placeholders(String contextPath) {
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
