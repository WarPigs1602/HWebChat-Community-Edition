package net.midiandmore.chat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static net.midiandmore.chat.Bootstrap.boot;
import static net.midiandmore.chat.Bootstrap.fatalError;
import static net.midiandmore.chat.Bootstrap.logError;
import static java.sql.DriverManager.getConnection;

public class SetupServlet extends HttpServlet {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException cnfe) {
            err.printf("MySQL driver not found: %s\r\n", cnfe.getMessage());
        }
    }

    private String getHomeWebCom() {
        return getProperty("user.home") + getProperty("file.separator") + ".homewebcom";
    }

    private String getConfigDir() {
        return getHomeWebCom() + getProperty("file.separator") + "config";
    }

    private boolean isSetupComplete() {
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
        var context = getServletContext();
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

    private String readResource(String path) {
        try {
            var context = getServletContext();
            var is = context.getResourceAsStream(path);
            if (is == null) {
                return "";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logError(e);
            return "";
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        var result = value;
        result = result.replace("&", "&");
        result = result.replace("<", "<");
        result = result.replace(">", ">");
        result = result.replace("\"", "" + "");
        result = result.replace("'", "'");
        return result;
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

    private void render(HttpServletRequest request, HttpServletResponse response, String title, String body) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        var pw = response.getWriter();
        pw.print("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>");
        pw.print(escape(title));
        pw.print("</title><style>");
        pw.print("*{box-sizing:border-box;margin:0;padding:0;}");
        pw.print("body{font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;background:linear-gradient(135deg,#1a1a2e 0%,#16213e 50%,#0f3460 100%);min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;}");
        pw.print(".setup-container{background:rgba(255,255,255,0.95);border-radius:20px;box-shadow:0 25px 80px rgba(0,0,0,0.4);max-width:900px;width:100%;overflow:hidden;}");
        pw.print(".setup-header{background:linear-gradient(135deg,#e94560 0%,#c23152 100%);color:white;padding:40px;text-align:center;}");
        pw.print(".setup-header h1{font-size:2.5em;margin-bottom:10px;text-shadow:2px 2px 4px rgba(0,0,0,0.3);}");
        pw.print(".setup-header p{opacity:0.9;font-size:1.1em;}");
        pw.print(".setup-body{padding:40px;}");
        pw.print(".error-message{background:#ffe6e6;color:#c23152;padding:15px 20px;border-radius:10px;margin-bottom:25px;border-left:4px solid #e94560;}");
        pw.print(".section{margin-bottom:35px;}");
        pw.print(".section-title{font-size:1.3em;color:#16213e;margin-bottom:20px;padding-bottom:10px;border-bottom:2px solid #e94560;display:flex;align-items:center;gap:10px;}");
        pw.print(".section-title .icon{font-size:1.5em;}");
        pw.print(".form-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px;}");
        pw.print("@media(max-width:600px){.form-grid{grid-template-columns:1fr;}}");
        pw.print(".form-group{display:flex;flex-direction:column;}");
        pw.print(".form-group label{font-weight:600;color:#333;margin-bottom:6px;font-size:0.95em;}");
        pw.print(".form-group input,.form-group select{padding:12px 15px;border:2px solid #ddd;border-radius:8px;font-size:1em;transition:border-color 0.3s;}");
        pw.print(".form-group input:focus,.form-group select:focus{outline:none;border-color:#e94560;}");
        pw.print(".form-group .hint{font-size:0.8em;color:#888;margin-top:4px;}");
        pw.print(".btn-submit{background:linear-gradient(135deg,#e94560 0%,#c23152 100%);color:white;border:none;padding:15px 40px;font-size:1.1em;border-radius:10px;cursor:pointer;font-weight:600;display:block;margin:30px auto 0;transition:transform 0.2s,box-shadow 0.2s;}");
        pw.print(".btn-submit:hover{transform:translateY(-2px);box-shadow:0 8px 25px rgba(233,69,96,0.4);}");
        pw.print(".footer{text-align:center;padding:20px;color:#666;font-size:0.9em;border-top:1px solid #eee;}");
        pw.print("</style></head><body>");
        pw.print("<div class=\"setup-container\"><div class=\"setup-header\"><h1>⚙️ HWebChat Setup</h1><p>Welcome to the automatic configuration of your chat server</p></div>");
        pw.print("<div class=\"setup-body\">");
        pw.print(body);
        pw.print("</div>");
        pw.print("<div class=\"footer\">HWebChat Community Edition | (c) 2005-2026 by Andreas Pschorn</div>");
        pw.print("</div></body></html>");
    }

    private String renderSetupForm(String error,
                                   String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String dbPrefix,
                                   String smtpHost, String smtpPort, String smtpUser, String smtpPassword, String smtpFrom,
                                   String smtpAuth, String smtpStarttls, String smtpSslFactory,
                                   String adminUser, String adminPassword, String adminRealm,
                                   String firstUserNick, String firstUserPassword, String firstUserEmail,
                                   String charset, String timeZone, String skin, String room) {
        var sb = new StringBuilder();
        if (error != null && !error.isEmpty()) {
            sb.append("<div class=\"error-message\">⚠️ ").append(escape(error)).append("</div>");
        }
        sb.append("<form method=\"POST\" action=\"\">");
        sb.append("<div class=\"section\"><div class=\"section-title\"><span class=\"icon\">🗄️</span> Database</div><div class=\"form-grid\">");
        sb.append("<div class=\"form-group\"><label for=\"db_host\">Database host *</label><input type=\"text\" id=\"db_host\" name=\"db_host\" value=\"").append(escape(dbHost)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"db_port\">Database port *</label><input type=\"text\" id=\"db_port\" name=\"db_port\" value=\"").append(escape(dbPort)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"db_name\">Database name *</label><input type=\"text\" id=\"db_name\" name=\"db_name\" value=\"").append(escape(dbName)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"db_user\">Database user *</label><input type=\"text\" id=\"db_user\" name=\"db_user\" value=\"").append(escape(dbUser)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"db_password\">Database password *</label><input type=\"password\" id=\"db_password\" name=\"db_password\" value=\"").append(escape(dbPassword)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"db_prefix\">Table prefix</label><input type=\"text\" id=\"db_prefix\" name=\"db_prefix\" value=\"").append(escape(dbPrefix)).append("\"></div>");
        sb.append("</div></div>");
        sb.append("<div class=\"section\"><div class=\"section-title\"><span class=\"icon\">📧</span> E-Mail / SMTP</div><div class=\"form-grid\">");
        sb.append("<div class=\"form-group\"><label for=\"smtp_host\">SMTP host *</label><input type=\"text\" id=\"smtp_host\" name=\"smtp_host\" value=\"").append(escape(smtpHost)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"smtp_port\">SMTP port *</label><input type=\"text\" id=\"smtp_port\" name=\"smtp_port\" value=\"").append(escape(smtpPort)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"smtp_user\">SMTP user</label><input type=\"text\" id=\"smtp_user\" name=\"smtp_user\" value=\"").append(escape(smtpUser)).append("\"></div>");
        sb.append("<div class=\"form-group\"><label for=\"smtp_password\">SMTP password</label><input type=\"password\" id=\"smtp_password\" name=\"smtp_password\" value=\"").append(escape(smtpPassword)).append("\"></div>");
        sb.append("<div class=\"form-group\"><label for=\"smtp_from\">Sender address *</label><input type=\"email\" id=\"smtp_from\" name=\"smtp_from\" value=\"").append(escape(smtpFrom)).append("\" required></div>");
        sb.append("</div></div>");
        sb.append("<div class=\"section\"><div class=\"section-title\"><span class=\"icon\">⚙️</span> SMTP options</div><div class=\"form-grid\">");
        sb.append("<div class=\"form-group\"><label for=\"smtp_auth\">Authentication</label><select id=\"smtp_auth\" name=\"smtp_auth\"><option value=\"true\"").append("true".equals(smtpAuth) ? " selected" : "").append(">On</option><option value=\"false\"").append("false".equals(smtpAuth) ? " selected" : "").append(">Off</option></select></div>");
        sb.append("<div class=\"form-group\"><label for=\"smtp_starttls\">STARTTLS</label><select id=\"smtp_starttls\" name=\"smtp_starttls\"><option value=\"true\"").append("true".equals(smtpStarttls) ? " selected" : "").append(">On</option><option value=\"false\"").append("false".equals(smtpStarttls) ? " selected" : "").append(">Off</option></select></div>");
        sb.append("<div class=\"form-group\"><label for=\"smtp_ssl_factory\">SSL socket factory</label><input type=\"text\" id=\"smtp_ssl_factory\" name=\"smtp_ssl_factory\" value=\"").append(escape(smtpSslFactory)).append("\"></div>");
        sb.append("</div></div>");
        sb.append("<div class=\"section\"><div class=\"section-title\"><span class=\"icon\">🔐</span> Administration console</div><div class=\"form-grid\">");
        sb.append("<div class=\"form-group\"><label for=\"admin_user\">Console username *</label><input type=\"text\" id=\"admin_user\" name=\"admin_user\" value=\"").append(escape(adminUser)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"admin_password\">Console password *</label><input type=\"password\" id=\"admin_password\" name=\"admin_password\" value=\"").append(escape(adminPassword)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"admin_realm\">Console realm</label><input type=\"text\" id=\"admin_realm\" name=\"admin_realm\" value=\"").append(escape(adminRealm)).append("\"></div>");
        sb.append("</div></div>");
        sb.append("<div class=\"section\"><div class=\"section-title\"><span class=\"icon\">👤</span> First user</div><div class=\"form-grid\">");
        sb.append("<div class=\"form-group\"><label for=\"first_user_nick\">Username *</label><input type=\"text\" id=\"first_user_nick\" name=\"first_user_nick\" value=\"").append(escape(firstUserNick)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"first_user_password\">Password *</label><input type=\"password\" id=\"first_user_password\" name=\"first_user_password\" value=\"").append(escape(firstUserPassword)).append("\" required></div>");
        sb.append("<div class=\"form-group\"><label for=\"first_user_email\">E-mail *</label><input type=\"email\" id=\"first_user_email\" name=\"first_user_email\" value=\"").append(escape(firstUserEmail)).append("\" required></div>");
        sb.append("</div></div>");
        sb.append("<div class=\"section\"><div class=\"section-title\"><span class=\"icon\">🌐</span> General settings</div><div class=\"form-grid\">");
        sb.append("<div class=\"form-group\"><label for=\"charset\">Charset</label><select id=\"charset\" name=\"charset\"><option value=\"UTF-8\"").append("UTF-8".equals(charset) ? " selected" : "").append(">UTF-8</option><option value=\"ISO-8859-1\"").append("ISO-8859-1".equals(charset) ? " selected" : "").append(">ISO-8859-1</option></select></div>");
        sb.append("<div class=\"form-group\"><label for=\"timezone\">Timezone</label><select id=\"timezone\" name=\"timezone\">");
        sb.append("<option value=\"CET\"").append("CET".equals(timeZone) ? " selected" : "").append(">CET (UTC+1)</option>");
        sb.append("<option value=\"UTC\"").append("UTC".equals(timeZone) ? " selected" : "").append(">UTC (UTC+0)</option>");
        sb.append("<option value=\"EET\"").append("EET".equals(timeZone) ? " selected" : "").append(">EET (UTC+2)</option>");
        sb.append("<option value=\"EST\"").append("EST".equals(timeZone) ? " selected" : "").append(">EST (UTC-5)</option>");
        sb.append("<option value=\"PST\"").append("PST".equals(timeZone) ? " selected" : "").append(">PST (UTC-8)</option>");
        sb.append("</select></div>");
        sb.append("<div class=\"form-group\"><label for=\"skin\">Default skin</label><input type=\"text\" id=\"skin\" name=\"skin\" value=\"").append(escape(skin)).append("\"></div>");
        sb.append("<div class=\"form-group\"><label for=\"room\">Default room</label><input type=\"text\" id=\"room\" name=\"room\" value=\"").append(escape(room)).append("\"></div>");
        sb.append("</div></div>");
        sb.append("<button type=\"submit\" class=\"btn-submit\">🚀 Complete setup and start chat</button>");
        sb.append("</form>");
        return sb.toString();
    }

    private boolean testDatabaseConnection(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword) {
        var url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?user=" + dbUser + "&password=" + dbPassword + "&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=UTC";
        try (var con = getConnection(url)) {
            return con.isValid(1000);
        } catch (SQLException e) {
            out.printf("* Database connection test failed: %s\r\n", e.getMessage());
            return false;
        }
    }

    private void createDatabaseTables(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String dbPrefix) {
        var url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?user=" + dbUser + "&password=" + dbPassword + "&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=UTC";
        try (var con = getConnection(url)) {
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
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "stats` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `ts` bigint(20) NOT NULL, `year` int(11) NOT NULL, `month` int(11) NOT NULL, `day` int(11) NOT NULL, `hour` int(11) NOT NULL, `users` int(11) NOT NULL DEFAULT 0, `rooms` int(11) NOT NULL DEFAULT 0, `peak` int(11) NOT NULL DEFAULT 0, PRIMARY KEY (`id`), KEY `idx_ts` (`ts`), KEY `idx_ymd` (`year`,`month`,`day`), KEY `idx_hour` (`hour`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + dbPrefix + "room_stats` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `room` varchar(255) NOT NULL, `ts` bigint(20) NOT NULL, `users` int(11) NOT NULL DEFAULT 0, `peak` int(11) NOT NULL DEFAULT 0, PRIMARY KEY (`id`), KEY `idx_room` (`room`), KEY `idx_ts` (`ts`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
             stmt.close();
            out.printf("* Database tables created successfully.\r\n");
        } catch (SQLException e) {
            fatalError(e);
        }
    }

    private void createFirstUser(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String dbPrefix,
                                 String nick, String password, String mail) {
        var url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?user=" + dbUser + "&password=" + dbPassword + "&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=UTC";
        try (var con = getConnection(url)) {
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
        } catch (SQLException e) {
            fatalError(e);
        }
    }

    private void createDefaultRoom(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String dbPrefix,
                                   String roomName, String topic, int locked, String lockReason, int standard, int allowSmilies, int chatNapping) {
        var url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?user=" + dbUser + "&password=" + dbPassword + "&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=UTC";
        try (var con = getConnection(url)) {
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
        } catch (SQLException e) {
            logError(e);
        }
    }

    private void createDefaultRooms(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String dbPrefix) {
        createDefaultRoom(dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix, "Development", "", 0, "", 1, 1, 0);
        createDefaultRoom(dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix, "Exil", "Hier landet der Müll des Chats ;)", 0, "", 1, 0, 0);
        createDefaultRoom(dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix, "Lounge", "❤️❤️❤️ Herzlich Willkommen im Chat ❤️❤️❤️", 0, "", 1, 1, 0);
        createDefaultRoom(dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix, "Staff-Lounge", "Nur für Staff-Mitglieder!", 1, "Nur Staff-Mitglieder können diesen Raum betreten...", 1, 1, 0);
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

    private List<String> buildConfigJson(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String dbPrefix,
                                         String smtpHost, String smtpPort, String smtpUser, String smtpPassword, String smtpFrom,
                                         String smtpAuth, String smtpStarttls, String smtpSslFactory,
                                         String adminUser, String adminPassword, String adminRealm,
                                         String charset, String timeZone, String skin, String room) {
        var lines = new ArrayList<String>();
        lines.add("[");
        lines.add("{\"name\":\"sql.host\",\"value\":\"" + escapeJson(dbHost + ":" + dbPort) + "\",\"description\":\"MySQL host\"},");
        lines.add("{\"name\":\"sql.db\",\"value\":\"" + escapeJson(dbName) + "\",\"description\":\"MySQL database\"},");
        lines.add("{\"name\":\"sql.user\",\"value\":\"" + escapeJson(dbUser) + "\",\"description\":\"MySQL user\"},");
        lines.add("{\"name\":\"sql.pw\",\"value\":\"" + escapeJson(dbPassword) + "\",\"description\":\"MySQL password\"},");
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
        lines.add("{\"name\":\"path_console\",\"value\":\"console\",\"description\":\"Console page\"},");
        lines.add("{\"name\":\"path_console_index\",\"value\":\"console\",\"description\":\"Console index page\"},");
        lines.add("{\"name\":\"path_start\",\"value\":\"/\",\"description\":\"Start page path\"},");
        lines.add("{\"name\":\"path_login\",\"value\":\"login\",\"description\":\"Login page\"},");
        lines.add("{\"name\":\"path_login_chat\",\"value\":\"login_chat\",\"description\":\"Login page!\"},");
        lines.add("{\"name\":\"path_output\",\"value\":\"content\",\"description\":\"Output page\"},");
        lines.add("{\"name\":\"path_chat\",\"value\":\"/HWebChat_Community_Edition/Chat\",\"description\":\"Chat WebSocket path\"},");
        lines.add("{\"name\":\"path_reg_form\",\"value\":\"register\",\"description\":\"Registration page\"},");
        lines.add("{\"name\":\"path_hwebchat\",\"value\":\"/HWebChat_Community_Edition/Start\",\"description\":\"Start page path\"},");
        lines.add("{\"name\":\"path_account\",\"value\":\"account\",\"description\":\"Account page\"},");
        lines.add("{\"name\":\"path_account_com\",\"value\":\"account\",\"description\":\"Account Community Page\"},");
        lines.add("{\"name\":\"path_captcha\",\"value\":\"captcha\",\"description\":\"Captcha page\"},");
        lines.add("{\"name\":\"path_help\",\"value\":\"help\",\"description\":\"Help page\"},");
        lines.add("{\"name\":\"path_emot\",\"value\":\"emot\",\"description\":\"Emoticon page\"},");
        lines.add("{\"name\":\"path_image\",\"value\":\"image\",\"description\":\"Image page\"},");
        lines.add("{\"name\":\"path_profile\",\"value\":\"profile\",\"description\":\"Profile page\"},");
        lines.add("{\"name\":\"path_message\",\"value\":\"read_message\",\"description\":\"Message page\"},");
        lines.add("{\"name\":\"path_password\",\"value\":\"password\",\"description\":\"Password page\"},");
        lines.add("{\"name\":\"path_link\",\"value\":\"link\",\"description\":\"Link page\"},");
        lines.add("{\"name\":\"path_file\",\"value\":\"file/\",\"description\":\"Files path\"},");
        lines.add("{\"name\":\"path_memory\",\"value\":\"memory\",\"description\":\"Memory page\"},");
        lines.add("{\"name\":\"path_logout\",\"value\":\"logout\",\"description\":\"Logout page\"},");
        lines.add("{\"name\":\"path_board\",\"value\":\"board\",\"description\":\"Forum\"},");
        lines.add("{\"name\":\"path_webchat\",\"value\":\"webchat_stats\",\"description\":\"API path\"},");
        lines.add("{\"name\":\"path_toplist\",\"value\":\"top\",\"description\":\"Toplist path\"},");
        lines.add("{\"name\":\"path_stats\",\"value\":\"stats\",\"description\":\"Stats page\"},");
        lines.add("{\"name\":\"path_upload\",\"value\":\"/HWebChat_Community_Edition/UploadFile\",\"description\":\"Image upload path\"},");
        lines.add("{\"name\":\"path_webchat\",\"value\":\"webchat_stats\",\"description\":\"API path\"},");
        lines.add("{\"name\":\"path_account\",\"value\":\"account\",\"description\":\"Account page\"},");
        lines.add("{\"name\":\"path_account_com\",\"value\":\"account_com\",\"description\":\"Account Community Page\"},");
        lines.add("{\"name\":\"path_reg_edit\",\"value\":\"reg_edit\",\"description\":\"Account management page\"},");
        lines.add("{\"name\":\"path_privchat\",\"value\":\"privchat\",\"description\":\"Private chat page\"},");
        lines.add("{\"name\":\"path_napping\",\"value\":\"napping\",\"description\":\"Napping page\"},");
        lines.add("{\"name\":\"path_napping_form\",\"value\":\"napping_form\",\"description\":\"Napping form page\"},");
        lines.add("{\"name\":\"path_chat_napping\",\"value\":\"/HWebChat_Community_Edition/Chat\",\"description\":\"Chat WebSocket path\"},");
        lines.add("{\"name\":\"path_chat_start\",\"value\":\"lobby\",\"description\":\"Chat start page\"},");
        lines.add("{\"name\":\"path_login_frame\",\"value\":\"chat\",\"description\":\"Login page\"},");
        lines.add("{\"name\":\"path_logout_com\",\"value\":\"logout_com\",\"description\":\"Logout page\"},");
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

    private void doSetup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var dbHost = request.getParameter("db_host");
        var dbPort = request.getParameter("db_port");
        var dbName = request.getParameter("db_name");
        var dbUser = request.getParameter("db_user");
        var dbPassword = request.getParameter("db_password");
        var dbPrefix = request.getParameter("db_prefix");
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

        if (dbHost == null || dbHost.isEmpty() || dbPort == null || dbPort.isEmpty() || dbName == null || dbName.isEmpty() ||
            dbUser == null || dbUser.isEmpty() || dbPassword == null || dbPassword.isEmpty() ||
            smtpHost == null || smtpHost.isEmpty() || smtpPort == null || smtpPort.isEmpty() || smtpFrom == null || smtpFrom.isEmpty() ||
            adminUser == null || adminUser.isEmpty() || adminPassword == null || adminPassword.isEmpty() ||
            firstUserNick == null || firstUserNick.isEmpty() || firstUserPassword == null || firstUserPassword.isEmpty() || firstUserEmail == null || firstUserEmail.isEmpty()) {
            render(request, response, "Setup", renderSetupForm("Please fill in all required fields.",
                dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix,
                smtpHost, smtpPort, smtpUser, smtpPassword, smtpFrom,
                smtpAuth != null ? smtpAuth : "true", smtpStarttls != null ? smtpStarttls : "true", smtpSslFactory != null ? smtpSslFactory : "javax.net.ssl.SSLSocketFactory",
                adminUser, adminPassword, adminRealm != null ? adminRealm : "Console",
                firstUserNick, firstUserPassword, firstUserEmail,
                charset, timeZone, skin, room));
            return;
        }

        if (!testDatabaseConnection(dbHost, dbPort, dbName, dbUser, dbPassword)) {
            render(request, response, "Setup", renderSetupForm("Database connection failed. Please check your credentials.",
                dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix,
                smtpHost, smtpPort, smtpUser, smtpPassword, smtpFrom,
                smtpAuth != null ? smtpAuth : "true", smtpStarttls != null ? smtpStarttls : "true", smtpSslFactory != null ? smtpSslFactory : "javax.net.ssl.SSLSocketFactory",
                adminUser, adminPassword, adminRealm != null ? adminRealm : "Console",
                firstUserNick, firstUserPassword, firstUserEmail,
                charset, timeZone, skin, room));
            return;
        }

        var homeWebCom = getHomeWebCom();
        var baseDir = new File(homeWebCom);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        copyServletResources("/default-homewebcom", baseDir);

        createDatabaseTables(dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix);
        createFirstUser(dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix, firstUserNick, firstUserPassword, firstUserEmail);
        createDefaultRooms(dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix);

        var configJson = buildConfigJson(
            dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix.isEmpty() ? "hwc_" : dbPrefix,
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
            render(request, response, "Setup", renderSetupForm("SMTP test failed: " + e.getMessage(),
                dbHost, dbPort, dbName, dbUser, dbPassword, dbPrefix,
                smtpHost, smtpPort, smtpUser, smtpPassword, smtpFrom,
                smtpAuth != null ? smtpAuth : "true", smtpStarttls != null ? smtpStarttls : "true", smtpSslFactory != null ? smtpSslFactory : "javax.net.ssl.SSLSocketFactory",
                adminUser, adminPassword, adminRealm != null ? adminRealm : "Console",
                firstUserNick, firstUserPassword, firstUserEmail,
                charset, timeZone, skin, room));
            return;
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

        var contextPath = request.getContextPath();
        var chatUrl = contextPath + "/Start";
        var body = "<h1 style=\"color: #10b981;\">✅ Setup completed successfully!</h1>";
        body += "<p style=\"margin: 20px 0; color: #666;\">The chat server has been configured successfully. Database tables have been created.</p>";
        body += "<p style=\"margin: 20px 0; color: #666;\">The first user <strong>" + escape(firstUserNick) + "</strong> has been created.</p>";
        body += "<p style=\"margin: 20px 0;\"><a href=\"" + chatUrl + "\" style=\"background: #e94560; color: white; padding: 12px 30px; text-decoration: none; border-radius: 8px; font-weight: bold;\">Go to chat →</a></p>";
        render(request, response, "Setup completed", body);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (isSetupComplete()) {
            render(request, response, "Setup already completed", "<h1>Setup already completed</h1><p>Configuration has already been performed. The chat is available at <a href=\"" + request.getContextPath() + "/Start\">" + request.getContextPath() + "/Start</a>.</p>");
            return;
        }
        render(request, response, "Setup", renderSetupForm(null,
            "localhost", "3306", "hwebchat", "user", "password", "hwc_",
            "smtp.example.org", "465", "foo@bar", "password", "foo@bar",
            "true", "true", "javax.net.ssl.SSLSocketFactory",
            "admin", "admin", "Console",
            "", "", "",
            "UTF-8", "CET", "native", "Lobby"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (isSetupComplete()) {
            render(request, response, "Setup already completed", "<h1>Setup already completed</h1><p>Configuration has already been performed.</p>");
            return;
        }
        doSetup(request, response);
    }
}
