package net.midiandmore.chat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Matcher;

final class SqlDialect {
    enum Type {
        MYSQL,
        POSTGRESQL,
        SQLITE;

        static Type fromConfig(String value) {
            if (value == null || value.isBlank()) {
                return SQLITE;
            }
            return switch (value.trim().toLowerCase(Locale.ROOT)) {
                case "mysql", "mariadb" -> MYSQL;
                case "postgres", "postgresql" -> POSTGRESQL;
                case "sqlite" -> SQLITE;
                default -> throw new IllegalArgumentException("Unsupported database type: " + value);
            };
        }

        String driverClass() {
            return switch (this) {
                case MYSQL -> "com.mysql.cj.jdbc.Driver";
                case POSTGRESQL -> "org.postgresql.Driver";
                case SQLITE -> "org.sqlite.JDBC";
            };
        }
    }

    private SqlDialect() {
    }

    static void initialize(Connection connection, Type type) throws SQLException {
        if (type == Type.SQLITE) {
            org.sqlite.Function.create(connection, "SHA2", new org.sqlite.Function() {
                @Override
                protected void xFunc() throws SQLException {
                    var input = value_text(0);
                    if (input == null) {
                        result();
                        return;
                    }
                    var bits = value_int(1);
                    if (bits != 512) {
                        throw new SQLException("Only SHA2 with a 512-bit digest is supported");
                    }
                    try {
                        var digest = MessageDigest.getInstance("SHA-512").digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        var hex = new StringBuilder(digest.length * 2);
                        for (var value : digest) {
                            hex.append(String.format("%02x", value & 0xff));
                        }
                        result(hex.toString());
                    } catch (NoSuchAlgorithmException e) {
                        throw new SQLException("SHA-512 is unavailable", e);
                    }
                }
            });
        }
    }

    static void ensurePostgresqlExtensions(Connection connection, Type type) throws SQLException {
        if (type == Type.POSTGRESQL) {
            try (var statement = connection.createStatement()) {
                statement.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto");
            }
        }
    }

    static Connection wrap(Connection connection, Type type) {
        if (type == Type.MYSQL) {
            return connection;
        }
        return (Connection) Proxy.newProxyInstance(
                SqlDialect.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    try {
                        if ("prepareStatement".equals(method.getName()) && args != null && args.length > 0 && args[0] instanceof String sql) {
                            args[0] = translate(sql, type);
                        } else if ("createStatement".equals(method.getName())) {
                            var statement = method.invoke(connection, args);
                            return wrapStatement(statement, type);
                        }
                        return method.invoke(connection, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    private static Object wrapStatement(Object statement, Type type) {
        return Proxy.newProxyInstance(
                SqlDialect.class.getClassLoader(),
                new Class<?>[]{Statement.class},
                (proxy, method, args) -> {
                    try {
                        if (args != null && args.length > 0 && args[0] instanceof String sql
                                && ("execute".equals(method.getName())
                                || "executeQuery".equals(method.getName())
                                || "executeUpdate".equals(method.getName())
                                || "executeLargeUpdate".equals(method.getName()))) {
                            args[0] = translate(sql, type);
                        }
                        return method.invoke(statement, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    static String translate(String sql, Type type) throws SQLException {
        if (type == Type.MYSQL) {
            return sql;
        }
        var translated = sql.replace('`', '"');
        translated = translated.replaceAll("(?i)\\bUNSIGNED\\b", "");
        translated = translated.replaceAll("(?i)\\bBIGINT\\s*\\(\\s*\\d+\\s*\\)", "BIGINT");
        translated = translated.replaceAll("(?i)\\bINT\\s*\\(\\s*\\d+\\s*\\)", "INTEGER");
        translated = translated.replaceAll("(?i)\\bTINYINT\\s*\\(\\s*\\d+\\s*\\)", "SMALLINT");
        translated = translated.replaceAll("(?i)\\bLONGTEXT\\b", "TEXT");
        translated = translated.replaceAll("(?i)\\bLONGBLOB\\b", type == Type.POSTGRESQL ? "BYTEA" : "BLOB");
        if (type == Type.POSTGRESQL) {
            translated = translated.replaceAll("(?i)\\bDATETIME\\b", "TIMESTAMP");
        }
        translated = translated.replaceAll("(?i)\\bADD\\s+COLUMN\\s+IF\\s+NOT\\s+EXISTS\\b", "ADD COLUMN");
        translated = translated.replaceAll("(?i)\\bMODIFY\\s+COLUMN\\s+IF\\s+EXISTS\\b", "ALTER COLUMN");
        translated = translated.replaceAll("(?i)\\bCONVERT\\s+TO\\s+CHARACTER\\s+SET\\s+\\w+\\s+COLLATE\\s+\\w+", "");
        if (type == Type.POSTGRESQL) {
            translated = translated.replaceAll("(?i)SHA2\\s*\\(\\s*\\?\\s*,\\s*512\\s*\\)", "encode(digest(convert_to(?, 'UTF8'), 'sha512'), 'hex')");
        }
        translated = translated.replaceAll("(?<![\\\"'])\\buser\\b(?![\\\"'])", "\"user\"");
        translated = translated.replaceAll("(?<![\\\"'])\\b(date|time|timestamp)\\b(?=\\s*(?:=|>|<|,|\\)|;|$|ASC\\b|DESC\\b|FROM\\b|WHERE\\b|GROUP\\b|ORDER\\b))", "\"$1\"");

        if (translated.trim().toUpperCase(Locale.ROOT).startsWith("CREATE TABLE")) {
            translated = translateCreateTable(translated);
        }
        if (translated.contains("AUTO_INCREMENT")) {
            var identityType = type == Type.POSTGRESQL ? "BIGINT GENERATED BY DEFAULT AS IDENTITY" : "INTEGER";
            translated = translated.replaceAll("(?i)\\b(?:BIGINT|INTEGER)\\s+NOT\\s+NULL\\s+AUTO_INCREMENT\\b", Matcher.quoteReplacement(identityType));
        }
        return translated;
    }

    private static String translateCreateTable(String sql) {
        var translated = sql.replaceAll("\"([^\"]+)\"\\s*\\(\\s*\\d+\\s*\\)", "\"$1\"");
        translated = translated.replaceAll("(?i),\\s*UNIQUE\\s+KEY\\s+\"[^\"]+\"\\s*\\(([^)]*)\\)", ", UNIQUE ($1)");
        translated = translated.replaceAll("(?i),\\s*KEY\\s+\"[^\"]+\"\\s*\\([^)]*\\)", "");
        translated = translated.replaceAll("(?i)\\)\\s*ENGINE\\s*=\\s*\\w+\\s+DEFAULT\\s+CHARSET\\s*=\\s*\\w+(?:\\s+COLLATE\\s*=\\s*\\w+)?", ")");
        translated = translated.replaceAll("(?i)\\s+ENGINE\\s*=\\s*\\w+", "");
        translated = translated.replaceAll("(?i)\\s+DEFAULT\\s+CHARSET\\s*=\\s*\\w+", "");
        translated = translated.replaceAll("(?i)\\s+COLLATE\\s*=\\s*\\w+", "");
        return translated;
    }
}
