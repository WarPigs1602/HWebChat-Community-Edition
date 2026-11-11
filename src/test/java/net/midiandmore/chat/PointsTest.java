package net.midiandmore.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.sql.DriverManager;
import org.junit.Test;

public class PointsTest {
    @Test
    public void countsPointsForRegisteredUserWithMixedCaseNick() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE test_users (nick TEXT, nick2 TEXT, points INTEGER NOT NULL DEFAULT 0, timestamp_login INTEGER NOT NULL DEFAULT 0)");
            statement.execute("INSERT INTO test_users (nick, nick2) VALUES ('tester', 'Tester')");
            var database = new Database(null);
            var prefix = Database.class.getDeclaredField("prefix");
            prefix.setAccessible(true);
            prefix.set(database, "test_");
            database.setCon(connection);
            var user = createUser("Tester");

            for (int i = 0; i < 60; i++) {
                ChatManager.updateUserPoints(database, user);
            }
            assertEquals("0", database.getData("Tester", "points"));
            ChatManager.updateUserPoints(database, user);
            assertEquals("1", database.getData("Tester", "points"));
            assertTrue(database.getLongData("tester", "timestamp_login") > 0);

            database.updateNick("Tester", "nick2", "TeStEr");
            assertEquals("TeStEr", database.getData("tester", "nick2"));
            user.setTimer(60);
            ChatManager.updateUserPoints(database, user);
            assertEquals("2", database.getData("Tester", "points"));

            var guest = createUser("Guest");
            guest.setTimer(60);
            ChatManager.updateUserPoints(database, guest);
            assertEquals(0, guest.getTimer());
            assertEquals("2", database.getData("Tester", "points"));
            database.setCon(null);
        }
    }

    @Test
    public void communityTimeoutResetHandlesMissingAndRenamedUsers() {
        var manager = new ChatManager(null);
        var user = createUser("Tester");
        manager.resetCommunityTimeout(user);
        user.setNewName("Other");
        manager.resetCommunityTimeout(user);

        var session = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(), new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getId" -> "session";
                    case "getAttribute" -> switch ((String) args[0]) {
                        case "nick" -> "Tester";
                        case "referer" -> "";
                        default -> null;
                    };
                    default -> null;
                });
        var community = new UsersCommunity(null, session);
        manager.getUsersCommunity().put("Tester", community);
        community.setTimeoutTimer(10);
        manager.resetCommunityTimeout(user);
        assertEquals(0, community.getTimeoutTimer());
        community.setTimeoutTimer(10);
        user.setNewName("Tester");
        manager.resetCommunityTimeout(user);
        assertEquals(0, community.getTimeoutTimer());
    }

    private Users createUser(String nick) {
        return new Users(null, nick, "session", "room", "", "", "", "", "000000", 1,
                null, null, 0, false, "", "", "", "", "", "", "");
    }
}
