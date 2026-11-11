package net.midiandmore.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.Test;

public class BoardEmptyCategoriesTest {
    @Test
    public void defaultTemplatesLinkToCategoryCreation() throws Exception {
        for (var skin : new String[]{"native", "native_en"}) {
            var path = Path.of("../../web/default-homewebcom/templates", skin, "template",
                    "board_cats_empty_moderated.html");
            assertCategoryLink(ChatServices.emptyCategoriesForModerator(Files.readString(path)));
        }
    }

    @Test
    public void previouslyCopiedTemplatesLinkToCategoryCreation() {
        for (var text : new String[]{"Es sind noch keine Foren eingerichtet. Neues Forum anlegen",
                                     "There are no forums yet. Creating a new forum"}) {
            var legacy = "<a href=\"%PATH_[board]%&add_cat=true&cat=%cat%\">" + text + "</a>";
            var rendered = ChatServices.emptyCategoriesForModerator(legacy);
            assertCategoryLink(rendered);
            assertFalse(rendered.contains("Neues Forum anlegen"));
            assertFalse(rendered.contains("Creating a new forum"));
        }
    }

    private void assertCategoryLink(String template) {
        var link = Pattern.compile("href=\"([^\"]+)\"").matcher(template);
        assertTrue(link.find());
        var uri = URI.create(link.group(1).replace("%PATH_[board]%", "/Start?page=board&skin=native"));
        assertEquals("page=board&skin=native&add_board=true", uri.getRawQuery());
        assertFalse(uri.getRawQuery().contains("cat="));
    }
}
