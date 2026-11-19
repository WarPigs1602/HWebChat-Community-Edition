package net.midiandmore.chat;

/**
 * Softwareinfos f&uuml;r den Chat
 * @author Andreas Pschorn
 */
public interface Software {
    /**
     * 
     */
    public static final char[] HEX                 = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    /**
     * 
     */
    public static final String SERVER_HOMEPAGE     = "github.com/WarPigs1602/HWebChat";
    /**
     * 
     */
    public static final String SERVER_MAIL         = "admin@hwebchat.de";
    /**
     * 
     */
    public static final String SERVER_PROJECT_NAME = "This is a vaporware chat!";
    /**
     * 
     */
    public static final String SERVER_SOFTWARE     = "HWebChat";
    /**
     * 
     */
    public static final String SERVER_STATUS       = "";
    /**
     * 
     */
    public static final String SERVER_VENDOR       = "Andreas Pschorn";
    /**
     * 
     */
    public static final String SERVER_VERSION      = loadVersion();
    /**
     * 
     */
    public static final String SERVER_YEAR         = "2005-2026";

    private static String loadVersion() {
        try (var input = Software.class.getResourceAsStream("/hwebchat-version.properties")) {
            if (input == null) {
                throw new IllegalStateException("Missing HWebChat version resource");
            }
            var properties = new java.util.Properties();
            properties.load(input);
            var version = properties.getProperty("version");
            if (version == null || version.isBlank()) {
                throw new IllegalStateException("Missing HWebChat version");
            }
            return version;
        } catch (java.io.IOException ex) {
            throw new java.io.UncheckedIOException("Cannot read HWebChat version", ex);
        }
    }
}