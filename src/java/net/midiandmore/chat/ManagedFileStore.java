package net.midiandmore.chat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.HexFormat;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;

/** Private file storage managed through the authenticated administration console. */
public final class ManagedFileStore {
    public static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    public static final long MAX_TOTAL_SIZE = 100L * 1024 * 1024;
    public static final int MAX_FILES = 1000;
    private static final int MAX_CATEGORIES = 100;
    private static final int MAX_CATEGORY_LENGTH = 80;
    private static final int MAX_TITLE_LENGTH = 200;
    public static final String UNCATEGORIZED_FILTER = "__uncategorized__";
    private static final Object STORE_LOCK = new Object();

    public static final class ValidationException extends IOException {
        private final String messageKey;

        private ValidationException(String messageKey) {
            super(messageKey);
            this.messageKey = messageKey;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }

    public static final class LimitExceededException extends IOException {
        private LimitExceededException(String message) {
            super(message);
        }
    }

    public static final class Entry {
        private final String name;
        private final long size;
        private final String modified;
        private final boolean published;
        private final String title;
        private final String category;

        private Entry(String name, long size, String modified, boolean published, String title, String category) {
            this.name = name;
            this.size = size;
            this.modified = modified;
            this.published = published;
            this.title = title;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }

        public String getModified() {
            return modified;
        }

        public boolean isPublished() {
            return published;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }
    }

    private record FileMetadata(String title, String category) {}

    private static final DateTimeFormatter MODIFIED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());
    private final Path directory = Path.of(System.getProperty("user.home"), ".homewebcom", "files")
            .toAbsolutePath().normalize();

    public Path directory() {
        return directory;
    }

