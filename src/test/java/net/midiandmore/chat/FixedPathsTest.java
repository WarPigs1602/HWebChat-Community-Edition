package net.midiandmore.chat;

import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FixedPathsTest {
    @Test
    public void expandsPageAssetsAndWebSocketInBothContexts() {
        for (String context : new String[] {"", "/chat"}) {
            var defaults = new HashMap<String, Object>();
            defaults.putAll(FixedPaths.placeholders(context));
            defaults.put("paths", FixedPaths.all());
            defaults.put("skin", "native");
            defaults.put("host_ws", "wss://example.test");
            var view = new View("literal", "", "de", defaults);
            for (var entry : new String[][] {
                {"index_com", "login.jsp"}, {"login", "login.jsp"}, {"login_frame", "login.jsp"},
                {"login_chat", "login_chat.jsp"}, {"logout", "logout.jsp"},
                {"news", "news.jsp"}, {"webchat", "webchat_stats.jsp"}
            }) {
                assertEquals(context + "/" + entry[1], view.expand("%PATH_[" + entry[0] + "]%"));
            }
            assertEquals(context + "/content/script_chat.jsp?skin=native",
                    view.expand("%PATH_[script_chat]%"));
            assertEquals(context + "/content/rules.jsp?skin=native",
                    view.expand("%PATH_[rules]%"));
            assertEquals(context + "/content/rules_com.jsp?skin=native&community=true",
                    view.expand("%PATH_[rules_com]%"));
            assertEquals(context + "/content/style_page.jsp?skin=native",
                    view.expand("%PATH_[style]%"));
            assertEquals("wss://example.test" + context + "/Chat",
                    view.expand("%PATH_[frame_chat_new]%"));
            assertEquals(context + "/file/theme.js", view.expand("%path_file%theme.js"));
        }
    }

    @Test
    public void preservesLegacyParameterSeparators() {
        var defaults = new HashMap<String, Object>();
        defaults.putAll(FixedPaths.placeholders("/chat"));
        defaults.put("paths", FixedPaths.all());
        defaults.put("skin", "native");
        defaults.put("nick", "Alice");
        var view = new View("literal", "", "de", defaults);
        assertEquals("/chat/login.jsp?room=Lobby", view.expand("%PATH_[login_frame]%&room=Lobby"));
        assertEquals("/chat/login_chat.jsp?owner=Alice&target=Bob",
                view.expand("%PATH_[login_chat]%&amp;owner=Alice&target=Bob"));
        assertEquals("/chat/profile.jsp?skin=native&user=Alice&owner=Alice",
                view.expand("%PATH_[profile]%&owner=Alice"));
        assertEquals("/chat/login.jsp&nbsp;", view.expand("%PATH_[login]%&nbsp;"));
    }

    @Test
    public void expandsLegacyCommandPaths() {
        var previousBoot = Bootstrap.boot;
        try {
            var util = new Util(new Bootstrap());
            assertEquals("/login_chat.jsp?owner=Alice&target=Bob",
                    util.replacePaths("%PATH_[login_chat]%&owner=Alice&target=Bob"));
            assertEquals("/login.jsp?room=Lobby", util.replacePaths("%PATH_[login_frame]%&amp;room=Lobby"));
            assertEquals("/news.jsp", util.replacePaths("%PATH_[news]%"));
            assertEquals("/profile.jsp?skin=%skin%&user=%nick%&owner=Alice",
                    util.replacePaths("%PATH_[profile]%&owner=Alice"));
        } finally {
            Bootstrap.boot = previousBoot;
        }
    }

    @Test
    public void removedPathEditorHasNoAddress() {
        assertNull(FixedPaths.get("config_path"));
    }

    @Test
    public void missingDocumentIsMarkedAsMissing() {
        var view = new View("template", "no_such_document", "de", new HashMap<>());
        view.toString();
        assertTrue(view.isMissing());
    }
}
