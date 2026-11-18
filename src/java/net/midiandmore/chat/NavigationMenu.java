package net.midiandmore.chat;

import jakarta.json.Json;
import jakarta.json.JsonException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Persistent configuration for the public navigation bar. */
public final class NavigationMenu {
    private static final List<String> BUILTINS = List.of(
            "start", "news", "board", "rules", "toplist", "stats", "logout",
            "chat", "account", "guest", "login", "register", "password",
            "language", "language-de", "language-en");
    private static final Set<String> BUILTIN_SET = Set.copyOf(BUILTINS);
    private static final int MAX_ITEMS = 100;

    public static final class ValidationException extends IllegalArgumentException {
        private final String messageKey;

        private ValidationException(String messageKey, String message) {
            super(message);
            this.messageKey = messageKey;
        }

        public String getMessageKey() { return messageKey; }
    }

    public static final class Item {
        private final String id;
        private final String label;
        private final String url;
        private final boolean enabled;

        public Item(String id, String label, String url, boolean enabled) {
            this.id = id;
            this.label = label;
            this.url = url;
            this.enabled = enabled;
        }

        public String getId() { return id; }
        public String getLabel() { return label; }
        public String getUrl() { return url; }
        public boolean isEnabled() { return enabled; }
        public boolean isCustom() { return !BUILTIN_SET.contains(id); }
    }

    private NavigationMenu() {}

    private static Path file() {
        return Path.of(Bootstrap.getBoot().getConfig().getUh(), ".homewebcom", "config", "navigation.json");
    }

    public static List<Item> defaults() {
        return BUILTINS.stream().map(id -> new Item(id, "", "", true)).toList();
    }

    public static synchronized List<Item> load() throws IOException {
        return load(file());
    }

    static List<Item> load(Path file) throws IOException {
        try (var reader = Json.createReader(Files.newBufferedReader(file, StandardCharsets.UTF_8))) {
            var items = new ArrayList<Item>();
            for (var value : reader.readArray()) {
                var object = value.asJsonObject();
                items.add(new Item(object.getString("id"), object.getString("label"),
                        object.getString("url"), object.getBoolean("enabled")));
            }
            validate(items);
            return items;
        } catch (NoSuchFileException ex) {
            return defaults();
        } catch (JsonException | IllegalArgumentException | ClassCastException | NullPointerException ex) {
            throw new IOException("Invalid navigation configuration", ex);
        }
    }

    public static synchronized void save(List<Item> items) throws IOException {
        save(file(), items);
    }

    static void save(Path file, List<Item> items) throws IOException {
        validate(items);
        var array = Json.createArrayBuilder();
        for (var item : items) {
            array.add(Json.createObjectBuilder().add("id", item.id).add("label", item.label)
                    .add("url", item.url).add("enabled", item.enabled));
        }
        var temp = Files.createTempFile(file.getParent(), "navigation-", ".tmp");
        try {
            try (var writer = Json.createWriter(Files.newBufferedWriter(temp, StandardCharsets.UTF_8))) {
                writer.writeArray(array.build());
            }
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public static void validate(List<Item> items) {
        if (items == null || items.size() > MAX_ITEMS) {
            throw new ValidationException("console.navigation.error_too_many", "Too many navigation items.");
        }
        var seen = new HashSet<String>();
        for (var item : items) {
            if (item == null || item.id == null || !seen.add(item.id)
                    || (!BUILTIN_SET.contains(item.id) && !item.id.matches("custom-[a-f0-9-]{36}"))) {
                throw new ValidationException("console.navigation.error_duplicate", "Invalid or duplicate navigation item.");
            }
            if (item.label == null || item.label.length() > 120 || item.url == null || item.url.length() > 2048
                    || (item.isCustom() && (item.label.isBlank() || item.url.isBlank()))
                    || (!item.url.isEmpty() && !validUrl(item.url))) {
                throw new ValidationException("console.navigation.error_fields", "Check the labels and destinations of all navigation items.");
            }
        }
        if (!seen.containsAll(BUILTIN_SET)) {
            throw new ValidationException("console.navigation.error_missing", "Built-in navigation items cannot be removed.");
        }
    }

    static boolean validUrl(String url) {
        if (!url.equals(url.strip()) || url.chars().anyMatch(c -> Character.isISOControl(c) || c == '\\')) {
            return false;
        }
        try {
            var uri = new URI(url);
            if (url.startsWith("/") && !url.startsWith("//")) {
                var path = uri.getPath();
                if (path == null || path.startsWith("//")
                        || path.chars().anyMatch(c -> Character.isISOControl(c) || c == '\\')) return false;
                for (var segment : path.split("/", -1)) {
                    if (segment.equals(".") || segment.equals("..")) return false;
                }
                return uri.getRawAuthority() == null && !uri.isAbsolute();
            }
            var scheme = uri.getScheme();
            if (scheme == null) return false;
            if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
                return uri.getHost() != null;
            }
            return scheme.equalsIgnoreCase("mailto") && !uri.getSchemeSpecificPart().isBlank();
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}