    public List<Entry> list() throws IOException {
        synchronized (STORE_LOCK) {
            ensureDirectory();
            var entries = new ArrayList<Entry>();
            try (var files = Files.list(directory)) {
                for (var file : files.toList()) {
                    if (!file.getFileName().toString().startsWith(".")
                            && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                        var name = file.getFileName().toString();
                        var metadata = metadata(name);
                        var published = hasPublicationMarker(name) && !metadata.title().isBlank();
                        entries.add(new Entry(name, Files.size(file),
                                MODIFIED_FORMAT.format(Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS).toInstant()),
                                published, metadata.title(), metadata.category()));
                    }
                }
            }
            entries.sort(Comparator.comparing(Entry::getName, String.CASE_INSENSITIVE_ORDER));
            return List.copyOf(entries);
        }
    }

    public void store(String name, InputStream input) throws IOException {
        synchronized (STORE_LOCK) {
            ensureDirectory();
            var target = resolve(name);
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                throw new FileAlreadyExistsException(name);
            }
            var temporary = Files.createTempFile(directory, ".upload-", ".tmp");
            try {
                Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
                var size = Files.size(temporary);
                if (size > MAX_FILE_SIZE) {
                    throw new LimitExceededException("File exceeds the per-file upload size limit");
                }
                var entries = list();
                var totalSize = entries.stream().mapToLong(Entry::getSize).sum();
                if (entries.size() >= MAX_FILES || totalSize + size > MAX_TOTAL_SIZE) {
                    throw new LimitExceededException("Managed file storage limit exceeded");
                }
                Files.move(temporary, target);
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
    }

    public Path file(String name) throws IOException {
        ensureDirectory();
        var file = resolve(name);
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new NoSuchFileException(name);
        }
        return file;
    }

    public void delete(String name) throws IOException {
        synchronized (STORE_LOCK) {
            var file = file(name);
            var archiveDirectory = archiveDirectory(false);
            if (archiveDirectory != null) {
                Files.deleteIfExists(marker(archiveDirectory, name));
                Files.deleteIfExists(metadataFile(archiveDirectory, name));
            }
            Files.delete(file);
        }
    }

    public void setPublished(String name, boolean published) throws IOException {
        synchronized (STORE_LOCK) {
            file(name);
            if (published && metadata(name).title().isBlank()) {
                throw new ValidationException("console.files.error_title_required");
            }
            var archiveDirectory = archiveDirectory(published);
            if (archiveDirectory == null) {
                return;
            }
            var marker = marker(archiveDirectory, name);
            if (!published) {
                Files.deleteIfExists(marker);
                return;
            }
            if (Files.exists(marker, LinkOption.NOFOLLOW_LINKS)) {
                if (!Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS)
                        || !name.equals(Files.readString(marker, StandardCharsets.UTF_8))) {
                    throw new IOException("Invalid download archive marker");
                }
                return;
            }
            var temporary = Files.createTempFile(archiveDirectory, ".publish-", ".tmp");
            try {
                Files.writeString(temporary, name, StandardCharsets.UTF_8,
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                Files.move(temporary, marker);
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
    }

    public List<Entry> publishedFiles() throws IOException {
        var published = new ArrayList<Entry>();
        for (var entry : list()) {
            if (entry.isPublished()) {
                published.add(entry);
            }
        }
        return List.copyOf(published);
    }

    public boolean isPublished(String name) throws IOException {
        file(name);
        return hasPublicationMarker(name) && !metadata(name).title().isBlank();
    }

    public void updateMetadata(String name, String title, String category) throws IOException {
        synchronized (STORE_LOCK) {
            file(name);
            var normalizedTitle = title == null ? "" : title.strip();
            if (normalizedTitle.length() > MAX_TITLE_LENGTH
                    || normalizedTitle.chars().anyMatch(Character::isISOControl)) {
                throw new ValidationException("console.files.error_title_invalid");
            }
            var normalizedCategory = category == null ? "" : category.strip();
            if (!normalizedCategory.isEmpty() && !categories().contains(normalizedCategory)) {
                throw new ValidationException("console.files.error_category_missing");
            }
            var oldMetadata = metadata(name);
            var hasPublicationMarker = hasPublicationMarker(name);
            if (hasPublicationMarker && normalizedTitle.isBlank()) {
                throw new ValidationException("console.files.error_title_required");
            }
            if (hasPublicationMarker && oldMetadata.title().isBlank()) {
                Files.deleteIfExists(marker(archiveDirectory(false), name));
            }
            writeMetadata(name, new FileMetadata(normalizedTitle, normalizedCategory));
        }
    }

    public List<String> categories() throws IOException {
        synchronized (STORE_LOCK) {
            var archiveDirectory = archiveDirectory(false);
            if (archiveDirectory == null) {
                return List.of();
            }
            var categoryFile = archiveDirectory.resolve("categories.json");
            if (!Files.exists(categoryFile, LinkOption.NOFOLLOW_LINKS)) {
                return List.of();
            }
            if (!Files.isRegularFile(categoryFile, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Invalid download archive categories file");
            }
            try (var reader = Json.createReader(Files.newBufferedReader(categoryFile, StandardCharsets.UTF_8))) {
                var json = reader.readArray();
                var result = new ArrayList<String>();
                var unique = new HashSet<String>();
                for (var i = 0; i < json.size(); i++) {
                    var decoded = json.getString(i);
                    if (decoded.isBlank() || decoded.length() > MAX_CATEGORY_LENGTH
                            || decoded.equalsIgnoreCase(UNCATEGORIZED_FILTER)
                            || decoded.chars().anyMatch(Character::isISOControl)) {
                        throw new IOException("Invalid download archive category");
                    }
                    if (!unique.add(decoded.toLowerCase(java.util.Locale.ROOT))) {
                        throw new IOException("Duplicate download archive category");
                    }
                    result.add(decoded);
                }
                if (result.size() > MAX_CATEGORIES) {
                    throw new IOException("Too many download archive categories");
                }
                result.sort(String.CASE_INSENSITIVE_ORDER);
                return List.copyOf(result);
            } catch (JsonException | ClassCastException | NullPointerException ex) {
                throw new IOException("Invalid download archive categories", ex);
            }
        }
    }

    public void addCategory(String category) throws IOException {
        synchronized (STORE_LOCK) {
            var normalized = normalizeCategory(category);
            var categories = new ArrayList<>(categories());
            if (categories.size() >= MAX_CATEGORIES) {
                throw new ValidationException("console.files.error_category_limit");
            }
            if (categories.stream().anyMatch(existing -> existing.equalsIgnoreCase(normalized))) {
                throw new ValidationException("console.files.error_category_exists");
            }
            categories.add(normalized);
            writeCategories(categories);
        }
    }

    public void removeCategory(String category) throws IOException {
        synchronized (STORE_LOCK) {
            var categories = new ArrayList<>(categories());
            var existing = categories.stream().filter(value -> value.equals(category)).findFirst()
                    .orElseThrow(() -> new ValidationException("console.files.error_category_missing"));
            for (var entry : list()) {
                if (existing.equals(entry.getCategory())) {
                    throw new ValidationException("console.files.error_category_in_use");
                }
            }
            categories.remove(existing);
            writeCategories(categories);
        }
    }

    private String normalizeCategory(String category) throws ValidationException {
        var normalized = category == null ? "" : category.strip();
        if (normalized.isEmpty() || normalized.length() > MAX_CATEGORY_LENGTH
                || normalized.equalsIgnoreCase(UNCATEGORIZED_FILTER)
                || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new ValidationException("console.files.error_category_invalid");
        }
        return normalized;
    }

    private void writeCategories(List<String> categories) throws IOException {
        var archiveDirectory = archiveDirectory(true);
        var target = archiveDirectory.resolve("categories.json");
        var temporary = Files.createTempFile(archiveDirectory, ".categories-", ".tmp");
        try {
            var builder = Json.createArrayBuilder();
            categories.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(builder::add);
            try (var writer = Json.createWriter(Files.newBufferedWriter(temporary, StandardCharsets.UTF_8))) {
                writer.writeArray(builder.build());
            }
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private FileMetadata metadata(String name) throws IOException {
        var archiveDirectory = archiveDirectory(false);
        if (archiveDirectory == null) {
            return new FileMetadata("", "");
        }
        var metadataFile = metadataFile(archiveDirectory, name);
        if (!Files.exists(metadataFile, LinkOption.NOFOLLOW_LINKS)) {
            return new FileMetadata("", "");
        }
        if (!Files.isRegularFile(metadataFile, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Invalid download archive file metadata");
        }
        try (var reader = Json.createReader(Files.newBufferedReader(metadataFile, StandardCharsets.UTF_8))) {
            JsonObject json = reader.readObject();
            if (!name.equals(json.getString("name", null))) {
                throw new IOException("Invalid download archive file metadata");
            }
            return new FileMetadata(json.getString("title", ""), json.getString("category", ""));
        } catch (JsonException | ClassCastException | NullPointerException ex) {
            throw new IOException("Invalid download archive file metadata", ex);
        }
    }

    private void writeMetadata(String name, FileMetadata metadata) throws IOException {
        var archiveDirectory = archiveDirectory(true);
        var target = metadataFile(archiveDirectory, name);
        var temporary = Files.createTempFile(archiveDirectory, ".metadata-", ".tmp");
        try {
            var json = Json.createObjectBuilder()
                    .add("name", name)
                    .add("title", metadata.title())
                    .add("category", metadata.category())
                    .build();
            try (var writer = Json.createWriter(Files.newBufferedWriter(temporary, StandardCharsets.UTF_8))) {
                writer.writeObject(json);
            }
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private boolean hasPublicationMarker(String name) throws IOException {
        var archiveDirectory = archiveDirectory(false);
        if (archiveDirectory == null) {
            return false;
        }
        var marker = marker(archiveDirectory, name);
        return Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS)
                && name.equals(Files.readString(marker, StandardCharsets.UTF_8));
    }

    private Path archiveDirectory(boolean create) throws IOException {
        var archiveDirectory = directory.resolve(".download-archive");
        if (create) {
            Files.createDirectories(archiveDirectory);
        }
        if (Files.isSymbolicLink(archiveDirectory)) {
            throw new IOException("Download archive metadata must not be a symbolic link");
        }
        if (!Files.exists(archiveDirectory, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        if (!Files.isDirectory(archiveDirectory, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Invalid download archive metadata directory");
        }
        return archiveDirectory;
    }

    private Path marker(Path archiveDirectory, String name) {
        return archiveDirectory.resolve(digest(name));
    }

    private Path metadataFile(Path archiveDirectory, String name) {
        return archiveDirectory.resolve("metadata-" + digest(name) + ".json");
    }

    private String digest(String name) {
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                    .digest(name.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private void ensureDirectory() throws IOException {
        Files.createDirectories(directory);
        if (Files.isSymbolicLink(directory)) {
            throw new IOException("Managed file storage must not be a symbolic link");
        }
    }

    private Path resolve(String name) {
        if (name == null || name.isBlank() || name.length() > 255 || name.startsWith(".")
                || name.contains("/") || name.contains("\\")
                || name.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid filename");
        }
        var filename = Path.of(name);
        if (!filename.getFileName().toString().equals(name)) {
            throw new IllegalArgumentException("Invalid filename");
        }
        var file = directory.resolve(filename).normalize();
        if (!directory.equals(file.getParent())) {
            throw new IllegalArgumentException("Invalid filename");
        }
        return file;
    }
}
