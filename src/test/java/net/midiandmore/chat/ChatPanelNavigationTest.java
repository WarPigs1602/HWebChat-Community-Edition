package net.midiandmore.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class ChatPanelNavigationTest {
    @Test
    public void everyBundledDesignGetsChatPanelNavigationRules() throws Exception {
        for (var skin : new String[]{"native", "native_en"}) {
            var directory = Path.of("../../web/default-homewebcom/templates", skin, "design");
            try (var designs = Files.list(directory)) {
                for (var path : designs.filter(file -> file.toString().endsWith(".html")).toList()) {
                    var original = Files.readString(path);
                    var result = ChatServices.addChatPanelNavigationRules(original);
                    assertTrue(path.toString(), result.contains("window.frameElement.classList.contains('chat-panel-iframe')"));
                    assertTrue(path.toString(), result.contains("html.chat-panel-embedded body > nav.navbar{display:none!important;}"));
                    assertTrue(path.toString(), result.indexOf("chat-panel-embedded") < result.toLowerCase().indexOf("</head>"));
                    var start = result.indexOf("<script>if(window.frameElement");
                    var end = result.indexOf("</style>", start) + "</style>".length();
                    assertEquals(original, result.substring(0, start) + result.substring(end));
                }
            }
        }
    }

    @Test
    public void designWithoutHeadKeepsDoctypeAndContent() {
        var result = ChatServices.addChatPanelNavigationRules("<!DOCTYPE html><body><nav class=\"navbar\">Navigation</nav></body>");
        assertTrue(result.startsWith("<!DOCTYPE html><script>"));
        assertTrue(result.endsWith("<body><nav class=\"navbar\">Navigation</nav></body>"));
        assertTrue(ChatServices.addChatPanelNavigationRules("<div>Content</div>").endsWith("<div>Content</div>"));
    }
}
