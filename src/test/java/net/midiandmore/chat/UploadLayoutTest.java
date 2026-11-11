package net.midiandmore.chat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class UploadLayoutTest {
    @Test
    public void chatUploadFormsIncludeContextForBothSkinsAndLayouts() throws Exception {
        for (var skin : new String[]{"native", "native_en"}) {
            for (var template : new String[]{"picture", "picture_napping"}) {
                var path = Path.of("../../web/default-homewebcom/templates", skin, "template", template + ".html");
                var html = UploadFile.markChatUploadForm(Files.readString(path));
                var formStart = html.indexOf("<form");
                var formEnd = html.indexOf("</form>", formStart);
                var marker = html.indexOf("name=\"upload_context\" value=\"chat\"");
                assertTrue(marker > formStart && marker < formEnd);
                assertFalse(UploadFile.useCommunityTemplate(true, "chat"));
            }
        }
    }

    @Test
    public void communityUploadsKeepCommunityLayout() {
        assertTrue(UploadFile.useCommunityTemplate(true, null));
        assertTrue(UploadFile.useCommunityTemplate(true, "community"));
        assertFalse(UploadFile.useCommunityTemplate(false, null));
        assertFalse(UploadFile.useCommunityTemplate(false, "chat"));
    }

    @Test(expected = IllegalStateException.class)
    public void uploadTemplateMustContainAForm() {
        UploadFile.markChatUploadForm("<p>No upload form</p>");
    }
}
