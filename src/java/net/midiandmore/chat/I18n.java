package net.midiandmore.chat;

import java.util.Locale;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Access to the resource bundles <code>i18n/messages*.properties</code> from
 * Java code. The same bundles are used by the JSP templates.
 */
final class I18n {

    static final String BUNDLE = "i18n.messages";
    private static final Logger LOG = Logger.getLogger(I18n.class.getName());

    private I18n() {
    }

    static Locale locale(String lang) {
        return "de".equalsIgnoreCase(lang) ? Locale.GERMAN : Locale.ENGLISH;
    }

    static String language(HttpServletRequest request) {
        String cookie = null;
        var cookies = request.getCookies();
        if (cookies != null) {
            for (var item : cookies) {
                if ("lang".equals(item.getName())) {
                    cookie = item.getValue();
                    break;
                }
            }
        }
        return language(cookie, request.getParameter("lang"), request.getHeader("Accept-Language"));
    }

    static String language(String cookie, String parameter, String acceptLanguage) {
        var selected = cookie != null && !cookie.isBlank() ? cookie : parameter;
        if (selected != null && !selected.isBlank()) {
            var selectedLocale = Locale.forLanguageTag(selected.strip().replace('_', '-'));
            return "de".equals(selectedLocale.getLanguage()) ? "de" : "en";
        }
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            try {
                var match = Locale.lookup(Locale.LanguageRange.parse(acceptLanguage), List.of(Locale.ENGLISH, Locale.GERMAN));
                if (Locale.GERMAN.equals(match)) {
                    return "de";
                }
            } catch (IllegalArgumentException ex) {
                LOG.fine("Ignoring malformed Accept-Language header");
            }
        }
        return "en";
    }

    static String chatLanguage(String cookie, String parameter, String session, String acceptLanguage) {
        var fallback = parameter != null && !parameter.isBlank() ? parameter : session;
        return language(cookie, fallback, acceptLanguage);
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

    static boolean has(String lang, String key) {
        return ResourceBundle.getBundle(BUNDLE, locale(lang)).containsKey(key);
    }
}
