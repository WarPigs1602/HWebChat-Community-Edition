package net.midiandmore.chat;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ChatFrameTemplateTest {

    @Test
    public void unauthenticatedFramesContainChatWindowAndScript() throws Exception {
        for (String skin : new String[] {"native", "native_en"}) {
            Path template = Path.of("../../web/default-homewebcom/templates", skin, "template/frameset.html");
            ChatElements elements = new ChatElements();
            try (Reader reader = Files.newBufferedReader(template, StandardCharsets.UTF_8)) {
                new ParserDelegator().parse(reader, elements, true);
            }
            assertTrue(skin + ": missing chat window", elements.chatWindow);
            assertTrue(skin + ": missing chat script", elements.chatScript);
        }
    }

    private static class ChatElements extends HTMLEditorKit.ParserCallback {
        boolean chatWindow;
        boolean chatScript;

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
            if (tag == HTML.Tag.DIV && "chat_window".equals(attributes.getAttribute(HTML.Attribute.ID))) {
                chatWindow = true;
            }
            if (tag == HTML.Tag.SCRIPT && "%PATH_[script_chat]%".equals(attributes.getAttribute(HTML.Attribute.SRC))) {
                chatScript = true;
            }
        }
    }
}
