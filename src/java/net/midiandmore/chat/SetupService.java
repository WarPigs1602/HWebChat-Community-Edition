package net.midiandmore.chat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import jakarta.mail.MessagingException;
import static java.lang.System.getProperty;
import static java.lang.System.out;
import static java.lang.System.err;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import static net.midiandmore.chat.Bootstrap.boot;
import static net.midiandmore.chat.Bootstrap.fatalError;
import static net.midiandmore.chat.Bootstrap.logError;
import static java.sql.DriverManager.getConnection;

/**
 * Logic of the first-start setup. The page itself is rendered by
 * <code>/WEB-INF/jsp/endpoints/setup.jsp</code>.
 */
public final class SetupService {

    private final ServletContext context;

    public SetupService(ServletContext context) {
        this.context = context;
    }

    private String getHomeWebCom() {
        return getProperty("user.home") + getProperty("file.separator") + ".homewebcom";
    }

    private String getConfigDir() {
        return getHomeWebCom() + getProperty("file.separator") + "config";
    }

    /**
     * Checks whether the setup has already been completed.
     *
     * @return <code>true</code> if the setup has been completed
     */
    public boolean isSetupComplete() {
        var configFile = new File(getConfigDir() + getProperty("file.separator") + "config.json");
        var markerFile = new File(getConfigDir() + getProperty("file.separator") + ".setup_complete");
        return markerFile.exists() && configFile.exists();
    }

    private void markSetupComplete() {
        try {
            var markerFile = new File(getConfigDir() + getProperty("file.separator") + ".setup_complete");
            new FileWriter(markerFile).close();
        } catch (IOException e) {
            logError(e);
        }
    }

