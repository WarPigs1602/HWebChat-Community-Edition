package net.midiandmore.chat;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Access to the resource bundles <code>i18n/messages*.properties</code> from
 * Java code. The same bundles are used by the JSP templates.
 */
final class I18n {

    static final String BUNDLE = "i18n.messages";

    private I18n() {
    }

    static Locale locale(String lang) {
        return "de".equalsIgnoreCase(lang) ? Locale.GERMAN : Locale.ENGLISH;
    }

    /**
     * Returns the message for the key in the given language ("de" or anything
     * else for English).
     */
    static String get(String lang, String key) {
        try {
            return ResourceBundle.getBundle(BUNDLE, locale(lang)).getString(key);
        } catch (MissingResourceException ex) {
            return key;
        }
    }
}
