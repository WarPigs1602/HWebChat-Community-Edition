package net.midiandmore.chat;

import java.util.regex.Pattern;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public final class CustomPageContent {

    private static final Pattern SLUG = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
    private static final PolicyFactory HTML_POLICY = new HtmlPolicyBuilder()
            .allowElements("a", "b", "blockquote", "br", "code", "div", "em", "h1", "h2", "h3",
                    "h4", "hr", "i", "li", "ol", "p", "pre", "span", "strong", "u", "ul")
            .allowAttributes("href", "title").onElements("a")
            .allowUrlProtocols("http", "https", "mailto")
            .toFactory();

    private CustomPageContent() {
    }

    public static boolean isValidSlug(String slug) {
        return slug != null && slug.length() <= 64 && SLUG.matcher(slug).matches();
    }

    public static String sanitize(String html) {
        return HTML_POLICY.sanitize(html);
    }
}
