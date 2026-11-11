package net.midiandmore.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.sql.DriverManager;
import org.junit.Test;

public class CommunityNavigationTest {
    @Test
    public void selectsNavigationFromActiveSessionNotRequestedDesign() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE test_users (nick TEXT, nick2 TEXT)");
            statement.execute("INSERT INTO test_users VALUES ('tester', 'Tester')");
            var database = new Database(null);
            var prefix = Database.class.getDeclaredField("prefix");
            prefix.setAccessible(true);
            prefix.set(database, "test_");
            database.setCon(connection);
            var manager = new ChatManager(null);
            var registered = session("registered", "Tester", true);
            var guest = session("guest", "Guest1", true);
            manager.getUsersCommunity().put("Tester", new UsersCommunity(null, registered));
            manager.getUsersCommunity().put("Guest1", new UsersCommunity(null, guest));

            for (var requested : new String[]{"start", "frameset_community", "frameset_community_reg"}) {
                assertEquals("start", ChatServices.selectCommunityDesign(requested, null, manager, database));
                assertEquals("frameset_community_reg", ChatServices.selectCommunityDesign(requested, registered, manager, database));
                assertEquals("frameset_community", ChatServices.selectCommunityDesign(requested, guest, manager, database));
                assertEquals("start", ChatServices.selectCommunityDesign(requested, session("stale", "Tester", true), manager, database));
                assertEquals("start", ChatServices.selectCommunityDesign(requested, session("registered", "Tester", false), manager, database));
            }
            assertEquals("account", ChatServices.selectCommunityDesign("account", registered, manager, database));
            assertEquals("napping", ChatServices.selectCommunityDesign("napping", guest, manager, database));
            assertNotNull(ChatServices.activeCommunitySession(guest, manager));
            manager.getUsersCommunity().remove("Guest1");
            assertNull(ChatServices.activeCommunitySession(guest, manager));
            assertEquals("start", ChatServices.selectCommunityDesign("frameset_community", guest, manager, database));
            assertNull(ChatServices.activeCommunitySession(session("missing", null, true), manager));
            database.setCon(null);
        }
    }

    private HttpSession session(String id, String nick, boolean community) {
        return (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getId" -> id;
                    case "getAttribute" -> switch ((String) args[0]) {
                        case "nick" -> nick;
                        case "community" -> community ? "true" : null;
                        case "referer" -> "";
                        default -> null;
                    };
                    default -> null;
                });
    }
}