    private void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdirs();
        }
        var files = source.listFiles();
        if (files == null) {
            return;
        }
        for (var file : files) {
            var dest = new File(target, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, dest);
            } else {
                var reader = new java.io.FileInputStream(file);
                var writer = new java.io.FileOutputStream(dest);
                reader.transferTo(writer);
                reader.close();
                writer.close();
            }
        }
    }

    private void copyServletResources(String basePath, File targetDir) throws IOException {
        var paths = new String[]{"config", "data", "htdocs", "pictures", "templates"};
        for (var path : paths) {
            var resourcePath = basePath + "/" + path;
            var realPath = context.getRealPath(resourcePath);
            if (realPath == null) {
                continue;
            }
            var sourceDir = new File(realPath);
            if (!sourceDir.exists()) {
                continue;
            }
            var targetSubDir = new File(targetDir, path);
            if (!targetSubDir.exists()) {
                targetSubDir.mkdirs();
            }
            copyDirectory(sourceDir, targetSubDir);
        }
    }

    private String escapeSql(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    private Connection openDatabaseConnection(SqlDialect.Type type, String dbHost, String dbPort, String dbName,
                                               String dbUser, String dbPassword, String sqlitePath) throws SQLException, ClassNotFoundException, IOException {
        Class.forName(type.driverClass());
        var url = switch (type) {
            case MYSQL -> "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=yes&characterEncoding=UTF-8&serverTimezone=UTC";
            case POSTGRESQL -> "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
            case SQLITE -> "jdbc:sqlite:" + sqlitePath;
        };
        if (type == SqlDialect.Type.SQLITE) {
            var parent = new File(sqlitePath).toPath().toAbsolutePath().getParent();
            if (parent != null) {
                java.nio.file.Files.createDirectories(parent);
            }
            var connection = getConnection(url);
            SqlDialect.initialize(connection, type);
            return SqlDialect.wrap(connection, type);
        }
        var connection = getConnection(url, dbUser, dbPassword);
        SqlDialect.ensurePostgresqlExtensions(connection, type);
        SqlDialect.initialize(connection, type);
        return SqlDialect.wrap(connection, type);
    }

    private boolean testDatabaseConnection(SqlDialect.Type type, String dbHost, String dbPort, String dbName,
                                            String dbUser, String dbPassword, String sqlitePath) {
        try (var con = openDatabaseConnection(type, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath)) {
            return con.isValid(1000);
        } catch (SQLException | ClassNotFoundException | IOException e) {
            logError(e);
            return false;
        }
    }

    private void createDatabaseTables(SqlDialect.Type type, String dbHost, String dbPort, String dbName, String dbUser,
                                      String dbPassword, String sqlitePath, String dbPrefix) {
        try (var con = openDatabaseConnection(type, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath)) {
            var stmt = con.createStatement();
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "users` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `nick` varchar(255) DEFAULT NULL, `nick2` varchar(255) DEFAULT NULL, `pwd` varchar(255) DEFAULT NULL, `pwd2` varchar(255) NOT NULL DEFAULT '', `mail` varchar(255) DEFAULT NULL, `sex` char(1) NOT NULL DEFAULT '-', `reminder` varchar(255) DEFAULT NULL, `answer` varchar(255) DEFAULT NULL, `homepage` varchar(255) DEFAULT NULL, `image_url` varchar(255) DEFAULT NULL, `image_upload` longblob DEFAULT NULL, `city` varchar(255) DEFAULT NULL, `hobby` varchar(255) DEFAULT NULL, `status` int(11) NOT NULL DEFAULT 1, `points` int(11) NOT NULL DEFAULT 0, `timestamp_reg` bigint(20) NOT NULL DEFAULT 0, `timestamp_login` bigint(20) NOT NULL DEFAULT 0, `bday_day` tinyint(4) NOT NULL DEFAULT 1, `bday_month` tinyint(4) NOT NULL DEFAULT 1, `bday_year` int(11) NOT NULL DEFAULT 1970, `login_room` varchar(255) NOT NULL DEFAULT 'Lounge', `description` varchar(255) DEFAULT NULL, `slogan` varchar(255) DEFAULT NULL, `signature` text DEFAULT NULL, `ignore` text DEFAULT NULL, `sv` int(11) NOT NULL DEFAULT 0, `icq` varchar(255) DEFAULT NULL, `live` varchar(255) DEFAULT NULL, `yahoo` varchar(255) DEFAULT NULL, `facebook` varchar(255) DEFAULT NULL, `twitter` varchar(255) DEFAULT NULL, `irc` varchar(255) DEFAULT NULL, `youtube` varchar(255) DEFAULT NULL, `visitors` text DEFAULT NULL, `color` varchar(7) DEFAULT '000000', `fam_status` varchar(255) DEFAULT NULL, `moderator` tinyint(1) NOT NULL DEFAULT 0, `instagram` varchar(255) DEFAULT '', `linkedin` varchar(255) DEFAULT '', `tiktok` varchar(255) DEFAULT '', `discord` varchar(255) DEFAULT '', `twitch` varchar(255) DEFAULT '', `github` varchar(255) DEFAULT '', `reddit` varchar(255) DEFAULT '', `snapchat` varchar(255) DEFAULT '', `pinterest` varchar(255) DEFAULT '', `whatsapp` varchar(255) DEFAULT '', `telegram` varchar(255) DEFAULT '', PRIMARY KEY (`id`), UNIQUE KEY `nick` (`nick`), UNIQUE KEY `nick2` (`nick2`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "session` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `nick` varchar(255) NOT NULL, `session` varchar(255) NOT NULL, `room` varchar(255) NOT NULL, `color` varchar(255) NOT NULL, `away_status` varchar(255) NOT NULL, `away_reason` varchar(255) NOT NULL, `gag` varchar(255) NOT NULL, `status` varchar(255) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "banlist` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `name` varchar(255) NOT NULL DEFAULT '', `reason` varchar(255) NOT NULL, `banner` varchar(255) NOT NULL, `time` varchar(255) NOT NULL DEFAULT '0', PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "messages` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `sender` varchar(255) NOT NULL, `target` varchar(255) NOT NULL, `text` text NOT NULL, `time` bigint(20) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "friends` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `nick` varchar(255) NOT NULL, `nick2` varchar(255) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "roomcfg` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `room` varchar(255) NOT NULL DEFAULT '', `topic` text DEFAULT NULL, `tar` int(11) NOT NULL DEFAULT 0, `locked` int(11) NOT NULL DEFAULT 0, `lock_reason` text DEFAULT NULL, `standard` int(11) NOT NULL DEFAULT 0, `allow_smilies` int(11) NOT NULL DEFAULT 1, `chat_napping` int(11) DEFAULT 0, `first_bgcolor` varchar(6) DEFAULT NULL, `second_bgcolor` varchar(6) DEFAULT NULL, `bordercolor` varchar(6) DEFAULT NULL, `textcolor` varchar(6) DEFAULT NULL, `linkcolor` varchar(6) DEFAULT NULL, `page_title` text NOT NULL, `mail` varchar(255) DEFAULT NULL, `owner` varchar(255) DEFAULT NULL, `su` longtext DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "napping` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `nick` varchar(255) NOT NULL, `room` varchar(255) NOT NULL, `title` varchar(255) NOT NULL, `bg_color_1` varchar(255) NOT NULL, `bg_color_2` varchar(255) NOT NULL, `color_1` varchar(255) NOT NULL, `color_2` varchar(255) NOT NULL, `link_color_1` varchar(255) NOT NULL, `link_color_2` varchar(255) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "board_cat` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `topic` varchar(255) NOT NULL, `description` longtext NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "board_boards` (`id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, `cat` bigint(20) NOT NULL, `topic` varchar(255) NOT NULL, `readonly` tinyint(1) NOT NULL DEFAULT 0, `description` longtext NOT NULL, `guests` tinyint(1) NOT NULL DEFAULT 0, `deleted` tinyint(1) NOT NULL DEFAULT 0, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "board` (`id` int(11) NOT NULL AUTO_INCREMENT, `topic` varchar(255) NOT NULL, `content` longtext NOT NULL, `ref` bigint(20) NOT NULL, `user` bigint(20) NOT NULL, `board` bigint(20) NOT NULL, `posted` bigint(20) NOT NULL, `ip` varchar(255) NOT NULL, `cat` bigint(20) NOT NULL, `deleted` tinyint(1) NOT NULL DEFAULT 0, `closed` tinyint(1) NOT NULL DEFAULT 0, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "guestbook` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `owner` varchar(255) NOT NULL, `sender` varchar(255) NOT NULL, `text` text NOT NULL, `time` bigint(20) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "chat_history` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `room` varchar(255) NOT NULL, `sender` varchar(255) NOT NULL, `target` varchar(255) DEFAULT NULL, `text` text NOT NULL, `timestamp` bigint(20) NOT NULL, `type` varchar(20) NOT NULL DEFAULT 'public', PRIMARY KEY (`id`), KEY `idx_room` (`room`), KEY `idx_sender` (`sender`), KEY `idx_timestamp` (`timestamp`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "stats_peaks` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `date` date NOT NULL, `peak_users` int(11) NOT NULL DEFAULT 0, `peak_room` varchar(255) NOT NULL DEFAULT '', PRIMARY KEY (`id`), UNIQUE KEY `date` (`date`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "stats_hourly` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `hour_bucket` datetime NOT NULL, `room` varchar(255) NOT NULL, `avg_users` int(11) NOT NULL DEFAULT 0, `peak_users` int(11) NOT NULL DEFAULT 0, `messages` int(11) NOT NULL DEFAULT 0, `sample_count` bigint(20) NOT NULL DEFAULT 0, `user_count_total` bigint(20) NOT NULL DEFAULT 0, PRIMARY KEY (`id`), UNIQUE KEY `hour_room` (`hour_bucket`, `room`(100))) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "stats_daily` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `day_bucket` date NOT NULL, `room` varchar(255) NOT NULL, `avg_users` int(11) NOT NULL DEFAULT 0, `peak_users` int(11) NOT NULL DEFAULT 0, `messages` int(11) NOT NULL DEFAULT 0, `sample_count` bigint(20) NOT NULL DEFAULT 0, `user_count_total` bigint(20) NOT NULL DEFAULT 0, PRIMARY KEY (`id`), UNIQUE KEY `day_room` (`day_bucket`, `room`(100))) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "stats_monthly` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `month_bucket` varchar(7) NOT NULL, `room` varchar(255) NOT NULL, `avg_users` int(11) NOT NULL DEFAULT 0, `peak_users` int(11) NOT NULL DEFAULT 0, `messages` int(11) NOT NULL DEFAULT 0, `sample_count` bigint(20) NOT NULL DEFAULT 0, `user_count_total` bigint(20) NOT NULL DEFAULT 0, PRIMARY KEY (`id`), UNIQUE KEY `month_room` (`month_bucket`, `room`(100))) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "user_relationships` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `user1` varchar(255) NOT NULL, `user2` varchar(255) NOT NULL, `interaction_count` int(11) NOT NULL DEFAULT 0, `last_interaction` bigint(20) NOT NULL DEFAULT 0, PRIMARY KEY (`id`), UNIQUE KEY `user_pair` (`user1`(100), `user2`(100))) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.close();
            out.printf("* Database tables created successfully.\r\n");
        } catch (SQLException | ClassNotFoundException | IOException e) {
            fatalError(e);
        }
    }

    private void createFirstUser(SqlDialect.Type type, String dbHost, String dbPort, String dbName, String dbUser,
                                 String dbPassword, String sqlitePath, String dbPrefix,
                                 String nick, String password, String mail) {
        try (var con = openDatabaseConnection(type, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath)) {
            var timestamp = System.currentTimeMillis();
            var salt = password + "$" + timestamp;
            var sql = "INSERT INTO `" + dbPrefix + "users` (`sex`, `nick`, `nick2`, `mail`, `color`, `pwd`, `pwd2`, `reminder`, `answer`, `timestamp_reg`, `timestamp_login`, `bday_day`, `bday_month`, `bday_year`, `status`, `moderator`) VALUES (?, ?, ?, ?, ?, SHA2(?,512), SHA2(?,512), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (var statement = con.prepareStatement(sql)) {
                statement.setString(1, "m");
                statement.setString(2, nick.toLowerCase());
                statement.setString(3, nick);
                statement.setString(4, mail);
                statement.setString(5, "000000");
                statement.setString(6, salt);
                statement.setString(7, salt);
                statement.setString(8, "security_question");
                statement.setString(9, "security_answer");
                statement.setLong(10, timestamp);
                statement.setLong(11, timestamp);
                statement.setString(12, "1");
                statement.setString(13, "1");
                statement.setString(14, "1970");
                statement.setInt(15, 10);
                statement.setInt(16, 1);
                statement.executeUpdate();
            }
            out.printf("* First user created successfully.\r\n");
        } catch (SQLException | ClassNotFoundException | IOException e) {
            fatalError(e);
        }
    }

    private void createDefaultRoom(SqlDialect.Type type, String dbHost, String dbPort, String dbName, String dbUser,
                                   String dbPassword, String sqlitePath, String dbPrefix,
                                   String roomName, String topic, int locked, String lockReason, int standard, int allowSmilies, int chatNapping) {
        try (var con = openDatabaseConnection(type, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath)) {
            var sql = "INSERT INTO `" + dbPrefix + "roomcfg` (`room`, `topic`, `tar`, `locked`, `lock_reason`, `standard`, `allow_smilies`, `chat_napping`, `first_bgcolor`, `second_bgcolor`, `bordercolor`, `textcolor`, `linkcolor`, `page_title`, `mail`, `owner`, `su`) VALUES (?, ?, 0, ?, ?, ?, ?, ?, NULL, NULL, NULL, NULL, NULL, '', NULL, NULL, '')";
            try (var statement = con.prepareStatement(sql)) {
                statement.setString(1, roomName);
                statement.setString(2, topic);
                statement.setInt(3, locked);
                statement.setString(4, lockReason);
                statement.setInt(5, standard);
                statement.setInt(6, allowSmilies);
                statement.setInt(7, chatNapping);
                statement.executeUpdate();
            }
            out.printf("* Default room '%s' created successfully.\r\n", roomName);
        } catch (SQLException | ClassNotFoundException | IOException e) {
            logError(e);
        }
    }

    private void createDefaultRooms(SqlDialect.Type type, String dbHost, String dbPort, String dbName, String dbUser,
                                    String dbPassword, String sqlitePath, String dbPrefix) {
        createDefaultRoom(type, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath, dbPrefix, "Development", "", 0, "", 1, 1, 0);
        createDefaultRoom(type, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath, dbPrefix, "Exil", "Hier landet der Müll des Chats ;)", 0, "", 1, 0, 0);
        createDefaultRoom(type, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath, dbPrefix, "Lounge", "❤️❤️❤️ Herzlich Willkommen im Chat ❤️❤️❤️", 0, "", 1, 1, 0);
        createDefaultRoom(type, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath, dbPrefix, "Staff-Lounge", "Nur Staff-Mitglieder!", 1, "Nur Staff-Mitglieder können diesen Raum betreten...", 1, 1, 0);
    }

    private void saveConfigFile(List<String> lines) {
        try {
            var configFile = new File(getConfigDir() + getProperty("file.separator") + "config.json");
            var writer = new FileWriter(configFile, StandardCharsets.UTF_8);
            for (var line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            fatalError(e);
        }
    }

    private void saveMailFile(String smtpHost, String smtpPort, String smtpUser, String smtpPassword, String smtpFrom,
                              String smtpAuth, String smtpStarttls, String smtpSslFactory) {
        var lines = new ArrayList<String>();
        lines.add("[");
        lines.add("{\"name\":\"mail.smtp.host\",\"value\":\"" + escapeJson(smtpHost) + "\",\"description\":\"SMTP host\"},");
        lines.add("{\"name\":\"mail.smtp.port\",\"value\":\"" + escapeJson(smtpPort) + "\",\"description\":\"SMTP port\"},");
        lines.add("{\"name\":\"mail.smtp.auth\",\"value\":\"" + escapeJson(smtpAuth) + "\",\"description\":\"SMTP authentication\"},");
        lines.add("{\"name\":\"mail.smtp.starttls.enable\",\"value\":\"" + escapeJson(smtpStarttls) + "\",\"description\":\"Enable STARTTLS\"},");
        lines.add("{\"name\":\"mail.smtp.socketFactory.class\",\"value\":\"" + escapeJson(smtpSslFactory) + "\",\"description\":\"SSL socket factory\"},");
        lines.add("{\"name\":\"mail.smtp.socketFactory.port\",\"value\":\"" + escapeJson(smtpPort) + "\",\"description\":\"SSL port\"},");
        lines.add("{\"name\":\"username\",\"value\":\"" + escapeJson(smtpUser) + "\",\"description\":\"SMTP username\"},");
        lines.add("{\"name\":\"password\",\"value\":\"" + escapeJson(smtpPassword) + "\",\"description\":\"SMTP password\"},");
        lines.add("{\"name\":\"from-mail-address\",\"value\":\"" + escapeJson(smtpFrom) + "\",\"description\":\"Sender address\"},");
        lines.add("{\"name\":\"use_auth\",\"value\":\"" + escapeJson(smtpAuth) + "\",\"description\":\"Use authentication\"}");
        lines.add("]");
        try {
            var mailFile = new File(getConfigDir() + getProperty("file.separator") + "mail.json");
            var writer = new FileWriter(mailFile, StandardCharsets.UTF_8);
            for (var line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            fatalError(e);
        }
    }

    private List<String> buildConfigJson(SqlDialect.Type dbType, String sqlitePath,
                                         String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String dbPrefix,
                                         String smtpHost, String smtpPort, String smtpUser, String smtpPassword, String smtpFrom,
                                         String smtpAuth, String smtpStarttls, String smtpSslFactory,
                                         String adminUser, String adminPassword, String adminRealm,
                                         String charset, String timeZone, String skin, String room) {
        var lines = new ArrayList<String>();
        lines.add("[");
        lines.add("{\"name\":\"sql.type\",\"value\":\"" + dbType.name().toLowerCase(java.util.Locale.ROOT) + "\",\"description\":\"Database type\"},");
        lines.add("{\"name\":\"sql.path\",\"value\":\"" + escapeJson(sqlitePath) + "\",\"description\":\"SQLite database file\"},");
        lines.add("{\"name\":\"sql.host\",\"value\":\"" + escapeJson(dbHost + ":" + dbPort) + "\",\"description\":\"Database host\"},");
        lines.add("{\"name\":\"sql.db\",\"value\":\"" + escapeJson(dbName) + "\",\"description\":\"Database name\"},");
        lines.add("{\"name\":\"sql.user\",\"value\":\"" + escapeJson(dbUser) + "\",\"description\":\"Database user\"},");
        lines.add("{\"name\":\"sql.pw\",\"value\":\"" + escapeJson(dbPassword) + "\",\"description\":\"Database password\"},");
        lines.add("{\"name\":\"sql.prefix\",\"value\":\"" + escapeJson(dbPrefix) + "\",\"description\":\"SQL table prefix\"},");
        lines.add("{\"name\":\"time_zone\",\"value\":\"" + escapeJson(timeZone) + "\",\"description\":\"Timezone\"},");
        lines.add("{\"name\":\"charset\",\"value\":\"" + escapeJson(charset) + "\",\"description\":\"Default charset\"},");
        lines.add("{\"name\":\"default_skin\",\"value\":\"" + escapeJson(skin) + "\",\"description\":\"Default skin\"},");
        lines.add("{\"name\":\"default_room\",\"value\":\"" + escapeJson(room) + "\",\"description\":\"Default room\"},");
        lines.add("{\"name\":\"admin_username\",\"value\":\"" + escapeJson(adminUser) + "\",\"description\":\"Admin console username\"},");
        lines.add("{\"name\":\"admin_password\",\"value\":\"" + escapeJson(adminPassword) + "\",\"description\":\"Admin console password\"},");
        lines.add("{\"name\":\"admin_realm\",\"value\":\"" + escapeJson(adminRealm) + "\",\"description\":\"Admin console realm\"},");
        lines.add("{\"name\":\"mail.smtp.host\",\"value\":\"" + escapeJson(smtpHost) + "\",\"description\":\"SMTP host\"},");
        lines.add("{\"name\":\"mail.smtp.port\",\"value\":\"" + escapeJson(smtpPort) + "\",\"description\":\"SMTP port\"},");
        lines.add("{\"name\":\"mail.smtp.auth\",\"value\":\"" + escapeJson(smtpAuth) + "\",\"description\":\"SMTP authentication\"},");
        lines.add("{\"name\":\"mail.smtp.starttls.enable\",\"value\":\"" + escapeJson(smtpStarttls) + "\",\"description\":\"Enable STARTTLS\"},");
        lines.add("{\"name\":\"mail.smtp.socketFactory.class\",\"value\":\"" + escapeJson(smtpSslFactory) + "\",\"description\":\"SSL socket factory\"},");
        lines.add("{\"name\":\"mail.smtp.socketFactory.port\",\"value\":\"" + escapeJson(smtpPort) + "\",\"description\":\"SSL port\"},");
        lines.add("{\"name\":\"username\",\"value\":\"" + escapeJson(smtpUser) + "\",\"description\":\"SMTP username\"},");
        lines.add("{\"name\":\"password\",\"value\":\"" + escapeJson(smtpPassword) + "\",\"description\":\"SMTP password\"},");
        lines.add("{\"name\":\"from-mail-address\",\"value\":\"" + escapeJson(smtpFrom) + "\",\"description\":\"Sender address\"},");
        lines.add("{\"name\":\"use_auth\",\"value\":\"true\",\"description\":\"Use authentication\"},");
        lines.add("{\"name\":\"guest\",\"value\":\"1\",\"description\":\"Enable guests\"},");
        lines.add("{\"name\":\"guest_prefix\",\"value\":\"Gast\",\"description\":\"Guest prefix\"},");
        lines.add("{\"name\":\"only_registered_users\",\"value\":\"0\",\"description\":\"Registered users only\"},");
        lines.add("{\"name\":\"only_standard_rooms\",\"value\":\"0\",\"description\":\"Standard rooms only\"},");
        lines.add("{\"name\":\"min_nick_length\",\"value\":\"2\",\"description\":\"Minimum nick length\"},");
        lines.add("{\"name\":\"max_nick_length\",\"value\":\"20\",\"description\":\"Maximum nick length\"},");
        lines.add("{\"name\":\"min_pwd_length\",\"value\":\"4\",\"description\":\"Minimum password length\"},");
        lines.add("{\"name\":\"max_pwd_length\",\"value\":\"20\",\"description\":\"Maximum password length\"},");
        lines.add("{\"name\":\"max_text_length\",\"value\":\"1024\",\"description\":\"Maximum text length in chat\"},");
        lines.add("{\"name\":\"allowed_chars\",\"value\":\"[\\\\w\\\\d_\\\\-äöüÄÖÜß@]*\",\"description\":\"Allowed characters for users\"},");
        lines.add("{\"name\":\"salt\",\"value\":\"1\",\"description\":\"Salt passwords\"},");
        lines.add("{\"name\":\"encrypt_pwd\",\"value\":\"SHA2(?,512)\",\"description\":\"Default password encryption\"},");
        lines.add("{\"name\":\"resolve_ip\",\"value\":\"1\",\"description\":\"Resolve IPs\"},");
        lines.add("{\"name\":\"use_proxy\",\"value\":\"1\",\"description\":\"Use reverse proxy\"},");
        lines.add("{\"name\":\"real_ip\",\"value\":\"x-real-ip-submitter\",\"description\":\"Real IP with reverse proxies\"},");
        lines.add("{\"name\":\"cloudflare\",\"value\":\"0\",\"description\":\"Resolve Cloudflare hostnames\"},");
        lines.add("{\"name\":\"status_admin\",\"value\":\"10\",\"description\":\"Minimum status for admins\"},");
        lines.add("{\"name\":\"status_staff\",\"value\":\"4\",\"description\":\"Minimum status for VIPs\"},");
        lines.add("{\"name\":\"ignore_ban_status\",\"value\":\"4\",\"description\":\"Minimum permission to bypass bans\"},");
        lines.add("{\"name\":\"lock_status\",\"value\":\"4\",\"description\":\"Minimum permission to distribute superuser rights in rooms\"},");
        lines.add("{\"name\":\"pool_max\",\"value\":\"10000\",\"description\":\"Maximum pool size\"},");
        lines.add("{\"name\":\"pool_min\",\"value\":\"64\",\"description\":\"Minimum pool size\"},");
        lines.add("{\"name\":\"ping\",\"value\":\"30\",\"description\":\"Ping interval\"},");
        lines.add("{\"name\":\"timeout\",\"value\":\"75\",\"description\":\"Timeout on connection problems\"},");
        lines.add("{\"name\":\"timeout_community\",\"value\":\"300\",\"description\":\"Timeout on connection problems\"},");
        lines.add("{\"name\":\"socket_timeout\",\"value\":\"300000000\",\"description\":\"Socket timeout\"},");
        lines.add("{\"name\":\"default_color\",\"value\":\"000000\",\"description\":\"Text color\"},");
        lines.add("{\"name\":\"random_color\",\"value\":\"1\",\"description\":\"Random colors for guests\"},");
        lines.add("{\"name\":\"min_age\",\"value\":\"12\",\"description\":\"Minimum age\"},");
        lines.add("{\"name\":\"max_age\",\"value\":\"150\",\"description\":\"Maximum age\"},");
        lines.add("{\"name\":\"session_cookie\",\"value\":\"hwebchat_session\",\"description\":\"Session cookie name\"},");
        lines.add("{\"name\":\"use_cookies\",\"value\":\"1\",\"description\":\"Enable cookies\"},");
        lines.add("{\"name\":\"server_port\",\"value\":\"8080\",\"description\":\"Server port\"},");
        lines.add("{\"name\":\"board_pages\",\"value\":\"10\",\"description\":\"Posts per forum page\"},");
        lines.add("{\"name\":\"board_re\",\"value\":\"Re: \",\"description\":\"Reply prefix for forum\"},");
        lines.add("{\"name\":\"emots_per_row\",\"value\":\"10\",\"description\":\"Emoticons per row\"},");
        lines.add("{\"name\":\"dice_max\",\"value\":\"1000\",\"description\":\"Die maximale Anzahl der Augen beim Würfeln\"},");
        lines.add("{\"name\":\"max_ban_duration\",\"value\":\"31337\",\"description\":\"Die maximal Dauer eines Bans in Sekunden\"},");
        lines.add("{\"name\":\"kick_room\",\"value\":\"Exil\",\"description\":\"Der Standardraum in den man reingekickt wird\"},");
        lines.add("{\"name\":\"default_catch_room\",\"value\":\"Staff-Lounge\",\"description\":\"Der Raum der Vips\"},");
        lines.add("{\"name\":\"default_ban_reason_board\",\"value\":\"Gebannt durch Fehlverhalten im Forum!\",\"description\":\"Der Standardbangrund\"},");
        lines.add("{\"name\":\"flood_max_lines_per_interval\",\"value\":\"5\",\"description\":\"Die maximale Anzahl der geposteten Zeilen innerhalb des Intervalls\"},");
        lines.add("{\"name\":\"flood_max_repeat\",\"value\":\"3\",\"description\":\"Die maximale Anzahl der Wiederholungen bevor man die Wiederholung blockiert\"},");
        lines.add("{\"name\":\"flood_timer_interval\",\"value\":\"2\",\"description\":\"Der Flootimerintervall\"},");
        lines.add("{\"name\":\"bright_0\",\"value\":\"?\",\"description\":\"Zu helle Farben blockeren, ? blockiert keine und von 0-f werden Farben blockiert (Farbe 1)\"},");
        lines.add("{\"name\":\"bright_1\",\"value\":\"?\",\"description\":\"Zu helle Farben blockeren, ? blockiert keine und von 0-f werden Farben blockiert! (Farbe 2)\"},");
        lines.add("{\"name\":\"bright_2\",\"value\":\"?\",\"description\":\"Zu helle Farben blockeren, ? blockiert keine und von 0-f werden Farben blockiert! (Farbe 3)\"},");
        lines.add("{\"name\":\"bright_3\",\"value\":\"?\",\"description\":\"Zu helle Farben blockeren, ? blockiert keine und von 0-f werden Farben blockiert! (Farbe 4)\"},");
        lines.add("{\"name\":\"bright_4\",\"value\":\"?\",\"description\":\"Zu helle Farben blockeren, ? blockiert keine und von 0-f werden Farben blockiert! (Farbe 5)\"},");
        lines.add("{\"name\":\"bright_5\",\"value\":\"?\",\"description\":\"Zu helle Farben blockeren, ? blockiert keine und von 0-f werden Farben blockiert! (Farbe 6)\"},");
        lines.add("{\"name\":\"captcha_width\",\"value\":\"255\",\"description\":\"Die Captcha Breite\"},");
        lines.add("{\"name\":\"captcha_height\",\"value\":\"30\",\"description\":\"Die Captcha Höhe\"},");
        lines.add("{\"name\":\"captcha_font_size\",\"value\":\"18\",\"description\":\"Die Captcha ZeicheGröße\"},");
        lines.add("{\"name\":\"captcha_font_type\",\"value\":\"Default\",\"description\":\"Die Captcha Schriftart\"},");
        lines.add("{\"name\":\"captcha_chars\",\"value\":\"abcdefhjkmnpqrstuvwxy23456789\",\"description\":\"Die Captcha Buchstaben die verwendet werden\"},");
        lines.add("{\"name\":\"captcha_chars_to_print\",\"value\":\"8\",\"description\":\"Die Anzahl der Captchatzeichen\"},");
        lines.add("{\"name\":\"captcha_circles_to_draw\",\"value\":\"20\",\"description\":\"Die Anzahl der Kreise im Captcha\"},");
        lines.add("{\"name\":\"captcha_bgcolor_rr\",\"value\":\"255\",\"description\":\"Die Captcha Hintergrundfarbe (RR in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_bgcolor_gg\",\"value\":\"255\",\"description\":\"Die Captcha Hintergrundfarbe (GG in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_bgcolor_bb\",\"value\":\"255\",\"description\":\"Die Captcha Hintergrundfarbe (BB in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_txcolor_rr\",\"value\":\"180\",\"description\":\"Die Captcha Textfarbe (RR in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_txcolor_gg\",\"value\":\"180\",\"description\":\"Die Captcha Textfarbe (GG in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_txcolor_bb\",\"value\":\"180\",\"description\":\"Die Captcha Textfarbe (BB in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_bdcolor_rr\",\"value\":\"255\",\"description\":\"Die Captcha Rahmenfarbe (RR in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_bdcolor_gg\",\"value\":\"255\",\"description\":\"Die Captcha Rahmenfarbe (GG in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_bdcolor_bb\",\"value\":\"255\",\"description\":\"Die Captcha Rahmenfarbe (BB in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_cicolor_rr\",\"value\":\"200\",\"description\":\"Die Captcha Kreisfarbe (RR in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_cicolor_gg\",\"value\":\"200\",\"description\":\"Die Captcha Kreisfarbe (GG in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_cicolor_bb\",\"value\":\"200\",\"description\":\"Die Captcha Kreisfarbe (BB in dezimalen Farben)\"},");
        lines.add("{\"name\":\"captcha_noise_lines\",\"value\":\"5\",\"description\":\"Die Anzahl der Captcha-St&ouml;rlinien\"},");
        lines.add("{\"name\":\"captcha_noise_dots\",\"value\":\"80\",\"description\":\"Die Anzahl der Captcha-St&ouml;rpunkte\"},");
        lines.add("{\"name\":\"captcha_text_shadow\",\"value\":\"2\",\"description\":\"Die Captcha-Textschattentiefe\"},");
        lines.add("{\"name\":\"toplist_limit\",\"value\":\"100\",\"description\":\"Die maximale Anzahl der Chatter in der Topliste\"},");
        lines.add("{\"name\":\"guestbook_welcome\",\"value\":\"Willkommen im Gästebuch! Hier kannst du dich für den Chatter eintragen und Nachrichten hinterlassen.\",\"description\":\"Die Willkommensnachricht im Gästebuch\"}");
        lines.add("]");
        return lines;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Performs the setup with the submitted form values.
     *
     * @param request the request with the form values
     * @return an error message to show next to the form, or
     * <code>null</code> if the setup succeeded
     * @throws IOException if the default files cannot be copied
     * @throws IllegalArgumentException if a value is not acceptable at all
     */
    public String run(HttpServletRequest request) throws IOException {
        var dbTypeValue = request.getParameter("db_type");
        SqlDialect.Type dbType;
        try {
            dbType = SqlDialect.Type.fromConfig(dbTypeValue);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported database type.", e);
        }
        var sqlitePath = request.getParameter("sqlite_path");
        if (sqlitePath == null || sqlitePath.isBlank()) {
            sqlitePath = getHomeWebCom() + getProperty("file.separator") + "data"
                    + getProperty("file.separator") + "hwebchat.sqlite";
        }
        var dbHost = request.getParameter("db_host");
        var dbPort = request.getParameter("db_port");
        var dbName = request.getParameter("db_name");
        var dbUser = request.getParameter("db_user");
        var dbPassword = request.getParameter("db_password");
        var dbPrefix = request.getParameter("db_prefix");
        if (dbPrefix == null) {
            dbPrefix = "";
        }
        if (!dbPrefix.matches("[A-Za-z0-9_]{0,20}")) {
            throw new IllegalArgumentException("The database prefix may contain only letters, numbers, and underscores (up to 20 characters).");
        }
        var smtpHost = request.getParameter("smtp_host");
        var smtpPort = request.getParameter("smtp_port");
        var smtpUser = request.getParameter("smtp_user");
        var smtpPassword = request.getParameter("smtp_password");
        var smtpFrom = request.getParameter("smtp_from");
        var smtpAuth = request.getParameter("smtp_auth");
        var smtpStarttls = request.getParameter("smtp_starttls");
        var smtpSslFactory = request.getParameter("smtp_ssl_factory");
        var adminUser = request.getParameter("admin_user");
        var adminPassword = request.getParameter("admin_password");
        var adminRealm = request.getParameter("admin_realm");
        var firstUserNick = request.getParameter("first_user_nick");
        var firstUserPassword = request.getParameter("first_user_password");
        var firstUserEmail = request.getParameter("first_user_email");
        var charset = request.getParameter("charset");
        var timeZone = request.getParameter("timezone");
        var skin = request.getParameter("skin");
        var room = request.getParameter("room");

        if ((dbType != SqlDialect.Type.SQLITE && (dbHost == null || dbHost.isEmpty() || dbPort == null || dbPort.isEmpty() || dbName == null || dbName.isEmpty() ||
            dbUser == null || dbUser.isEmpty() || dbPassword == null || dbPassword.isEmpty())) ||
            smtpHost == null || smtpHost.isEmpty() || smtpPort == null || smtpPort.isEmpty() || smtpFrom == null || smtpFrom.isEmpty() ||
            adminUser == null || adminUser.isEmpty() || adminPassword == null || adminPassword.isEmpty() ||
            firstUserNick == null || firstUserNick.isEmpty() || firstUserPassword == null || firstUserPassword.isEmpty() || firstUserEmail == null || firstUserEmail.isEmpty()) {
            return "Please fill in all required fields.";
        }

        if (!testDatabaseConnection(dbType, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath)) {
            return "Database connection failed. Please check your credentials.";
        }

        var homeWebCom = getHomeWebCom();
        var baseDir = new File(homeWebCom);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        copyServletResources("/default-homewebcom", baseDir);

        createDatabaseTables(dbType, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath, dbPrefix);
        createFirstUser(dbType, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath, dbPrefix, firstUserNick, firstUserPassword, firstUserEmail);
        createDefaultRooms(dbType, dbHost, dbPort, dbName, dbUser, dbPassword, sqlitePath, dbPrefix);

        var configJson = buildConfigJson(
            dbType, sqlitePath, dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix.isEmpty() ? "hwc_" : dbPrefix,
            smtpHost, smtpPort, smtpUser, smtpPassword, smtpFrom,
            smtpAuth != null ? smtpAuth : "true", smtpStarttls != null ? smtpStarttls : "true", smtpSslFactory != null ? smtpSslFactory : "javax.net.ssl.SSLSocketFactory",
            adminUser, adminPassword, adminRealm != null ? adminRealm : "Console",
            charset != null ? charset : "UTF-8",
            timeZone != null ? timeZone : "CET",
            skin != null ? skin : "native",
            room != null ? room : "Lobby"
        );
        saveConfigFile(configJson);
        saveMailFile(smtpHost, smtpPort, smtpUser, smtpPassword, smtpFrom,
                     smtpAuth != null ? smtpAuth : "true", smtpStarttls != null ? smtpStarttls : "true", smtpSslFactory != null ? smtpSslFactory : "javax.net.ssl.SSLSocketFactory");
        try {
            SendMail.sendTestEmail(smtpHost, smtpPort, smtpUser, smtpPassword, smtpFrom,
                                   smtpAuth != null ? smtpAuth : "true", smtpStarttls != null ? smtpStarttls : "true", smtpSslFactory != null ? smtpSslFactory : "javax.net.ssl.SSLSocketFactory",
                                   firstUserEmail);
        } catch (MessagingException e) {
            return "SMTP test failed: " + e.getMessage();
        }
        markSetupComplete();
        if (Bootstrap.boot != null) {
            Bootstrap.boot.setSetupNeeded(false);
            try {
                Bootstrap.boot.setConfig(new Config(Bootstrap.boot));
                Bootstrap.boot.setChatServices(new ChatServices(Bootstrap.boot));
                Bootstrap.boot.setUtil(new Util(Bootstrap.boot));
                Bootstrap.boot.setChatManager(new ChatManager(Bootstrap.boot));
                Bootstrap.boot.setCommands(new Commands(Bootstrap.boot));
                Bootstrap.boot.setCaptcha(new Captcha(Bootstrap.boot));
                Bootstrap.boot.setSendMail(new SendMail(Bootstrap.boot));
            } catch (Exception e) {
                logError(e);
            }
        }

        return null;
    }
}
