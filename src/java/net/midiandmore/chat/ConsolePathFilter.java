package net.midiandmore.chat;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Serves the admin console under the path configured as
 * <code>console.path</code>. If the path is changed, the default address
 * <code>/console.jsp</code> is no longer reachable.
 */
public final class ConsolePathFilter implements Filter {

    private static final String CONSOLE_JSP = "/" + FixedPaths.DEFAULT_CONSOLE_PATH;

    private FilterConfig config;

    @Override
    public void init(FilterConfig filterConfig) {
        config = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        var request = (HttpServletRequest) servletRequest;
        var response = (HttpServletResponse) servletResponse;
        // The configuration is only available after the chat was started.
        if (!Web.isStarted()) {
            Web.start(config.getServletContext());
        }
        var path = request.getRequestURI().substring(request.getContextPath().length());
        var console = "/" + Web.consolePath();
        if (path.equals(console)) {
            request.getRequestDispatcher(CONSOLE_JSP).forward(request, response);
        } else if (path.equals(CONSOLE_JSP)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            chain.doFilter(request, response);
        }
    }
}
