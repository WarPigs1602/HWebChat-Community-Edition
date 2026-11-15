package net.midiandmore.chat;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Serves dynamic script and style assets under short names without the
 * {@code script_}/{@code style_} prefix, for example
 * {@code /content/chat.js?skin=x} or {@code /content/chat.css?skin=x}.
 * The request (including query parameters) is forwarded to the corresponding
 * content JSP so that dynamic parameters remain available.
 */
public final class ContentAssetFilter implements Filter {

    private static final Map<String, String> SCRIPTS = Map.ofEntries(
            Map.entry("bootstrap", "/content/script_bootstrap.jsp"),
            Map.entry("chat", "/content/script_chat.jsp"),
            Map.entry("chat_privchat", "/content/script_chat_privchat.jsp"),
            Map.entry("color", "/content/script_color.jsp"),
            Map.entry("colorpicker", "/content/script_farbtastic.jsp"),
            Map.entry("commands", "/content/script_commands.jsp"),
            Map.entry("emoji", "/content/script_emoji.jsp"),
            Map.entry("framekiller", "/content/script_framekiller.jsp"),
            Map.entry("iframe", "/content/script_iframe.jsp"),
            Map.entry("jquery", "/content/script_jquery.jsp"),
            Map.entry("link", "/content/script_link.jsp"),
            Map.entry("nav", "/content/script_nav.jsp"),
            Map.entry("popper", "/content/script_popper.jsp"),
            Map.entry("post", "/content/script_post.jsp"),
            Map.entry("post_privchat", "/content/script_post_privchat.jsp"),
            Map.entry("prism", "/content/script_prism.jsp"),
            Map.entry("prototype", "/content/script_prototype.jsp"),
            Map.entry("room", "/content/script_rooms.jsp"),
            Map.entry("room_napping", "/content/script_rooms_napping.jsp"),
            Map.entry("userlist", "/content/script_userlist.jsp"),
            Map.entry("userlist_napping", "/content/script_userlist_napping.jsp"),
            Map.entry("window", "/content/script_window.jsp")
    );

    private static final Map<String, String> STYLES = Map.ofEntries(
            Map.entry("style", "/content/style_page.jsp"),
            Map.entry("bootstrap", "/content/style_bootstrap.jsp"),
            Map.entry("chat", "/content/style_chat.jsp"),
            Map.entry("chat_privchat", "/content/style_chat_privchat.jsp"),
            Map.entry("chat_privchat_napping", "/content/style_chat_privchat_napping.jsp"),
            Map.entry("colorpicker", "/content/style_farbtastic.jsp"),
            Map.entry("iframe", "/content/style_iframe.jsp"),
            Map.entry("napping", "/content/style_page_napping.jsp"),
            Map.entry("napping_privchat", "/content/style_page_napping_privchat.jsp"),
            Map.entry("nav", "/content/style_nav.jsp"),
            Map.entry("prism", "/content/style_prism.jsp"),
            Map.entry("privchat", "/content/style_page_privchat.jsp")
    );

    private FilterConfig config;

    @Override
    public void init(FilterConfig filterConfig) {
        config = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        var request = (HttpServletRequest) servletRequest;
        var response = (HttpServletResponse) servletResponse;
        var path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/content/")) {
            var name = path.substring("/content/".length());
            String target = null;
            if (name.endsWith(".js")) {
                target = SCRIPTS.get(name.substring(0, name.length() - 3));
            } else if (name.endsWith(".css")) {
                target = STYLES.get(name.substring(0, name.length() - 4));
            }
            if (target != null) {
                request.getRequestDispatcher(target).forward(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
