package net.midiandmore.chat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class NewsRepository {
    record Article(long id, String title, String body, String author, long published) {}

    private final Connection connection;
    private final String table;

    NewsRepository(Database db, String prefix) throws SQLException {
        connection = db.getNewsConnection();
        table = "`" + prefix + "news`";
        try (var statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table
                + " (`id` bigint(20) NOT NULL AUTO_INCREMENT, `title` varchar(200) NOT NULL,"
                + " `body` text NOT NULL, `author` varchar(255) NOT NULL, `published` bigint(20) NOT NULL,"
                + " PRIMARY KEY (`id`))")) {
            statement.executeUpdate();
        }
    }

    List<Article> list() throws SQLException {
        var result = new ArrayList<Article>();
        try (var statement = connection.prepareStatement("SELECT id, title, body, author, published FROM "
                + table + " ORDER BY published DESC, id DESC");
                var rows = statement.executeQuery()) {
            while (rows.next()) {
                result.add(new Article(rows.getLong(1), rows.getString(2), rows.getString(3),
                        rows.getString(4), rows.getLong(5)));
            }
        }
        return result;
    }

    Optional<Article> latest() throws SQLException {
        try (var statement = connection.prepareStatement("SELECT id, title, body, author, published FROM "
                + table + " ORDER BY published DESC, id DESC LIMIT 1");
                var rows = statement.executeQuery()) {
            return rows.next() ? Optional.of(new Article(rows.getLong(1), rows.getString(2),
                    rows.getString(3), rows.getString(4), rows.getLong(5))) : Optional.empty();
        }
    }

    void create(String title, String body, String author) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO " + table
                + " (title, body, author, published) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, title);
            statement.setString(2, body);
            statement.setString(3, author);
            statement.setLong(4, System.currentTimeMillis());
            statement.executeUpdate();
        }
    }

    boolean update(long id, String title, String body) throws SQLException {
        try (var statement = connection.prepareStatement("UPDATE " + table + " SET title=?, body=? WHERE id=?")) {
            statement.setString(1, title);
            statement.setString(2, body);
            statement.setLong(3, id);
            return statement.executeUpdate() == 1;
        }
    }

    boolean delete(long id) throws SQLException {
        try (var statement = connection.prepareStatement("DELETE FROM " + table + " WHERE id=?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }
}
