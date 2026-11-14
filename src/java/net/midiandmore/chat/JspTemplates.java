package net.midiandmore.chat;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renders the templates below <code>/WEB-INF/jsp</code>. Dynamic values are
 * read by the JSPs through EL (<code>${nick}</code>, <code>${paths.login}</code>,
 * <code>${com.design_nav_start}</code>, ...); they are provided by the
 * {@link View} that is being rendered. Texts are taken from the resource
 * bundles <code>i18n/messages*.properties</code> for the requested language.
 * <p>
 * Rendering does not need the real request, so templates can be used from the
 * WebSocket endpoint as well. A synthetic request exposes the view values as
 * request attributes.
 */
final class JspTemplates {

    // JSTL stores request scoped settings below this name
    static final String FMT_LOCALE = "jakarta.servlet.jsp.jstl.fmt.locale.request";

    private static final Logger LOG = Logger.getLogger(JspTemplates.class.getName());
    private static final Map<String, String> DIRECTORIES = Map.of(
            "template", "pages",
            "console", "console",
            "mail", "mail",
            "system", "system");
    private static volatile ServletContext context;

    private JspTemplates() {
    }

    static void init(ServletContext servletContext) {
        context = servletContext;
    }

    static String path(String type, String name) {
        var directory = DIRECTORIES.get(type);
        return directory == null ? null : "/WEB-INF/jsp/" + directory + "/" + name + ".jsp";
    }

    /**
     * Renders a template.
     *
     * @param type template type (template, console, mail, system)
     * @param name template name without extension
     * @param lang language code ("de" or "en")
     * @param view source of the values used by the template, may be
     * <code>null</code>
     * @return the rendered template or <code>null</code> if there is no JSP
     * for it
     */
    static String render(String type, String name, String lang, View view) {
        var ctx = context;
        var path = path(type, name);
        if (ctx == null || path == null) {
            return null;
        }
        var language = "en".equalsIgnoreCase(lang) ? "en" : "de";
        try {
            if (ctx.getResource(path) == null) {
                return null;
            }
            var out = new StringWriter();
            var dispatcher = ctx.getRequestDispatcher(path);
            if (dispatcher == null) {
                return null;
            }
            dispatcher.include(request(ctx, Locale.forLanguageTag(language), view), response(out));
            return out.toString().stripLeading();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Could not render " + path, ex);
            return "Error: The template '" + name + "' could not be rendered: " + ex.getMessage() + "\r\n";
        }
    }

    static String render(String type, String name, String lang) {
        return render(type, name, lang, null);
    }

    private static HttpServletRequest request(ServletContext ctx, Locale locale, View view) {
        Map<String, Object> attributes = new ConcurrentHashMap<>();
        attributes.put(FMT_LOCALE, locale);
        InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
            case "getAttribute" -> {
                var value = attributes.get((String) args[0]);
                yield value != null || view == null ? value : view.lookup((String) args[0]);
            }
            case "setAttribute" -> {
                if (args[1] == null) {
                    attributes.remove((String) args[0]);
                } else {
                    attributes.put((String) args[0], args[1]);
                }
                yield null;
            }
            case "removeAttribute" -> attributes.remove((String) args[0]);
            case "getAttributeNames" -> Collections.enumeration(attributes.keySet());
            case "getServletContext" -> ctx;
            case "getContextPath" -> ctx.getContextPath();
            case "getDispatcherType" -> DispatcherType.REQUEST;
            case "getMethod" -> "GET";
            case "getProtocol" -> "HTTP/1.1";
            case "getScheme" -> "http";
            case "getServerName" -> "localhost";
            case "getServerPort" -> 80;
            case "getRequestURI", "getServletPath" -> "/";
            case "getLocale" -> locale;
            case "getLocales" -> Collections.enumeration(java.util.List.of(locale));
            case "getCharacterEncoding" -> "UTF-8";
            case "getParameterMap" -> new HashMap<String, String[]>();
            case "getParameterNames", "getHeaderNames" -> Collections.emptyEnumeration();
            case "getHeaders" -> Collections.emptyEnumeration();
            case "getSession" -> null;
            case "toString" -> "JspTemplates.request";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> defaultValue(method);
        };
        return (HttpServletRequest) Proxy.newProxyInstance(JspTemplates.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class}, handler);
    }

    private static HttpServletResponse response(StringWriter out) {
        var writer = new PrintWriter(out);
        InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
            case "getWriter" -> writer;
            case "getOutputStream" -> throw new IllegalStateException("getWriter() is used");
            case "getCharacterEncoding" -> "UTF-8";
            case "getBufferSize" -> 8192;
            case "getStatus" -> 200;
            case "getLocale" -> Locale.ROOT;
            case "flushBuffer" -> {
                writer.flush();
                yield null;
            }
            case "toString" -> "JspTemplates.response";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> defaultValue(method);
        };
        return (HttpServletResponse) Proxy.newProxyInstance(JspTemplates.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class}, handler);
    }

    private static Object defaultValue(Method method) {
        var type = method.getReturnType();
        if (!type.isPrimitive() || type == void.class) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == double.class) {
            return 0d;
        }
        if (type == float.class) {
            return 0f;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        return (char) 0;
    }
}
