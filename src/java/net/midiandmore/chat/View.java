package net.midiandmore.chat;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * A template together with the values it is rendered with. The values are
 * read by the JSP through EL (<code>${name}</code>). Rendering happens when the
 * view is converted to a string, so values can be set in any order and views
 * can be nested: a view used as the value of another view sees the values of
 * the surrounding views as well.
 */
final class View implements CharSequence {

    private static final ThreadLocal<ArrayDeque<View>> ACTIVE = ThreadLocal.withInitial(ArrayDeque::new);
    private static final AtomicLong VERSION = new AtomicLong();
    private static final Pattern TOKEN = Pattern.compile(
            "%(?:PATH_\\[([^\\]%]+)\\]|com\\[([^\\]%]+)\\]|([A-Za-z_][A-Za-z0-9_]*))%");

    private final String type;
    private final String name;
    private final String lang;
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, Object> defaults;
    private String rendered;
    private long renderedVersion = -1;

    /**
     * @param type template type (design, template, pages, console, mail,
     * script, style, system)
     * @param name template name
     * @param lang language ("de" or "en")
     * @param defaults values every template of the request can use; values
     * may be {@link Supplier}s which are evaluated when first needed
     */
    View(String type, String name, String lang, Map<String, Object> defaults) {
        this.type = type;
        this.name = name;
        this.lang = lang;
        this.defaults = defaults;
    }

    Map<String, Object> defaults() {
        return defaults;
    }

    /**
     * A view that renders to the given text.
     */
    static View of(String text) {
        var view = new View("literal", "", "de", null);
        view.rendered = text == null ? "" : text;
        view.renderedVersion = Long.MAX_VALUE;
        return view;
    }

    String type() {
        return type;
    }

    String name() {
        return name;
    }

    String lang() {
        return lang;
    }

    /**
     * Sets a value for the template.
     *
     * @return this view
     */
    View set(String key, Object value) {
        values.put(key, value instanceof Number ? String.valueOf(value) : value);
        VERSION.incrementAndGet();
        return this;
    }

    /**
     * Looks up a value for rendering: first the values of this view, then its
     * defaults, then the same for the surrounding views.
     */
    Object lookup(String key) {
        for (var view : ACTIVE.get()) {
            var value = view.local(key);
            if (value != null) {
                return value;
            }
        }
        return local(key);
    }

    private Object local(String key) {
        var value = values.get(key);
        if (value == null && defaults != null) {
            value = defaults.get(key);
            if (value instanceof Supplier<?> supplier) {
                value = supplier.get();
                defaults.put(key, value == null ? "" : value);
            }
        }
        return value instanceof Supplier<?> supplier ? supplier.get() : value;
    }

    /**
     * Resolves <code>%name%</code>, <code>%PATH_[name]%</code> and
     * <code>%com[name]%</code> tokens in strings that come from the JSON
     * configuration files (paths.json, commands.json).
     */
    String expand(String text) {
        if (text == null) {
            return "";
        }
        for (int pass = 0; pass < 6 && text.indexOf('%') >= 0; pass++) {
            var matcher = TOKEN.matcher(text);
            var sb = new StringBuilder();
            boolean changed = false;
            while (matcher.find()) {
                String replacement;
                if (matcher.group(1) != null) {
                    replacement = lookupMap("paths", matcher.group(1));
                    replacement = replacement == null ? "" : replacement;
                } else if (matcher.group(2) != null) {
                    replacement = lookupMap("com", matcher.group(2));
                    replacement = replacement == null ? "" : replacement;
                } else {
                    var value = lookup(matcher.group(3));
                    replacement = value == null ? matcher.group() : String.valueOf(value);
                }
                changed |= !replacement.equals(matcher.group());
                matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            text = sb.toString();
            if (!changed) {
                break;
            }
        }
        return text;
    }

    private String lookupMap(String map, String key) {
        return lookup(map) instanceof Map<?, ?> values && values.get(key) != null ? String.valueOf(values.get(key)) : null;
    }

    @Override
    public String toString() {
        long version = VERSION.get();
        if (rendered != null && renderedVersion == version) {
            return rendered;
        }
        var active = ACTIVE.get();
        active.push(this);
        try {
            var text = JspTemplates.render(type, name, lang, this);
            rendered = text != null ? text : notFound();
        } finally {
            active.pop();
        }
        renderedVersion = version;
        return rendered;
    }

    private String notFound() {
        var db = Bootstrap.boot != null && Bootstrap.boot.getConfig() != null ? Bootstrap.boot.getConfig().getDb() : null;
        if (db != null && (type.equals("script") || type.equals("style"))) {
            var raw = type.equals("script") ? db.getScript(name, "native", lang) : db.getStyle(name, "native", lang);
            if (!raw.toLowerCase().startsWith("error")) {
                return raw;
            }
        }
        return "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p><b>Error: The requested "
                + type + " '" + name + "' does not exist!</b></p><hr><address>" + Software.SERVER_SOFTWARE + " "
                + Software.SERVER_VERSION + "-" + Software.SERVER_STATUS + "</address></body><html>";
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }
}
